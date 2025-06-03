// src/stores/useChatStore.js
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { useProfileStore } from "@/stores/useProfileStore";
import {
  MyGroupAPI,
  MyMessageAPI,
  GroupAPI,
  UserAPI,
  MyStatusAPI,
} from "@/api/app";

export const useChatStore = defineStore("chat", () => {
  const profile = useProfileStore();

  const groupList = ref([]);
  const selectedGroupId = ref(null);
  const availableGroups = ref([]);
  const showOverlay = ref(false);
  const groupListUpdated = ref(false); // signal flag

  const selectedGroupObject = computed(
    () => groupList.value.find((g) => g.id === selectedGroupId.value) || null
  );

  const syncGroupList = async () => {
    try {
      const latestGroups = await MyGroupAPI.getMyGroups();

      const newGroupList = [];

      // Step 1: Refresh members for existing groups first
      for (const group of groupList.value) {
        const updatedMembers = await MyGroupAPI.getMyGroupMember(group.id);
        group.members = updatedMembers || [];
      }

      // Step 2: Add all groups (reuse existing or fetch new)
      for (const group of latestGroups) {
        const existing = groupList.value.find((g) => g.id === group.id);

        if (!existing) {
          // 🟢 Fetch members and messages for new group
          const [resMessages, resMembers] = await Promise.all([
            MyMessageAPI.getMyGroupMessages(group.id),
            MyGroupAPI.getMyGroupMember(group.id),
          ]);

          newGroupList.push({
            id: group.id,
            name: group.name,
            messages: resMessages?.content?.reverse() || [],
            members: resMembers || [],
          });
        } else {
          newGroupList.push(existing);
        }
      }

      groupList.value = newGroupList;
      groupListUpdated.value = !groupListUpdated.value;
    } catch (err) {
      console.error("Group list sync failed:", err);
    }
  };

  function hasUnread(group) {
    const member = group.members?.find(
      (m) => m.userId === profile.currentUserId
    );
    const lastMessage = group.messages?.[group.messages.length - 1];
    if (!member?.readAt || !lastMessage?.createdAt) return false;

    return (
      new Date(lastMessage.createdAt).getTime() >
      new Date(member.readAt).getTime()
    );
  }

  async function setReadAtIfNeeded(groupId) {
    const group = groupList.value.find((g) => g.id === groupId);
    if (!group || !hasUnread(group)) return;

    await setReadAt(groupId);
  }

  const setReadAt = async (groupId) => {
    const group = groupList.value.find((g) => g.id === groupId);
    if (!group) return;

    const now = new Date().toISOString();
    try {
      await MyStatusAPI.setReadAt(groupId, now);
      const member = group.members.find(
        (m) => m.userId === profile.currentUserId
      );
      if (member) member.readAt = now;
      console.log("Updated readAt for group", groupId);
    } catch (err) {
      console.error("setReadAt failed:", err);
    }
  };

  const getAllUsers = async () => {
    try {
      const users = await UserAPI.getAllUsers();
      return users.filter((user) => user.id !== profile.currentUserId);
    } catch (err) {
      console.error("Failed to fetch users", err);
      alert(err.message);
      return [];
    }
  };

  const getAllGroups = async () => {
    try {
      const data = await GroupAPI.getAllGroups();
      return data.map((g) => ({ id: g.id, name: g.name }));
    } catch (err) {
      console.error("Global Group loading failed:", err);
      alert(err.message);
      return [];
    }
  };

  const getAvailableGroups = async () => {
    try {
      const [allGroupData, myGroupData] = await Promise.all([
        GroupAPI.getAllGroups(),
        MyGroupAPI.getMyGroups(),
      ]);

      const joinedGroupIds = new Set(myGroupData.map((g) => g.id));

      const available = allGroupData
        .filter((g) => !joinedGroupIds.has(g.id))
        .reduce((acc, g) => {
          acc[g.id] = { name: g.name };
          return acc;
        }, {});

      availableGroups.value = available;
      return available;
    } catch (err) {
      console.error("Available groups loading failed:", err);
      alert(err.message);
      return {};
    }
  };

  const createGroup = async ({ name }) => {
    try {
      const createdGroup = await MyGroupAPI.createMyGroup(name);
      const newGroup = {
        id: createdGroup.id,
        name: createdGroup.name,
        messages: [],
        members: [],
      };
      groupList.value.push(newGroup);
      return createdGroup;
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
      groupList.value = groupList.value.filter((g) => g.id !== id);
      if (selectedGroupId.value === id) selectedGroupId.value = null;
    } catch (err) {
      console.error("Group delete failed:", err);
      alert(err.message);
    }
  };

  const sendMessageToRoom = async (message) => {
    const selectedId = selectedGroupId.value;
    if (!message.trim() || !selectedId) return;

    try {
      const newMsg = await sendMessage(message, [selectedId]);
      const group = groupList.value.find((g) => g.id === selectedId);
      if (group && Array.isArray(group.messages)) {
        group.messages.push(...newMsg);
      }
    } catch (err) {
      console.error("Send message failed:", err);
      alert(err.message);
    }
  };

  const sendMessage = async (message, groupIds) => {
    try {
      return await MyMessageAPI.postMyMessage(message, groupIds);
    } catch (err) {
      console.error("Send message failed:", err);
      alert(err.message);
    }
  };

  const getNewMessages = async () => {
    try {
      const data = await MyMessageAPI.getMyNewMessages();
      const messageList = Array.isArray(data?.content) ? data.content : [];

      for (const message of messageList) {
        const groupId = message.groupId;
        const group = groupList.value.find((g) => g.id === groupId);
        if (!group) continue;

        if (!Array.isArray(group.messages)) {
          group.messages = [];
        }

        const index = group.messages.findIndex(
          (m) => m.messageId === message.messageId
        );

        if (index === -1) {
          group.messages.push(message); // New message
        } else {
          group.messages.splice(index, 1, message); // Replace existing
        }
      }
    } catch (err) {
      console.error("Fetching new messages failed:", err);
    }
  };

  const getMessages = async (groups) => {
    const promises = Object.entries(groups).map(([id, group]) =>
      MyMessageAPI.getMyGroupMessages(id).then((messages) => {
        if (!Array.isArray(group.messages)) {
          group.messages = [];
        }
        group.messages.push(...messages.content.reverse());
      })
    );
    await Promise.all(promises);
  };

  const updateMessage = async (messageId, newContent) => {
    try {
      await MyMessageAPI.updateMyMessage(messageId, newContent);

      for (const group of groupList.value) {
        const msg = group.messages.find((m) => m.messageId === messageId);
        if (msg) {
          msg.content = newContent;
          break;
        }
      }
    } catch (err) {
      console.error("Update message failed:", err);
      alert(err.message);
    }
  };

  const deleteMessage = async (messageId) => {
    try {
      await MyMessageAPI.deleteMyMessage(messageId);

      for (const group of groupList.value) {
        const index = group.messages?.findIndex(
          (msg) => msg.messageId === messageId
        );
        if (index !== -1 && index !== undefined) {
          group.messages.splice(index, 1);
          break;
        }
      }
    } catch (err) {
      console.error("Delete message failed:", err);
      alert(err.message);
    }
  };

  const joinGroup = async (groupId) => {
    try {
      await MyGroupAPI.joinGroup(groupId);

      const name = availableGroups.value[groupId]?.name || "未命名群組";
      const newGroup = { id: groupId, name, messages: [], members: [] };

      delete availableGroups.value[groupId];

      await Promise.all([
        MyMessageAPI.getMyGroupMessages(groupId).then((res) => {
          newGroup.messages = res?.content?.reverse() || [];
        }),
        MyGroupAPI.getMyGroupMember(groupId).then((members) => {
          newGroup.members = members || [];
        }),
      ]);

      groupList.value.push(newGroup);
      selectedGroupId.value = groupId;
      showOverlay.value = false;
      groupListUpdated.value = !groupListUpdated.value;
    } catch (err) {
      console.error("Join group failed:", err);
      alert(err.message);
    }
  };

  const leaveGroup = async (groupId) => {
    try {
      await MyGroupAPI.removeMyGroupMember(groupId, [profile.currentUserId]);
      const index = groupList.value.findIndex((g) => g.id === groupId);
      if (index !== -1) groupList.value.splice(index, 1);
      if (selectedGroupId.value === groupId) selectedGroupId.value = null;
    } catch (err) {
      console.error("Leave group failed:", err);
      alert(err.message);
    }
  };

  const addMember = async (groupId, userIds) => {
    try {
      await MyGroupAPI.addMyGroupMember(groupId, userIds);

      const fetchedUsers = await UserAPI.getAllUsers();
      const newMembers = fetchedUsers.filter((u) => userIds.includes(u.id));
      const group = groupList.value.find((g) => g.id === groupId);

      if (group && group.members) {
        for (const user of newMembers) {
          const exists = group.members.find((m) => m.userId === user.id);
          if (!exists) {
            group.members.push({ userId: user.id, username: user.username });
          }
        }
      }
    } catch (err) {
      console.error("Add member failed:", err);
      alert(err.message);
    }
  };

  const removeMember = async (groupId, userId) => {
    try {
      await MyGroupAPI.removeMyGroupMember(groupId, [userId]);

      const group = groupList.value.find((g) => g.id === groupId);
      if (group && Array.isArray(group.members)) {
        group.members = group.members.filter((m) => m.userId !== userId);
      }
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
    groupList,
    groupListUpdated,
    selectedGroupId,
    selectedGroupObject,
    availableGroups,
    showOverlay,

    syncGroupList,
    hasUnread,
    setReadAtIfNeeded,
    setReadAt,
    getAllUsers,
    createGroup,
    getAllGroups,
    getAvailableGroups,
    updateGroup,
    deleteGroup,
    joinGroup,
    leaveGroup,
    sendMessageToRoom,
    sendMessage,
    getMessages,
    getNewMessages,
    updateMessage,
    deleteMessage,
    addMember,
    removeMember,
    getMembers,
  };
});
