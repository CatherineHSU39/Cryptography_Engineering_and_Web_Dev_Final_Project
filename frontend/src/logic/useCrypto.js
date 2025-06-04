// src/logic/useCrypto.js

const DB_NAME = "cryptoKeysDB";
const STORE_NAME = "keys";
const KEY_ID = "rsa-key";
const SALT = new TextEncoder().encode("static-salt-change-in-prod"); // consider making this dynamic

export {
  DB_NAME,
  STORE_NAME,
  KEY_ID,
  SALT,
  deriveAesKey,
  saveToIndexedDB,
  loadFromIndexedDB,
  toPem,
};

// --- Derive AES key using PBKDF2 ---
async function deriveAesKey(password) {
  const enc = new TextEncoder();
  const baseKey = await crypto.subtle.importKey(
    "raw",
    enc.encode(password),
    { name: "PBKDF2" },
    false,
    ["deriveKey"]
  );

  return await crypto.subtle.deriveKey(
    {
      name: "PBKDF2",
      salt: SALT,
      iterations: 100_000,
      hash: "SHA-256",
    },
    baseKey,
    { name: "AES-GCM", length: 256 },
    false,
    ["encrypt", "decrypt"]
  );
}

// --- Save to IndexedDB ---
function saveToIndexedDB(obj) {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      db.createObjectStore(STORE_NAME, { keyPath: "id" });
    };

    request.onsuccess = () => {
      const db = request.result;
      const tx = db.transaction(STORE_NAME, "readwrite");
      const store = tx.objectStore(STORE_NAME);
      store.put(obj);
      tx.oncomplete = resolve;
      tx.onerror = () => reject(tx.error);
    };

    request.onerror = () => reject(request.error);
  });
}

// --- Load from IndexedDB ---
function loadFromIndexedDB(id) {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onsuccess = () => {
      const db = request.result;
      const tx = db.transaction(STORE_NAME, "readonly");
      const store = tx.objectStore("keys");
      const getRequest = store.get(id);
      getRequest.onsuccess = () => resolve(getRequest.result);
      getRequest.onerror = () => reject(getRequest.error);
    };
    request.onerror = () => reject(request.error);
  });
}

// --- Convert Base64 to PEM format ---
function toPem(b64, type) {
  const lines = b64.match(/.{1,64}/g).join("\n");
  return `-----BEGIN ${type}-----\n${lines}\n-----END ${type}-----`;
}
