// src/stores/useEncryptionStore.js
import * as base64 from "@stablelib/base64";
import { defineStore } from "pinia";
import { ref } from "vue";
import { KMSAPI } from "@/api/kms";
import { DEKAPI } from "@/api/dek";
import { useRSAKEMStore } from "@/stores/useRSAKEMStore";
import { useMLKEMStore } from "@/stores/useMLKEMStore";
import { useProfileStore } from "./useProfileStore";
import { encrypt, decrypt, parseContent } from "@/logic/useEncryption";

export const useEncryptionStore = defineStore("encryption", () => {
  const dekMap = ref(new Map());
  const messageCache = ref([]);
  const current_kem_alg = ref();
  const isRegistered = ref();

  const profile = useProfileStore();
  const mlkem = useMLKEMStore();
  const rsakem = useRSAKEMStore();

  async function init() {
    const regStatus = await KMSAPI.getRegStatus();
    console.log(`[KMS] KMS return status: ${regStatus.Registered}`);
    isRegistered.value = regStatus.Registered === "true";
    console.log(`[KMS] Registration status: ${isRegistered.value}`);
    setCurrentKEM("NONE");
  }

  async function syncDekMap() {
    console.log("[KMS] Starting DEK sync...");
    const data = await DEKAPI.getDeks();
    console.log("[KMS] Raw response from DEKAPI.getNewDeks:", data);
    const encrypted_deks = Array.isArray(data?.content) ? data.content : [];

    await cacheDeks(encrypted_deks);
  }

  async function getNewDeks() {
    try {
      console.log("[KMS] Starting Get New DEKs...");

      const data = await DEKAPI.getNewDeks();
      console.log("[KMS] Raw response from DEKAPI.getNewDeks:", data);

      const encrypted_deks = Array.isArray(data?.content) ? data.content : [];
      await cacheDeks(encrypted_deks);
    } catch (err) {
      console.error("[KMS] Failed to get new DEKs:", err);
    }
  }

  async function cacheDeks(encrypted_deks) {
    try {
      console.log(`[KMS] Retrieved ${encrypted_deks.length} encrypted DEKs.`);

      const keyInfo = await KMSAPI.decryptDek(
        current_kem_alg.value,
        encrypted_deks
      );
      console.log("[KMS] Key info from KMSAPI.decryptDek:", keyInfo);

      const deks = keyInfo.encrypted_response;
      if (current_kem_alg.value !== "NONE") {
        const sharedSecret = await decapKey(keyInfo);
        deks = await decrypt(sharedSecret, keyInfo.encrypted_response);
        console.log("[KMS] Shared secret obtained from decapsulation.");
      }

      const dek_dict = parseContent(deks);
      for (const [id, dek] of Object.entries(dek_dict)) {
        dekMap.value.set(id, dek);
      }

      console.log("[KMS] DEK sync complete.");
    } catch (err) {
      console.error("[KMS] Failed to cache DEKs:", err);
    }
  }

  async function decapKey(keyInfo) {
    const { kem_alg, kem_ct } = keyInfo;
    console.log(`[KMS] Decapsulating key using ${kem_alg}`);
    if (kem_alg === "RSA") {
      return await rsakem.decap(kem_ct);
    } else if (kem_alg === "MLKEM") {
      return await mlkem.decap(kem_ct);
    } else {
      throw new Error("[KMS] Unsupported KEM algorithm: " + kem_alg);
    }
  }

  function setCurrentKEM(alg) {
    current_kem_alg.value = alg;
    console.log(`[KMS] Current KEM algorithm set to: ${alg}`);
  }

  async function tryRegister(password) {
    if (isRegistered.value) {
      console.log("[KMS] Already registered, skipping registration.");
      return;
    }
    console.log("[KMS] Registering user with KMS...");
    const rsa_pk = await rsakem.initRSAPem(password);
    const mlkem_pk = await mlkem.initMLKEMPem(password);
    await KMSAPI.register(mlkem_pk, rsa_pk);
    isRegistered.value = true;
    console.log("[KMS] Registration successful.");
    alert("registered KMS");
  }

  async function decryptMessage(messageList) {
    console.log(`[KMS] Decrypting ${messageList.length} messages...`);

    const plainMessageList = await Promise.all(
      messageList.map(async (message) => {
        const parsed = await parseContent(message.content);
        const dekId = parsed.dekIds?.[profile.currentUserId];

        if (dekId && !dekMap.value.has(dekId)) {
          console.warn(`[KMS] Missing DEK ${dekId}, skipping decryption`);
          return {
            ...message,
            content: "decrypting...",
          };
        }

        const key = dekMap.value.get(dekId);
        if (!key) throw new Error(`DEK ${dekId} not found`);

        const plaintext = await decrypt(key, parsed.ciphertext);
        if (!plaintext) throw new Error("Failed to decrypt message");

        return {
          ...message,
          content: plaintext,
        };
      })
    );

    console.log("[KMS] Message decryption complete.");
    return plainMessageList;
  }

  async function encryptMessage(plaintext, recipientIds) {
    console.log("[KMS] Encrypting message...");
    const dekData = await KMSAPI.genNewDek(recipientIds);

    const message = await encrypt(dekData.dek, plaintext);
    message.dekIds = dekData.dekIds;

    const content = base64.encode(
      new TextEncoder().encode(JSON.stringify(message))
    );

    console.log(`[KMS] Message content: ${content}`);

    const myId = profile.currentUserId;
    const myDekId = dekData.dekIds?.[myId];
    if (myDekId) {
      dekMap.value.set(myDekId, dekData.dek);
      console.log(`[KMS] Stored own DEK with id: ${myDekId}`);
    }

    const encrypted_deks = dekData.encrypted_deks;
    console.log(`[KMS] encrypted_deks: ${encrypted_deks}`);
    console.log("[KMS] Message encryption complete.");

    return {
      content,
      encrypted_deks,
    };
  }

  return {
    isRegistered,
    current_kem_alg,
    init,
    syncDekMap,
    getNewDeks,
    tryRegister,
    setCurrentKEM,
    encryptMessage,
    decryptMessage,
  };
});
