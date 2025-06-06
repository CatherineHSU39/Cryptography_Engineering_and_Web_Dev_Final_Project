# API 列表

For access control see [access control.md](../database/access%20control.md)

## 🟣 Simulated KMS API (FastAPI)

這裡所有請求都需要經過授權才能執行

| Endpoint                 | 方法 | 角色           | 用途                                  |
| ------------------------ | ---- | -------------- | ------------------------------------- |
| `/kms/generate-data-key` | POST | user           | 產生一個 DEK 並以各個收件者的金鑰加密 |
| `/kms/wrap-key`          | POST | user           | 包裝一個使用者自己產生的 DEK          |
| `/kms/decrypt-data-key`  | POST | user           | 解包 DEK（要確認 JWT 是有效的）       |
| `/kms/rotate-cmk`        | POST | admin          | 輪替 CMK 並重新包裝 DEK               |
| `/kms/audit-log`         | GET  | auditor        | 讀取日誌                              |
| `/kms/log-integrity`     | GET  | admin, auditor | 確認日誌雜湊鍊的完整性                |

## 🔵 Messaging Backend API (Spring)

這裡所有請求都需要經過授權才能執行

| Endpoint             | 方法   | 用途                       |
| -------------------- | ------ | -------------------------- |
| `/app/messages`      | POST   | 新增訊息及包裝過的 DEK     |
| `/app/messages/{id}` | GET    | 取得特定訊息及包裝過的 DEK |
| `/app/groups/{id}`   | GET    | 取得某個聊天室             |
| `/app/groups`        | GET    | 取得聊天室列表             |
| `/app/groups`        | POST   | 新增聊天室                 |
| `/app/groups/{id}`   | DELETE | 刪除聊天室                 |
| `/app/groups/{id}`   | PUT    | 更新聊天室                 |

## 🟢 Authentication API (Spring)

| Endpoint           | 方法 | 用途                                            |
| ------------------ | ---- | ----------------------------------------------- |
| `/auth/register`   | POST | 註冊                                            |
| `/auth/login`      | POST | 登入                                            |
| `/auth/verify-otp` | POST | 2FA 驗證                                        |
| `/auth/token`      | POST | 取得 JWT (`sub`, `role`, `2fa_verified`, `exp`) |
