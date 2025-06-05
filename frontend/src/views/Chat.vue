<!-- src/views/Chat.vue -->
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import ChatLayout from '@/components/ChatLayout.vue';
import { useChatStore } from '@/stores/useChatStore';
import { useProfileStore } from '@/stores/useProfileStore';
import { useEncryptionStore } from '@/stores/useEncryptionStore';
import { useRSAKEMStore } from '@/stores/useRSAKEMStore';

const chat = useChatStore();
const profile = useProfileStore();
const rsakem = useRSAKEMStore();
const encryption = useEncryptionStore();
const isLoadingGroups = ref(true);

let pollInterval = {};

onMounted(async () => {
  try {
    await Promise.all([
      encryption.init(),
      chat.syncMessagePage(),
      encryption.syncDekMap(),
      chat.syncGroupList()
    ]);
    await chat.getAvailableGroups();
    console.log("Initialized chat");
  } catch (err) {
    console.error("Initialization failed:", err);
    alert(err.message);
  }
  isLoadingGroups.value = false;

  // 🟢 Start polling new messages (commented out for now)
  pollInterval.getMessages = setInterval(chat.getNewMessages, 2000);
  pollInterval.syncGroups = setInterval(chat.syncGroupList, 10000);
  pollInterval.syncDeks = setInterval(encryption.getNewDeks, 2000);

  // One-time DEK sync
  try {
    await encryption.getNewDeks();
  } catch (err) {
    console.warn("getNewDeks failed:", err);
  }
});

onUnmounted(() => {
  clearInterval(pollInterval.getMessages);
  clearInterval(pollInterval.syncGroups);
  clearInterval(pollInterval.syncDeks);
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
