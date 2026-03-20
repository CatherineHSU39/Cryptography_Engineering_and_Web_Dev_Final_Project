# ADR-012: mTLS Between All Internal Services

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | — |
| **SADD reference** | §6.6 — mTLS Between Internal Services |

---

## Context

Inside the Docker network, services communicate freely using Docker internal DNS. No mutual authentication between services was defined in the original design. A compromised internal service can impersonate any other service — the Backend could call the DEK service or KMS without any credential verification, and a rogue container could make requests to any service.

This violates the zero-trust principle — trust should never be implicit based on network location alone.

---

## Decision

Enforce **mutual TLS (mTLS)** for all service-to-service communication inside the Docker network. Each service holds a TLS certificate issued by an internal Certificate Authority (CA). Services reject connections from peers that do not present a valid certificate from the internal CA.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **No service-to-service auth** | Services trust any caller on the internal network | Simple; no certificate management | A compromised service or rogue container can impersonate any other service | ❌ Superseded |
| **Shared secret / API key** | Services authenticate using a shared secret | Simple to implement | Shared secrets are difficult to rotate; compromise of one service exposes the secret to all | ❌ Rejected |
| **Per-service API tokens** | Each service pair shares a unique token | Better than shared secret; per-service isolation | Token management overhead; still susceptible to token theft if a service is compromised | ❌ Rejected |
| **mTLS with internal CA** | Each service has a certificate; mutual authentication on every connection | Strong mutual authentication; certificate rotation possible; standard and well-tooled | Certificate management overhead; internal CA must be maintained | ✅ Selected |
| **Service mesh (Istio, Linkerd)** | mTLS managed by a service mesh sidecar | Automatic certificate rotation; observability | Significant operational complexity; overkill for current service count | ❌ Deferred — can be adopted later if service count grows |

---

## Rationale

mTLS was selected because it provides mutual authentication — both the caller and the callee verify each other's identity on every connection. This means a compromised service cannot impersonate another service, and a rogue container that joins the Docker network cannot make authenticated requests to internal services.

A service mesh was considered but deferred — the operational complexity of running a full service mesh (Istio, Linkerd) is not justified at the current service count. Direct mTLS with an internal CA provides the same security guarantee with less overhead.

Shared secrets and per-service API tokens were rejected because they are susceptible to theft — once a secret is exfiltrated, it can be used indefinitely until manually rotated. Certificate-based authentication with defined expiry and rotation policy is a stronger model.

---

## Consequences

**Enables:**
- Every service-to-service connection is mutually authenticated — no implicit trust based on network location
- A compromised service cannot impersonate another service
- Certificate expiry and rotation enforces regular key material refresh

**Constrains:**
- Internal CA must be maintained — certificate issuance, rotation, and revocation
- All services must be updated to present and verify certificates on every connection
- Certificate rotation must be coordinated to avoid service disruption

**Requires:**
- Internal CA provisioned and secured
- TLS certificates issued per service with defined expiry (e.g. 90 days)
- Automated certificate rotation process
- All service-to-service HTTP clients configured to require and verify peer certificates

---

## References

- [SADD §6.6 — mTLS Between Internal Services](../architecture/security-architecture-design-document.md#66-mtls-between-internal-services)
- OWASP Transport Layer Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html
