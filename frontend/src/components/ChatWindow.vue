<!-- src/components/ChatWindow.vue -->
<template>
  <div class="flex flex-col h-full bg-gray-50">
    <!-- 當 group 是 null 時顯示提示 -->
    <div v-if="!group" class="flex-1 flex items-center justify-center text-gray-500">
      <p>請先從左側選擇一個聊天室</p>
    </div>

    <!-- 選到群組之後才顯示聊天內容 -->
    <div v-else class="flex flex-col h-full">
      <!-- (省略 header 與 message lists) -->
      <div id="messageList" class="flex-1 overflow-auto p-4" ref="messageListContainer">
        <MessageBubble
          v-for="msg in messages"
          :key="msg.messageId"
          :senderName="lookupUsername(msg.senderId)"
          :encryptedMessage="msg.encryptedMessage"
          :createdAt="msg.createdAt"
        />
      </div>

      <!-- 輸入框 + 發送按鈕 -->
      <div class="flex items-center bg-white border-t border-gray-300 p-4 space-x-2">
        <input
          v-model="newMessage"
          type="text"
          placeholder="輸入訊息..."
          class="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-green-400"
          @keydown.enter="onSend"
        />
        <button
          @click="onSend"
          class="bg-green-500 text-white rounded-md px-4 py-2 hover:bg-green-600"
        >
          發送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import MessageBubble from './MessageBubble.vue'
import { sendMessageToRoom } from '@/api.js' 

// props: 全部從 ChatLayout 透下來
const props = defineProps({
  group:      { type: Object, default: null },
  messages:   { type: Array,  default: () => [] }
})

// emit: 這一層只會叫作 'sendMessage'
const emit = defineEmits(['sendMessage'])

const newMessage = ref('')
const messageListContainer = ref(null)

const currentUserId = localStorage.getItem('currentUserId') || 'unknown-user'

// helper：把 senderId 換成對應的 username
function lookupUsername(userId) {
  if (!props.group || !props.group.members) return userId
  const someone = props.group.members.find(m => m.userId === userId)
  return someone ? someone.username : userId
}

async function onSend() {
  const text = newMessage.value.trim()
  if (!text || !props.group) return

  // **注意：** 這邊先用 emit，一律交給上層去處理「呼叫 API 並 push 回傳結果」
  emit('sendMessage', text)

  newMessage.value = ''
  nextTick(() => {
    const el = messageListContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// 當外部 props.messages 有新訊息時自動捲底
watch(
  () => props.messages.length,
  () => {
    nextTick(() => {
      const el = messageListContainer.value
      if (el) el.scrollTop = el.scrollHeight
    })
  }
)
</script>
