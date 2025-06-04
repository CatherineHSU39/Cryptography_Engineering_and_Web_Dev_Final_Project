<template>
  <transition name="fade">
    <div
      v-if="chat.showOverlay"
      class="fixed inset-0 bg-[var(--color-background-mute)]/60 backdrop-blur-sm flex items-center justify-center z-50"
      @click.self="chat.showOverlay = false"
    >
      <div class="bg-[var(--color-background)] w-[90%] max-w-md rounded-xl shadow-xl p-6">
        <!-- Tabs -->
        <div class="flex justify-between mb-4 border-b border-[var(--color-border)]">
          <button
            :class="tab === 'join' ? activeTabClass : tabClass"
            @click="tab = 'join'"
          >
            加入群組
          </button>
          <button
            :class="tab === 'create' ? activeTabClass : tabClass"
            @click="tab = 'create'"
          >
            創建群組
          </button>
        </div>

        <!-- Join Tab -->
        <div v-if="tab === 'join'">
          <div
            v-if="Object.keys(chat.availableGroups).length === 0"
            class="text-center text-[var(--color-text)] opacity-60"
          >
            沒有可加入的群組
          </div>
          <ul v-else class="space-y-2 max-h-64 overflow-y-auto text-[var(--color-text)]">
            <li
              v-for="(group, id) in chat.availableGroups"
              :key="id"
              class="flex justify-between items-center"
            >
              <span>{{ group.name }}</span>
              <button @click="join(id)" class="text-[var(--color-link-primary)] hover:underline">
                加入
              </button>
            </li>
          </ul>
        </div>

        <!-- Create Tab -->
        <div v-else>
          <input
            v-model="newGroupName"
            placeholder="群組名稱"
            class="w-full border border-[var(--color-border)] rounded p-2 mb-4 bg-transparent text-[var(--color-text)]"
          />

          <div class="mb-4 max-h-48 overflow-y-auto border border-[var(--color-border)] rounded p-2 text-[var(--color-text)]">
            <div v-for="user in allUsers" :key="user.id" class="flex items-center space-x-2 mb-1">
              <input type="checkbox" :value="user.id" v-model="selectedUserIds" />
              <span>{{ user.username }}</span>
            </div>
          </div>

          <button
            @click="create"
            class="w-full bg-[var(--color-link-primary)] text-white rounded p-2 hover:bg-[var(--color-link-muted)]"
          >
            建立
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<script setup>
import { ref, watch } from 'vue';
import { useChatStore } from '@/stores/useChatStore';

const chat = useChatStore();

const tab = ref('join');
const newGroupName = ref('');
const allUsers = ref([]);
const selectedUserIds = ref([]);

// Join an existing group
const join = async (id) => {
  try {
    await chat.joinGroup(id);
    chat.groupListUpdated = !chat.groupListUpdated;
  } catch (err) {
    alert("加入群組失敗：" + err.message);
  }
};

// Create a new group
const create = async () => {
  const name = newGroupName.value.trim();
  if (!name) {
    alert("請輸入群組名稱");
    return;
  }

  try {
    const createdGroup = await chat.createGroup({ name });
    const groupId = createdGroup.id;

    const memberIds = [...selectedUserIds.value];
    await chat.addMember(groupId, memberIds);

    if (!chat.groupList[groupId]) {
      chat.groupList[groupId] = {
        name,
        messages: [],
        members: [],
      };
    }

    chat.selectedGroupId = groupId;
    chat.showOverlay = false;
    chat.groupListUpdated = !chat.groupListUpdated;

    // Reset form state
    newGroupName.value = '';
    selectedUserIds.value = [];
  } catch (err) {
    alert("創建群組失敗：" + err.message);
  }
};

// Reset state on overlay open
watch(() => chat.showOverlay, async (val) => {
  if (val) {
    tab.value = 'join';
    selectedUserIds.value = [];
    newGroupName.value = '';
    await chat.getAvailableGroups();
    allUsers.value = await chat.getAllUsers();
  }
});

// Lazy load users when switching to create tab
watch(tab, async (newTab) => {
  if (chat.showOverlay && newTab === 'create' && allUsers.value.length === 0) {
    allUsers.value = await chat.getAllUsers();
  }
});

const tabClass = 'px-4 py-2 text-[var(--color-text)]';
const activeTabClass = 'px-4 py-2 border-b-2 border-[var(--color-link-primary)] text-[var(--color-link-primary)] font-semibold';
</script>
