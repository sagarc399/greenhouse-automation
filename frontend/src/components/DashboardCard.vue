<template>
  <div
    class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden
           hover:shadow-card-hover transition-shadow duration-200"
  >
    <!-- Colored top stripe -->
    <div class="h-1" :style="{ backgroundColor: borderColor }"></div>

    <div class="p-6">
      <div class="flex items-start justify-between gap-3">
        <!-- Value + label -->
        <div class="min-w-0">
          <p class="text-3xl font-bold text-[#1A1A1A] leading-tight tabular-nums">
            {{ value ?? '—' }}
          </p>
          <p class="text-sm text-[#555555] mt-1 leading-snug">{{ label }}</p>
        </div>
        <!-- Icon bubble -->
        <div
          class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl flex-shrink-0"
          :style="{ backgroundColor: iconBg }"
        >
          {{ icon }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label:       { type: String,           default: '' },
  value:       { type: [Number, String], default: null },
  icon:        { type: String,           default: '📊' },
  borderColor: { type: String,           default: '#66BB6A' },
  iconBg:      { type: String,           default: '#F1F8E9' }
})
</script>
