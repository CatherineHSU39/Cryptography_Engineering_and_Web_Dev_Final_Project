# 資料庫設計

## Users

| 欄位                  | 型別                              | 限制條件                   | 說明                                     |
| --------------------- | --------------------------------- | -------------------------- | ---------------------------------------- |
| id                    | UUID                              | Primary key, not null      | 使用者 id                                |
| username              | String                            | Unique, not null           | 登入名稱                                 |
| password_hash         | String                            | Not null                   | bcrypt 雜湊過的密碼                      |
| encrypted_totp_secret | String                            | Not null                   | 用於 2FA 的 TOTP 共用密鑰（用 DEK 加密） |
| role                  | Enum (`user`, `admin`, `auditor`) | Not null, check constraint | 存取權                                   |
| created_at            | Timestamp                         | Default = now(), not null  | 註冊時間                                 |

## Messages

| 欄位              | 型別         | 限制條件                          | 說明             |
| ----------------- | ------------ | --------------------------------- | ---------------- |
| id                | UUID         | Primary key, not null             | 訊息 id          |
| sender_id         | UUID         | Foreign key → Users(id), not null | 訊息發送者       |
| group_id          | UUID or Null | Optional foreign key              | 所屬群組或討論串 |
| encrypted_message | Blob         | Not null                          | 加密過的訊息內容 |
| created_at        | Timestamp    | Default = now(), not null         | 訊息建立時間     |
| updated_at        | Timestamp    | Default = now(), not null         | 最後修改時間     |

## Groups

| 欄位       | 型別      | 限制條件                  | 說明       |
| ---------- | --------- | ------------------------- | ---------- |
| id         | UUID      | Primary key               | 聊天室 id  |
| name       | String    | not null                  | 聊天室名稱 |
| created_at | Timestamp | Default = now(), not null | 建立時間   |

## GroupMembers

| 欄位                           | 型別      | 限制條件                                       | 說明           |
| ------------------------------ | --------- | ---------------------------------------------- | -------------- |
| group_id                       | UUID      | Foreign key → Groups(id), not null             | 所屬聊天室     |
| user_id                        | UUID      | Foreign key → Users(id), not null              | 成員的 user_id |
| joined_at                      | Timestamp | Default = now(), not null                      | 加入時間       |
| PRIMARY KEY(group_id, user_id) |           | 確保任一個 user 跟任一個聊天室的組合只出現一次 |

## EncryptedDEKs

| 欄位          | 型別                | 限制條件                  | 說明                |
| ------------- | ------------------- | ------------------------- | ------------------- |
| id            | UUID                | Primary key, not null     | 包裝過的 DEK 的 id  |
| owner_id      | UUID                | REFERENCES Users(id)      | 擁有者              |
| cmk_version   | Integer             | ≥ 1, not null             | 用來包裝的 CMK 版本 |
| encrypted_dek | Blob                | Not null                  | 包裝過的 DEK        |
| purpose       | Enum(message, totp) | DEK 的用途                |
| created_at    | Timestamp           | Default = now(), not null | 建立時間            |

## MessageDEKLinks

| 欄位                                   | 型別 | 限制條件                                  | 說明                                 |
| -------------------------------------- | ---- | ----------------------------------------- | ------------------------------------ |
| message_id                             | UUID | Foreign key → Messages(id), not null      | 對應的訊息                           |
| recipient_id                           | UUID | Foreign key → Users(id), not null         | 指定收件人                           |
| dek_id                                 | UUID | Foreign key → EncryptedDEKs(id), not null | 對應的 DEK                           |
| PRIMARY KEY (message_id, recipient_id) |      |                                           | 確保每則訊息對每位收件人僅有一個 DEK |

## UserTOTPDEKLinks

| 欄位                  | 型別 | 限制條件                                  | 說明                       |
| --------------------- | ---- | ----------------------------------------- | -------------------------- |
| user_id               | UUID | Foreign key → Users(id), not null         | 對應的使用者               |
| dek_id                | UUID | Foreign key → EncryptedDEKs(id), not null | 對應的 DEK                 |
| PRIMARY KEY (user_id) |      |                                           | 確保每位使用者僅有一個 DEK |
| UNIQUE (dek_id)       |      |                                           | 確保 DEK 不重複            |

## CMKs

| 欄位                      | 型別      | 限制條件                          | 說明                     |
| ------------------------- | --------- | --------------------------------- | ------------------------ |
| id                        | UUID      | Not null                          | CMK group id             |
| user_id                   | UUID      | Foreign key → Users(id), Not null | 金鑰所有人               |
| version                   | Integer   | ≥ 1, part of primary key          | CMK 的版本編號           |
| key_material              | Blob      | Not null                          | 金鑰資料（靜態加密儲存） |
| active                    | Boolean   | One per CMK group                 | 是否為目前啟用版本       |
| created_at                | Timestamp | Default = now(), not null         | 金鑰建立時間             |
| PRIMARY KEY (id, version) |           |                                   |                          |

## AuditLog

| 欄位      | 型別         | 限制條件                                         | 說明                       |
| --------- | ------------ | ------------------------------------------------ | -------------------------- |
| id        | UUID         | Primary key, not null                            | 日誌 id                    |
| actor_id  | UUID         | Foreign key → Users(id), not null                | 執行者                     |
| action    | String       | 操作類型（例如：wrap、unwrap、rotate）, not null |
| target_id | UUID or Null | Optional foreign key                             | 受影響的資源               |
| timestamp | Timestamp    | Default = now(), not null                        | 操作執行時間               |
| hash      | String       | Not null                                         | 此日誌的 SHA-256 雜湊值    |
| prev_hash | String       | Not null                                         | 前一項目的雜湊（用於鏈結） |
