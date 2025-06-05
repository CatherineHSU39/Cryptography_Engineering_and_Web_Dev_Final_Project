// src/stores/useMLKEMStore.js
import { defineStore } from "pinia";
import { ref } from "vue";
import {
  deriveAesKey,
  saveToIndexedDB,
  loadFromIndexedDB,
  toPem,
} from "@/logic/useKey";
import * as base64 from "@stablelib/base64";
import { MlKem512 } from "mlkem";

const KEY_ID_PREFIX = "mlkem-key";

export const useMLKEMStore = defineStore("mlkem", () => {
  const aesKey = ref(null);
  const sk = ref(null);
  const mlkem = new MlKem512();

  function getKeyId(userId) {
    if (!userId) throw new Error("[MLKEM] Missing userId");
    return `${KEY_ID_PREFIX}-${userId}`;
  }

  async function initMLKEMPem(userId, password) {
    console.log("[MLKEM] Initializing MLKEM key pair...");
    const seed = crypto.getRandomValues(new Uint8Array(64));
    const [publicKey, privateKey] = await mlkem.deriveKeyPair(seed);
    console.log("[MLKEM] Key pair derived");

    const aes = await deriveAesKey(password);
    aesKey.value = aes;
    console.log("[MLKEM] AES key derived from password");

    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encryptedPrivKey = await crypto.subtle.encrypt(
      { name: "AES-GCM", iv },
      aes,
      privateKey
    );
    console.log("[MLKEM] Private key encrypted");

    await saveToIndexedDB({
      id: getKeyId(userId),
      pubKey: publicKey,
      privKey: encryptedPrivKey,
      iv,
    });
    console.log("[MLKEM] Key pair saved to IndexedDB");

    const b64 = base64.encode(publicKey);
    return b64;
  }

  async function loadKeyPair(userId, password) {
    console.log("[MLKEM] Loading key pair from IndexedDB...");
    const aes = await deriveAesKey(password);
    aesKey.value = aes;

    const data = await loadFromIndexedDB(getKeyId(userId));
    if (!data) {
      console.warn("[MLKEM] No key data found in IndexedDB");
      return null;
    }

    const decryptedPrivKey = await crypto.subtle.decrypt(
      { name: "AES-GCM", iv: data.iv },
      aes,
      data.privKey
    );

    sk.value = new Uint8Array(decryptedPrivKey);
    console.log("[MLKEM] Key pair loaded into store");
  }

  async function decap(ciphertextBase64) {
    if (!sk.value)
      throw new Error("[MLKEM] Private key not loaded for decapsulation");

    console.log("[MLKEM] Decapsulating shared secret...");
    const sharedSecret = await mlkem.decap(
      base64.decode(ciphertextBase64),
      sk.value
    );
    return sharedSecret;
  }

  async function changePassword(userId, oldPassword, newPassword) {
    console.log("[MLKEM] Changing password...");
    const oldAesKey = await deriveAesKey(oldPassword);
    const data = await loadFromIndexedDB(getKeyId(userId));
    if (!data) throw new Error("[MLKEM] No key found in IndexedDB");

    let decryptedPrivateKey;
    try {
      decryptedPrivateKey = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv: data.iv },
        oldAesKey,
        data.privKey
      );
    } catch (e) {
      console.error("[MLKEM] Failed to decrypt with old password");
      throw new Error("Incorrect current password");
    }

    const newAesKey = await deriveAesKey(newPassword);
    const newIv = crypto.getRandomValues(new Uint8Array(12));
    const newEncrypted = await crypto.subtle.encrypt(
      { name: "AES-GCM", iv: newIv },
      newAesKey,
      decryptedPrivateKey
    );

    await saveToIndexedDB({
      id: getKeyId(userId),
      pubKey: data.pubKey,
      privKey: newEncrypted,
      iv: newIv,
    });

    aesKey.value = newAesKey;
    console.log("[MLKEM] Encrypted with new password and saved");
  }

  return {
    initMLKEMPem,
    loadKeyPair,
    changePassword,
    decap,
    sk,
  };
});
