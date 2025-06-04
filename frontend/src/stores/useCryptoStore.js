// src/stores/useCryptoStore.js
import { defineStore } from "pinia";
import { ref } from "vue";
import {
  deriveAesKey,
  saveToIndexedDB,
  loadFromIndexedDB,
  toPem,
  KEY_ID,
} from "@/logic/useCrypto";

export const useCryptoStore = defineStore("crypto", () => {
  const aesKey = ref(null);

  async function initRSAPem(password) {
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

    await saveToIndexedDB({
      id: KEY_ID,
      pubKey,
      privKey: encrypted,
      iv,
    });

    const b64 = btoa(String.fromCharCode(...new Uint8Array(pubKey)));
    return toPem(b64, "PUBLIC KEY");
  }

  async function loadKeyPair(password) {
    const aes = await deriveAesKey(password);
    aesKey.value = aes;

    const data = await loadFromIndexedDB(KEY_ID);
    if (!data) return null;

    const decrypted = await crypto.subtle.decrypt(
      { name: "AES-GCM", iv: data.iv },
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

    return { privateKey, publicKey };
  }

  async function changePassword(oldPassword, newPassword) {
    const oldAesKey = await deriveAesKey(oldPassword);
    const data = await loadFromIndexedDB(KEY_ID);
    if (!data) throw new Error("No key found to update");

    let decryptedPrivateKey;
    try {
      decryptedPrivateKey = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv: data.iv },
        oldAesKey,
        data.privKey
      );
    } catch (e) {
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
      id: KEY_ID,
      pubKey: data.pubKey,
      privKey: newEncrypted,
      iv: newIv,
    });

    aesKey.value = newAesKey;
  }

  return {
    initRSAPem,
    loadKeyPair,
    changePassword,
  };
});
