<!-- src/components/ChatWindow.vue -->
<script setup>
import { ref, nextTick, watch } from 'vue';
import MessageBubble from './MessageBubble.vue';
import { useChatStore } from '@/stores/useChatStore';
import { useProfileStore } from '@/stores/useProfileStore';

const chat = useChatStore();
const profile = useProfileStore();
const newMessage = ref('');
const messageListContainer = ref(null);

// Context menu state
const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  messageId: null,
  flip: false,
});


const editingMessage = ref({
  show: false,
  messageId: null,
  groupId: null,
  content: '',
});

function showContextMenu({ messageId, target }) {
  const bubbleRect = target.getBoundingClientRect();
  const container = messageListContainer.value;
  const containerRect = container.getBoundingClientRect();

  const scrollTop = container.scrollTop;
  const scrollBottom = scrollTop + container.clientHeight;

  const bubbleTopInContainer = target.offsetTop;
  const bubbleBottomInContainer = bubbleTopInContainer + target.offsetHeight;

  const menuHeight = 80;
  const willOverflow = bubbleBottomInContainer + menuHeight > scrollBottom;

  contextMenu.value = {
    show: true,
    x: target.offsetLeft + target.offsetWidth - 128, // align right
    y: willOverflow
      ? bubbleTopInContainer - menuHeight - 4  // flip up
      : bubbleBottomInContainer + 4,           // normal down
    messageId,
    flip: willOverflow,
  };
}



function hideContextMenu() {
  contextMenu.value.show = false;
}

function onEditMessage() {
  const messageId = contextMenu.value.messageId;
  hideContextMenu();

  for (const group of chat.groupList) {
    const message = group.messages.find((m) => m.messageId === messageId);
    if (message) {
      editingMessage.value = {
        show: true,
        messageId,
        groupId: group.id,
        content: message.content,
      };
      break;
    }
  }
}

async function onSaveEdit() {
  const { messageId, content } = editingMessage.value;
  try {
    await chat.updateMessage(messageId, content);
    editingMessage.value.show = false;
  } catch (err) {
    alert("Failed to update message: " + err.message);
  }
}

function onDeleteMessage() {
  chat.deleteMessage(contextMenu.value.messageId);
  hideContextMenu();
}

// Scroll to bottom
function scrollToBottom() {
  nextTick(() => {
    const el = messageListContainer.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

function handleScroll() {
  const el = messageListContainer.value;
  if (!el || !chat.selectedGroupObject) return;

  const isAtBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 10;
  if (isAtBottom) {
    chat.setReadAtIfNeeded(chat.selectedGroupObject.id);
  }
}


// Send message handler
async function onSend() {
  const text = newMessage.value.trim();
  if (!text || !chat.selectedGroupObject) return;
  await chat.sendMessageToRoom(text);
  newMessage.value = '';
  scrollToBottom();
}

// Resolve username from group members
function lookupUsername(userId) {
  const group = chat.selectedGroupObject;
  if (!group || !group.members) return userId;
  const member = group.members.find((m) => m.userId === userId);
  return member?.username || userId;
}

// Scroll on message count or group change
watch(
  () => chat.selectedGroupObject?.messages?.length,
  () => {
    scrollToBottom();
    if (chat.selectedGroupId) chat.setReadAtIfNeeded(chat.selectedGroupId);
  }
);

watch(() => chat.selectedGroupObject, scrollToBottom);
</script>

<template>
  <div class="flex flex-col h-full bg-[var(--color-background)]" @click="hideContextMenu">
    <div v-if="!chat.selectedGroupObject" class="flex-1 flex items-center justify-center text-[var(--color-text-soft)]">
      <p>請先從左側選擇一個聊天室</p>
    </div>

    <div v-else class="flex flex-col h-full relative">
      <div
        id="messageList"
        class="flex-1 overflow-auto p-4 relative"
        ref="messageListContainer"
        @scroll="handleScroll"
      >

        <div
          v-for="msg in chat.selectedGroupObject?.messages || []"
          :key="msg.messageId"
          :class="[
            'flex',
            msg.senderId === profile.currentUserId ? 'justify-end' : 'justify-start'
          ]"
        >
          <div class="max-w-[80%] sm:max-w-[66%]">
            <MessageBubble
              v-if="msg.senderId && msg.content && msg.createdAt"
              :senderName="lookupUsername(msg.senderId)"
              :encryptedMessage="msg.content"
              :createdAt="msg.createdAt"
              :updatedAt="msg.updatedAt"
              :isSelf="msg.senderId === profile.currentUserId"
              :senderId="msg.senderId"
              :messageId="msg.messageId"
              @contextmenu="showContextMenu"
            />
          </div>
        </div>

        <!-- Context Menu -->
        <div
          v-if="contextMenu.show"
          :style="{ top: `${contextMenu.y}px`, left: `${contextMenu.x}px` }"
          class="absolute w-32 bg-[var(--color-background-soft)] border border-[var(--color-border)] shadow-lg rounded p-2 z-50"
          :class="{ 'origin-bottom-right': contextMenu.flip, 'origin-top-right': !contextMenu.flip }"
        >
          <button class="block w-full text-left px-4 py-2 hover:bg-[var(--color-background-mute)]" @click="onEditMessage">編輯</button>
          <button class="block w-full text-left px-4 py-2 hover:bg-[var(--color-background-mute)]" @click="onDeleteMessage">刪除</button>
        </div>
      </div>

        <!-- Edit Modal -->
        <transition name="fade">
          <div
            v-if="editingMessage.show"
            class="fixed inset-0 bg-[var(--color-overlay-bg)] backdrop-blur-sm bg-opacity-60 flex items-center justify-center z-50"
            @click.self="editingMessage.show = false"
            >
            <div
              class="bg-[var(--color-background-soft)] text-[var(--color-text)] rounded-xl shadow-2xl p-6 w-[90%] max-w-md animate-fade-in"
            >

              <h2 class="text-lg font-semibold mb-2">編輯訊息</h2>
              <textarea
                v-model="editingMessage.content"
                class="w-full border border-[var(--color-border)] bg-transparent text-[var(--color-text)] rounded p-2 mb-4"
                rows="4"
              ></textarea>
              <div class="flex justify-end space-x-2">
                <button @click="onSaveEdit" class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600">儲存</button>
              </div>
            </div>
          </div>
        </transition>

      <div class="flex items-center bg-[var(--color-background-mute)] border-t border-[var(--color-border)] p-4 space-x-2">
        <input
          v-model="newMessage"
          type="text"
          placeholder="輸入訊息..."
          class="flex-1 border border-[var(--color-border)] bg-transparent text-[var(--color-text)] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-green-400"
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
