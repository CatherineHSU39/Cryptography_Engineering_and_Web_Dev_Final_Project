<template>
  <div class="flex items-center justify-center h-full w-full">
    <div class="px-4 w-full flex justify-center">
      <div class="max-w-md w-full bg-[var(--color-background)] rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-bold mb-6 text-center text-[var(--color-heading)]">註冊</h2>

        <form @submit.prevent="onSubmit">
          <div class="mb-4">
            <label class="block text-[var(--color-text)] mb-1" for="username">Username</label>
            <input
              v-model="username"
              id="username"
              type="username"
              placeholder="請輸入名稱"
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
              required
            />
          </div>

          <div class="mb-4">
            <label class="block text-[var(--color-text)] mb-1" for="password">Password</label>
            <input
              v-model="password"
              id="password"
              type="password"
              placeholder="請輸入密碼"
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
              required
            />
          </div>

          <div class="mb-6">
            <label class="block text-[var(--color-text)] mb-1" for="confirm">Confirm Password</label>
            <input
              v-model="confirm"
              id="confirm"
              type="password"
              placeholder="請再次輸入密碼"
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
              required
            />
          </div>

          <button
            type="submit"
            class="w-full bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold py-2 rounded-md transition-colors"
          >
            註冊
          </button>

          <p class="text-sm text-center text-[var(--color-text)] mt-4">
            已經有帳號？
            <router-link to="/login" class="text-[var(--color-link-primary)] hover:underline">
              登入
            </router-link>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useRegister } from '@/logic/useRegister';
import { useEncryptionStore } from '@/stores/useEncryptionStore';

const username = ref('')
const password = ref('')
const confirm = ref('')

const { handleRegister } = useRegister();
const encryption = useEncryptionStore();

const onSubmit = async () => {
  await handleRegister(username.value, password.value, confirm.value);
};

</script>