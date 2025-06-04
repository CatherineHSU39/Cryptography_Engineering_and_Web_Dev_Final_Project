// src/logic/useChat.js
import { MyStatusAPI } from "@/api/app";
import { MyMessageAPI } from "@/api/app";
import { MyGroupAPI } from "@/api/app";

export function useChat() {
  const groupList = {}; // Internal state

  const initChat = async () => {
    try {
      await MyStatusAPI.syncUser();
      const groups = await getGroups();
      await getMessages(groups);
      await getMembers(groups);
      Object.assign(groupList, groups); // update internal state
      return { groupList };
    } catch (err) {
      console.error("Initialization failed:", err);
      alert(err.message);
      return { groupList: {} };
    }
  };

  // === Group Logic ===
  const getGroups = async () => {
    const groups = {};
    try {
      const data = await MyGroupAPI.getMyGroups();
      data.forEach((g) => {
        groups[g.id] = { id: g.id, name: g.name };
      });
      return groups;
    } catch (err) {
      console.error("Group loading failed:", err);
      alert(err.message);
      return {};
    }
  };

  const createGroup = async ({ name }) => {
    try {
      return await MyGroupAPI.createMyGroup(name);
    } catch (err) {
      console.error("Group creation failed:", err);
      alert(err.message);
    }
  };

  const updateGroup = async ({ groupId, name }) => {
    try {
      await MyGroupAPI.updateMyGroup(groupId, name);
    } catch (err) {
      console.error("Group update failed:", err);
      alert(err.message);
    }
  };

  const deleteGroup = async (id) => {
    try {
      await MyGroupAPI.deleteMyGroup(id);
    } catch (err) {
      console.error("Group delete failed:", err);
      alert(err.message);
    }
  };

  // === Message Logic ===
  const sendMessageToRoom = async (message) => {
    const selectedId = localStorage.getItem("selectedGroupId");
    if (!message.trim() || !selectedId) return;
    try {
      const newMsg = await sendMessage([selectedId], message);
      groupList[selectedId].messages.push(newMsg);
    } catch (err) {
      console.error("Send message failed:", err);
      alert(err.message);
    }
  };

  const sendMessage = async (groupIds, message) => {
    try {
      return await MyMessageAPI.postMyMessage({
        content: message,
        groupIds,
      });
    } catch (err) {
      console.error("Send message failed:", err);
      alert(err.message);
    }
  };

  const getMessages = async (groups) => {
    const promises = Object.entries(groups).map(([id, group]) =>
      MyMessageAPI.getMyGroupMessages(id).then((messages) => {
        group.messages = messages;
      })
    );
    await Promise.all(promises);
  };

  const deleteMessage = async (messageId) => {
    try {
      await MyMessageAPI.deleteMessage(messageId);
    } catch (err) {
      console.error("Delete message failed:", err);
      alert(err.message);
    }
  };

  // === Member Logic ===
  const addMember = async (groupId, userId) => {
    try {
      await MyGroupAPI.addMyGroupMember(groupId, [userId]);
    } catch (err) {
      console.error("Add member failed:", err);
      alert(err.message);
    }
  };

  const removeMember = async (groupId, userId) => {
    try {
      await MyGroupAPI.removeMyGroupMember(groupId, [userId]);
    } catch (err) {
      console.error("Remove member failed:", err);
      alert(err.message);
    }
  };

  const getMembers = async (groups) => {
    const promises = Object.entries(groups).map(([id, group]) =>
      MyGroupAPI.getMyGroupMember(id).then((members) => {
        group.members = members;
      })
    );
    await Promise.all(promises);
  };

  return {
    initChat,

    // Groups
    createGroup,
    getGroups,
    updateGroup,
    deleteGroup,

    // Messages
    sendMessageToRoom,
    sendMessage,
    getMessages,
    deleteMessage,

    // Members
    addMember,
    removeMember,
    getMembers,
  };
}
