from fastapi import FastAPI
from pydantic import BaseModel
from pqcrypto.kem.kyber512 import generate_keypair, encrypt, decrypt
import base64
import os
import time
import hashlib
app = FastAPI()

from fastapi import FastAPI
from pydantic import BaseModel
from pqcrypto.kem.kyber512 import generate_keypair, encrypt, decrypt
import base64
import os
import time

app = FastAPI()

# 模擬 KMS 狀態
class KMSState:
    def __init__(self):
        self.cmk_public, self.cmk_private = generate_keypair()
        self.audit_log = []  # [{msg, time, hash}]
        self.prev_hash = b'\x00' * 32  # 初始 hash

    def rotate_cmk(self):
        self.cmk_public, self.cmk_private = generate_keypair()
        self.log("Rotated CMK")

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

        self.prev_hash = entry_hash  # 更新鏈條
kms_state = KMSState()

# 請求資料模型
class WrapKeyRequest(BaseModel):
    key: str  # base64 encoded

class DecryptKeyRequest(BaseModel):
    encrypted_key: str  # base64 encoded ciphertext
    shared_secret: str  # base64 encoded shared secret

@app.get("/kms/health")
def health_check():
    return {"status": "KMS is running"}

@app.post("/kms/generate-data-key")
def generate_data_key():
    # 模擬產生隨機對稱金鑰（例如 AES key）
    key = os.urandom(32)
    ciphertext, shared_secret = encrypt(kms_state.cmk_public)

    kms_state.log("Generated data key")

    return {
        "plaintext_key": base64.b64encode(shared_secret).decode(),
        "encrypted_key": base64.b64encode(ciphertext).decode()
    }

@app.post("/kms/decrypt-data-key")
def decrypt_data_key(req: DecryptKeyRequest):
    ciphertext = base64.b64decode(req.encrypted_key)
    decrypted_key = decrypt(ciphertext, kms_state.cmk_private)

    kms_state.log("Decrypted data key")

    return {
        "plaintext_key": base64.b64encode(decrypted_key).decode()
    }

@app.post("/kms/wrap-key")
def wrap_key(req: WrapKeyRequest):
    key = base64.b64decode(req.key)
    ciphertext, shared_secret = encrypt(kms_state.cmk_public)

    kms_state.log("Wrapped key")

    return {
        "encrypted_key": base64.b64encode(ciphertext).decode(),
        "shared_secret": base64.b64encode(shared_secret).decode()
    }

@app.post("/kms/rotate-cmk")
def rotate_cmk():
    kms_state.rotate_cmk()
    return {"status": "CMK rotated"}

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
