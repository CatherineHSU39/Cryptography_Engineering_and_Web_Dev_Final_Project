from fastapi import FastAPI

app = FastAPI()

@app.get("/kms/health")
def health_check():
    return {"status": "KMS is running"}

# You can later add:
# - /generate-data-key
# - /wrap-key
# - /decrypt-data-key
# - /rotate-cmk
# - /audit-log
# - /log-integrity