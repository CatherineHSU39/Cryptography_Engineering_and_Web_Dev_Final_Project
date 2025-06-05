// src/logic/useEncryption.js
import * as base64 from "@stablelib/base64";
import { XChaCha20Poly1305 } from "@stablelib/xchacha20poly1305";

export async function decrypt(keyBase64, messageContent) {
  try {
    const key = base64.decode(keyBase64);

    const xchacha = new XChaCha20Poly1305(key);
    const plaintext = xchacha.open(
      base64.decode(messageContent.nonce),
      base64.decode(messageContent.ciphertext),
      base64.decode(messageContent.aad)
    );

    if (!plaintext) throw new Error("Decryption failed: authentication error");
    return new TextDecoder().decode(plaintext);
  } catch (err) {
    console.error("decrypt() error:", err);
    return null;
  }
}

export async function encrypt(keyBase64, plaintext) {
  const key = base64.decode(keyBase64);
  const xchacha = new XChaCha20Poly1305(key);
  const nonce = crypto.getRandomValues(new Uint8Array(24));
  const aad = new TextEncoder().encode("message-aad");
  const ciphertext = xchacha.seal(
    nonce,
    new TextEncoder().encode(plaintext),
    aad
  );

  return {
    nonce: base64.encode(nonce), // base64 string
    aad: base64.encode(aad), // base64 string
    ciphertext: base64.encode(ciphertext), // base64 string
  };
}

export async function parseContent(base64encoded) {
  const decoded = base64.decode(base64encoded);
  console.log("[Parse] Base64-decoded Uint8Array:", decoded);

  const jsonStr = new TextDecoder().decode(decoded);
  console.log("[Parse] Decoded JSON string:", jsonStr);

  const obj = JSON.parse(jsonStr);

  return obj;
}
