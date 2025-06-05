const DEK_BASE_URL = "/dek";

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
    throw new Error(err.message || "DEK error");
  }

  return hasBody ? await response.json() : {};
}

export const DEKAPI = {
  sync: async () => {
    const res = await fetch(`${DEK_BASE_URL}/users`, {
      method: "POST",
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getNewDeks: async (limit = 100, offset = 0) => {
    const dekQuery = encodeURIComponent(JSON.stringify({ limit, offset }));
    const res = await fetch(`${DEK_BASE_URL}/deks/new?dekQuery=${dekQuery}`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getDeks: async (limit = 20, offset = 0) => {
    const dekQuery = encodeURIComponent(JSON.stringify({ limit, offset }));
    const res = await fetch(`${DEK_BASE_URL}/deks?dekQuery=${dekQuery}`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  postDeks: async (encrypted_deks) => {
    const res = await fetch(`${DEK_BASE_URL}/deks`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(encrypted_deks),
    });
    return handleResponse(res);
  },
};
