<!-- src/views/Profile.vue -->
<template>
  <!-- 1. 外層容器：佔滿整個 main 的寬度與高度 -->
  <div class="flex items-center justify-center h-full w-full">
    <!-- 2. 次外層：左右保留一點 padding -->
    <div class="px-4 w-full flex justify-center">
      <!-- 3. 真正的卡片：限制 max-w-md，手機則自動縮滿 -->
      <div class="max-w-md w-full bg-white rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-bold mb-4 text-center">我的資料</h2>
        <div class="space-y-3">
          <div>
            <span class="font-semibold">Email：</span>
            <span>{{ email }}</span>
          </div>
          <div>
            <span class="font-semibold">使用者 ID：</span>
            <span>#{{ userId }}</span>
          </div>
        </div>
        <button
          @click="logout"
          class="mt-6 w-full bg-red-500 hover:bg-red-600 text-white py-2 rounded-md transition-colors"
        >
          登出
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// 假設你在登入時把 email 和 userId 放到 localStorage
const email = ref(localStorage.getItem('email') || 'demo@example.com')
const userId = ref(localStorage.getItem('userId') || '123456')

function logout() {
  localStorage.removeItem('jwt')
  localStorage.removeItem('email')
  localStorage.removeItem('userId')
  router.push('/login')
}
</script>
