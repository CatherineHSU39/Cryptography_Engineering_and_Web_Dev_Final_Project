<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 bg-[var(--color-background-mute)]/60 backdrop-blur-sm flex items-center justify-center z-50"
      @click.self="close"
    >
      <div class="bg-[var(--color-background)] w-[90%] max-w-md rounded-xl shadow-xl p-6 text-[var(--color-text)]">
        <h2 class="text-xl font-semibold mb-4">個人資料</h2>

        <p><strong>ID：</strong>{{ userId }}</p>

        <div class="flex justify-between items-center mb-2">
          <p><strong>用戶名稱：</strong>{{ username }}</p>
          <button @click="showEditOverlay = true" class="text-sm text-[var(--color-link-primary)] hover:underline">
            編輯
          </button>
        </div>

        <!-- Change Password Button -->
        <button
            @click="showPasswordOverlay = true"
            class="mt-4 w-full bg-[var(--color-primary)] text-white rounded p-2 hover:bg-[var(--color-primary-hover)]"
            >
            變更密碼
        </button>

        <button
          @click="logout"
          class="mt-6 w-full bg-red-500 text-white rounded p-2 hover:bg-red-600"
        >
          登出
        </button>
      </div>
    </div>
  </transition>

  <!-- Change Username Overlay -->
  <transition name="fade">
    <div
      v-if="showEditOverlay"
      class="fixed inset-0 bg-[var(--color-background-mute)]/60 backdrop-blur-sm flex items-center justify-center z-60"
      @click.self="showEditOverlay = false"
    >
      <div class="bg-[var(--color-background)] w-[90%] max-w-md rounded-xl shadow-xl p-6 text-[var(--color-text)]">
        <h2 class="text-lg font-semibold mb-4">變更用戶名稱</h2>

        <input
          v-model="newUsername"
          placeholder="新的用戶名稱"
          class="w-full mb-3 border border-[var(--color-border)] rounded p-2 bg-transparent text-[var(--color-text)]"
        />
        <input
          v-model="password"
          type="password"
          placeholder="密碼以驗證身份"
          class="w-full mb-4 border border-[var(--color-border)] rounded p-2 bg-transparent text-[var(--color-text)]"
        />

        <div class="flex justify-end space-x-2">
          <button
            @click="saveUsername"
            class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            儲存
          </button>
        </div>
      </div>
    </div>
  </transition>

  <!-- Change Password Overlay -->
    <transition name="fade">
    <div
        v-if="showPasswordOverlay"
        class="fixed inset-0 bg-[var(--color-background-mute)]/60 backdrop-blur-sm flex items-center justify-center z-60"
        @click.self="showPasswordOverlay = false"
    >
        <div class="bg-[var(--color-background)] w-[90%] max-w-md rounded-xl shadow-xl p-6 text-[var(--color-text)]">
        <h2 class="text-lg font-semibold mb-4">變更密碼</h2>

        <input
            v-model="currentPassword"
            type="password"
            placeholder="目前密碼"
            class="w-full mb-3 border border-[var(--color-border)] rounded p-2 bg-transparent text-[var(--color-text)]"
        />
        <input
            v-model="newPassword"
            type="password"
            placeholder="新密碼"
            class="w-full mb-3 border border-[var(--color-border)] rounded p-2 bg-transparent text-[var(--color-text)]"
        />
        <input
            v-model="confirmPassword"
            type="password"
            placeholder="再次輸入新密碼"
            class="w-full mb-4 border border-[var(--color-border)] rounded p-2 bg-transparent text-[var(--color-text)]"
        />

        <div class="flex justify-end space-x-2">
            <button
            @click="savePassword"
            class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
            儲存
            </button>
        </div>
        </div>
    </div>
    </transition>

</template>


<script setup>
import { useRouter } from 'vue-router';
import { defineProps, defineEmits, ref, watch } from 'vue';
import { useProfileStore } from '@/stores/useProfileStore';

const props = defineProps({
  visible: Boolean,
  username: String,
  userId: String,
});

const emit = defineEmits(['close']);
const router = useRouter();
const profile = useProfileStore();

const showPasswordOverlay = ref(false);
const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');

const showEditOverlay = ref(false);
const newUsername = ref('');
const password = ref('');

// Automatically set default username when edit overlay is opened
watch(showEditOverlay, (showing) => {
  if (showing) {
    newUsername.value = props.username;
    password.value = '';
  }
});

const logout = () => {
  profile.logout();
  emit('close');
  router.push('/');
};

const close = () => emit('close');

const saveUsername = async () => {
  if (!newUsername.value.trim() || !password.value.trim()) {
    alert('請輸入新用戶名稱和密碼');
    return;
  }

  try {
    await profile.updateUser(newUsername.value.trim(), password.value.trim());
    alert('用戶名稱已更新');
    showEditOverlay.value = false;
  } catch (err) {
    alert('更新失敗：' + err.message);
  }
};

const savePassword = async () => {
    await profile.updatePassword(
      currentPassword.value.trim(),
      newPassword.value.trim(),
      confirmPassword.value.trim()
    );
    showPasswordOverlay.value = false;
    currentPassword.value = '';
    newPassword.value = '';
    confirmPassword.value = '';
};


</script>



<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
