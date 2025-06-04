const DEK_BASE_URL = "/dek";

function getHeaders() {
  return { "Content-Type": "application/json" };
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
  getDeks: async (dekIds) => {
    const res = await fetch(`${DEK_BASE_URL}`, {
      headers: getHeaders(),
      body: JSON.stringify({ dekIds }),
    });
    return handleResponse(res);
  },

  postDeks: async (ownerId, dek) => {
    const res = await fetch(`${DEK_BASE_URL}`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ ownerId, dek }),
    });
    return handleResponse(res);
  },
};
