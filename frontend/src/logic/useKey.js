// src/logic/useKey.js

const DB_NAME = "cryptoKeysDB";
const STORE_NAME = "keys";
const SALT = new TextEncoder().encode("static-salt-change-in-prod");

export {
  DB_NAME,
  STORE_NAME,
  SALT,
  deriveAesKey,
  saveToIndexedDB,
  loadFromIndexedDB,
  deleteFromIndexedDB,
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

// --- Save to IndexedDB (supports per-user keying) ---
function saveToIndexedDB(obj) {
  const id = obj.id;
  console.log(`[IndexedDB] Saving object with ID: ${id}`, obj);

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
      const putRequest = store.put(obj);

      putRequest.onsuccess = () => {
        console.log(`[IndexedDB] Successfully saved ID: ${id}`);
        tx.oncomplete = () => {
          db.close();
          resolve();
        };
      };

      putRequest.onerror = () => reject(putRequest.error);
      tx.onerror = () => reject(tx.error);
    };

    request.onerror = () => reject(request.error);
  });
}

// --- Load from IndexedDB (by keyId) ---
function loadFromIndexedDB(keyId) {
  const id = `${keyId}`;
  console.log(`[IndexedDB] Attempting to load object with ID: ${id}`);

  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onsuccess = () => {
      const db = request.result;
      const tx = db.transaction(STORE_NAME, "readonly");
      const store = tx.objectStore(STORE_NAME);
      const getRequest = store.get(id);

      getRequest.onsuccess = () => {
        const result = getRequest.result;
        if (result) {
          console.log(`[IndexedDB] Loaded object for ID: ${id}`, result);
        } else {
          console.warn(`[IndexedDB] No object found for ID: ${id}`);
        }
        resolve(result);
      };

      getRequest.onerror = () => reject(getRequest.error);
    };
    request.onerror = () => reject(request.error);
  });
}

// --- Delete from IndexedDB (optional helper) ---
function deleteFromIndexedDB(userId, algo) {
  const id = `${algo}-key-${userId}`;
  console.log(`[IndexedDB] Deleting object with ID: ${id}`);

  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onsuccess = () => {
      const db = request.result;
      const tx = db.transaction(STORE_NAME, "readwrite");
      const store = tx.objectStore(STORE_NAME);
      const deleteRequest = store.delete(id);

      deleteRequest.onsuccess = () => {
        console.log(`[IndexedDB] Deleted object with ID: ${id}`);
        resolve();
      };
      deleteRequest.onerror = () => reject(deleteRequest.error);
    };
    request.onerror = () => reject(request.error);
  });
}

// --- Convert Base64 to PEM format ---
function toPem(b64, type) {
  const lines = b64.match(/.{1,64}/g).join("\n");
  return `-----BEGIN ${type}-----\n${lines}\n-----END ${type}-----`;
}
