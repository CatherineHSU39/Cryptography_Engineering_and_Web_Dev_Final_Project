<!-- src/views/Chat.vue -->
<template>
  <div class="h-full w-full">
    <div v-if="isLoadingGroups" class="h-full flex items-center justify-center text-gray-500">
      <p>正在載入聊天室列表⋯⋯</p>
    </div>
    <div v-else class="h-full">
      <ChatLayout
        :groups="groupList"
        :selectedGroupId="selectedGroupId"
        :selectedGroupObject="selectedGroupObject"
        :messages="selectedGroupObject?.messages || []"
        @onSelectGroup="handleSelectGroup"
        @onSendMessage="handleSendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ChatLayout from '../components/ChatLayout.vue'
import { getAllGroups, getGroupById, sendMessageToRoom } from '@/api.js'

const groupList = ref([])
const selectedGroupId = ref(null)
const selectedGroupObject = ref(null)
const isLoadingGroups = ref(true)
const isLoadingMessages = ref(false)

onMounted(async () => {
  try {
    // mock data
    groupList.value = [
      { id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', name: 'General' },
      { id: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', name: 'Random' },
      { id: 'cccccccc-cccc-4ccc-8ccc-cccccccccccc', name: 'Vue Fans' }
    ]
    // const all = await getAllGroups()
    // groupList.value = all
  } catch (e) {
    console.error('載入群組清單失敗', e)
  } finally {
    isLoadingGroups.value = false
  }
})

async function handleSelectGroup(groupId) {
  selectedGroupId.value = groupId
  isLoadingMessages.value = true
  try {
    // mock data
const detail = {
  'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa': {
    id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
    name: 'General',
    members: [
      { userId: 'u1111111-aaaa-1111-aaaa-111111111111', username: 'Alice' },
      { userId: 'u2222222-bbbb-2222-bbbb-222222222222', username: 'Bob' }
    ],
    messages: [
      {
        messageId: 'm0000001-aaaa-0001-aaaa-000000000001',
        groupId: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        senderId: 'u1111111-aaaa-1111-aaaa-111111111111',
        encryptedMessage: '嗨，Bob，你好嗎？',
        createdAt: '2025-06-01T08:00:00.000Z'
      },
      {
        messageId: 'm0000002-bbbb-0002-bbbb-000000000002',
        groupId: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        senderId: 'u2222222-bbbb-2222-bbbb-222222222222',
        encryptedMessage: '我很好，Alice，謝謝！',
        createdAt: '2025-06-01T08:01:00.000Z'
      }
    ]
  },
  'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb': {
    id: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
    name: 'Random',
    members: [
      { userId: 'u3333333-cccc-3333-cccc-333333333333', username: 'Carol' },
      { userId: 'u4444444-dddd-4444-dddd-444444444444', username: 'Dave' }
    ],
    messages: [
      {
        messageId: 'm0000003-cccc-0003-cccc-000000000003',
        groupId: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
        senderId: 'u3333333-cccc-3333-cccc-333333333333',
        encryptedMessage: 'Random 群組第一則訊息',
        createdAt: '2025-06-02T09:00:00.000Z'
      }
    ]
  },
  'cccccccc-cccc-4ccc-8ccc-cccccccccccc': {
    id: 'cccccccc-cccc-4ccc-8ccc-cccccccccccc',
    name: 'Vue Fans',
    members: [
      { userId: 'u5555555-eeee-5555-eeee-555555555555', username: 'Eve' }
    ],
    messages: [
      {
        messageId: 'm0000004-eeee-0004-eeee-000000000004',
        groupId: 'cccccccc-cccc-4ccc-8ccc-cccccccccccc',
        senderId: 'u5555555-eeee-5555-eeee-555555555555',
        encryptedMessage: '歡迎加入 Vue Fans！',
        createdAt: '2025-06-03T10:00:00.000Z'
      }
    ]
  }
}
    // const detail = await getGroupById(groupId)
    selectedGroupObject.value = detail[groupId]
  } catch (e) {
    console.error(`載入群組 ${groupId} 詳細資訊失敗`, e)
    selectedGroupObject.value = null
  } finally {
    isLoadingMessages.value = false
  }
}

async function handleSendMessage(text) {
  // currentUserId 你要在登入那邊存到 localStorage，或用 Vuex/Pinia 管理
  const currentUserId = localStorage.getItem('currentUserId') || ''
  if (!selectedGroupId.value || !text.trim()) return

  try {
    // 呼叫 POST /app/messages
    const newMsg = await sendMessageToRoom({
      senderId: currentUserId,
      groupId: selectedGroupId.value,
      encryptedMessage: text.trim()
    })
    // newMsg = { messageId, groupId, senderId, encryptedMessage, createdAt }

    // 把最新回傳的這筆 message push 到現有的 messages 陣列裡
    selectedGroupObject.value.messages.push(newMsg)
  } catch (e) {
    console.error('傳送訊息失敗', e)
  }
}
</script>
