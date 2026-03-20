# ADR-014: TOTP as Unified Second Factor

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-005](ADR-005-jwt-sole-authentication.md) |
| **SADD reference** | §6.8 — Introduce TOTP as Unified Second Factor |

---

## Context

The original design had no second factor at account registration or login — a password alone was sufficient to obtain a JWT. This means a stolen or guessed password grants full account access. All downstream security properties (FIDO, DEK protection) depend on the initial login being legitimate — without a second factor this assumption can be violated.

A second factor is needed at account registration and new device login. Additionally, the FIDO key registration step-up confirmation (ADR-010) requires a second factor mechanism — a decision was needed on which mechanism to use consistently across both contexts.

---

## Decision

Introduce **TOTP (Time-based One-Time Password)** as the unified second factor for:
1. Account registration — TOTP enrollment required before account activation
2. New device login — password + TOTP required to obtain a JWT on an untrusted device
3. FIDO key registration step-up confirmation — TOTP code required alongside JWT when registering a FIDO key (ADR-010)

A single TOTP mechanism covers all three contexts, providing a consistent user experience and a single implementation surface.

**Recovery path:** Backup codes issued at TOTP enrollment for recovery if the TOTP device is lost. Secondary recovery via email + identity verification available as a fallback.

---

## Options Considered

| Option | Description | Security | UX friction | Verdict |
|---|---|---|---|---|
| **SMS OTP** | 6-digit code sent via SMS | 🟠 Lower — vulnerable to SIM swapping | Low | ❌ Rejected — SIM swapping is a known, exploited attack; phone number dependency |
| **Email OTP** | Code sent to registered email | 🟡 Medium — depends on email account security | Low | ❌ Rejected — email account security outside system control; delivery reliability risk |
| **Push notification (Duo, Okta)** | Approve/deny push to registered app | ✅ High | Very low | ❌ Rejected — requires third-party service dependency; vendor lock-in |
| **Hardware key (YubiKey)** | Physical hardware second factor | ✅ Very high | Medium | ❌ Rejected — redundant given FIDO2 already provides hardware-backed auth; requiring two hardware devices is unreasonable UX |
| **TOTP (Google Authenticator, Authy)** | 6-digit rotating code from authenticator app | ✅ High | Low | ✅ Selected |

---

## Rationale

TOTP was selected for the following reasons:

1. **No phone number or external service dependency:** TOTP is app-based — it works offline and does not depend on SMS networks, email delivery, or third-party push notification services. This eliminates availability and reliability risks.

2. **Covers all three contexts with one mechanism:** Using TOTP for account registration, new device login, and FIDO key registration step-up means one implementation, one user-facing flow, and one set of operational procedures. Email OTP and TOTP as separate mechanisms would increase complexity without meaningful additional security.

3. **Industry standard:** TOTP (RFC 6238) is the most widely deployed 2FA mechanism — users are familiar with authenticator apps and the flow is well-understood. Google, GitHub, AWS, and most major platforms use TOTP as their primary 2FA method.

4. **Hardware key redundancy:** A hardware key (YubiKey) was considered but rejected because the system already uses FIDO2 as a hardware-backed factor for DEK decryption and trusted device login. Requiring two separate hardware devices (TOTP hardware token + FIDO2 device) would be an unreasonable UX burden for most users.

**Why the same TOTP covers FIDO registration step-up (not a separate email OTP):** The user has already used their TOTP device to log in — it is present at the time of FIDO registration. A separate email OTP channel would add implementation surface area and introduce email delivery as a dependency on a security-critical operation. Consistency and reliability favour reusing TOTP.

**Recovery path rationale:** Backup codes provide an offline recovery option independent of any network or service. Email + identity verification provides a secondary path for users who lose both their TOTP device and their backup codes. Both paths are documented in the operational runbook.

---

## Consequences

**Enables:**
- Password alone is no longer sufficient to register or log in — stolen passwords do not grant account access
- Consistent second factor across registration, login, and sensitive operations
- No external service dependency for 2FA delivery

**Constrains:**
- Users must install and maintain a TOTP authenticator app
- TOTP codes are time-sensitive — clock skew on the client device can cause failures; a ±30 second tolerance window is applied server-side
- TOTP does not protect against real-time phishing of the code — mitigated by the 30-second window and by FIDO2 replacing TOTP on trusted devices (ADR-015)
- TOTP enrollment must be completed before FIDO registration — enrollment order must be enforced

**Requires:**
- TOTP secret generation and enrollment flow at account registration
- Backup code generation and secure delivery at enrollment
- TOTP validation integrated into login, registration, and FIDO key registration endpoints
- Email + identity verification recovery flow documented and implemented
- Server-side ±30 second clock skew tolerance

---

## References

- [SADD §6.8 — Introduce TOTP as Unified Second Factor](../architecture/security-architecture-design-document.md#68-introduce-totp-as-unified-second-factor)
- [ADR-005 — Use JWT as Sole Authentication Mechanism](ADR-005-jwt-sole-authentication.md)
- [ADR-010 — TOTP Step-Up Confirmation for FIDO Key Registration](ADR-010-totp-step-up-confirmation-fido-registration.md)
- [ADR-015 — Progressive Device Trust](ADR-015-progressive-device-trust.md)
- RFC 6238 — TOTP: https://datatracker.ietf.org/doc/html/rfc6238
- OWASP Multi-Factor Authentication Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html
