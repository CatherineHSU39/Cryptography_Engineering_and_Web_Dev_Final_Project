<script setup>
defineProps({
  msg: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <div class="greetings">
    <h1 class="text-[var(--color-primary)]">{{ msg }}</h1>
    <h3 class="text-[var(--color-text)]">
      You’ve successfully created a project with
      <a href="https://vite.dev/" target="_blank" rel="noopener" class="text-[var(--color-link-muted)] hover:underline">Vite</a>
      +
      <a href="https://vuejs.org/" target="_blank" rel="noopener" class="text-[var(--color-link-muted)] hover:underline">Vue 3</a>.
    </h3>
  </div>
</template>

<style scoped>
h1 {
  font-weight: 500;
  font-size: 2.6rem;
  position: relative;
  top: -10px;
}

h3 {
  font-size: 1.2rem;
}

.greetings h1,
.greetings h3 {
  text-align: center;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
