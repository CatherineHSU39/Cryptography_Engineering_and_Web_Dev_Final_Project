import { defineStore } from "pinia";
import { ref } from "vue";
import {
  deriveAesKey,
  saveToIndexedDB,
  loadFromIndexedDB,
  toPem,
} from "@/logic/useKey";
import * as base64 from "@stablelib/base64";
import { useProfileStore } from "@/stores/useProfileStore";

const KEY_TTL_MS = 10 * 60 * 1000;

export const useRSAKEMStore = defineStore("rsakem", () => {
  const profile = useProfileStore();
  const aesKey = ref(null);
  const sk = ref(null);
  const pk = ref(null);
  let expireTimer = null;

  function getKeyId() {
    if (!profile.currentUserId) throw new Error("User not initialized");
    const keyId = `rsa-key-${profile.currentUserId}`;
    console.log(`[RSAKEM] Computed key ID: ${keyId}`);
    return keyId;
  }

  function setExpiration() {
    clearTimeout(expireTimer);
    expireTimer = setTimeout(() => {
      console.log("[RSAKEM] Expiring key pair from memory...");
      pk.value = null;
      sk.value = null;
    }, KEY_TTL_MS);
  }

  async function initRSAPem(password) {
    console.log("[RSAKEM] Generating new RSA key pair...");
    const keyPair = await crypto.subtle.generateKey(
      {
        name: "RSA-OAEP",
        modulusLength: 2048,
        publicExponent: new Uint8Array([1, 0, 1]),
        hash: "SHA-256",
      },
      true,
      ["encrypt", "decrypt"]
    );

    const pubKey = await crypto.subtle.exportKey("spki", keyPair.publicKey);
    const privKey = await crypto.subtle.exportKey("pkcs8", keyPair.privateKey);

    const aes = await deriveAesKey(password);
    aesKey.value = aes;

    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encrypted = await crypto.subtle.encrypt(
      { name: "AES-GCM", iv },
      aes,
      privKey
    );

    const keyId = getKeyId();
    console.log(`[RSAKEM] Saving key to IndexedDB with ID: ${keyId}`);

    await saveToIndexedDB({
      id: keyId,
      pubKey,
      privKey: encrypted,
      iv,
    });

    const b64 = btoa(String.fromCharCode(...new Uint8Array(pubKey)));
    return toPem(b64, "PUBLIC KEY");
  }

  async function loadKeyPair(password) {
    console.log("[RSAKEM] Loading RSA key pair from IndexedDB...");
    const aes = await deriveAesKey(password);
    aesKey.value = aes;

    const keyId = getKeyId();
    console.log(`[RSAKEM] Attempting to load key ID: ${keyId}`);
    const data = await loadFromIndexedDB(keyId);

    if (!data) {
      console.warn(`[RSAKEM] No key found in IndexedDB for ID: ${keyId}`);
      return null;
    }

    if (!data.iv || !data.privKey || !data.pubKey) {
      console.error("[RSAKEM] Corrupted key data in IndexedDB", data);
      throw new Error("Corrupted key data");
    }

    try {
      const decrypted = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv: new Uint8Array(data.iv) },
        aes,
        data.privKey
      );

      const privateKey = await crypto.subtle.importKey(
        "pkcs8",
        decrypted,
        { name: "RSA-OAEP", hash: "SHA-256" },
        true,
        ["decrypt"]
      );

      const publicKey = await crypto.subtle.importKey(
        "spki",
        data.pubKey,
        { name: "RSA-OAEP", hash: "SHA-256" },
        true,
        ["encrypt"]
      );

      pk.value = publicKey;
      sk.value = privateKey;
      setExpiration();
      console.log("[RSAKEM] RSA key pair loaded into memory");
    } catch (err) {
      console.error("[RSAKEM] Failed to decrypt RSA key pair", err);
      throw new Error(
        "Failed to load key. Possibly wrong password or corrupted data."
      );
    }
  }

  async function decap(ciphertextBase64) {
    if (!sk.value) throw new Error("[RSAKEM] Private key not loaded");

    console.log("[RSAKEM] Decapsulating RSA ciphertext...");
    const decryptedKey = await crypto.subtle.decrypt(
      { name: "RSA-OAEP" },
      sk.value,
      base64.decode(ciphertextBase64)
    );
    return new Uint8Array(decryptedKey);
  }

  async function changePassword(oldPassword, newPassword) {
    console.log("[RSAKEM] Changing encryption password...");
    const keyId = getKeyId();
    const oldAesKey = await deriveAesKey(oldPassword);
    const data = await loadFromIndexedDB(keyId);
    if (!data) throw new Error("[RSAKEM] No key found to update");

    let decryptedPrivateKey;
    try {
      decryptedPrivateKey = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv: new Uint8Array(data.iv) },
        oldAesKey,
        data.privKey
      );
    } catch (e) {
      console.error("[RSAKEM] Incorrect password");
      throw new Error("Incorrect current password");
    }

    const newAesKey = await deriveAesKey(newPassword);
    const newIv = crypto.getRandomValues(new Uint8Array(12));
    const newEncrypted = await crypto.subtle.encrypt(
      { name: "AES-GCM", iv: newIv },
      newAesKey,
      decryptedPrivateKey
    );

    console.log(`[RSAKEM] Saving updated key with ID: ${keyId}`);
    await saveToIndexedDB({
      id: keyId,
      pubKey: data.pubKey,
      privKey: newEncrypted,
      iv: newIv,
    });

    aesKey.value = newAesKey;
    console.log("[RSAKEM] Password updated and keys saved");
  }

  return {
    initRSAPem,
    loadKeyPair,
    changePassword,
    decap,
    sk,
  };
});
