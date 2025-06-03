<template>
  <div class="h-full flex flex-col bg-[var(--color-background)] relative">
    <div class="px-4 py-4 border-b border-[var(--color-border)] flex justify-between items-center">
      <h2 class="text-xl font-semibold text-[var(--color-heading)]">聊天室清單</h2>
      <button @click="chat.showOverlay = true" class="text-sm text-[var(--color-link-primary)] hover:underline">+ 群組</button>
    </div>

    <ul class="flex-1 overflow-auto">
      <li
        v-for="group in chat.groupList"
        :key="group.id"
        class="px-4 py-3 cursor-pointer hover:bg-[var(--color-background-soft)] relative"
        :class="{ 'bg-[var(--color-background-mute)] font-semibold': group.id === chat.selectedGroupId }"
        @click="selectGroup(group.id)"
        @contextmenu.prevent="openSettings(group.id)"
      >
        <span class="text-[var(--color-text)]">{{ group.name }}</span>

        <!-- Blue unread indicator -->
        <span
          v-if="chat.hasUnread(group)"
          class="absolute top-1/2 right-4 transform -translate-y-1/2 w-2 h-2 bg-blue-500 rounded-full"
        ></span>
      </li>
    </ul>

    <GroupSettingsOverlay
      :visible="!!settingsGroupId"
      :groupId="settingsGroupId"
      @close="settingsGroupId = null"
    />
  </div>
</template>



<script setup>
import GroupSettingsOverlay from './GroupSettingsOverlay.vue';
import { ref, watch } from 'vue';
import { useChatStore } from '@/stores/useChatStore';
import { useProfileStore } from '@/stores/useProfileStore';

const chat = useChatStore();
const profile = useProfileStore();

const settingsGroupId = ref(null);

function selectGroup(id) {
  chat.selectedGroupId = id;
}

function openSettings(groupId) {
  settingsGroupId.value = groupId;
}

watch(
  () => chat.groupListUpdated,
  (triggered) => {
    console.log('Group list was synced. Update display.');
  }
);
</script>

