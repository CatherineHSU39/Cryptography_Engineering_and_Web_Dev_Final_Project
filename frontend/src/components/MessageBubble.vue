<!-- src/components/MessageBubble.vue -->
<template>
  <div
    class="flex"
    :class="{ 'justify-end': isSelf, 'justify-start': !isSelf }"
  >
    <div
      class="max-w-2/3 px-4 py-2 rounded-lg mb-1"
      :class="{
        'bg-green-100 text-gray-800': isSelf,
        'bg-white text-gray-900 border border-gray-200': !isSelf
      }"
    >
      <p class="text-sm font-medium mb-1">{{ senderName }}</p>
      <p>{{ encryptedMessage }}</p>
      <p class="text-xs text-gray-500 mt-1 text-right">{{ formattedTime }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  senderName: {
    type: String,
    required: true
  },
  encryptedMessage: {
    type: String,
    required: true
  },
  createdAt: {
    type: String,
    required: true
  }
})

const isSelf = computed(() => props.senderName === 'You')

const formattedTime = computed(() => {
  const d = new Date(props.createdAt)
  const h = d.getHours().toString().padStart(2, '0')
  const m = d.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
})
</script>