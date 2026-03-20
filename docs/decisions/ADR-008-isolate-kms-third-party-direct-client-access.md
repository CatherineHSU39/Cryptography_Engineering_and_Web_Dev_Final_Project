# ADR-008: Isolate KMS — Third-Party KMS with Direct Client Access

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-002](ADR-002-kms-internal-service.md), [ADR-004](ADR-004-nginx-routes-all-services.md) |
| **SADD reference** | §6.2 — Isolate KMS from NGINX Routing |

---

## Context

ADR-002 established KMS as an internal Docker service, and ADR-004 routed all services including KMS through NGINX. This means KMS — the most sensitive service in the system — is directly reachable from the internet via NGINX. Any NGINX misconfiguration, DDoS attack, or brute-force attempt can directly target KMS.

Additionally, the switch to asymmetric CMK (ADR-007) changes the role of KMS — clients now need to call KMS directly for DEK decryption rather than routing through the Backend. This makes an internal Docker KMS model less appropriate.

---

## Decision

Replace the internal Docker KMS with a **third-party managed KMS** (AWS KMS, GCP KMS, or Azure Key Vault). Remove KMS entirely from NGINX routing — KMS is no longer part of the Docker network. Clients call the third-party KMS HTTPS endpoint directly for DEK decryption operations. The Backend calls the third-party KMS only for CMK creation and FIDO challenge operations.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Keep internal KMS, remove from NGINX** | KMS stays in Docker network but NGINX no longer routes to it | KMS not directly reachable from internet | Still an internal service to maintain; clients cannot call it directly for decryption | ❌ Rejected — adds routing complexity without solving key management burden |
| **Third-party KMS, routed via Backend** | Third-party KMS; all client KMS calls proxied through Backend | Backend enforces auth before forwarding | Backend sees plaintext DEK on unwrap response — reintroduces critical weakness | ❌ Rejected |
| **Third-party KMS, direct client access** | Third-party KMS; clients call KMS directly for decryption; Backend calls KMS only for admin operations | KMS completely removed from Docker network; private CMK never leaves provider HSM; clients authenticate directly with KMS using JWT + FIDO | External service dependency; latency on client-to-KMS calls | ✅ Selected |

---

## Rationale

A third-party managed KMS was selected for three reasons:

1. **Security:** Third-party KMS providers store private keys in Hardware Security Modules (HSMs) — the private CMK never leaves the HSM. This is a stronger guarantee than a self-managed Docker container where key material could theoretically be extracted.

2. **Attack surface reduction:** Removing KMS from the Docker network and NGINX routing entirely eliminates it as an internal attack surface. An attacker who compromises the Docker network cannot reach KMS.

3. **Operational burden:** Managing a production KMS is a significant operational responsibility — key storage, availability, security patching, and audit logging. Third-party providers handle all of this with SLA guarantees and compliance certifications (SOC 2, FIPS 140-2).

Direct client-to-KMS access for decryption was chosen over Backend-proxied access because proxying would require the Backend to handle the unwrap response — reintroducing plaintext DEK exposure on the Backend, which ADR-007 explicitly eliminates.

---

## Consequences

**Enables:**
- KMS completely removed from Docker network and NGINX routing — zero internal attack surface
- Private CMK stored in HSM — strongest available key protection
- Clients authenticate directly with KMS using JWT + FIDO2 — no intermediary that could intercept plaintext DEK
- Operational KMS management (availability, patching, audit) handled by provider

**Constrains:**
- External service dependency — KMS availability depends on third-party provider SLA
- Latency on client-to-KMS calls for decryption — acceptable given decryption is a user-initiated action, not a background operation
- Provider must support asymmetric CMKs (confirmed for AWS KMS, GCP KMS, Azure Key Vault)

**Requires:**
- Selection of a specific third-party KMS provider
- Client-side KMS SDK integration
- Backend KMS SDK integration for CMK creation and FIDO challenge
- IAM / access policy configuration on the KMS provider to restrict operations per client identity

---

## References

- [SADD §6.2 — Isolate KMS from NGINX Routing](../architecture/security-architecture-design-document.md#62-isolate-kms-from-nginx-routing)
- [ADR-002 — Use KMS as Internal Docker Service](ADR-002-kms-internal-service.md)
- [ADR-004 — Route All Services Through NGINX](ADR-004-nginx-routes-all-services.md)
- [ADR-007 — Switch to Asymmetric CMK](ADR-007-asymmetric-cmk-client-side-encryption.md)
- AWS KMS: https://docs.aws.amazon.com/kms/latest/developerguide/
- GCP KMS: https://cloud.google.com/kms/docs
- Azure Key Vault: https://learn.microsoft.com/en-us/azure/key-vault/
