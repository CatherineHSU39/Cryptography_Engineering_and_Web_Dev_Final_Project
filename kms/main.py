from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from kyber_py.ml_kem import ML_KEM_512
from typing import List, Dict
import base64
import os
import time
import hashlib
import uuid
import json
from nacl.bindings import (
    crypto_aead_xchacha20poly1305_ietf_encrypt,
    crypto_aead_xchacha20poly1305_ietf_decrypt,
    crypto_aead_xchacha20poly1305_ietf_NPUBBYTES,
)
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.backends import default_backend

from jwt_auth import verify_jwt
from custom_swagger import custom_openapi

app = FastAPI(title="KMS API", version="1.0.0")
app.openapi = lambda: custom_openapi(app)

# ---------------------------- 狀態管理 ----------------------------
class KMSState:
    def __init__(self):
        self.user_shared_keys: Dict[str, bytes] = {}
        self.user_kyber_pub_keys: Dict[str, bytes] = {}
        self.user_rsa_pub_keys: Dict[str, object] = {}
        self.stored_deks: Dict[str, Dict[str, str]] = {}
        self.audit_log = []
        self.prev_hash = b'\x00' * 32

    def get_user_shared_key(self, user_id: str):
        if user_id not in self.user_shared_keys:
            self.user_shared_keys[user_id] = os.urandom(32)
            self.log(f"Generated shared key for {user_id}")
        return self.user_shared_keys[user_id]

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

    def rsa_encap(self, user_id):
        rsa_pub_key = self.user_rsa_pub_keys[user_id]
        shared_key = os.urandom(32)
        kem_ct = rsa_pub_key.encrypt(
            shared_key,
            padding.OAEP(
                mgf=padding.MGF1(algorithm=hashes.SHA256()),
                algorithm=hashes.SHA256(),
                label=None
            )
        )
        return kem_ct, shared_key

    def kyber_encap(self, user_id):
        kyber_pub = self.user_kyber_pub_keys[user_id]
        shared_key, kem_ct= ML_KEM_512.encaps(kyber_pub)
        return kem_ct, shared_key

    def wrap_response_kyber(self, user_id: str, kem_alg: str, response: bytes) -> tuple[bytes, bytes, bytes]:
        if kem_alg == "RSA":
            kem_ct, shared_key = self.rsa_encap(user_id)
        elif kem_alg == "MLKEM":
            kem_ct, shared_key = self.kyber_encap(user_id)
        else:
            raise ValueError("Unsupported algorithm")

        nonce = os.urandom(crypto_aead_xchacha20poly1305_ietf_NPUBBYTES)
        ciphertext = crypto_aead_xchacha20poly1305_ietf_encrypt(
            response,
            None,
            nonce,
            shared_key
        )

        return ciphertext, kem_ct, nonce

kms_state = KMSState()

# ---------------------------- 資料模型 ----------------------------
class RegisterRequest(BaseModel):
    kyber_public_key: str
    rsa_public_key: str

class GenerateDataKeyRequest(BaseModel):
    user_ids: List[str]

class EncryptedDekItem(BaseModel):
    id: str
    ownerId: str
    nonce: str
    encrypted_dek: str
    aad: str

class DecryptDataKeyRequest(BaseModel):
    kem_alg: str
    encrypted_deks: List[EncryptedDekItem]

class EncryptResponse(BaseModel):
    dek: str
    encrypted_deks: List[EncryptedDekItem]
    dekIds: Dict[str, str]

class DecryptResponse(BaseModel):
    kem_ct: str
    nonce: str
    encrypted_response: str
    kem_alg: str
    aead_alg: str

# ---------------------------- API 實作 ----------------------------

@app.get("/health")
def health_check():
    return {"status": "KMS is running"}

@app.post("/register")
def register_user(req: RegisterRequest, valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    if user_id in kms_state.user_kyber_pub_keys and user_id in kms_state.user_rsa_pub_keys:
        raise HTTPException(status_code=400, detail="User already registered")

    try:
        rsa_key = serialization.load_pem_public_key(
            req.rsa_public_key.encode(),
            backend=default_backend()
        )

    except Exception:
        raise HTTPException(status_code=400, detail="Invalid RSA public key")

    kms_state.user_kyber_pub_keys[user_id] = base64.b64decode(req.kyber_public_key)
    kms_state.user_rsa_pub_keys[user_id] = rsa_key
    kms_state.get_user_shared_key(user_id)
    kms_state.log(f"Registered user {user_id}")
    return {"Registered": "true"}

@app.get("/register/status")
def register_user(valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    if user_id in kms_state.user_kyber_pub_keys and user_id in kms_state.user_rsa_pub_keys:
        return {"Registered": "true"}
    else:
        return {"Registered": "false"}
    

@app.post("/generate-data-key")
def generate_data_key(req: GenerateDataKeyRequest, valid_token: str = Depends(verify_jwt)):
    dek = os.urandom(32)
    encrypted_deks = []
    ownerships = {}
    created_by = valid_token["sub"]

    for user_id in req.user_ids:
        dek_id = str(uuid.uuid4())
        key = kms_state.get_user_shared_key(user_id)
        nonce = os.urandom(crypto_aead_xchacha20poly1305_ietf_NPUBBYTES)

        aad_dict = {
            "id": dek_id,
            "cmk_version": 1,
            "encryption_alg": "XChaCha20-Poly1305",
            "owner_id": user_id,
            "created_by": created_by,
            "purpose": "chat"
        }
        aad_bytes = json.dumps(aad_dict).encode()

        ciphertext = crypto_aead_xchacha20poly1305_ietf_encrypt(dek, aad_bytes, nonce, key)

        encrypted_deks.append(EncryptedDekItem(
            id=dek_id,
            ownerId=user_id,
            nonce=base64.b64encode(nonce).decode(),
            encrypted_dek=base64.b64encode(ciphertext).decode(),
            aad=base64.b64encode(aad_bytes).decode()
        ))

        ownerships[user_id] = dek_id

    kms_state.log(f"Generated DEK {dek_id} for users: {','.join(req.user_ids)}")

    return EncryptResponse(
        dek=base64.b64encode(dek).decode(),
        encrypted_deks=encrypted_deks,
        dekIds=ownerships
    )

@app.post("/decrypt-data-key")
def decrypt_data_key(req: DecryptDataKeyRequest, valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    cmk = kms_state.get_user_shared_key(user_id)
    dek_dict = {}
    kem_alg = req.kem_alg

    # Decrypt each DEK using AEAD and collect into dict
    for i, enc in enumerate(req.encrypted_deks):
        try:
            nonce = base64.b64decode(enc.nonce)
            ciphertext = base64.b64decode(enc.encrypted_dek)
            aad = base64.b64decode(enc.aad)
            dek = crypto_aead_xchacha20poly1305_ietf_decrypt(ciphertext, aad, nonce, cmk)
            dek_dict[enc.id] = base64.b64encode(dek).decode()
            kms_state.log(f"User {user_id} decrypted DEK index {i}")
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"Failed to decrypt DEK at index {i}: {str(e)}")

    dek_json_bytes = json.dumps(dek_dict).encode()

    # Wrap or return plaintext depending on KEM algorithm
    if kem_alg == "NONE":
        return DecryptResponse(
            kem_ct="NONE",
            nonce="NONE",
            encrypted_response=base64.b64encode(dek_json_bytes).decode(),
            kem_alg=kem_alg,
            aead_alg="xchacha20poly1305"
        )
    else:
        ciphertext, kem_ct, nonce = kms_state.wrap_response_kyber(user_id, kem_alg, dek_json_bytes)
        return DecryptResponse(
            kem_ct=base64.b64encode(kem_ct).decode(),
            nonce=base64.b64encode(nonce).decode(),
            encrypted_response=base64.b64encode(ciphertext).decode(),
            kem_alg=kem_alg,
            aead_alg="xchacha20poly1305"
        )


@app.get("/audit-log")
def get_audit_log():
    return {"log": kms_state.audit_log}

@app.get("/log-integrity")
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

@app.post("/deks")
def store_deks(deks: List[EncryptedDekItem], valid_token: str = Depends(verify_jwt)):
    for item in deks:
        dek_id = base64.b64encode(os.urandom(16)).decode()
        kms_state.stored_deks[dek_id] = {
            "owner": item.owner_id,
            "value": item.dek
        }
    kms_state.log(f"Stored {len(deks)} deks")
    return {"status": "ok"}

@app.get("/deks")
def get_deks(dek_ids: List[str] = [], valid_token: str = Depends(verify_jwt)):
    user_id = valid_token["sub"]
    result = {}
    for dek_id in dek_ids:
        dek_entry = kms_state.stored_deks.get(dek_id)
        if dek_entry and dek_entry["owner"] == user_id:
            result[dek_id] = dek_entry["value"]
    return result
