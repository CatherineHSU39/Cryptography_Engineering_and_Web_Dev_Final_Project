// src/stores/useProfileStore.js
import { defineStore } from "pinia";
import { ref } from "vue";
import { MyStatusAPI } from "@/api/app";
import { UserAPI, AuthAPI } from "@/api/auth";

export const useProfileStore = defineStore("profile", () => {
  const currentUserId = ref(localStorage.getItem("currentUserId") || null);
  const currentUsername = ref(localStorage.getItem("username") || null);
  const isLoggedIn = ref(!!localStorage.getItem("jwt"));

  const syncUser = async () => {
    await MyStatusAPI.syncUser();
    currentUserId.value = localStorage.getItem("currentUserId");
    currentUsername.value = localStorage.getItem("username");
    isLoggedIn.value = !!localStorage.getItem("jwt");
  };

  const updateUser = async (username, password) => {
    await UserAPI.updateUser(username, password);
    await syncUser();
  };

  const updatePassword = async (
    currentPassword,
    newPassword,
    confirmPassword
  ) => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      alert("請完整填寫所有欄位");
      return;
    }
    if (newPassword !== confirmPassword) {
      alert("新密碼與確認密碼不一致");
      return;
    }

    try {
      await updateUser(currentUsername.value, newPassword.trim());
      alert("密碼已更新");
    } catch (err) {
      alert("更新失敗：" + err.message);
    }
  };

  const logout = async () => {
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
    localStorage.removeItem("currentUserId");
    isLoggedIn.value = false;
    currentUserId.value = null;
    currentUsername.value = null;
  };

  const login = async (username, password) => {
    try {
      await AuthAPI.login(username, password);
      await syncUser();

      currentUserId.value = localStorage.getItem("currentUserId");
      currentUsername.value = localStorage.getItem("username");
      isLoggedIn.value = !!localStorage.getItem("jwt");

      return true; // ✅ success
    } catch (err) {
      console.error("Login failed:", err);
      alert(err.message);
      return false; // ❌ failed
    }
  };

  return {
    currentUserId,
    currentUsername,
    isLoggedIn,

    syncUser,
    updateUser,
    updatePassword,
    logout,
    login,
  };
});
