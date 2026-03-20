# Security Architecture Design Document

## End-to-End Encrypted Real-Time Messaging System

| Field       | Detail     |
| ----------- | ---------- |
| **Version** | 2.0        |
| **Status**  | Draft      |
| **Date**    | 2026-03-21 |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Project Purpose & Goals](#2-project-purpose--goals)
3. [System Overview](#3-system-overview)
4. [Original Design](#4-original-design)
5. [Security Analysis of Original Design](#5-security-analysis-of-original-design)
6. [Design Decisions & Solutions](#6-design-decisions--solutions)
7. [New Design](#7-new-design)
8. [Real-Time Messaging Extension](#8-real-time-messaging-extension)
9. [Security Properties of New Design](#9-security-properties-of-new-design)
10. [Migration Path](#10-migration-path)
11. [Glossary](#11-glossary)
12. [References](#12-references)

---

## 1. Executive Summary

This document describes the security architecture of an end-to-end encrypted (E2EE) real-time messaging system. It covers the original design, a structured analysis of its security weaknesses, the decisions made to address each weakness, and the resulting revised architecture.

The system is designed so that plaintext message content and plaintext Data Encryption Keys (DEKs) never exist on any server at any point. All encryption and decryption of message content occurs exclusively on the client device. Authentication is enforced using short-lived JWTs combined with FIDO2 hardware-backed signatures. The infrastructure is containerised within a Docker network with strict internal service isolation.

---

## 2. Project Purpose & Goals

### 2.1 Purpose

To provide a secure messaging platform where:

- Message content is end-to-end encrypted and unreadable by any server
- Each user's encryption keys are protected by hardware-backed FIDO2 authentication
- Multiple recipients can receive the same message without the server holding any plaintext

### 2.2 Security Goals

| Goal                | Description                                                                     |
| ------------------- | ------------------------------------------------------------------------------- |
| **Confidentiality** | No server ever holds plaintext message content or plaintext DEKs                |
| **Authentication**  | All sensitive operations require JWT + FIDO2 verification                       |
| **Integrity**       | Messages cannot be tampered with in transit or at rest                          |
| **Forward secrecy** | Each message uses a unique DEK — compromise of one DEK exposes only one message |
| **Non-repudiation** | FIDO2 signatures prove client identity for sensitive operations                 |

### 2.3 Non-Goals

- Protection against a fully compromised client device
- Anonymity of message metadata (who spoke to whom, when)
- Protection against a malicious user within a conversation

---

## 3. System Overview

### 3.1 Actors

| Actor           | Description                                                                                                              |
| --------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **Web client**  | Browser or desktop app; performs all local encryption and decryption                                                     |
| **Auth server** | Issues and validates JWTs; manages FIDO2 registration                                                                    |
| **Backend**     | Handles business logic, message storage, and WebSocket connections                                                       |
| **KMS**         | Third-party key management service (AWS KMS / GCP KMS / Azure Key Vault); holds CMKs; never exposes private key material |
| **DEK service** | Stores encrypted DEKs (one per recipient per message); never holds plaintext                                             |
| **NGINX**       | Reverse proxy; sole public-facing entry point; handles TLS termination and rate limiting                                 |

### 3.2 Key Concepts

| Term                    | Description                                                                                                                                                                                                                                                                                                                                               |
| ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **DEK**                 | Data Encryption Key — symmetric key used to encrypt a single message; generated client-side                                                                                                                                                                                                                                                               |
| **CMK**                 | Customer Master Key — asymmetric keypair held in KMS; public key encrypts DEKs client-side; private key decrypts DEKs inside KMS only                                                                                                                                                                                                                     |
| **FIDO2**               | Hardware-backed authentication standard; client signs a challenge with a private key that never leaves the device                                                                                                                                                                                                                                         |
| **Envelope encryption** | A two-layer encryption pattern where a DEK encrypts the message (inner envelope) and a CMK encrypts the DEK (outer envelope). In this system, the wrap operation (DEK encryption) is performed client-side using the recipient's public CMK. The unwrap operation (DEK decryption) is performed inside KMS using the private CMK, which never leaves KMS. |

---

## 4. Original Design

### 4.1 Original System Flow

```mermaid
sequenceDiagram
    autonumber
    participant C as web-client
    participant A as auth-server
    participant B as Backend
    participant K as KMS
    participant D as DEK server

    Note over C,K: Registration
    C->>A: (1) Register & sign in
    A-->>C: (2) Return JWT
    C->>K: (3) Register FIDO pubkey (with JWT)
    Note over K: KMS creates symmetric CMK\nfor this user.\nCMK never leaves KMS.
    K-->>C: 200 OK

    Note over C,K: Sign in
    C->>A: (4) Sign in
    A-->>C: (5) Return JWT
    C->>B: (6) Access main APIs (with JWT)

    Note over C,D: Send message
    C->>K: (7) Request DEK + recipient list (with JWT)
    Note over K: KMS generates DEK.\nEncrypts DEK with each\nrecipient's symmetric CMK.\n⚠ Plaintext DEK exists in KMS memory.
    K-->>C: (8) Plaintext DEK + Encrypted DEKs\n(1 per recipient, encrypted with symmetric CMK)
    alt New conversation
        C->>D: (9) Store encrypted DEKs
    end
    C->>C: Encrypt message locally with plaintext DEK
    C->>B: (10) Store encrypted message (with JWT)

    Note over C,K: Retrieve & decrypt message
    C->>B: (11) Retrieve encrypted messages
    C->>D: (12) Retrieve encrypted DEKs
    C->>K: (13) Request decrypt DEKs (with JWT + FIDO)\nSend encrypted DEK (symmetric CMK encrypted)
    Note over K: KMS decrypts DEK using\nrecipient's symmetric CMK.\nRe-encrypts DEK with\nrecipient's FIDO pubkey.\n⚠ Plaintext DEK exists in KMS memory again.
    K-->>C: (14) Return DEK encrypted with FIDO pubkey
    C->>C: Decrypt DEK with FIDO private key locally
    C->>C: Decrypt message with plaintext DEK locally
```

### 4.2 Original Architecture

```mermaid
flowchart TD
    Internet([External traffic]):::external

    subgraph DOCKER["Docker network"]

        subgraph PUBLIC["Public-facing zone"]
            NGINX["NGINX reverse proxy"]:::proxy
        end

        subgraph INTERNAL["Internal services"]
            BACKEND["Backend"]:::service
            AUTH["Auth server"]:::service
            DEK["DEK service"]:::service
            KMS["KMS service"]:::service
        end

        subgraph DBZONE["DB zone"]
            DB[("DB container\nShared — all services")]:::db
        end

    end

    Internet --> NGINX
    NGINX --> BACKEND
    NGINX --> AUTH
    NGINX --> DEK
    NGINX --> KMS

    BACKEND --> DB
    AUTH --> DB
    DEK --> DB
    KMS --> DB

    classDef external fill:#e6f1fb,stroke:#185FA5,color:#0C447C
    classDef proxy fill:#EEEDFE,stroke:#534AB7,color:#3C3489
    classDef service fill:#E1F5EE,stroke:#0F6E56,color:#085041
    classDef db fill:#FAEEDA,stroke:#BA7517,color:#633806
```

### 4.3 DEK Encryption Model

The original design uses **envelope encryption** with **symmetric CMKs**:

- One message is encrypted once with a DEK (inner envelope)
- The DEK is encrypted separately for each recipient using that recipient's symmetric CMK inside KMS (outer envelope)
- KMS performs both the wrap (DEK encryption) and unwrap (DEK decryption) operations — this is the standard symmetric envelope encryption pattern
- This is equivalent to the PGP / S/MIME model and is cryptographically sound
- Storage is efficient: one ciphertext stored, N small encrypted DEK entries stored

> **Note on terminology:** The original design follows standard symmetric envelope encryption as defined by AWS KMS / GCP KMS / Azure Key Vault, where KMS both wraps and unwraps the DEK using a symmetric CMK. The new design (section 7) uses a **client-side asymmetric envelope encryption** variant — the wrap is performed client-side using the recipient's public CMK, and KMS only performs the unwrap using the private CMK. Both are forms of envelope encryption; they differ in where the wrap operation occurs and whether the CMK is symmetric or asymmetric.

---

## 5. Security Analysis of Original Design

### 5.1 Plaintext DEK Exposure in KMS (Critical)

| Field        | Detail                    |
| ------------ | ------------------------- |
| **Severity** | Critical                  |
| **Location** | Steps 7–8 and steps 13–14 |

**Description:** The symmetric CMK model requires KMS to generate the DEK and encrypt it for each recipient (steps 7–8), and later to decrypt the DEK and re-encrypt it with the FIDO pubkey (steps 13–14). In both operations, the plaintext DEK exists inside KMS memory.

**Impact:** If KMS is compromised — via memory dump, side-channel attack, or insider threat — every DEK ever processed is exposed, and with it every message ever sent.

**Root cause:** Symmetric CMK requires the same party (KMS) to both encrypt and decrypt. There is no way to let clients encrypt DEKs locally without exposing the symmetric key itself.

---

### 5.2 KMS Directly Reachable via NGINX (Critical)

| Field        | Detail                       |
| ------------ | ---------------------------- |
| **Severity** | Critical                     |
| **Location** | Architecture — NGINX routing |

**Description:** NGINX routes external traffic directly to KMS. KMS is the most sensitive service in the system — it holds all CMKs — yet it is reachable directly from the internet through the proxy.

**Impact:** Increases KMS attack surface. Any misconfiguration of NGINX routing rules could expose KMS directly. DDoS and brute-force attacks can target KMS directly.

**Root cause:** All services including KMS were placed behind NGINX without distinguishing between public-facing and internal-only services.

---

### 5.3 Shared Database Container (Critical)

| Field        | Detail                      |
| ------------ | --------------------------- |
| **Severity** | Critical                    |
| **Location** | Architecture — DB container |

**Description:** All services (Auth, Backend, DEK, KMS) share a single database container with no stated isolation between schemas or users.

**Impact:** A SQL injection or application-layer breach in any single service can expose data belonging to all other services. Auth tokens, encrypted messages, encrypted DEKs, and key material are all co-located.

**Root cause:** Single shared database chosen for simplicity without per-service isolation.

---

### 5.4 FIDO Key Registration Vulnerable to Substitution (Medium)

| Field        | Detail                            |
| ------------ | --------------------------------- |
| **Severity** | Medium                            |
| **Location** | Step 3 — FIDO pubkey registration |

**Description:** FIDO public key registration requires only a JWT. If a JWT is compromised before FIDO registration, an attacker can register their own FIDO key and impersonate the user for all subsequent DEK decryption operations.

**Impact:** Full account takeover for message decryption. The attacker can decrypt all future messages sent to this user.

**Root cause:** Single-factor confirmation for a high-privilege registration operation.

---

### 5.5 JWT as Sole Auth for Most Operations (Medium)

| Field        | Detail                 |
| ------------ | ---------------------- |
| **Severity** | Medium                 |
| **Location** | Steps 6, 7, 10, 11, 12 |

**Description:** JWT is the only authentication mechanism for the majority of operations. JWTs are stateless — once issued they cannot be revoked before expiry. No token binding to device or session is shown.

**Impact:** A stolen JWT grants wide access to message storage, retrieval, and DEK operations until expiry. Logout does not invalidate the token.

**Root cause:** Stateless JWT used without short expiry enforcement, rotation policy, or token binding.

---

### 5.6 No Service-to-Service Authentication (Medium)

| Field        | Detail                  |
| ------------ | ----------------------- |
| **Severity** | Medium                  |
| **Location** | Internal Docker network |

**Description:** Inside the Docker network, services can communicate freely using Docker internal DNS. No mutual authentication between services is shown.

**Impact:** A compromised internal service can impersonate any other service. Backend could call KMS or DEK service without any credential verification.

**Root cause:** Internal network trusted implicitly — no zero-trust posture inside the Docker network.

---

### 5.7 No Key Rotation Policy (Medium)

| Field        | Detail      |
| ------------ | ----------- |
| **Severity** | Medium      |
| **Location** | System-wide |

**Description:** No rotation policy is defined for DEKs, CMKs, FIDO keys, or JWT signing keys.

**Impact:** Long-lived keys increase the blast radius of any key compromise. A compromised CMK could expose historical messages if DEKs are not rotated per-message.

**Root cause:** Key lifecycle management not addressed in the original design.

---

## 6. Design Decisions & Solutions

### 6.1 Switch from Symmetric CMK to Asymmetric CMK

**Problem addressed:** §5.1 — Plaintext DEK exposure in KMS

**Options considered:**

| Option                                       | Description                                                                  | Verdict                                           |
| -------------------------------------------- | ---------------------------------------------------------------------------- | ------------------------------------------------- |
| Keep symmetric CMK                           | No change — KMS continues to generate and encrypt DEKs                       | ❌ Rejected — plaintext DEK always in KMS memory  |
| Store encrypted messages per recipient       | One ciphertext copy per recipient instead of shared DEK                      | ❌ Rejected — O(N) storage, impractical for media |
| Asymmetric CMK — client encrypts DEK locally | Client fetches recipient public CMK, encrypts DEK locally; KMS only decrypts | ✅ Selected                                       |

**Decision:** Switch to asymmetric CMK (RSA or ECC). The public CMK is retrievable and cacheable by clients. Clients encrypt DEKs locally using the recipient's public CMK. KMS only performs decryption using the private CMK, which never leaves KMS.

**Trade-offs accepted:**

- Requires asymmetric CMK support from KMS provider (all major providers support this)
- Requires a public CMK cache inside the Docker network to avoid per-message KMS calls for key fetching
- Public CMKs must be invalidated and refreshed on CMK rotation

---

### 6.2 Isolate KMS from NGINX Routing

**Problem addressed:** §5.2 — KMS directly reachable via NGINX

**Decision:** Remove KMS from NGINX routing table entirely. KMS (third-party) is accessed directly by the client for decryption operations over HTTPS, and by the Backend for session setup. NGINX never proxies traffic to KMS.

**Trade-offs accepted:** Clients make direct HTTPS calls to the third-party KMS endpoint for decryption — this is acceptable as third-party KMS providers expose authenticated public HTTPS APIs designed for this purpose.

---

### 6.3 Separate Database Per Service

**Problem addressed:** §5.3 — Shared database container

**Decision:** Replace the single shared DB container with four isolated databases, one per service. Each service holds credentials only for its own database. Cross-service data access at the database layer is architecturally impossible.

**Trade-offs accepted:** Slightly higher infrastructure overhead. Migrations and backups must be managed per database.

---

### 6.4 Require OTP Confirmation for FIDO Registration

**Problem addressed:** §5.4 — FIDO key substitution attack

**Decision:** FIDO pubkey registration requires JWT plus a secondary out-of-band confirmation (email OTP or SMS OTP). This ensures that even a stolen JWT cannot be used to register a malicious FIDO key.

**Trade-offs accepted:** Slightly more friction at registration. Acceptable given that registration is a one-time operation.

---

### 6.5 Short-Lived JWT with Rotation

**Problem addressed:** §5.5 — JWT as sole auth

**Decision:** JWTs are issued with short expiry (15 minutes). A refresh token rotation mechanism is implemented — refresh tokens are single-use and invalidated on use. For WebSocket sessions, JWT refresh is performed in-band over the WebSocket connection before expiry.

**Trade-offs accepted:** Slightly more complex token management. Acceptable given the security benefit of near-real-time revocation capability.

---

### 6.6 mTLS Between Internal Services

**Problem addressed:** §5.6 — No service-to-service authentication

**Decision:** All service-to-service communication inside the Docker network uses mutual TLS (mTLS). Each service holds a certificate issued by an internal CA. Services reject connections from uncertified peers.

**Trade-offs accepted:** Certificate management overhead. Internal CA must be maintained and rotated.

---

### 6.7 Per-Message DEK

**Problem addressed:** §5.7 — No key rotation policy

**Decision:** Each message uses a freshly generated DEK. This provides forward secrecy at the message level — compromise of one DEK exposes only one message. CMK rotation policy is defined per-provider (e.g. annual rotation in AWS KMS). FIDO key rotation is triggered on device change or security event.

**Trade-offs accepted:** Slightly higher DEK storage volume — one encrypted DEK entry per recipient per message. Acceptable given that encrypted DEK entries are small (a few hundred bytes).

---

## 7. New Design

### 7.1 Revised System Flow

```mermaid
sequenceDiagram
    autonumber
    participant C as web-client
    participant A as auth-server
    participant B as Backend
    participant D as DEK service
    participant K as KMS (third-party)

    Note over C,K: Registration (one time per user)
    C->>A: (1) Register & sign in
    A-->>C: (2) Return JWT
    C->>K: (3) Request asymmetric CMK creation\n(JWT + OTP confirmation)
    Note over K: KMS generates asymmetric CMK keypair.\nPrivate CMK never leaves KMS.
    K-->>C: (4) Return public CMK
    C->>B: (5) Store public CMK (with JWT)
    B-->>C: 200 OK

    Note over C,K: Sign in & session setup
    C->>A: (6) Sign in
    A-->>C: (7) Return JWT (short-lived)
    C->>B: (8) WebSocket upgrade (JWT in header)
    B->>K: (9) FIDO challenge request
    K-->>C: (10) FIDO challenge
    C-->>K: (11) Sign challenge (FIDO private key)
    K-->>C: (12) Return encrypted session DEK
    Note over C: Decrypt session DEK locally\nwith FIDO private key.\nPlaintext DEK in client memory only.

    Note over C,K: Send message
    C->>B: (13) Fetch recipient public CMK (JWT)
    B-->>C: (14) Return cached public CMK of recipient
    C->>C: (15) Generate new DEK for this message
    C->>C: (16) Encrypt message with DEK (local)
    C->>C: (17) Encrypt DEK with own public CMK (local)
    C->>C: (18) Encrypt DEK with recipient public CMK (local)
    Note over C: All encryption is local.\nKMS not involved in send.\nPlaintext DEK never leaves client.
    C->>B: (19) Send encrypted message +\nencrypted DEKs per recipient (WSS + JWT)
    B->>B: (20) Store encrypted message in DB
    B->>D: (21) Store encrypted DEKs (one per recipient)
    B-->>C: Push encrypted message +\nrecipient encrypted DEK (WSS)

    Note over C,K: Retrieve & decrypt message
    C->>K: (22) Decrypt DEK request (JWT + FIDO signed)
    Note over K: KMS decrypts using recipient\nprivate CMK internally.\nPrivate CMK never leaves KMS.
    K-->>C: (23) Return plaintext DEK
    C->>C: (24) Decrypt message locally with DEK

    Note over C,B: Offline catch-up
    C->>B: (25) Connect, request missed messages (JWT)
    B->>D: Retrieve encrypted DEKs
    D-->>B: Return encrypted DEKs
    B-->>C: Return encrypted messages + encrypted DEKs
    C->>K: Decrypt DEK (JWT + FIDO signed)
    K-->>C: Return plaintext DEK
    C->>C: Decrypt messages locally

    Note over C,B: JWT refresh mid-session
    C->>B: Send refresh token (WSS)
    B->>B: Validate, invalidate old refresh token
    B-->>C: Issue new short-lived JWT
```

### 7.2 Revised Architecture

```mermaid
flowchart TD
    Internet([External traffic]):::external
    THIRDKMS(["KMS — third-party\nAWS / GCP / Azure\nPrivate CMK never leaves"]):::thirdparty

    subgraph DOCKER["Docker network"]

        subgraph PUBLIC["Public-facing zone"]
            NGINX["NGINX\nHTTPS + WSS · rate limiting · TLS termination"]:::proxy
        end

        subgraph INTERNAL["Internal service zone"]
            BACKEND["Backend\nREST + WebSocket handler"]:::service
            AUTH["Auth server\nJWT · FIDO · token refresh"]:::service
            DEK["DEK service\nEncrypted DEKs only\n1 per recipient per message"]:::service
            PRESENCE["Presence service\nOnline status · WS routing"]:::service
            BROKER["Message broker\nRedis Pub/Sub or Kafka\nRoutes encrypted blobs only"]:::broker
            CMKCACHE["Public CMK cache\nRedis · public keys only\nNo secrets stored"]:::cache
        end

        subgraph DBZONE["Isolated DB zone"]
            AUTHDB[("Auth DB\nUsers · tokens")]:::db
            MSGDB[("Messages DB\nEncrypted messages")]:::db
            DEKDB[("DEK DB\nEncrypted DEKs\n1 per recipient per message")]:::db
            PRESENCEDB[("Presence DB\nRedis")]:::db
        end

    end

    Internet -->|HTTPS + WSS| NGINX
    NGINX -->|WSS + REST| BACKEND
    NGINX -->|HTTPS| AUTH
    NGINX -.->|"✕ blocked"| DEK
    NGINX -.->|"✕ blocked"| BROKER
    NGINX -.->|"✕ blocked"| PRESENCE
    NGINX -.->|"✕ blocked"| CMKCACHE

    BACKEND <-->|mTLS| AUTH
    BACKEND <-->|mTLS| DEK
    BACKEND <-->|mTLS| PRESENCE
    BACKEND <-->|mTLS| BROKER
    BACKEND <-->|mTLS| CMKCACHE

    CLIENT -->|"Fetch public CMK (cached)"| CMKCACHE
    CLIENT -->|"Decrypt DEK request\nJWT + FIDO signed · HTTPS"| THIRDKMS
    THIRDKMS -->|"Plaintext DEK (to client only)"| CLIENT
    BACKEND -->|"CMK creation · FIDO challenge\nHTTPS mTLS"| THIRDKMS

    AUTH --> AUTHDB
    BACKEND --> MSGDB
    DEK --> DEKDB
    PRESENCE --> PRESENCEDB

    classDef external fill:#e6f1fb,stroke:#185FA5,color:#0C447C
    classDef proxy fill:#EEEDFE,stroke:#534AB7,color:#3C3489
    classDef service fill:#E1F5EE,stroke:#0F6E56,color:#085041
    classDef broker fill:#d8d5f5,stroke:#3C3489,color:#26215C
    classDef cache fill:#FAECE7,stroke:#993C1D,color:#712B13
    classDef db fill:#FAEEDA,stroke:#BA7517,color:#633806
    classDef thirdparty fill:#E1F5EE,stroke:#0F6E56,color:#085041
```

### 7.3 How Each Weakness Is Addressed

| Weakness                 | Original                                 | New Design                                                              |
| ------------------------ | ---------------------------------------- | ----------------------------------------------------------------------- |
| Plaintext DEK in KMS     | ❌ KMS generates and holds plaintext DEK | ✅ Client encrypts DEK locally; KMS only decrypts                       |
| KMS reachable via NGINX  | ❌ NGINX routes to KMS                   | ✅ KMS removed from NGINX; third-party KMS accessed directly by client  |
| Shared database          | ❌ Single DB for all services            | ✅ One isolated DB per service                                          |
| FIDO key substitution    | ❌ JWT only for registration             | ✅ JWT + OTP confirmation required                                      |
| JWT revocation           | ❌ Long-lived, no rotation               | ✅ Short-lived + single-use refresh token rotation                      |
| No internal service auth | ❌ Open internal network                 | ✅ mTLS between all internal services                                   |
| No key rotation          | ❌ Not defined                           | ✅ Per-message DEK; annual CMK rotation; FIDO rotation on device change |

---

## 8. Real-Time Messaging Extension

### 8.1 New Components

| Component             | Purpose                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------ |
| **WebSocket handler** | Persistent client connections managed inside Backend                                                   |
| **Message broker**    | Redis Pub/Sub or Kafka; routes encrypted message blobs between WebSocket nodes; internal only          |
| **Presence service**  | Tracks online status and WebSocket connection routing                                                  |
| **Public CMK cache**  | Redis cache of recipient public CMKs; eliminates per-message KMS calls for key fetching; internal only |

### 8.2 Real-Time Flow

```mermaid
sequenceDiagram
    autonumber
    participant C1 as Sender client
    participant C2 as Recipient client
    participant N as NGINX WSS
    participant B as Backend
    participant BR as Message broker
    participant D as DEK service
    participant K as KMS (third-party)

    Note over C1,K: Session setup (once per login)
    C1->>N: WebSocket upgrade (JWT in header)
    N->>B: Validate JWT, open WS connection
    B->>K: FIDO challenge request
    K-->>C1: FIDO challenge
    C1-->>K: Sign challenge (FIDO private key)
    K-->>C1: Return encrypted session DEK
    Note over C1: Decrypt session DEK locally\nwith FIDO key.\nPlaintext DEK in client memory only.

    Note over C1,C2: Sending a real-time message
    C1->>B: Fetch recipient public CMK (JWT)
    B-->>C1: Return cached public CMK of C2
    C1->>C1: Generate new DEK for this message
    C1->>C1: Encrypt message with DEK (local)
    C1->>C1: Encrypt DEK with own public CMK (local)
    C1->>C1: Encrypt DEK with C2 public CMK (local)
    Note over C1: All encryption is local.\nKMS not involved in send.\nPlaintext DEK never leaves client.
    C1->>B: Send encrypted message +\nencrypted DEKs per recipient (WSS + JWT)
    B->>B: Store encrypted message in DB
    B->>D: Store encrypted DEKs (one per recipient)
    B->>BR: Publish event (encrypted message + DEKs)
    BR->>B: Route to recipient WS connection
    B->>C2: Push encrypted message +\nC2 encrypted DEK (WSS)
    C2->>K: Decrypt DEK request (JWT + FIDO signed)
    K-->>C2: Return plaintext DEK
    C2->>C2: Decrypt message locally

    Note over C2,B: Offline recipient catch-up
    C2->>B: Connect, request missed messages (JWT)
    B->>D: Retrieve C2 encrypted DEKs
    D-->>B: Return encrypted DEKs
    B-->>C2: Return encrypted messages + encrypted DEKs
    C2->>K: Decrypt DEK (JWT + FIDO signed)
    K-->>C2: Return plaintext DEK
    C2->>C2: Decrypt messages locally

    Note over C1,B: JWT refresh mid-session
    C1->>B: Send refresh token (WSS)
    B->>B: Validate, invalidate old refresh token
    B-->>C1: Issue new short-lived JWT
```

### 8.3 Offline Recipients

Because the sender encrypts the DEK locally using the recipient's public CMK (which is cached and always available), there is no special handling required for offline recipients. The sender can always encrypt for any recipient regardless of their online status. When the offline recipient reconnects, they retrieve their encrypted DEKs and decrypt them locally — the same flow as real-time delivery.

This is a direct benefit of the asymmetric CMK model and eliminates the need for a pre-key bundle system.

### 8.4 Additional Security Considerations for Real-Time

| Concern                          | Mitigation                                                                  |
| -------------------------------- | --------------------------------------------------------------------------- |
| WebSocket JWT expiry mid-session | In-band JWT refresh over WSS before expiry                                  |
| Long-lived WebSocket session     | Periodic FIDO re-challenge every N hours                                    |
| Message broker as attack surface | Broker is internal only; blocked from NGINX; passes encrypted blobs only    |
| Connection flooding              | Rate limit WebSocket upgrades at NGINX; max concurrent connections per user |
| Dead connections                 | Heartbeat / ping-pong mechanism to detect and close stale connections       |

---

## 9. Security Properties of New Design

### 9.1 Guarantees

| Property                        | Guarantee                                                                      |
| ------------------------------- | ------------------------------------------------------------------------------ |
| **Plaintext DEK isolation**     | Plaintext DEK exists only in client memory; never on any server                |
| **Plaintext message isolation** | Plaintext message exists only in client memory; never on any server            |
| **Private CMK isolation**       | Private CMK exists only inside third-party KMS; never transmitted              |
| **Forward secrecy**             | Each message uses a unique DEK; compromise of one DEK exposes one message only |
| **Authentication strength**     | All DEK decryption requires JWT + FIDO2 hardware signature                     |
| **Internal service isolation**  | mTLS enforced between all services; no implicit trust inside Docker network    |
| **Database isolation**          | Per-service databases; cross-service data access architecturally impossible    |

### 9.2 Remaining Risks

| Risk                                  | Severity | Mitigation                                                                                          |
| ------------------------------------- | -------- | --------------------------------------------------------------------------------------------------- |
| Compromised client device             | High     | Out of scope — device security is the user's responsibility                                         |
| Third-party KMS provider breach       | High     | Asymmetric CMK model limits exposure to decryption operations only; no plaintext DEKs stored in KMS |
| JWT theft via XSS                     | Medium   | Short-lived JWT; Content Security Policy; HttpOnly cookies for token storage                        |
| Metadata exposure (who messaged whom) | Low      | Accepted — out of scope for this design                                                             |
| Public CMK cache poisoning            | Medium   | CMK cache is internal only; cache entries signed and validated against KMS on TTL refresh           |

### 9.3 Security Assumptions

- The client device and browser are not compromised
- The third-party KMS provider operates honestly and securely
- TLS certificates are valid and certificate pinning is enforced where applicable
- FIDO2 private keys are stored in hardware-backed secure enclaves (TPM, Secure Enclave)

---

## 10. Migration Path

### 10.1 Phase 1 — Infrastructure Isolation

1. Split shared DB into four isolated databases with per-service credentials
2. Remove KMS and DEK service from NGINX routing table
3. Implement mTLS between all internal services
4. Deploy public CMK cache (Redis) as internal-only service

### 10.2 Phase 2 — CMK Migration

1. Generate new asymmetric CMK keypair per user in third-party KMS
2. Re-encrypt existing DEKs: decrypt with old symmetric CMK, re-encrypt with new asymmetric public CMK
3. Distribute new public CMKs to public CMK cache
4. Deprecate symmetric CMK operations in KMS

### 10.3 Phase 3 — Auth Hardening

1. Enforce short-lived JWT expiry and refresh token rotation
2. Add OTP confirmation step to FIDO key registration flow
3. Implement in-band JWT refresh for WebSocket sessions

### 10.4 Phase 4 — Real-Time Extension

1. Add WebSocket support to Backend and NGINX
2. Deploy message broker (Redis Pub/Sub or Kafka) as internal-only service
3. Deploy presence service
4. Implement periodic FIDO re-challenge for long-lived WebSocket sessions

---

## 11. Glossary

| Term                    | Definition                                                                                                                                                                                                                                                  |
| ----------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **ADR**                 | Architecture Decision Record — a document capturing a single architectural decision                                                                                                                                                                         |
| **CMK**                 | Customer Master Key — a keypair managed by KMS used to protect DEKs                                                                                                                                                                                         |
| **DEK**                 | Data Encryption Key — a symmetric key used to encrypt a single message                                                                                                                                                                                      |
| **E2EE**                | End-to-End Encryption — encryption where only communicating clients can read messages                                                                                                                                                                       |
| **Envelope encryption** | A two-layer pattern where a DEK encrypts data and a CMK encrypts the DEK. The wrap (DEK encryption) and unwrap (DEK decryption) operations may be performed by KMS (symmetric) or by the client using an asymmetric CMK public key (this system's approach) |
| **FIDO2**               | Fast IDentity Online 2 — a standard for hardware-backed passwordless authentication                                                                                                                                                                         |
| **JWT**                 | JSON Web Token — a signed token used for stateless authentication                                                                                                                                                                                           |
| **KMS**                 | Key Management Service — a service that creates, stores, and manages cryptographic keys                                                                                                                                                                     |
| **mTLS**                | Mutual TLS — TLS where both client and server authenticate with certificates                                                                                                                                                                                |
| **OTP**                 | One-Time Password — a short-lived code used for out-of-band confirmation                                                                                                                                                                                    |
| **Unwrap**              | The KMS operation that decrypts an encrypted DEK using the private CMK; the CMK never leaves KMS                                                                                                                                                            |
| **WebAuthn**            | Web Authentication API — the browser API implementing FIDO2                                                                                                                                                                                                 |
| **Wrap**                | The operation that encrypts a plaintext DEK using a CMK; performed client-side in this system using the recipient's public CMK                                                                                                                              |
| **WSS**                 | WebSocket Secure — WebSocket over TLS                                                                                                                                                                                                                       |

---

## 12. References

| Reference                                  | Description                                                                              |
| ------------------------------------------ | ---------------------------------------------------------------------------------------- |
| FIDO2 / WebAuthn                           | https://fidoalliance.org/fido2/                                                          |
| AWS KMS asymmetric keys                    | https://docs.aws.amazon.com/kms/latest/developerguide/symmetric-asymmetric.html          |
| Google Cloud KMS                           | https://cloud.google.com/kms/docs                                                        |
| Azure Key Vault                            | https://learn.microsoft.com/en-us/azure/key-vault/                                       |
| RFC 7519 — JWT                             | https://datatracker.ietf.org/doc/html/rfc7519                                            |
| Signal Protocol                            | https://signal.org/docs/                                                                 |
| OWASP Transport Layer Security Cheat Sheet | https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html |
