from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from pqc.kem import kyber512 as kemalg
from typing import List, Dict
import base64
import os
import time
import hashlib
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
import json

from jwt_auth import verify_jwt
from custom_swagger import custom_openapi

app = FastAPI(title="KMS API", version="1.0.0")

app.openapi = lambda: custom_openapi(app)

# ---------------------------- 狀態管理 ----------------------------
class KMSState:
    def __init__(self):
        self.user_keys: Dict[str, Dict[str, bytes]] = {}  # user_id -> {'public': .., 'private': ..}
        self.user_rsa_keys: Dict[str, bytes] = {}          # user_id -> RSA public key (PEM)
        self.data_keys: Dict[str, bytes] = {}              # data_key_id -> plaintext shared secret
        self.stored_deks: Dict[str, Dict[str, str]] = {}   # dek_id -> {'owner': user_id, 'value': base64-dek}
        self.audit_log = []  # [{msg, time, hash}]
        self.prev_hash = b'\x00' * 32  # 初始 hash

    def get_or_create_user_cmk(self, user_id: str):
        if user_id not in self.user_keys:
            pk, sk = kemalg.keypair()
            self.user_keys[user_id] = {'public': pk, 'private': sk}
            self.log(f"Generated CMK for {user_id}")
        return self.user_keys[user_id]['public'], self.user_keys[user_id]['private']

    def log(self, message: str):
        timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
        data = f"[{timestamp}] {message}"
        combined = self.prev_hash + data.encode()
        entry_hash = hashlib.sha256(combined).digest()

        self.audit_log.append({
            "timestamp": timestamp,
            "message": message,
            "hash": entry_hash.hex()
        })

        self.prev_hash = entry_hash

    def rotate_all_cmk(self):
        for user_id in self.user_keys:
            pk, sk = kemalg.keypair()
            self.user_keys[user_id] = {'public': pk, 'private': sk}
        self.log("Rotated CMKs for all users")

kms_state = KMSState()

# ---------------------------- 資料模型 ----------------------------
class RegisterRequest(BaseModel):
    rsa_public_key: str  # PEM format

class GenerateDataKeyRequest(BaseModel):
    user_ids: List[str]

class DecryptDataKeyRequest(BaseModel):
    encrypted_deks: List[str]

class DekStoreItem(BaseModel):
    Dek: str
    ownerId: str
# ---------------------------- API 實作 ----------------------------

@app.get("/kms/health")
def health_check():
    return {"status": "KMS is running"}

@app.post("/kms/register")
def register_user(req: RegisterRequest, valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    if user_id in kms_state.user_rsa_keys:
        raise HTTPException(status_code=400, detail="User already registered")
    kms_state.user_rsa_keys[user_id] = req.rsa_public_key.encode()
    kms_state.get_or_create_user_cmk(user_id)
    kms_state.log(f"Registered user {user_id}")
    return {"status": "Registered"}

@app.post("/kms/generate-data-key")
def generate_data_key(req: GenerateDataKeyRequest, valid_token: str = Depends(verify_jwt)):
    key = os.urandom(32)  # symmetric key
    result = {}
    for user_id in req.user_ids:
        pk, _ = kms_state.get_or_create_user_cmk(user_id)
        ss, kem_ct = kemalg.encap(pk)
        result[user_id] = base64.b64encode(kem_ct).decode()
    kms_state.log(f"Generated data key for users: {','.join(req.user_ids)}")
    return {
        "plaintext_key": base64.b64encode(key).decode(),
        "encrypted_deks": result
    }

@app.post("/kms/decrypt-data-key")
def decrypt_data_key(req: DecryptDataKeyRequest, valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    if user_id not in kms_state.user_keys or user_id not in kms_state.user_rsa_keys:
        raise HTTPException(status_code=404, detail="User not registered or missing RSA key")
    _, sk = kms_state.get_or_create_user_cmk(user_id)
    rsa_pub_key = RSA.import_key(kms_state.user_rsa_keys[user_id])
    cipher_rsa = PKCS1_OAEP.new(rsa_pub_key)

    response_list = []
    for enc in req.encrypted_deks:
        kem_ct = base64.b64decode(enc)
        ss = kemalg.decap(kem_ct, sk)
        response_list.append({"dek": base64.b64encode(ss).decode()})

    json_payload = json.dumps(response_list).encode()
    encrypted = cipher_rsa.encrypt(json_payload)

    kms_state.log(f"User {user_id} decrypted data keys")
    return {"encrypted_keys": base64.b64encode(encrypted).decode()}

@app.post("/kms/rotate-cmk")
def rotate_cmk():
    kms_state.rotate_all_cmk()
    return {"status": "CMKs rotated"}

@app.get("/kms/audit-log")
def get_audit_log():
    return {"log": kms_state.audit_log}

@app.get("/kms/log-integrity")
def verify_log_integrity():
    prev = b'\x00' * 32
    for entry in kms_state.audit_log:
        data = f"[{entry['timestamp']}] {entry['message']}"
        combined = prev + data.encode()
        expected_hash = hashlib.sha256(combined).digest()
        actual_hash = bytes.fromhex(entry['hash'])
        if expected_hash != actual_hash:
            return {"status": "FAILED", "message": "Log integrity check failed"}
        prev = expected_hash
    return {"status": "OK", "message": "All logs are intact"}

@app.post("/kms/deks")
def store_deks(deks: List[DekStoreItem], valid_token: str = Depends(verify_jwt)):
    for item in deks:
        dek_id = base64.b64encode(os.urandom(16)).decode()
        kms_state.stored_deks[dek_id] = {
            "owner": item.ownerId,
            "value": item.Dek
        }
    kms_state.log(f"Stored {len(deks)} deks")
    return {"status": "ok"}

@app.get("/kms/deks")
def get_deks(dek_ids: List[str] = [], valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    result = {}
    for dek_id in dek_ids:
        dek_entry = kms_state.stored_deks.get(dek_id)
        if dek_entry and dek_entry["owner"] == user_id:
            result[dek_id] = dek_entry["value"]
    return result
