# ADR-011: Short-Lived JWT with Refresh Token Rotation

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-005](ADR-005-jwt-sole-authentication.md) |
| **SADD reference** | §6.5 — Short-Lived JWT with Rotation |

---

## Context

ADR-005 established JWT as the authentication mechanism with no defined expiry enforcement and no refresh token rotation. A stolen JWT remains valid until it naturally expires — there is no way to revoke it. This means logout does not invalidate a token, and a stolen token grants wide access for its full lifetime.

---

## Decision

Issue JWTs with a **short expiry of 15 minutes**. Implement **single-use refresh token rotation** — each refresh token can be used exactly once to obtain a new JWT and a new refresh token. The old refresh token is immediately invalidated on use. For WebSocket sessions, JWT refresh is performed in-band over the WebSocket connection before expiry.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **Long-lived JWT, no rotation** | JWT valid for hours or days; no refresh mechanism | Simple | Stolen token valid for long window; no revocation capability | ❌ Superseded |
| **Short-lived JWT, no refresh** | JWT expires in 15 minutes; user must re-authenticate | Limits theft window | Poor UX — user re-authenticates every 15 minutes | ❌ Rejected |
| **Short-lived JWT + refresh token rotation** | JWT expires in 15 minutes; single-use refresh token issues new JWT + new refresh token | Limits theft window; near-real-time revocation via refresh token invalidation; smooth UX | More complex token management; refresh token storage required server-side | ✅ Selected |
| **Session-based auth** | Server-side sessions; immediate revocation | Immediate revocation | Stateful — horizontal scaling requires shared session store | ❌ Rejected |

---

## Rationale

Short-lived JWT with refresh token rotation was selected because it provides near-real-time revocation capability without requiring stateful sessions. When a refresh token is used, the old one is immediately invalidated — a stolen refresh token can only be used once before the legitimate user's next refresh invalidates it. This creates a detection opportunity: if both the attacker and the legitimate user attempt to use the same refresh token, the second use will fail and can trigger a security alert.

The 15-minute JWT expiry was chosen as the industry standard balance between security (limiting theft window) and performance (minimising refresh frequency).

In-band JWT refresh over WebSocket was chosen to avoid interrupting real-time sessions — the client refreshes the token transparently before it expires without closing the WebSocket connection.

---

## Consequences

**Enables:**
- Stolen JWT is valid for at most 15 minutes
- Refresh token theft is detectable — duplicate use triggers security alert
- Logout can invalidate the refresh token server-side, effectively revoking session access within 15 minutes
- Smooth UX — users do not need to re-authenticate every 15 minutes

**Constrains:**
- Refresh tokens must be stored server-side to support invalidation — adds small amount of server-side state
- WebSocket sessions require in-band refresh logic
- Clock skew between client and server must be managed — a small tolerance window (e.g. 30 seconds) is applied

**Requires:**
- Refresh token store in Auth DB
- In-band JWT refresh protocol implemented in the WebSocket handler
- Security alerting on duplicate refresh token use

---

## References

- [SADD §6.5 — Short-Lived JWT with Rotation](../architecture/security-architecture-design-document.md#65-short-lived-jwt-with-rotation)
- [ADR-005 — Use JWT as Sole Authentication Mechanism](ADR-005-jwt-sole-authentication.md)
- RFC 7519 — JWT: https://datatracker.ietf.org/doc/html/rfc7519
- OAuth 2.0 Refresh Token Rotation: https://auth0.com/docs/secure/tokens/refresh-tokens/refresh-token-rotation
