<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('greenhouse.title') }}</h1>
      <button class="btn-primary" @click="openCreate">+ {{ $t('greenhouse.new') }}</button>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.name') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.location') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.zones') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="g in store.items"
            :key="g.id"
            class="border-b border-gray-100 hover:bg-gray-50"
          >
            <td class="px-6 py-4 font-medium">{{ g.name }}</td>
            <td class="px-6 py-4 text-gray-500">{{ g.location || '—' }}</td>
            <td class="px-6 py-4">{{ g.zoneCount ?? '—' }}</td>
            <td class="px-6 py-4 text-right space-x-2">
              <button class="btn-secondary text-xs" @click="openEdit(g)">{{ $t('common.edit') }}</button>
              <button class="btn-danger text-xs"    @click="confirmDelete(g)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!store.items.length">
            <td colspan="4" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">
          {{ editingItem ? $t('greenhouse.edit') : $t('greenhouse.new') }}
        </h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('common.location') }}</label>
              <input v-model="form.location" class="form-input" />
            </div>
            <div>
              <label class="form-label">{{ $t('common.description') }}</label>
              <textarea v-model="form.description" class="form-input" rows="3" />
            </div>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button type="button" class="btn-secondary" @click="showModal = false">{{ $t('common.cancel') }}</button>
            <button type="submit" class="btn-primary">{{ $t('common.save') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useGreenhouseStore } from '@/stores/greenhouse'

const { t } = useI18n()
const store = useGreenhouseStore()
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const form = ref({ name: '', location: '', description: '' })

onMounted(async () => {
  await store.fetchAll()
  loading.value = false
})

function openCreate() {
  editingItem.value = null
  form.value = { name: '', location: '', description: '' }
  showModal.value = true
}

function openEdit(item) {
  editingItem.value = item
  form.value = { name: item.name, location: item.location || '', description: item.description || '' }
  showModal.value = true
}

async function save() {
  if (editingItem.value) {
    await store.update(editingItem.value.id, form.value)
  } else {
    await store.create(form.value)
  }
  showModal.value = false
}

async function confirmDelete(item) {
  if (confirm(t('greenhouse.deleteConfirm'))) {
    await store.remove(item.id)
  }
}
</script>
