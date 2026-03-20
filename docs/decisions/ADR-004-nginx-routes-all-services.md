# ADR-004: Route All Services Through NGINX

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-02 |
| **Superseded by** | [ADR-008](ADR-008-isolate-kms-third-party-direct-client-access.md) |
| **SADD reference** | §4.2 — Original Architecture |

---

## Context

The system uses NGINX as a reverse proxy and sole public-facing entry point. A decision was needed on which internal services NGINX should route external traffic to. The initial approach was to treat all services uniformly and route traffic to all of them through NGINX.

---

## Decision

Configure NGINX to route external traffic to **all internal services** — Backend, Auth server, DEK service, and KMS service — without distinguishing between public-facing and internal-only services.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Route all services through NGINX** | NGINX proxies to Backend, Auth, DEK, and KMS equally | Simple uniform routing; easy to configure | KMS and DEK service are sensitive internal services that should never be directly reachable from external traffic; increases attack surface of most sensitive services | ✅ Selected |
| **Route only public-facing services through NGINX** | NGINX proxies only to Backend and Auth; KMS and DEK accessible only internally | Minimises attack surface; sensitive services unreachable from outside | Slightly more complex routing configuration | ❌ Not considered at initial design stage |

---

## Rationale

All services were placed uniformly behind NGINX without distinguishing between services that legitimately need external access (Backend, Auth) and services that should be internal-only (KMS, DEK). This was a simplification that did not account for the sensitivity difference between services.

---

## Consequences

**Enables:**
- Simple, uniform NGINX configuration

**Constrains:**
- KMS — the most sensitive service in the system — is directly reachable from the internet via NGINX
- A NGINX misconfiguration could expose KMS or DEK service directly
- DDoS and brute-force attacks can target KMS directly without passing through any application-layer guard

**Requires:**
- Strict NGINX location blocks to restrict access to KMS and DEK endpoints
- Regular audit of NGINX routing rules

---

## References

- [SADD §4.2 — Original Architecture](../architecture/security-architecture-design-document.md#42-original-architecture)
- [SADD §5.2 — KMS Directly Reachable via NGINX](../architecture/security-architecture-design-document.md#52-kms-directly-reachable-via-nginx-critical)
- [ADR-008 — Isolate KMS, Third-Party KMS Direct Client Access](ADR-008-isolate-kms-third-party-direct-client-access.md)
