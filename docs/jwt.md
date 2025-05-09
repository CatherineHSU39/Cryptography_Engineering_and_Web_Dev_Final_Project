# JWT 設計

| 類別        | 使用情境                                                  | 簽署者      | 驗證者       | 金鑰            |
| ----------- | --------------------------------------------------------- | ----------- | ------------ | --------------- |
| User JWT    | 使用者驗證                                                | Auth Server | Backend, KMS | user-jwt-v1.pub |
| Service JWT | 伺服器驗證 (主要是驗證伺服器會需要要求 DEK 以加解密 TOTP) | Auth Server | KMS          | auth-jwt-v1.pub |

## JWT Header 格式

```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "user-jwt-v1" // or "auth-server-v1"
}
```

## User JWT Payload 範例

```json
{
  "sub": "user_123",
  "iss": "suth-server",
  "aud": "kms",
  "role": "user",
  "2fa_verified": true,
  "exp": 1712350000
}
```

## Service JWT Payload 範例

```json
{
  "sub": "auth-server",
  "iss": "auth-server",
  "aud": "kms",
  "role": "service",
  "exp": 1712350000
}
```
