# ADR-003: Use Single Shared Database Container

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-02 |
| **Superseded by** | [ADR-009](ADR-009-separate-database-per-service.md) |
| **SADD reference** | §4.2 — Original Architecture |

---

## Context

The system comprises multiple services — Auth server, Backend, DEK service, and KMS service — each requiring persistent storage. A decision was needed on whether to use a single shared database or separate databases per service.

---

## Decision

Use a **single shared database container** accessible by all services. All services connect to the same database instance using shared credentials.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Single shared database** | One database container shared by all services | Simple setup; easy to manage; lower infrastructure overhead | No data isolation between services; a breach in one service exposes all data; shared credentials increase blast radius | ✅ Selected |
| **Separate database per service** | Each service has its own isolated database | Strong data isolation; breach in one service cannot expose another's data; per-service credentials | Higher infrastructure overhead; more complex migrations and backups | ❌ Deferred — isolation overhead not considered necessary at initial design stage |

---

## Rationale

A single shared database was selected to minimise infrastructure complexity at the initial design stage. All services were trusted equally within the Docker network, and the operational overhead of managing multiple databases was not considered justified at the time.

---

## Consequences

**Enables:**
- Simple database management — one instance to maintain, back up, and monitor

**Constrains:**
- No data isolation between services — a SQL injection or application breach in any service exposes all data including auth tokens, encrypted messages, encrypted DEKs, and key material
- Shared credentials mean a compromised service credential grants access to all service data
- Violates the principle of least privilege at the database layer

**Requires:**
- Strict application-layer access control to compensate for lack of database-layer isolation

---

## References

- [SADD §4.2 — Original Architecture](../architecture/security-architecture-design-document.md#42-original-architecture)
- [SADD §5.3 — Shared Database Container](../architecture/security-architecture-design-document.md#53-shared-database-container-critical)
- [ADR-009 — Separate Database Per Service](ADR-009-separate-database-per-service.md)
