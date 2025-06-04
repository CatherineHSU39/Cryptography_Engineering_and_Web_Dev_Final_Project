# jwt_auth.py
import os
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import jwt, JWTError
from jose import jwk as jose_jwk
import httpx

JWT_ISSUER = os.getenv("JWT_ISSUER_URI")
if not JWT_ISSUER:
    raise RuntimeError("JWT_ISSUER_URI is not set")

EXPECTED_AUDIENCE = "kms backend"
ALLOWED_ALGS = {"RS256"}

security = HTTPBearer()
jwks_cache = {}

async def fetch_jwk(kid: str):
    global jwks_cache
    if not jwks_cache:
        async with httpx.AsyncClient() as client:
            # Fetch OpenID config
            config = await client.get(f"{JWT_ISSUER}/.well-known/openid-configuration")
            jwks_uri = config.json()["jwks_uri"]
            # Fetch and cache keys
            r = await client.get(jwks_uri)
            jwks_cache = {k["kid"]: k for k in r.json()["keys"]}
    if kid not in jwks_cache:
        raise HTTPException(403, detail=f"Unknown key ID: {kid}")
    return jwks_cache[kid]

async def verify_jwt(
    token: HTTPAuthorizationCredentials = Depends(security)
):
    try:
        header = jwt.get_unverified_header(token.credentials)
        kid = header.get("kid")
        token_alg = header.get("alg")
        if not kid or not token_alg:
            raise HTTPException(403, detail="Missing `kid` or `alg` in token header")

        jwk_dict = await fetch_jwk(kid)
        jwk_alg = jwk_dict.get("alg")

        # Validate alg match and allowlist
        if jwk_alg != token_alg:
            raise HTTPException(403, detail=f"Token alg {token_alg} doesn't match JWK alg {jwk_alg}")
        if jwk_alg not in ALLOWED_ALGS:
            raise HTTPException(403, detail=f"Disallowed algorithm: {jwk_alg}")

        public_key = jose_jwk.construct(jwk_dict, algorithm=token_alg)
        payload = jwt.decode(
            token.credentials,
            key=public_key,
            algorithms=[token_alg],
            issuer=JWT_ISSUER,
            audience=EXPECTED_AUDIENCE,
        )

        return payload

    except JWTError as e:
        raise HTTPException(403, detail=f"Invalid JWT: {e}")