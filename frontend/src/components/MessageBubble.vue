<!-- src/components/MessageBubble.vue-->
<template>
  <div class="flex flex-col" :class="{ 'items-end': isSelf, 'items-start': !isSelf }">
    <!-- Sender name (always reserve space) -->
    <p
      class="text-sm font-medium mb-1 ml-2 text-[var(--color-heading)] min-h-[1.25rem]"
    >
      <!-- Only show name if not self -->
      <span v-if="!isSelf">{{ senderName }}</span>
    </p>

    <!-- Message + Timestamp Row -->
    <div class="flex items-end gap-1" :class="{ 'flex-row-reverse': isSelf }">
      <!-- Message Bubble -->
      <div
        ref="bubble"
        class="px-3 sm:px-4 py-2 rounded-lg text-sm sm:text-base"
        :class="{
          'bg-[var(--color-accent)] text-[var(--color-text)]': isSelf,
          'bg-[var(--color-background)] text-[var(--color-text)] border border-[var(--color-border)]': !isSelf
        }"
        @contextmenu.prevent="onRightClick"
      >
        <p class="break-words whitespace-pre-wrap leading-relaxed">{{ encryptedMessage }}</p>
      </div>

      <!-- Timestamp with edited tag stacked and aligned -->
      <div
        class="flex flex-col text-xs text-[var(--color-text)] opacity-60 mb-0.5"
        :class="{ 'items-end': isSelf, 'items-start': !isSelf }"
        :title="new Date(createdAt).toLocaleString()"
      >
        <span v-if="isEdited">(已編輯)</span>
        <span>{{ formattedTime }}</span>
      </div>
    </div>
  </div>
</template>


<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  messageId: String,
  senderId: String,
  senderName: String,
  encryptedMessage: String,
  createdAt: [String, Date],
  updatedAt: [String, Date],
  isSelf: Boolean,
});


const emit = defineEmits(['contextmenu']);
const bubble = ref(null);

function onRightClick(e) {
  if (props.isSelf) {
    emit('contextmenu', {
      messageId: props.messageId,
      target: bubble.value,
    });
  }
}

const formattedTime = computed(() => {
  const iso = (props.createdAt ?? '').toString();
  const timeMatch = iso.match(/T(\d{2}):(\d{2})/); // Matches the HH:MM part
  return timeMatch ? `${timeMatch[1]}:${timeMatch[2]}` : '';
});


const isEdited = computed(() => {
  const createdStr = (props.createdAt ?? '').toString().slice(0, 19);  // "YYYY-MM-DDTHH:MM:SS"
  const updatedStr = (props.updatedAt ?? props.createdAt ?? '').toString().slice(0, 19);
  return createdStr !== updatedStr;
});


</script>
