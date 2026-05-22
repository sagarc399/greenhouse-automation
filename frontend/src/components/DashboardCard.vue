<template>
  <div class="card flex items-center gap-4">
    <div :class="['w-12 h-12 rounded-xl flex items-center justify-center text-2xl', color]">
      {{ icon }}
    </div>
    <div>
      <p class="text-2xl font-bold text-gray-900">{{ value }}</p>
      <p class="text-sm text-gray-500">{{ label }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [Number, String],
  icon: String,
  color: { type: String, default: 'bg-gray-100 text-gray-600' }
})
</script>
