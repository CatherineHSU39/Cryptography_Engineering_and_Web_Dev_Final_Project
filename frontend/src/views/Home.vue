<!-- src/views/Home.vue -->
<template>
  <div class="flex items-center justify-center h-full w-full bg-[var(--color-background-mute)]">
    <div class="max-w-2xl w-full p-6 bg-[var(--color-background)] rounded-lg shadow">
      <h1 class="text-3xl font-bold mb-4 text-center text-[var(--color-heading)]">
        歡迎來到 My Chat App
      </h1>
      <p class="text-center text-[var(--color-text)]">
        請先註冊或登入，然後進入聊天室開始聊天。
      </p>
      <div class="mt-6 flex justify-center space-x-4">
        <router-link
          to="/register"
          class="bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white px-4 py-2 rounded transition-colors"
        >
          註冊
        </router-link>
        <router-link
          to="/login"
          class="bg-[var(--color-link-primary)] hover:bg-[var(--color-link-muted)] text-white px-4 py-2 rounded transition-colors"
        >
          登入
        </router-link>
      </div>
    </div>
  </div>
</template>
