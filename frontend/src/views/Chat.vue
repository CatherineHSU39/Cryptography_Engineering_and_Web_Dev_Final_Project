<!-- src/views/Chat.vue -->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import ChatLayout from '@/components/ChatLayout.vue';
import { useChatStore } from '@/stores/useChatStore';
import { useProfileStore } from '@/stores/useProfileStore';

const chat = useChatStore();
const profile = useProfileStore();
const isLoadingGroups = ref(true);

let pollInterval = null;

onMounted(async () => {
  try {
    await chat.syncGroupList();
    await chat.getAvailableGroups();
    console.log("Initialized chat");
  } catch (err) {
    console.error("Initialization failed:", err);
    alert(err.message);
  }
  isLoadingGroups.value = false;

  // 🟢 Start polling new messages
  pollInterval = setInterval(async () => {
    await chat.getNewMessages();
  }, 2000);

  setInterval(async () => {
    await chat.syncGroupList();
  }, 10000);
});

onUnmounted(() => {
  // 🔴 Cleanup to prevent memory leaks
  clearInterval(pollInterval);
});
</script>

<template>
  <div class="h-full w-full">
    <div
      v-if="isLoadingGroups"
      class="h-full flex items-center justify-center text-[var(--color-text)]"
    >
      <p>正在載入聊天室列表⋯⋯</p>
    </div>

    <div v-else class="h-full relative">
      <ChatLayout />
    </div>
  </div>
</template>
