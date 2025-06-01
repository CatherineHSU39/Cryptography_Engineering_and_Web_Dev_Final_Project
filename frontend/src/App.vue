<template>
  <div class="flex flex-col h-screen">
    <!-- ===== Header ===== -->
    <header class="bg-gray-200">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <!-- 應用名稱 -->
        <div class="text-xl font-semibold">My Chat App</div>

        <!-- 導航連結容器：使用 flex + space-x-4 來自動在連結間加水平間距 -->
        <nav class="flex items-center space-x-4">
          <!-- Register 與 Login 永遠顯示 -->
          <router-link
            to="/register"
            class="text-green-600 hover:underline"
          >
            Register
          </router-link>
          <router-link
            to="/login"
            class="text-blue-600 hover:underline"
          >
            Login
          </router-link>

          <!-- 只有登入後才顯示 Chat 和 Profile -->
          <template v-if="isLoggedIn">
            <router-link
              to="/chat"
              class="text-gray-800 hover:underline"
            >
              Chat
            </router-link>
            <router-link
              to="/profile"
              class="text-gray-800 hover:underline"
            >
              Profile
            </router-link>
            <!-- 登出按鈕 -->
            <button
              @click="logout"
              class="text-red-600 hover:underline"
            >
              Logout
            </button>
          </template>
        </nav>
      </div>
    </header>

    <!-- ===== Main ===== -->
    <main class="flex-1 w-full bg-gray-100 overflow-auto">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 1. 管理是否已登入 (由 localStorage 拿 JWT 來判斷)
const router = useRouter()
const token = ref(null)

onMounted(() => {
  token.value = localStorage.getItem('jwt')
})

const isLoggedIn = computed(() => {
  return !!token.value
})

// 2. 登出函式：清除 JWT，導回 /login
function logout() {
  localStorage.removeItem('jwt')
  localStorage.removeItem('email')
  token.value = null
  router.push('/login')
}
</script>