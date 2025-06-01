<!-- src/components/Sidebar.vue -->
<template>
  <div class="h-full flex flex-col bg-white">
    <div class="px-4 py-4 border-b border-gray-200">
      <h2 class="text-xl font-semibold">聊天室清單</h2>
    </div>
    <ul class="flex-1 overflow-auto">
      <li
        v-for="group in groups"
        :key="group.id"
        @click="selectGroup(group.id)"
        class="px-4 py-3 cursor-pointer hover:bg-gray-100 flex items-center"
        :class="{ 'bg-gray-200 font-semibold': group.id === selectedGroupId }"
      >
        <span>{{ group.name }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
const props = defineProps({
  // 從 ChatLayout 傳進來的 groups (Array<{id,name}>)
  groups: {
    type: Array,
    required: true
  },
  // 目前被選到的 groupId (String or null)
  selectedGroupId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['selectGroup'])
function selectGroup(id) {
  emit('selectGroup', id)
}
</script>