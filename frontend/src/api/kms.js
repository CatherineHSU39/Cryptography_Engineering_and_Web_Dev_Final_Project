const KMS_BASE_URL = "/kms";

function getHeaders() {
  return { "Content-Type": "application/json" };
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
  register: async (rsa_public_key) => {
    const res = await fetch(`${KMS_BASE_URL}/register`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ rsa_public_key }),
    });
    return handleResponse(res);
  },

  genNewDek: async (user_ids) => {
    const res = await fetch(`${KMS_BASE_URL}/generate-data-key`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ user_ids }),
    });
    return handleResponse(res);
  },

  decryptDek: async (encrypted_deks) => {
    const res = await fetch(`${KMS_BASE_URL}/decrypt-data-key`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ encrypted_deks }),
    });
    return handleResponse(res);
  },
};
