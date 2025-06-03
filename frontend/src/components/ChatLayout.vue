<!-- src/components/ChatLayout.vue -->
<script setup>
import Sidebar from '@/components/Sidebar.vue';
import ChatWindow from '@/components/ChatWindow.vue';
import GroupOverlay from '@/components/GroupOverlay.vue';
import { useChatStore } from '@/stores/useChatStore';

const chat = useChatStore();
</script>

<template>
  <!-- Mobile layout: vertical stack -->
  <div class="flex flex-col sm:flex-row h-full w-full bg-[var(--color-background-mute)] relative">
    <!-- Sidebar: full-width on mobile, 1/4 width on desktop -->
    <div class="w-full sm:w-1/4 h-1/2 sm:h-full border-b sm:border-r border-[var(--color-border)]">
      <Sidebar />
    </div>

    <!-- ChatWindow: full-width on mobile, 3/4 width on desktop -->
    <div class="flex-1 h-full">
      <ChatWindow />
    </div>
  </div>

  <!-- Group overlay outside main flex layout -->
  <div>
    <GroupOverlay />
  </div>
</template>

