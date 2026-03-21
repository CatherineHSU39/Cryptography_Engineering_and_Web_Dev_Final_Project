# ADR-001: Use Symmetric CMK for DEK Envelope Encryption

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-02 |
| **Superseded by** | [ADR-007](ADR-007-asymmetric-cmk-client-side-encryption.md) |
| **SADD reference** | §4.3 — DEK Encryption Model |

---

## Context

The system requires end-to-end encrypted messaging where multiple recipients can receive the same message. A key management strategy was needed to protect Data Encryption Keys (DEKs) at rest — one that could support multiple recipients per message without storing multiple copies of the full message ciphertext.

The system uses a KMS (Key Management Service) to manage Customer Master Keys (CMKs). A decision was needed on whether those CMKs should be symmetric or asymmetric.

---

## Decision

Use **symmetric CMKs** managed inside KMS. DEK encryption (wrap) and DEK decryption (unwrap) are both performed by KMS using the same symmetric key. The client sends the plaintext DEK to KMS for wrapping; KMS returns one encrypted DEK per recipient. On retrieval, the client sends an encrypted DEK to KMS for unwrapping; KMS returns the plaintext DEK re-encrypted with the user's FIDO public key for safe transit back to the client.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Symmetric CMK (KMS wraps and unwraps)** | KMS generates, wraps, and unwraps DEKs using a symmetric key | Simple implementation; standard KMS pattern; well-supported by all providers | KMS sees plaintext DEK on every wrap and unwrap operation | ✅ Selected |
| **Asymmetric CMK (client wraps, KMS unwraps)** | Client encrypts DEK locally with recipient's public CMK; KMS only decrypts | Plaintext DEK never leaves client; KMS not involved in send path | Requires asymmetric CMK support; requires public CMK distribution mechanism | ❌ Deferred — complexity not justified at this stage |
| **Per-recipient message encryption** | Full message encrypted separately for each recipient | No shared DEK | O(N) storage cost; impractical for media and large messages | ❌ Rejected |

---

## Rationale

Symmetric CMK was selected because it is the standard envelope encryption pattern as defined and recommended by AWS KMS, GCP KMS, and Azure Key Vault. It is well-understood, well-documented, and straightforward to implement. The asymmetric alternative was considered but deferred — the additional complexity of public key distribution and client-side wrapping was not considered necessary at the initial design stage.

The multi-recipient DEK model (one message ciphertext, N encrypted DEK entries) was chosen over per-recipient message encryption because it is storage-efficient and scales well for media and large messages. This is the same model used by PGP and S/MIME.

---

## Consequences

**Enables:**
- Efficient multi-recipient messaging — one ciphertext stored regardless of recipient count
- Standard KMS integration pattern — compatible with all major KMS providers out of the box

**Constrains:**
- KMS must be in the critical path of every message send and every message decrypt — KMS sees plaintext DEK on both operations
- A KMS compromise exposes DEKs processed during the compromise window
- No way to perform client-side DEK wrapping without exposing the symmetric key itself

**Requires:**
- KMS to be highly available — unavailability blocks all message send and decrypt operations
- Strict access control and audit logging on KMS to limit exposure window

---

## References

- [SADD §4.3 — DEK Encryption Model](../architecture/security-architecture-design-document.md#43-dek-encryption-model)
- [ADR-007 — Switch to Asymmetric CMK](ADR-007-asymmetric-cmk-client-side-encryption.md)
- AWS KMS Envelope Encryption: https://docs.aws.amazon.com/kms/latest/developerguide/concepts.html#enveloping
