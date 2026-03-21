# ADR-010: TOTP Step-Up Confirmation for FIDO Key Registration

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | [ADR-006](ADR-006-fido2-dek-decryption-gating.md) |
| **SADD reference** | §6.4 — Require TOTP Step-Up Confirmation for FIDO Key Registration |

---

## Context

ADR-006 established FIDO2 as the second factor for DEK decryption. However, the FIDO key registration step — where the user's FIDO public key is registered with the system — was protected only by a JWT. If a JWT is stolen before FIDO registration, an attacker can register their own FIDO key and permanently impersonate the user for all DEK decryption operations. This is a key substitution attack.

A step-up confirmation is needed at FIDO key registration to ensure the operation was intentionally initiated by the legitimate user — not just any holder of a valid JWT.

---

## Decision

Require a **TOTP code as step-up confirmation** for FIDO key registration. The user must submit a valid TOTP code alongside the JWT when registering a FIDO public key. This confirms that the person registering the key has physical possession of the TOTP device — a stolen JWT alone is insufficient.

This step-up is an **authorization confirmation**, not authentication — its purpose is to confirm that a specific sensitive operation was deliberately initiated by the legitimate user.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **JWT only** | No change — FIDO registration requires only JWT | Simple | Stolen JWT enables key substitution attack | ❌ Superseded |
| **Email OTP confirmation** | Send a one-time code to the registered email address | Out-of-band confirmation; independent of session | Depends on email delivery reliability; adds a separate OTP mechanism to the system; email account could be compromised | ❌ Rejected |
| **SMS OTP confirmation** | Send a one-time code via SMS | Familiar to users | Vulnerable to SIM swapping; adds a separate OTP mechanism; phone number dependency | ❌ Rejected |
| **TOTP step-up** | Require the existing TOTP code as confirmation | Reuses the existing TOTP mechanism — no new OTP channel needed; user already has TOTP device available at registration time; consistent UX | User must have TOTP device available at FIDO registration time | ✅ Selected |

---

## Rationale

TOTP was selected over email OTP and SMS OTP for three reasons:

1. **Consistency:** The system already requires TOTP at login (ADR-014). Reusing TOTP for FIDO key registration step-up means one fewer OTP mechanism in the system, reducing implementation surface area and user confusion.

2. **Availability:** The user has already used their TOTP device to log in — it is present and available at the time of FIDO registration. A separate email or SMS OTP would require switching channels.

3. **Reliability:** TOTP is local and does not depend on email delivery or SMS network availability. Email OTP delivery failures would block FIDO registration — an unacceptable UX and availability risk for a security-critical operation.

This mirrors the pattern used by Google and GitHub, which require your existing 2FA code to confirm adding a new security key. The TOTP code here is deliberately framed as a step-up confirmation, not authentication — it authorizes a specific sensitive action rather than verifying identity.

---

## Consequences

**Enables:**
- FIDO key substitution attack is prevented — a stolen JWT alone cannot register a malicious FIDO key
- Consistent UX — same TOTP device used for login and for FIDO registration confirmation
- No additional OTP channel or infrastructure required

**Constrains:**
- User must have their TOTP device available when registering a FIDO key
- TOTP enrollment (ADR-014) must be completed before FIDO registration is possible

**Requires:**
- TOTP verification integrated into the FIDO key registration endpoint
- Auth server to validate TOTP code and JWT together before accepting FIDO key registration
- Clear user-facing messaging distinguishing "TOTP for login" from "TOTP to confirm key registration"

---

## References

- [SADD §6.4 — Require TOTP Step-Up Confirmation for FIDO Key Registration](../architecture/security-architecture-design-document.md#64-require-totp-step-up-confirmation-for-fido-key-registration)
- [ADR-006 — Use FIDO2 for DEK Decryption Gating](ADR-006-fido2-dek-decryption-gating.md)
- [ADR-014 — TOTP as Unified Second Factor](ADR-014-totp-unified-second-factor.md)
- FIDO2 / WebAuthn: https://fidoalliance.org/fido2/
