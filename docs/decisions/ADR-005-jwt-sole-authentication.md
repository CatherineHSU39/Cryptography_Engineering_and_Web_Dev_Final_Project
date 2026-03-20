# ADR-005: Use JWT as Sole Authentication Mechanism

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-02 |
| **Superseded by** | [ADR-011](ADR-011-short-lived-jwt-refresh-token-rotation.md), [ADR-014](ADR-014-totp-unified-second-factor.md) |
| **SADD reference** | §4.1 — Original System Flow |

---

## Context

The system requires an authentication mechanism to gate access to APIs, message storage, retrieval, and DEK operations. A decision was needed on what authentication mechanism to use for the majority of operations.

---

## Decision

Use **JWT (JSON Web Token)** as the sole authentication mechanism for all operations except DEK decryption (which additionally requires FIDO2). JWTs are issued at login with no defined expiry enforcement, no refresh token rotation, and no second factor at registration or login.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **JWT only** | Stateless JWT for all operations | Simple; stateless; well-understood; easy to implement | No revocation before expiry; no second factor; long-lived tokens increase theft window | ✅ Selected |
| **JWT + short expiry + rotation** | Short-lived JWT with refresh token rotation | Near-real-time revocation capability; limits theft window | More complex token management | ❌ Deferred |
| **JWT + TOTP 2FA** | JWT issued only after password + TOTP | Stronger identity assurance at login | Additional implementation complexity | ❌ Deferred |
| **Session-based auth** | Server-side sessions with session IDs | Immediate revocation | Stateful — does not scale horizontally as easily | ❌ Rejected |

---

## Rationale

JWT was selected for its simplicity and stateless nature, which fits well with a horizontally scalable microservice architecture. The absence of expiry enforcement and a second factor was accepted as a simplification at the initial design stage, with the understanding that these could be added later.

---

## Consequences

**Enables:**
- Simple, stateless authentication — easy to implement and integrate across all services
- Horizontal scalability — no shared session state required

**Constrains:**
- A stolen JWT grants wide access until it naturally expires — no revocation mechanism
- Password alone is sufficient to obtain a JWT — no second factor
- Logout does not invalidate the token — the token remains valid until expiry regardless of user action

**Requires:**
- Careful handling of JWT secrets to prevent forgery
- Application-layer checks to compensate for lack of revocation

---

## References

- [SADD §4.1 — Original System Flow](../architecture/security-architecture-design-document.md#41-original-system-flow)
- [SADD §5.5 — JWT as Sole Auth for Most Operations](../architecture/security-architecture-design-document.md#55-jwt-as-sole-auth-for-most-operations-medium)
- [ADR-011 — Short-Lived JWT with Refresh Token Rotation](ADR-011-short-lived-jwt-refresh-token-rotation.md)
- [ADR-014 — TOTP as Unified Second Factor](ADR-014-totp-unified-second-factor.md)
- RFC 7519 — JWT: https://datatracker.ietf.org/doc/html/rfc7519
