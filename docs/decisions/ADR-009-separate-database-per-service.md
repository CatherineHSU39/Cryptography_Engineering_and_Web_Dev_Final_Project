# ADR-009: Separate Database Per Service

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-003](ADR-003-shared-database.md) |
| **SADD reference** | §6.3 — Separate Database Per Service |

---

## Context

ADR-003 established a single shared database container accessible by all services. A security review identified this as a critical weakness — a breach in any one service exposes data belonging to all services, including auth tokens, encrypted messages, encrypted DEKs, and key material.

---

## Decision

Replace the single shared database with **four isolated databases**, one per service:

| Service | Database |
|---|---|
| Auth server | Auth DB — users, tokens |
| Backend | Messages DB — encrypted messages |
| DEK service | DEK DB — encrypted DEKs |
| Presence service | Presence DB — online status |

Each service holds credentials only for its own database. No service credential grants access to another service's database.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Single shared database** | All services share one DB instance | Simple; low overhead | No isolation; breach in one service exposes all data | ❌ Superseded by this ADR |
| **Separate schemas, shared instance** | One DB instance, separate schemas per service with per-service DB users | Some isolation; lower infrastructure overhead than separate instances | Schema-level isolation is weaker than instance-level; a misconfigured DB user can cross schemas; shared instance is still a single point of failure | ❌ Rejected — isolation guarantee weaker than separate instances |
| **Separate database per service** | Each service has its own isolated DB instance | Strong isolation; breach in one service cannot expose another's data; per-service credentials; independent scaling | Higher infrastructure overhead; migrations and backups managed per database | ✅ Selected |

---

## Rationale

Separate databases per service was selected because it provides the strongest isolation guarantee — cross-service data access at the database layer is architecturally impossible, not just policy-controlled. This removes an entire class of lateral movement attack that is present when services share a database.

The separate schemas approach was considered but rejected because schema-level isolation still relies on correct DB user privilege configuration, which is a policy control rather than an architectural guarantee. A misconfigured DB user or a privilege escalation vulnerability could still enable cross-schema access.

The higher infrastructure overhead is accepted — each database is small in scope, and the isolation benefit outweighs the management cost.

---

## Consequences

**Enables:**
- Cross-service data access at the database layer is architecturally impossible
- Each service can use the database engine most appropriate for its data (e.g. Redis for Presence)
- Independent scaling and backup policies per service

**Constrains:**
- Migrations and backups must be managed per database
- Cross-service queries are not possible — any cross-service data needs must go through service APIs

**Requires:**
- Four separate database instances provisioned and configured
- Per-service database credentials managed in a secrets manager
- Independent backup and restore procedures per database

---

## References

- [SADD §6.3 — Separate Database Per Service](../architecture/security-architecture-design-document.md#63-separate-database-per-service)
- [ADR-003 — Use Single Shared Database Container](ADR-003-shared-database.md)
