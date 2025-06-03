// src/logic/useRegister.js
import { AuthAPI } from "@/api/auth";
import { useRouter } from "vue-router";

export function useRegister() {
  const router = useRouter();

  const handleRegister = async (username, password, confirm) => {
    if (password !== confirm) {
      alert("密碼不一致");
      return;
    }
    try {
      await AuthAPI.register(username, password);
      alert(`成功註冊帳號: ${username}`);
      router.push("/login");
    } catch (err) {
      console.error("Register failed:", err);
      alert(err.message);
    }
  };

  return { handleRegister };
}
