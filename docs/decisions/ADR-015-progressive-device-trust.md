# ADR-015: Progressive Device Trust — TOTP on New Device, FIDO on Trusted Device

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | — |
| **SADD reference** | §6.9 — Progressive Device Trust |

---

## Context

ADR-014 introduced TOTP as the second factor at login. A decision was needed on whether TOTP should be required on every login or only on new/untrusted devices. Additionally, the system already uses FIDO2 for DEK decryption (ADR-006) — a question arose as to whether FIDO2 could serve as the second factor at login for returning users on trusted devices, replacing TOTP after initial trust is established.

Two concerns drove this decision:

1. **UX:** Requiring TOTP on every login adds friction for returning users. FIDO2 is faster and hardware-backed — a better daily-use factor.
2. **Security:** FIDO2 is a stronger factor than TOTP for ongoing logins — it is hardware-backed, phishing-resistant, and origin-bound. Once a device is trusted, FIDO2 is the preferred second factor.

---

## Decision

Implement a **progressive device trust model**:

- **New device:** Password → TOTP → FIDO key registration (confirmed with TOTP step-up) → device token issued
- **Trusted device (subsequent logins):** Password → FIDO signature → JWT issued; device token refreshed
- **FIDO failure fallback:** Password → TOTP → FIDO re-registration → device token refreshed

**Device token:** A long-lived signed token stored as an HttpOnly, Secure, SameSite=Strict cookie. Expires after **90 days of inactivity** (sliding window reset on each successful login). Revocable server-side.

---

## Options Considered

| Option | Description | Security | UX | Verdict |
|---|---|---|---|---|
| **TOTP on every login** | Always require password + TOTP | High | Friction on every login | ❌ Rejected — unnecessary friction once device is trusted; FIDO2 is a stronger factor anyway |
| **FIDO only after first login** | TOTP on first login; FIDO only thereafter | High | Smooth | ❌ Rejected — FIDO alone cannot bootstrap trust on a brand new device; no FIDO key exists yet |
| **TOTP on new device, FIDO on trusted device** | TOTP establishes device trust; FIDO serves as second factor on subsequent logins | ✅ High | ✅ Smooth after first login | ✅ Selected |
| **Trusted device cookie, no FIDO at login** | Device cookie only; no second factor on return visits | Lower | Smooth | ❌ Rejected — device cookie theft grants login without any second factor |

---

## Rationale

**Why TOTP gates initial device trust rather than FIDO alone:**
FIDO2 private keys are generated and stored on the device hardware — they cannot be exported and are implicitly device-scoped. However, on a brand new device there is no FIDO key registered yet. TOTP, which is independent of the device, provides the second factor needed to safely establish the first trust anchor. Without TOTP, a stolen password would be sufficient to register a FIDO key on any device.

**Why FIDO replaces TOTP rather than supplementing it on trusted devices:**
FIDO2 is a stronger factor than TOTP — hardware-backed, phishing-resistant (origin-bound), and requires physical user presence. Requiring both TOTP and FIDO on every trusted device login adds friction without meaningful additional security. Upgrading from TOTP to FIDO after trust is established improves both security and UX simultaneously. This mirrors the model used by Apple ID and GitHub.

**Why 90 days inactivity expiry:**
90 days is the industry standard used by Google, GitHub, and AWS. It balances security (auto-expiry limits the window of a lost or stolen device being exploited) with UX (active users are not repeatedly asked to re-establish device trust). A sliding window — reset on each successful login — means active users are never interrupted, while dormant devices eventually lose trust automatically.

**Why HttpOnly Secure SameSite=Strict cookie for device token storage:**
localStorage and sessionStorage are accessible by JavaScript — an XSS vulnerability can exfiltrate tokens stored there. An HttpOnly cookie is inaccessible to JavaScript, eliminating this attack vector. Secure ensures the cookie is only sent over HTTPS. SameSite=Strict prevents CSRF-based token submission.

**Why a separate session trust (JWT) and device trust (device token) distinction:**
These are two different trust concepts with different lifetimes and purposes. Session trust (JWT, 15 minutes) authorises individual API calls within a session. Device trust (device token, 90 days) allows the device to skip TOTP on subsequent logins. Conflating them would either make sessions too long-lived or device trust too short-lived.

---

## Consequences

**Enables:**
- TOTP friction only on first login per device — smooth UX for returning users
- FIDO2 as the daily second factor on trusted devices — stronger and faster than TOTP
- Device token revocation gives users control over which devices are trusted — essential response to lost or stolen devices
- 90-day inactivity expiry limits the window of dormant device exploitation

**Constrains:**
- Device token management adds server-side state — device token store required in Auth DB
- 90-day inactivity expiry may surprise users of rarely-used devices
- FIDO failure fallback (TOTP re-registration) must be rate-limited to prevent it becoming an attack vector — an attacker with a stolen password could attempt to repeatedly trigger the fallback to bypass FIDO

**Requires:**
- Device token store in Auth DB
- Device token issuance on successful new device flow completion
- Device token validation on trusted device login
- Device token refresh (reset 90-day timer) on each successful trusted device login
- Device token revocation API — user-facing "manage trusted devices" and admin revocation
- FIDO failure fallback rate limiting
- User-facing device management screen showing all trusted devices with last-seen timestamps

---

## References

- [SADD §6.9 — Progressive Device Trust](../architecture/security-architecture-design-document.md#69-progressive-device-trust--totp-on-new-device-fido-on-trusted-device)
- [ADR-006 — Use FIDO2 for DEK Decryption Gating](ADR-006-fido2-dek-decryption-gating.md)
- [ADR-014 — TOTP as Unified Second Factor](ADR-014-totp-unified-second-factor.md)
- FIDO2 / WebAuthn: https://fidoalliance.org/fido2/
- OWASP Multi-Factor Authentication Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html
