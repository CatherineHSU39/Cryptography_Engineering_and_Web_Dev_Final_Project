const BASE_URL = "/app"; // Change if needed

function getAuthHeaders() {
  const token = localStorage.getItem("jwt");
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

async function handleResponse(response) {
  const contentType = response.headers.get("content-type");

  if (!response.ok) {
    let message = "Auth error";
    if (contentType && contentType.includes("application/json")) {
      const err = await response.json();
      message = err.message || message;
    } else {
      message = await response.text();
    }
    throw new Error(message);
  }

  if (contentType && contentType.includes("application/json")) {
    return response.json();
  } else {
    return {};
  }
}

export const MyStatusAPI = {
  syncUser: async () => {
    const res = await fetch(`${BASE_URL}/me/messages/new`, {
      method: "PUT",
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  setReadAt: async (groupId, timestamp) => {
    const res = await fetch(
      `${BASE_URL}/me/groups/${groupId}/messages/status`,
      {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify({ timestamp }),
      }
    );
    return handleResponse(res);
  },
};

export const MyMessageAPI = {
  getMyNewMessages: async () => {
    const res = await fetch(`${BASE_URL}/me/messages/new`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getMyGroupMessages: async (groupId) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}/messages`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  postMyMessage: async (message, groupIds) => {
    const res = await fetch(`${BASE_URL}/me/messages`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ content: message, groupIds }),
    });
    return handleResponse(res);
  },

  updateMyMessage: async (messageId, newContent) => {
    const res = await fetch(`${BASE_URL}/me/messages/${messageId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      body: JSON.stringify({ content: newContent }),
    });
    return handleResponse(res);
  },

  deleteMyMessage: async (messageId) => {
    const res = await fetch(`${BASE_URL}/me/messages/${messageId}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
};

export const MyGroupAPI = {
  getMyGroups: async () => {
    const res = await fetch(`${BASE_URL}/me/groups`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  createMyGroup: async (name) => {
    const res = await fetch(`${BASE_URL}/me/groups`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ name }),
    });
    return handleResponse(res);
  },

  updateMyGroup: async (groupId, name) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      body: JSON.stringify({ name }),
    });
    return handleResponse(res);
  },

  deleteMyGroup: async (groupId) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getMyGroupMember: async (groupId) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}/members`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  addMyGroupMember: async (groupId, memberIds) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}/members`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ memberIds }),
    });
    return handleResponse(res);
  },

  removeMyGroupMember: async (groupId, memberIds) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}/members`, {
      method: "DELETE",
      headers: getAuthHeaders(),
      body: JSON.stringify({ memberIds }),
    });
    return handleResponse(res);
  },

  joinGroup: async (groupId) => {
    const res = await fetch(`${BASE_URL}/me/groups/${groupId}/members/join`, {
      method: "POST",
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
};

export const UserAPI = {
  getAllUsers: async () => {
    const res = await fetch(`${BASE_URL}/users`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
};

export const MessageAPI = {
  getGorupMessages: async (groupId) => {
    const res = await fetch(`${BASE_URL}/groups/${groupId}/messages`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
};

export const GroupAPI = {
  getAllGroups: async () => {
    const res = await fetch(`${BASE_URL}/groups`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
};
