<template>
  <!-- 1. 最外層：這一層要 flex 置中，且撐滿父層的高與寬 -->
  <div class="flex items-center justify-center h-full w-full">
    <!--
      2. 次外層：只提供左右 1rem (px-4) 的 padding，
         但仍然 w-full 讓它橫向填滿「整個 main 的寬度」，
         再用 flex justify-center 將卡片置中。
    -->
    <div class="px-4 w-full flex justify-center">
      <!--
        3. 卡片層：用 max-w-md 限制最大寬度，桌機上不會超過 448px，
           手機寬度若 < 448px 時就自動縮到「整個可用寬度 (扣掉左右 px-4)」。
      -->
      <div class="max-w-md w-full bg-white rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-bold mb-6 text-center">登入</h2>

        <form @submit.prevent="submit">
          <div class="mb-4">
            <label class="block text-gray-700 mb-1" for="username">Username</label>
            <input
              v-model="username"
              id="username"
              type="username"
              placeholder="請輸入名稱"
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
            />
          </div>

          <div class="mb-6">
            <label class="block text-gray-700 mb-1" for="password">Password</label>
            <input
              v-model="password"
              id="password"
              type="password"
              placeholder="請輸入密碼"
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
            />
          </div>

          <button
            type="submit"
            class="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 rounded-md transition-colors"
          >
            登入
          </button>

          <p class="text-sm text-center text-gray-600 mt-4">
            還沒有帳號？
            <router-link to="/register" class="text-blue-500 hover:underline">
              註冊
            </router-link>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
    name: 'Login',
    data() {
        return {
            username: '',
            password: ''
        }
    },
    methods: {
        async handleLogin() {
            try {
                const res = authenticateUser(this.username, this.password);
                if (res.data && res.data.jwt) {
                    localStorage.setItem('jwt', res.data.jwt)
                }
                // Redirect after successful login
                this.$router.push('/chat')
            } catch (error) {
                console.error('Login failed:', error)
            }
        }
    }
}
</script>

