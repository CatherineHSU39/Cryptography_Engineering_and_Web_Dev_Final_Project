<!-- src/layouts/ChatLayout.vue -->
<template>
  <div class="flex h-full w-full bg-gray-100">
    <!-- 左半邊 Sidebar -->
    <div class="w-1/4 h-full border-r border-gray-300">
      <Sidebar
        :groups="groups"
        :selectedGroupId="selectedGroupId"
        @selectGroup="id => emit('onSelectGroup', id)"
      />
    </div>

    <!-- 右半邊 ChatWindow -->
    <div class="flex-1 h-full">
      <ChatWindow
        :group="selectedGroupObject"
        :messages="messages"
        @sendMessage="text => emit('onSendMessage', text)"
      />
    </div>
  </div>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
import ChatWindow from '@/components/ChatWindow.vue'

// props: 從 Chat.vue 傳下來
const props = defineProps({
  groups:             { type: Array, required: true },
  selectedGroupId:    { type: String, default: null },
  selectedGroupObject:{ type: Object, default: null },
  messages:           { type: Array, default: () => [] }
})

// 這一層要往上 emit 兩個事件：onSelectGroup 與 onSendMessage
const emit = defineEmits(['onSelectGroup', 'onSendMessage'])
</script>
