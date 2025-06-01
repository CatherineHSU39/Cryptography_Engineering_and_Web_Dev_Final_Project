<!-- src/views/Home.vue -->
<template>
  <div class="flex items-center justify-center h-full w-full bg-gray-50">
    <div class="max-w-2xl w-full p-6 bg-white rounded-lg shadow">
      <h1 class="text-3xl font-bold mb-4 text-center">歡迎來到 My Chat App</h1>
      <p class="text-center text-gray-600">
        請先註冊或登入，然後進入聊天室開始聊天。
      </p>
      <div class="mt-6 flex justify-center space-x-4">
        <router-link to="/register" class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded">
          註冊
        </router-link>
        <router-link to="/login" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
          登入
        </router-link>
      </div>
    </div>
  </div>
</template>

