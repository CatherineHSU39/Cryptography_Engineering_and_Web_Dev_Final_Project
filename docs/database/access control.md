## 🟣 KMS (FastAPI)

Handles encryption, DEK wrapping/unwrapping, CMK rotation, and logging.

| API                    | Role           | DB Access Required                              |
| ---------------------- | -------------- | ----------------------------------------------- |
| /kms/generate-data-key | user           | INSERT INTO encrypted_deks                      |
| /kms/wrap-key          | user           | INSERT INTO encrypted_deks                      |
| /kms/decrypt-data-key  | user           | SELECT FROM encrypted_deks, SELECT FROM cmks    |
| /kms/rotate-cmk        | admin          | UPDATE cmks, INSERT cmks, UPDATE encrypted_deks |
| /kms/audit-log         | auditor        | SELECT FROM audit_log                           |
| /kms/log-integrity     | admin, auditor | SELECT FROM audit_log                           |

### ✅ KMS DB Permissions:

- SELECT, INSERT, UPDATE ON encrypted_deks
- SELECT, INSERT, UPDATE ON cmks
- SELECT, INSERT ON audit_log

## 🔵 Messaging Backend (Spring)

Manages messages, groups, and membership.

| API                               | DB tables Impacted                        |
| --------------------------------- | ----------------------------------------- |
| /app/messages                     | INSERT messages, INSERT message_dek_links |
| /app/messages/{id}                | SELECT messages, SELECT message_dek_links |
| /app/groups (GET/POST/PUT/DELETE) | Full R/W on groups, group_members         |

### ✅ Backend DB Permissions:

- SELECT, INSERT, UPDATE, DELETE ON messages
- SELECT, INSERT, UPDATE, DELETE ON message_dek_links
- SELECT, INSERT, UPDATE, DELETE ON groups
- SELECT, INSERT, UPDATE, DELETE ON group_members
- SELECT ON users_backend_view

## 🟢 Auth Server (Spring)

Handles registration, login, 2FA, and issuing tokens.

| API              | DB Tables Impacted                       |
| ---------------- | ---------------------------------------- |
| /auth/register   | INSERT users, INSERT user_totp_dek_links |
| /auth/login      | SELECT users, SELECT user_totp_dek_links |
| /auth/verify-otp | SELECT FROM user_totp_dek_links          |
| /auth/token      | SELECT FROM users                        |

### ✅ Auth Server DB Permissions:

- SELECT, INSERT, UPDATE ON users
- SELECT, INSERT, UPDATE ON user_totp_dek_links

## ✅ Final Permission Table

| Table               | backend | auth_server | kms              |
| ------------------- | ------- | ----------- | ---------------- |
| users               | ❌      | ✅ R/W      | ❌               |
| users_backend_view  | ✅ R    | ❌          | ❌               |
| messages            | ✅ R/W  | ❌          | ❌               |
| groups              | ✅ R/W  | ❌          | ❌               |
| group_members       | ✅ R/W  | ❌          | ❌               |
| message_dek_links   | ✅ R/W  | ❌          | ❌               |
| encrypted_deks      | ❌      | ❌          | ✅ R/W           |
| user_totp_dek_links | ❌      | ✅ R/W      | ❌               |
| cmks                | ❌      | ❌          | ✅ R/W           |
| audit_log           | ❌      | ✅ INSERT   | ✅ SELECT/INSERT |
