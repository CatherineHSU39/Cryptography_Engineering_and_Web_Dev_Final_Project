# ADR-007: Switch to Asymmetric CMK — Client-Side Envelope Encryption

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-001](ADR-001-symmetric-cmk.md) |
| **SADD reference** | §6.1 — Switch from Symmetric CMK to Asymmetric CMK |

---

## Context

ADR-001 established the use of symmetric CMKs for DEK envelope encryption. Under that model, KMS performs both the wrap (DEK encryption) and unwrap (DEK decryption) operations. This means the plaintext DEK exists in KMS memory on every message send and every message decrypt.

A security review identified this as a critical weakness — a KMS compromise during either operation exposes the plaintext DEK. Because DEK is per-message, each exposed DEK reveals one message. However, a sustained or undetected KMS compromise could expose all messages processed during that period.

A new model is needed where KMS never sees the plaintext DEK.

---

## Decision

Switch to **asymmetric CMK** (RSA or ECC keypair). The public CMK is retrievable by clients and cached internally. Clients encrypt DEKs locally using the recipient's public CMK — KMS is not involved in the send path. KMS only performs the unwrap (decryption) operation using the private CMK, which never leaves KMS.

This is referred to as **client-side envelope encryption** — the wrap is client-side, the unwrap is KMS-side.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Keep symmetric CMK** | No change — KMS wraps and unwraps | Simple; no migration needed | KMS sees plaintext DEK on every operation; critical risk | ❌ Rejected |
| **Per-recipient message encryption** | Encrypt full message separately for each recipient | No shared DEK; KMS not involved | O(N) storage; impractical for media and large messages | ❌ Rejected |
| **Asymmetric CMK — client wraps, KMS unwraps** | Client encrypts DEK with public CMK locally; KMS only decrypts with private CMK | Plaintext DEK never leaves client; KMS not in send critical path; private CMK never leaves KMS | Requires asymmetric CMK support from provider; requires public CMK distribution and caching | ✅ Selected |

---

## Rationale

Asymmetric CMK was selected because it eliminates plaintext DEK exposure at the source — the wrap operation moves to the client where the plaintext DEK already exists. KMS becomes a decryption-only service, which is a much narrower and less risky role.

All major KMS providers (AWS KMS, GCP KMS, Azure Key Vault) support asymmetric CMKs with RSA and ECC, so no provider lock-in or exotic cryptographic assumptions are introduced.

The public CMK can be safely cached inside the Docker network (it is not a secret) — this eliminates the need for a KMS call on every message send for key fetching, keeping KMS out of the send critical path entirely.

The per-recipient message encryption alternative was rejected because it would multiply storage costs by the number of recipients — impractical for group conversations involving media or large files.

---

## Consequences

**Enables:**
- Plaintext DEK never exists on any server at any point in the send flow
- KMS is removed from the message send critical path — reducing KMS load and making KMS unavailability non-blocking for sends
- Client-side encryption is compatible with offline recipient scenarios — sender can always encrypt for any recipient using their cached public CMK

**Constrains:**
- Requires asymmetric CMK support from the KMS provider (confirmed supported by all major providers)
- Public CMK cache must be maintained inside the Docker network — cache entries must be invalidated on CMK rotation
- CMK rotation now requires re-wrapping all stored encrypted DEKs with the new public CMK — a background migration operation

**Requires:**
- Public CMK cache (Redis) deployed as an internal-only service
- CMK rotation procedure defined and documented
- Client-side DEK encryption implementation using recipient public CMK

---

## References

- [SADD §6.1 — Switch from Symmetric CMK to Asymmetric CMK](../architecture/security-architecture-design-document.md#61-switch-from-symmetric-cmk-to-asymmetric-cmk)
- [ADR-001 — Use Symmetric CMK for DEK Envelope Encryption](ADR-001-symmetric-cmk.md)
- AWS KMS Asymmetric Keys: https://docs.aws.amazon.com/kms/latest/developerguide/symmetric-asymmetric.html
- GCP Cloud KMS: https://cloud.google.com/kms/docs
- Azure Key Vault: https://learn.microsoft.com/en-us/azure/key-vault/
