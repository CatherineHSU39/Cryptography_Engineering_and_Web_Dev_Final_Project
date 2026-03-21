# Architecture Decision Records

This directory contains Architecture Decision Records (ADRs) for the End-to-End Encrypted Real-Time Messaging System.

Each ADR documents a single architectural decision — the context that motivated it, the options considered, the rationale for the choice made, and the consequences. ADRs are immutable once accepted — superseded decisions are kept for historical reference and linked forward to their replacement.

---

## Status Definitions

| Status         | Meaning                                                 |
| -------------- | ------------------------------------------------------- |
| **Accepted**   | Decision was made and implemented                       |
| **Draft**      | Decision documented but not yet implemented             |
| **Superseded** | Replaced by a newer ADR — kept for historical reference |

---

## Old Design Decisions

These ADRs document decisions made during the original system design. They are accepted and implemented. Where a new design decision supersedes them, the `Superseded by` field links forward.

| ADR                                               | Title                                         | Date       | Status   | Superseded by    |
| ------------------------------------------------- | --------------------------------------------- | ---------- | -------- | ---------------- |
| [ADR-001](ADR-001-symmetric-cmk.md)               | Use symmetric CMK for DEK envelope encryption | 2025-05-02 | Accepted | ADR-007          |
| [ADR-002](ADR-002-kms-internal-service.md)        | Use KMS as internal Docker service            | 2025-05-02 | Accepted | ADR-008          |
| [ADR-003](ADR-003-shared-database.md)             | Use single shared database container          | 2025-05-02 | Accepted | ADR-009          |
| [ADR-004](ADR-004-nginx-routes-all-services.md)   | Route all services through NGINX              | 2025-05-02 | Accepted | ADR-008          |
| [ADR-005](ADR-005-jwt-sole-authentication.md)     | Use JWT as sole authentication mechanism      | 2025-05-02 | Accepted | ADR-011, ADR-014 |
| [ADR-006](ADR-006-fido2-dek-decryption-gating.md) | Use FIDO2 for DEK decryption gating           | 2025-05-27 | Accepted | ADR-010          |

---

## New Design Decisions

These ADRs document decisions made during the security architecture redesign. They are currently in Draft status and will be promoted to Accepted as each migration phase completes.

| ADR                                                                | Title                                                                 | Date       | Status | Supersedes       |
| ------------------------------------------------------------------ | --------------------------------------------------------------------- | ---------- | ------ | ---------------- |
| [ADR-007](ADR-007-asymmetric-cmk-client-side-encryption.md)        | Switch to asymmetric CMK — client-side envelope encryption            | 2026-03-21 | Draft  | ADR-001          |
| [ADR-008](ADR-008-isolate-kms-third-party-direct-client-access.md) | Isolate KMS — third-party KMS with direct client access               | 2026-03-21 | Draft  | ADR-002, ADR-004 |
| [ADR-009](ADR-009-separate-database-per-service.md)                | Separate database per service                                         | 2026-03-21 | Draft  | ADR-003          |
| [ADR-010](ADR-010-totp-step-up-confirmation-fido-registration.md)  | TOTP step-up confirmation for FIDO key registration                   | 2026-03-21 | Draft  | ADR-006          |
| [ADR-011](ADR-011-short-lived-jwt-refresh-token-rotation.md)       | Short-lived JWT with refresh token rotation                           | 2026-03-21 | Draft  | ADR-005          |
| [ADR-012](ADR-012-mtls-internal-services.md)                       | mTLS between all internal services                                    | 2026-03-21 | Draft  | —                |
| [ADR-013](ADR-013-key-rotation-policy.md)                          | Define CMK, FIDO, and JWT signing key rotation policy                 | 2026-03-21 | Draft  | —                |
| [ADR-014](ADR-014-totp-unified-second-factor.md)                   | TOTP as unified second factor                                         | 2026-03-21 | Draft  | ADR-005          |
| [ADR-015](ADR-015-progressive-device-trust.md)                     | Progressive device trust — TOTP on new device, FIDO on trusted device | 2026-03-21 | Draft  | —                |

---

## Supersession Map

```
ADR-001 ──────────────────────────────────► ADR-007
ADR-002 ──────────────────────────────────► ADR-008
ADR-003 ──────────────────────────────────► ADR-009
ADR-004 ──────────────────────────────────► ADR-008
ADR-005 ──────────────────────────────────► ADR-011
        └─────────────────────────────────► ADR-014
ADR-006 ──────────────────────────────────► ADR-010
```

---

## Related Documents

- [Security Architecture Design Document](../architecture/security-architecture-design-document.md)
