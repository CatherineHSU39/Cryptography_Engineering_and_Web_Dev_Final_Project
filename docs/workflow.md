# 流程

## 工作流程（信封加密模型）：

**訊息發送：**

1. 用戶端認證並請求 DEK
2. KMS 生成隨機 ChaCha20 DEK
3. 對每位收件人，使用其 CMK 或公鑰加密 DEK
4. KMS 回傳：
   - 明文 DEK（僅供立即使用）
   - 每位收件人對應的加密 DEK 清單
5. 用戶端使用 DEK 加密訊息內容並立即銷毀明文 DEK
6. 用戶將以下資料傳送至伺服器：
   - 加密訊息
   - 加密 DEK 清單及對應收件人 ID
   - Metadata（發送者 ID、群組 ID、時間戳）

**訊息接收：**

1. 用戶端從伺服器取得加密訊息與其對應的加密 DEK
2. 用戶端認證後提交加密 DEK 至 KMS
3. KMS 驗證角色與權限，並在模擬 HSM 中解密 DEK
4. KMS 記錄此請求並加入雜湊鏈稽核日誌
5. 回傳明文 DEK，供用戶端解密訊息

## 授權流程

認證模型：自建 OAuth2 與本地登入 + 雙因素認證（2FA）

本系統採用混合式認證模型，結合本地帳密登入與 OAuth2 權杖授權機制。使用者先透過後端管理的憑證庫進行帳密驗證，再進行基於 TOTP 的雙因素驗證（如使用 Google Authenticator）。當認證與 2FA 驗證皆成功後，系統將簽發一組 JWT 權杖，其中包含使用者身份、角色與 2FA 驗證狀態。

**登入流程：**

1. `POST /auth/login`

   - 接收使用者名稱與密碼
   - 透過本地用戶資料庫驗證憑證

2. `POST /auth/verify-otp`

   - 接收 TOTP 驗證碼（例如來自 Google Authenticator）
   - 成功後將此 session 標記為已完成 2FA 驗證

3. `POST /auth/token`

   - 簽發帶有以下 claims 的 JWT 權杖：
     - `sub`：使用者 ID
     - `role`：user | admin | auditor
     - `2fa_verified`：true
     - `exp`：過期時間戳

前端會將此權杖安全儲存，並於所有 API 請求中以 `Authorization: Bearer <token>` 附加傳送。

**KMS 與後端的權杖驗證流程：**

> - 所有 API 端點皆需驗證 JWT 權杖之有效性
> - 敏感操作（如 /rotate-cmk、/decrypt-data-key）需確保 2fa_verified 為 true

1. 從 `Authorization: Bearer <token>` 拿到 JWT
2. 讀取 JWT header -> 得知 JWT 種類 (user 或 service)
3. 使用對應的 key 驗證
4. 根據 JWT 中的角色（role）或對象（sub）欄位給予存取權

**權杖安全性考量：**

- JWT 權杖具短效期限，並以後端機密或非對稱金鑰（如 RS256）簽名保護
- 當 session 逾時需重新驗證或使用 refresh token 更新
- 稽核日誌會記錄每一操作對應之使用者身份資訊
- 用戶端從伺服器取得加密訊息與其對應的加密 DEK
