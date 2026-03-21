# ADR-006: Use FIDO2 for DEK Decryption Gating

| Field | Detail |
|---|---|
| **Status** | Accepted |
| **Date** | 2025-05-27 |
| **Superseded by** | [ADR-010](ADR-010-totp-step-up-confirmation-fido-registration.md) |
| **SADD reference** | §4.1 — Original System Flow |

---

## Context

The system stores encrypted DEKs on the DEK service. When a user needs to decrypt a message, they must retrieve their encrypted DEK and ask KMS to unwrap it. A decision was needed on what authentication should gate this DEK decryption operation — the most sensitive operation in the system.

DEK decryption is more sensitive than general API access because it is the operation that, if compromised, allows an attacker to read message content. JWT alone was considered insufficient to gate this operation.

---

## Decision

Require **FIDO2 hardware-backed authentication** in addition to JWT for all DEK decryption requests. The client signs a server-issued challenge with their FIDO2 private key. KMS verifies the signature using the registered FIDO2 public key before performing the unwrap operation.

---

## Options Considered

| Option | Description | Pros | Cons | Verdict |
|---|---|---|---|---|
| **JWT only** | DEK decryption gated by JWT alone | Simple; no additional auth step | A stolen JWT grants access to DEK decryption — undermines the entire E2EE model | ❌ Rejected |
| **JWT + FIDO2** | DEK decryption requires JWT + hardware-backed FIDO2 signature | Hardware-backed; phishing-resistant; private key never leaves device; non-repudiation | Requires FIDO2 device registration; adds one round-trip for challenge/response | ✅ Selected |
| **JWT + TOTP** | DEK decryption requires JWT + TOTP code | Simpler than FIDO2; no hardware requirement | TOTP is software-based — vulnerable to phishing and malware; weaker than FIDO2 for this use case | ❌ Rejected — FIDO2 is more appropriate for hardware-gated key operations |
| **JWT + PIN** | DEK decryption requires JWT + user PIN | Simple | PIN is knowledge-based — can be stolen, guessed, or phished; no hardware backing | ❌ Rejected |

---

## Rationale

FIDO2 was selected because DEK decryption is the highest-sensitivity operation in the system — it is the point where the E2EE guarantee is either upheld or broken. A hardware-backed second factor is the most appropriate gate for this operation because:

- The FIDO2 private key never leaves the device hardware (TPM, Secure Enclave)
- The signature is bound to the origin — phishing attacks on a different domain cannot capture a valid signature
- Physical user presence is required — a remotely compromised account cannot perform DEK decryption without physical access to the device
- Non-repudiation — the signature proves the legitimate device was present

TOTP was rejected for this specific operation because it is software-based and therefore vulnerable to malware and phishing in a way that FIDO2 is not. For the highest-sensitivity operation in the system, hardware-backed authentication is the correct choice.

The FIDO2 registration flow initially required only a JWT (no second factor confirmation). This was later identified as a weakness — see ADR-010.

---

## Consequences

**Enables:**
- Hardware-backed, phishing-resistant gating of the most sensitive operation in the system
- Non-repudiation of DEK decryption events — each decryption is provably tied to a specific device

**Constrains:**
- Users must register a FIDO2-capable device before they can decrypt messages
- DEK decryption requires physical presence of the registered device
- FIDO2 key registration required only a JWT — vulnerable to key substitution if JWT is stolen (addressed in ADR-010)

**Requires:**
- FIDO2 key registration flow per user per device
- Challenge/response round-trip on every DEK decryption request
- FIDO2 public key storage in KMS or Auth server

---

## References

- [SADD §4.1 — Original System Flow](../architecture/security-architecture-design-document.md#41-original-system-flow)
- [SADD §5.4 — FIDO Key Registration Vulnerable to Substitution](../architecture/security-architecture-design-document.md#54-fido-key-registration-vulnerable-to-substitution-medium)
- [ADR-010 — TOTP Step-Up Confirmation for FIDO Key Registration](ADR-010-totp-step-up-confirmation-fido-registration.md)
- FIDO2 / WebAuthn: https://fidoalliance.org/fido2/
