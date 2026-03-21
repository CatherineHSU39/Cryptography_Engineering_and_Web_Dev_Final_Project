# ADR-013: Define CMK, FIDO, and JWT Signing Key Rotation Policy

| Field | Detail |
|---|---|
| **Status** | Draft |
| **Date** | 2026-03-21 |
| **Supersedes** | — |
| **SADD reference** | §6.7 — Define CMK and FIDO Key Rotation Policy |

---

## Context

The original design used per-message DEKs — DEK rotation was inherently in place. However, no rotation policy was defined for CMKs, FIDO keys, or JWT signing keys. Long-lived keys increase the blast radius and exploitation window of any key compromise.

---

## Decision

Define explicit rotation policies for all remaining key types:

| Key type | Rotation trigger | Notes |
|---|---|---|
| **CMK** | Annual, or immediately on suspected compromise | Rotation requires re-wrapping all stored encrypted DEKs with the new public CMK — background operation |
| **FIDO key** | On device change, device loss, or security event | User-initiated; new FIDO registration required on new device |
| **JWT signing key** | Quarterly, or immediately on suspected compromise | Auth server manages rotation; overlap window to allow in-flight JWTs to remain valid |
| **Internal CA certificates** | Every 90 days per service certificate | Automated rotation preferred |

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **No defined rotation policy** | Keys rotated ad-hoc or never | No operational overhead | Long-lived keys increase compromise window and blast radius indefinitely | ❌ Superseded |
| **Annual rotation for all key types** | All keys rotated on a fixed annual schedule | Simple; predictable | Annual window is too long for some key types (JWT signing key, internal certificates) | ❌ Rejected — one-size-fits-all is too coarse |
| **Per-key-type rotation policy** | Each key type has a rotation schedule appropriate to its sensitivity and operational cost | Right-sized for each key type; minimises operational overhead while maintaining security | More complex to track and manage | ✅ Selected |

---

## Rationale

Different key types have different sensitivity levels and different rotation costs — a one-size-fits-all policy is either over-engineered for low-sensitivity keys or under-engineered for high-sensitivity ones.

CMK rotation is the most operationally expensive — it requires re-wrapping all stored encrypted DEKs — so annual rotation is the appropriate balance. All major KMS providers support automatic annual CMK rotation.

FIDO key rotation is event-driven rather than scheduled because FIDO keys are device-bound — they are automatically replaced when a user registers a new device, and manual rotation can be triggered on a security event (lost device, suspected compromise).

JWT signing key rotation is quarterly — frequent enough to limit the window of a signing key compromise, but infrequent enough to be manageable. An overlap window ensures in-flight JWTs remain valid during rotation.

Internal CA certificates rotate every 90 days per service — this aligns with the device token inactivity expiry (ADR-015) and is short enough to limit the window of a certificate compromise.

---

## Consequences

**Enables:**
- Defined rotation schedule for all key types — operational runbook can be written against concrete timelines
- Reduces the blast radius and exploitation window of any key compromise
- CMK rotation with re-wrapping ensures old encrypted DEKs are re-protected with the new key

**Constrains:**
- CMK rotation requires a background re-wrapping operation across all stored encrypted DEKs — must be planned to avoid availability impact
- JWT signing key rotation requires an overlap window — Auth server must support multiple active signing keys simultaneously during rotation
- Automated certificate rotation requires tooling (e.g. cert-manager in Kubernetes, or a custom rotation script)

**Requires:**
- CMK rotation procedure documented in operational runbook
- JWT signing key overlap window implemented in Auth server
- Automated certificate rotation tooling for internal CA certificates
- Monitoring and alerting for upcoming key expiry dates

---

## References

- [SADD §6.7 — Define CMK and FIDO Key Rotation Policy](../architecture/security-architecture-design-document.md#67-define-cmk-and-fido-key-rotation-policy)
- AWS KMS Key Rotation: https://docs.aws.amazon.com/kms/latest/developerguide/rotate-keys.html
- FIDO2 / WebAuthn: https://fidoalliance.org/fido2/
