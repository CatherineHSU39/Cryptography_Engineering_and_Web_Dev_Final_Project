<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 bg-[var(--color-background-mute)]/60 backdrop-blur-sm flex items-center justify-center z-50"
      @click.self="close"
    >
      <div class="bg-[var(--color-background)] w-[90%] max-w-md rounded-xl shadow-xl p-6">
        <!-- Tabs -->
        <div class="flex justify-between mb-4 border-b border-[var(--color-border)]">
          <button :class="tab === 'info' ? activeTabClass : tabClass" @click="tab = 'info'">
            群組資訊
          </button>
          <button :class="tab === 'members' ? activeTabClass : tabClass" @click="tab = 'members'">
            成員管理
          </button>
        </div>

        <!-- Info Tab -->
        <div v-if="tab === 'info'">
          <input
            v-model="groupName"
            class="w-full border border-[var(--color-border)] rounded p-2 mb-4 bg-transparent text-[var(--color-text)]"
            placeholder="群組名稱"
          />
          <div class="flex justify-between mb-2">
            <button class="text-[var(--color-danger)] hover:text-[var(--color-danger-hover)]" @click="onDelete">
              刪除群組
            </button>
            <button class="text-[var(--color-text)] opacity-80 hover:opacity-100" @click="onLeave">
              離開群組
            </button>
            <button class="bg-[var(--color-link-primary)] hover:bg-[var(--color-link-muted)] text-white px-4 py-2 rounded" @click="onUpdate">
              更新
            </button>
          </div>
        </div>

        <!-- Members Tab -->
        <div v-else>
          <div class="mb-2 font-medium text-[var(--color-text)]">已加入成員：</div>
          <ul class="space-y-1 mb-4 max-h-40 overflow-y-auto text-[var(--color-text)]">
            <li
              v-for="member in group?.members || []"
              :key="member.userId"
              class="flex justify-between"
            >
              <span>{{ member.username }}</span>
              <button
                v-if="member.userId !== profile.currentUserId"
                @click="removeMember(member.userId)"
                class="text-[var(--color-danger)] text-sm hover:text-[var(--color-danger-hover)]"
              >
                移除
              </button>
            </li>
          </ul>
          <div class="mb-2 font-medium text-[var(--color-text)]">新增成員：</div>
          <select
            v-model="selectedUserToAdd"
            class="w-full border border-[var(--color-border)] p-2 rounded bg-transparent text-[var(--color-text)]"
          >
            <option disabled value="">選擇使用者</option>
            <option
              v-for="user in availableUsers"
              :key="user.id"
              :value="user.id"
              class="text-black dark:text-white"
            >
              {{ user.username }}
            </option>
          </select>
          <button
            class="mt-2 bg-[var(--color-success)] hover:bg-[var(--color-primary-hover)] text-white px-4 py-2 rounded"
            @click="addMember"
          >
            加入
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<script setup>
import { ref, watch, computed } from 'vue';
import { useChatStore } from '@/stores/useChatStore';
import { useProfileStore } from '@/stores/useProfileStore';

const props = defineProps({
  visible: Boolean,
  groupId: String
});
const emit = defineEmits(['close']);

const chat = useChatStore();
const profile = useProfileStore();
const tab = ref('info');
const groupName = ref('');
const selectedUserToAdd = ref('');
const availableUsers = ref([]);

const group = computed(() => chat.groupList.find(g => g.id === props.groupId));

watch(() => props.groupId, async (id) => {
  if (id) {
    tab.value = 'info';
    groupName.value = group.value?.name || '';

    const allUsers = await chat.getAllUsers();
    const memberIds = new Set(group.value?.members.map(m => m.userId));
    availableUsers.value = allUsers.filter(user => !memberIds.has(user.id));
  }
});

function close() {
  emit('close');
}

function onUpdate() {
  chat.updateGroup({ groupId: props.groupId, name: groupName.value });
  close();
}

async function onDelete() {
  await chat.deleteGroup(props.groupId);
  close();
}

async function onLeave() {
  await chat.leaveGroup(props.groupId);
  close();
}

function removeMember(userId) {
  chat.removeMember(props.groupId, userId);
}

function addMember() {
  if (selectedUserToAdd.value) {
    chat.addMember(props.groupId, [selectedUserToAdd.value]);
    selectedUserToAdd.value = '';
  }
}

const tabClass = 'px-4 py-2 text-[var(--color-text)]';
const activeTabClass = 'px-4 py-2 border-b-2 border-[var(--color-link-primary)] text-[var(--color-link-primary)] font-semibold';
</script>
