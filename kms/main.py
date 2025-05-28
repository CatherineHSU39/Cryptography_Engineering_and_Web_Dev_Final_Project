from fastapi import FastAPI, Depends, HTTPException, Header
from pydantic import BaseModel
from pqcrypto.kem.kyber512 import generate_keypair, encrypt, decrypt
from typing import List, Dict
from jose import jwt, JWTError
import base64
import os
import time
import hashlib
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
import json

app = FastAPI()

# 模擬 KMS 狀態
class KMSState:
    def __init__(self):
        self.user_keys: Dict[str, Dict[str, bytes]] = {}  # user_id -> {'public': .., 'private': ..}
        self.user_rsa_keys: Dict[str, bytes] = {}          # user_id -> RSA public key (PEM)
        self.data_keys: Dict[str, bytes] = {}              # data_key_id -> shared_secret
        self.audit_log = []  # [{msg, time, hash}]
        self.prev_hash = b'\x00' * 32  # 初始 hash

    def get_or_create_user_cmk(self, user_id: str):
        if user_id not in self.user_keys:
            pub, priv = generate_keypair()
            self.user_keys[user_id] = {'public': pub, 'private': priv}
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
            pub, priv = generate_keypair()
            self.user_keys[user_id] = {'public': pub, 'private': priv}
        self.log("Rotated CMKs for all users")

kms_state = KMSState()

# ---------------------------- 資料模型 ----------------------------
class RegisterRequest(BaseModel):
    rsa_public_key: str  # PEM format

class GenerateDataKeyRequest(BaseModel):
    user_ids: List[str]

class DecryptDataKeyRequest(BaseModel):
    data_key_ids: List[str]

# ---------------------------- 工具函數 ----------------------------
SECRET = "super-secret"
ALGORITHM = "RS256"

def get_user_id_from_jwt(authorization: str = Header(...)) -> str:
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid Authorization header")
    token = authorization.removeprefix("Bearer ").strip()
    try:
        payload = jwt.decode(token, SECRET, algorithms=[ALGORITHM])
        return payload.get("user_id")
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

# ---------------------------- API 實作 ----------------------------
@app.get("/kms/health")
def health_check():
    return {"status": "KMS is running"}

@app.post("/kms/register")
def register_user(req: RegisterRequest, user_id: str = Depends(get_user_id_from_jwt)):
    if user_id in kms_state.user_rsa_keys:
        raise HTTPException(status_code=400, detail="User already registered")
    kms_state.user_rsa_keys[user_id] = req.rsa_public_key.encode()
    kms_state.get_or_create_user_cmk(user_id)
    kms_state.log(f"Registered user {user_id}")
    return {"status": "Registered"}

@app.post("/kms/generate-data-key")
def generate_data_key(req: GenerateDataKeyRequest):
    key = os.urandom(32)  # symmetric key
    result = {}
    for user_id in req.user_ids:
        pub, _ = kms_state.get_or_create_user_cmk(user_id)
        ciphertext, shared_secret = encrypt(pub)
        data_key_id = base64.b64encode(os.urandom(16)).decode()
        kms_state.data_keys[data_key_id] = shared_secret
        result[user_id] = data_key_id
    kms_state.log(f"Generated data key for users: {','.join(req.user_ids)}")
    return {
        "plaintext_key": base64.b64encode(key).decode(),
        "data_key_mapping": result
    }

@app.post("/kms/decrypt-data-key")
def decrypt_data_key(req: DecryptDataKeyRequest, user_id: str = Depends(get_user_id_from_jwt)):
    if user_id not in kms_state.user_keys:
        raise HTTPException(status_code=404, detail="User not registered")
    if user_id not in kms_state.user_rsa_keys:
        raise HTTPException(status_code=404, detail="User RSA key not found")

    rsa_pub_key = RSA.import_key(kms_state.user_rsa_keys[user_id])
    cipher_rsa = PKCS1_OAEP.new(rsa_pub_key)

    response_list = []
    for key_id in req.data_key_ids:
        if key_id not in kms_state.data_keys:
            raise HTTPException(status_code=404, detail=f"Data key ID {key_id} not found")
        plaintext_key = kms_state.data_keys[key_id]
        response_list.append({"dekId": key_id, "value": base64.b64encode(plaintext_key).decode()})

    json_payload = json.dumps(response_list).encode()
    encrypted = cipher_rsa.encrypt(json_payload)

    kms_state.log(f"User {user_id} decrypted data keys: {','.join(req.data_key_ids)}")
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
