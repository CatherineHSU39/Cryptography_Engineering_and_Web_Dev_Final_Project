import axios from 'axios'

const backend = axios.create({
  baseURL: 'http://localhost:8080/app',
})


backend.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export async function getAllGroups() {
  const res = await backend.get('/groups')
  return res.data
}
// export async function createGroup(createGroupRequest) {
//     return backend.post('/groups', createGroupRequest)
//       .then((res) => res.data);
// }

export async function getGroupById(groupId) {
    return backend
      .get(`/groups/${groupId}`)
      .then((res) => res.data);
}
export async function updateGroup(groupId, updateGroupRequest) {
    return backend
      .put(`/groups/${groupId}`, updateGroupRequest)
      .then((res) => res.data);
}
export async function deleteGroup(groupId) {
    return backend
      .delete(`/groups/${groupId}`)
      .then((res) => res.data);
}

export async function getMessages(groupId) {
  const res = await backend.get(`/groups/${groupId}/messages`)
  return res.data
}

export async function sendMessageToRoom(senderId, groupId, encryptedMessage) {
  const res = await backend.post(`/messages`, { senderId, groupId, encryptedMessage })
  return res.data
}

export async function markAsRead(groupId) {
  await backend.put(`/groups/${groupId}/messages/status`)
}


const auth = axios.create({
  baseURL: 'http://localhost:8081/',
})

export async function authenticateUser(username, password) {
  const res = await auth.post(`/identity/token`, { username, password })
  
  return res.data
}

export async function verify2FA(code) {
  const res = await auth.post(`/identity/2fa-verification`, { code })
  return res.data
}

export async function registerUser(username, password) {
  await auth.post(`/users`, { username, password })
}


export async function setup2FA(jwt) {
  await auth.put(`/users/me/2fa`, { jwt })
}

// export default {

//   /**
//    * GetUserResponse
//    * GET  /app/users/me
//    * 取得當前已登入使用者資料 (UserResponse)
//    */
//   getUserResponse() {
//     return apiClient
//       .get('/users/me')
//       .then((res) => res.data);
//   },

//   /**
//    * UpdateUserRequest / UserResponse
//    * PUT  /app/users/me
//    * 更新當前使用者 (UserRequest 送到後端，回傳 UserResponse)
//    * @param {Object} updateUserRequest  { username?: string, password?: string, ... }
//    * @returns {Promise<Object>} 更新後的 UserResponse
//    */
//   updateUserRequest(updateUserRequest) {
//     return apiClient
//       .put('/users/me', updateUserRequest)
//       .then((res) => res.data);
//   },

//   /**
//    * GetUserResponseById
//    * GET  /app/users/{id}
//    * 根據 ID 取得使用者 (UserResponse)
//    * @param {string|number} userId
//    * @returns {Promise<Object>} 單一 UserResponse
//    */
//   getUserResponseById(userId) {
//     return apiClient
//       .get(`/users/${userId}`)
//       .then((res) => res.data);
//   },

//   //
//   // ─── GROUP CONTROLLER │ 對應第二張圖的 CUGroupRequest／CUGroupResponse／GetAllGroupResponse／GetGroupResponse、GroupMemberResponse ─────────
//   //

//   /**
//    * GetAllGroupResponse
//    * GET  /app/groups
//    * 取得所有群組列表 (GetAllGroupResponse[])
//    * @returns {Promise<Array<Object>>} 陣列，每個物件形狀為 { id, name }
//    */
//   getAllGroupResponse() {
//     return apiClient
//       .get('/groups')
//       .then((res) => res.data);
//   },

//   /**
//    * CUGroupRequest => CUGroupResponse
//    * POST /app/groups
//    * 建立新群組 (Request body shape: CUGroupRequest, Response: CUGroupResponse)
//    * @param {Object} createGroupRequest  { name: string, memberIds: Array<string|number> }
//    * @returns {Promise<Object>} 回傳 CUGroupResponse { id, name, memberIds }
//    */
//   CUGroupRequest(createGroupRequest) {
//     return apiClient
//       .post('/groups', createGroupRequest)
//       .then((res) => res.data);
//   },

//   /**
//    * GetGroupResponse
//    * GET  /app/groups/{id}
//    * 取得單一群組詳細資料 (GetGroupResponse)
//    * @param {string|number} groupId
//    * @returns {Promise<Object>} { id, name, members: GroupMemberResponse[], messages: MessageResponse[] }
//    */
//   getGroupResponse(groupId) {
//     return apiClient
//       .get(`/groups/${groupId}`)
//       .then((res) => res.data);
//   },

//   /**
//    * UpdateGroupRequest => CUGroupResponse
//    * PUT /app/groups/{id}
//    * 更新群組 (Request shape 為 CUGroupRequest，可只帶 name 或 memberIds)
//    * @param {string|number} groupId
//    * @param {Object} updateGroupRequest  { name?: string, memberIds?: Array<string|number> }
//    * @returns {Promise<Object>} 回傳更新後的 CUGroupResponse { id, name, memberIds }
//    */
//   updateGroupRequest(groupId, updateGroupRequest) {
//     return apiClient
//       .put(`/groups/${groupId}`, updateGroupRequest)
//       .then((res) => res.data);
//   },

//   /**
//    * DeleteGroupResponse (void 或者空內容)
//    * DELETE /app/groups/{id}
//    * 刪除單一群組
//    * @param {string|number} groupId
//    * @returns {Promise<void>}
//    */
//   deleteGroupResponse(groupId) {
//     return apiClient
//       .delete(`/groups/${groupId}`)
//       .then((res) => res.data);
//   },

//   //
//   // ─── MESSAGE CONTROLLER │ 對應第二張圖的 MessageRequest／MessageResponse ────────────────────
//   //

//   /**
//    * MessageRequest => MessageResponse
//    * POST /app/messages
//    * 發送訊息到某個群組 (Request: MessageRequest, Response: MessageResponse)
//    * @param {Object} sendMessageRequest { senderId: string|number, groupId: string|number, encryptedMessage: string }
//    * @returns {Promise<Object>} 回傳 MessageResponse { messageId, groupId, senderId, encryptedMessage, createdAt }
//    */
//   MessageRequest(sendMessageRequest) {
//     return apiClient
//       .post('/messages', sendMessageRequest)
//       .then((res) => res.data);
//   },

//   /**
//    * GetMessageResponse
//    * GET /app/messages/{id}
//    * 取得單一訊息 (MessageResponse)
//    * @param {string|number} messageId
//    * @returns {Promise<Object>} { messageId, groupId, senderId, encryptedMessage, createdAt }
//    */
//   getMessageResponse(messageId) {
//     return apiClient
//       .get(`/messages/${messageId}`)
//       .then((res) => res.data);
//   }
// };
