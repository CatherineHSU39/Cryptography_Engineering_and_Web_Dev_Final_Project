<template>
  <div class="flex flex-col h-screen bg-[var(--color-background)]">
    <header class="bg-[var(--color-background-soft)]">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="text-xl font-semibold text-[var(--color-heading)]">
          My Chat App
        </div>

        <nav class="flex items-center space-x-4">
          <template v-if="!isLoggedIn">
            <router-link to="/register" class="text-[var(--color-link-primary)] hover:underline">Register</router-link>
            <router-link to="/login" class="text-[var(--color-link-primary)] hover:underline">Login</router-link>
          </template>

          <template v-else>
            <button @click="toggleProfile" class="text-[var(--color-text)] hover:underline">
              {{ profile.currentUsername || 'Profile' }}
            </button>
          </template>
        </nav>
      </div>
    </header>

    <main class="flex-1 w-full bg-[var(--color-background-mute)] overflow-auto">
      <router-view />
    </main>

    <!-- Profile Modal -->
    <ProfileOverlay
      :visible="showProfile"
      :username="profile.currentUsername"
      :userId="profile.currentUserId"
      @close="showProfile = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProfileOverlay from '@/components/ProfileOverlay.vue';
import { useProfileStore } from '@/stores/useProfileStore';

const router = useRouter();
const profile = useProfileStore();

const showProfile = ref(false);

const isLoggedIn = computed(() => profile.isLoggedIn);

function toggleProfile() {
  showProfile.value = !showProfile.value;
}
</script>
