const KMS_BASE_URL = "/kms";

function getAuthHeaders() {
  const token = localStorage.getItem("jwt");
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

async function handleResponse(response) {
  const contentType = response.headers.get("content-type");
  const hasBody = contentType && contentType.includes("application/json");

  if (!response.ok) {
    const err = hasBody ? await response.json() : { message: "Unknown error" };
    throw new Error(err.message || "KMS error");
  }

  return hasBody ? await response.json() : {};
}

export const KMSAPI = {
  getRegStatus: async () => {
    const res = await fetch(`${KMS_BASE_URL}/register/status`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  register: async (kyber_public_key, rsa_public_key) => {
    const res = await fetch(`${KMS_BASE_URL}/register`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ kyber_public_key, rsa_public_key }),
    });
    return handleResponse(res);
  },

  genNewDek: async (user_ids) => {
    const res = await fetch(`${KMS_BASE_URL}/generate-data-key`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ user_ids }),
    });
    return handleResponse(res);
  },

  decryptDek: async (kem_alg, encrypted_deks) => {
    const res = await fetch(`${KMS_BASE_URL}/decrypt-data-key`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ kem_alg, encrypted_deks }),
    });
    return handleResponse(res);
  },
};
