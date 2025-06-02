const AUTH_BASE_URL = "/auth";

function getHeaders() {
  return { "Content-Type": "application/json" };
}

async function handleResponse(response) {
  const contentType = response.headers.get("content-type");
  const hasBody = contentType && contentType.includes("application/json");

  if (!response.ok) {
    const err = hasBody ? await response.json() : { message: "Unknown error" };
    throw new Error(err.message || "Auth error");
  }

  return hasBody ? await response.json() : {};
}

function handleJWT(data) {
  if (data.token) {
    localStorage.setItem("currentUserId", data.id);
    localStorage.setItem("username", data.username);
    localStorage.setItem("jwt", data.token);
  } else {
    throw new Error("Login failed: No token received");
  }
}

export const AuthAPI = {
  login: async (username, password) => {
    const res = await fetch(`${AUTH_BASE_URL}/identity/token`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ username, password }),
    });
    const data = await handleResponse(res);
    handleJWT(data);
  },

  register: async (username, password) => {
    const res = await fetch(`${AUTH_BASE_URL}/users`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ username, password }),
    });
    return handleResponse(res);
  },
};

export const UserAPI = {
  updateUser: async (username, password) => {
    const token = localStorage.getItem("jwt");
    const res = await fetch(`${AUTH_BASE_URL}/users`, {
      method: "PUT",
      headers: {
        ...getHeaders(),
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ username, password }),
    });
    const data = await handleResponse(res);
    handleJWT(data); // Update localStorage with new token and user info if returned
    return data;
  },
};

//   getCurrentUser: async () => {
//     const token = localStorage.getItem("token");
//     const res = await fetch(`${AUTH_BASE_URL}/me`, {
//       headers: {
//         ...getHeaders(),
//         Authorization: `Bearer ${token}`,
//       },
//     });
//     return handleResponse(res);
//   },
