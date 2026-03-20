# ADR-002: Use KMS as Internal Docker Service

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-02 |
| **Superseded by** | [ADR-008](ADR-008-isolate-kms-third-party-direct-client-access.md) |
| **SADD reference** | §4.2 — Original Architecture |

---

## Context

The system requires a Key Management Service (KMS) to manage Customer Master Keys (CMKs) and perform DEK wrap/unwrap operations. A decision was needed on whether to host KMS as an internal service within the Docker network or use a third-party managed KMS provider.

---

## Decision

Host KMS as an **internal Docker service** within the same Docker network as all other services. NGINX routes external traffic to KMS alongside other services. KMS is treated as a first-party internal service.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Internal Docker KMS** | KMS runs as a container inside the Docker network | Full control; no external dependency; simpler initial setup | Must maintain KMS software, security patches, and key storage; NGINX exposes KMS to external traffic | ✅ Selected |
| **Third-party managed KMS** | Use AWS KMS / GCP KMS / Azure Key Vault | Managed security; compliance certifications; high availability guaranteed; private keys never leave provider HSM | External dependency; latency on every KMS call; cost per API call | ❌ Deferred — external dependency not desired at initial design stage |

---

## Rationale

An internal KMS was selected to avoid external service dependencies and keep the initial architecture self-contained. The operational complexity of maintaining a production KMS was considered acceptable at the time.

---

## Consequences

**Enables:**
- Self-contained architecture — no external service dependencies
- Full control over KMS configuration and behaviour

**Constrains:**
- KMS is reachable via NGINX — increases attack surface significantly
- All services including KMS share the same Docker network — no isolation boundary between KMS and other services
- Operational burden of maintaining KMS security, patches, and key storage falls on the team

**Requires:**
- Strict NGINX routing rules to limit KMS exposure
- Regular security patching of KMS software
- Secure key storage within the container

---

## References

- [SADD §4.2 — Original Architecture](../architecture/security-architecture-design-document.md#42-original-architecture)
- [SADD §5.2 — KMS Directly Reachable via NGINX](../architecture/security-architecture-design-document.md#52-kms-directly-reachable-via-nginx-critical)
- [ADR-008 — Isolate KMS, Third-Party KMS Direct Client Access](ADR-008-isolate-kms-third-party-direct-client-access.md)
