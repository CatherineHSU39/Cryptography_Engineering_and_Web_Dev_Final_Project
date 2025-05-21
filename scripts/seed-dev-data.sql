-- Fixed UUIDs
-- Alice: 11111111-1111-4111-8111-111111111111
-- Bob:   22222222-2222-4222-8222-222222222222
-- Group: aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa
-- Message: abababab-abab-4bab-8bab-abababababab
-- DEK: dddddddd-dddd-4ddd-8ddd-dddddddddddd
-- CMK: cccccccc-cccc-4ccc-8ccc-cccccccccccc
-- Audit: eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee

-- Sample Users
INSERT INTO users (id, username, password_hash, encrypted_totp_secret, role, created_at)
VALUES
  ('11111111-1111-4111-8111-111111111111', 'alice', 'bcrypt_hash_1', 'totp_enc_1', 'ROLE_USER', now()),
  ('22222222-2222-4222-8222-222222222222', 'bob', 'bcrypt_hash_2', 'totp_enc_2', 'ROLE_ADMIN', now());

-- Sample Groups
INSERT INTO groups (id, name, created_at)
VALUES
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'General', now());

-- Group Memberships
INSERT INTO group_members (group_id, user_id, joined_at)
VALUES
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '11111111-1111-4111-8111-111111111111', now()),
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', '22222222-2222-4222-8222-222222222222', now());

-- Sample Message
INSERT INTO messages (id, sender_id, group_id, encrypted_message, created_at, updated_at)
VALUES
  ('abababab-abab-4bab-8bab-abababababab', '11111111-1111-4111-8111-111111111111',
   'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', decode('deadbeef', 'hex'), now(), now());

-- Sample Encrypted DEKs
INSERT INTO encrypted_deks (id, owner_id, cmk_version, encrypted_dek, purpose, created_at)
VALUES
  ('dddddddd-dddd-4ddd-8ddd-dddddddddddd', '11111111-1111-4111-8111-111111111111', 1,
   decode('beadfeed', 'hex'), 'message', now());

-- Link DEK to Message
INSERT INTO message_dek_links (message_id, recipient_id, dek_id)
VALUES
  ('abababab-abab-4bab-8bab-abababababab', '22222222-2222-4222-8222-222222222222', 'dddddddd-dddd-4ddd-8ddd-dddddddddddd');

-- Link DEK to TOTP
INSERT INTO user_totp_dek_links (user_id, dek_id)
VALUES
  ('11111111-1111-4111-8111-111111111111', 'dddddddd-dddd-4ddd-8ddd-dddddddddddd');

-- Insert CMK
INSERT INTO cmks (id, user_id, version, key_material, active, created_at)
VALUES
  ('cccccccc-cccc-4ccc-8ccc-cccccccccccc', '11111111-1111-4111-8111-111111111111', 1,
   decode('cafebabe', 'hex'), true, now());

-- Insert AuditLog
INSERT INTO audit_log (id, actor_id, action, target_id, timestamp, hash, prev_hash)
VALUES
  ('eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee', '22222222-2222-4222-8222-222222222222',
   'wrap', 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', now(), 'hash_1', 'hash_0');