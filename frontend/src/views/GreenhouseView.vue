<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Page header ─────────────────────────────────────────── -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('greenhouse.title') }}</h1>
        <p class="text-sm text-[#555555] mt-0.5">{{ $t('common.name') }} · {{ $t('common.location') }}</p>
      </div>
      <button class="btn-primary" @click="openCreate">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        {{ $t('greenhouse.new') }}
      </button>
    </div>

    <!-- ── Loading ─────────────────────────────────────────────── -->
    <div v-if="loading" class="flex items-center justify-center py-24">
      <div class="w-10 h-10 border-4 border-greenhouse-200 border-t-greenhouse-600 rounded-full animate-spin"></div>
    </div>

    <!-- ── Table ───────────────────────────────────────────────── -->
    <div v-else class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ $t('common.name') }}</th>
            <th>{{ $t('common.location') }}</th>
            <th>{{ $t('common.zones') }}</th>
            <th>{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="g in store.items" :key="g.id">
            <td>
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-greenhouse-100 rounded-lg flex items-center justify-center text-base flex-shrink-0">🏠</div>
                <span class="font-semibold text-[#1A1A1A]">{{ g.name }}</span>
              </div>
            </td>
            <td class="text-[#555555]">{{ g.location || '—' }}</td>
            <td>
              <span class="badge-active">{{ g.zoneCount ?? 0 }} {{ $t('common.zones') }}</span>
            </td>
            <td>
              <div class="flex items-center justify-end gap-2">
                <button class="btn-secondary text-xs py-1.5 px-3" @click="openEdit(g)">{{ $t('common.edit') }}</button>
                <button class="btn-danger    text-xs py-1.5 px-3" @click="confirmDelete(g)">{{ $t('common.delete') }}</button>
              </div>
            </td>
          </tr>
          <!-- Empty state -->
          <tr v-if="!store.items.length">
            <td colspan="4" class="!py-16 text-center">
              <svg class="w-14 h-14 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
                <path d="M17 8C8 10 5.9 16.17 3.82 21.34L5.71 22l1-2.3A4.49 4.49 0 008 20C19 20 22 3 22 3c-1 2-8 2-8 2S5 5 5 11c0 3 2.5 5.5 5.5 5.5 2.5 0 4.5-2 4.5-2V8z"/>
              </svg>
              <p class="text-sm text-gray-400 font-medium">{{ $t('common.noData') }}</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── Create / Edit modal ─────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-md animate-fade-in">
          <!-- Modal header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-base font-semibold text-[#1A1A1A]">
              {{ editingItem ? $t('greenhouse.edit') : $t('greenhouse.new') }}
            </h2>
            <button class="btn-icon" @click="showModal = false">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <!-- Form -->
          <form @submit.prevent="save" class="p-6 space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" :placeholder="$t('common.name')" required />
            </div>
            <div>
              <label class="form-label">{{ $t('common.location') }}</label>
              <input v-model="form.location" class="form-input" :placeholder="$t('common.location')" />
            </div>
            <div>
              <label class="form-label">{{ $t('common.description') }}</label>
              <textarea v-model="form.description" class="form-textarea" rows="3" :placeholder="$t('common.description')" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" class="btn-secondary" @click="showModal = false">{{ $t('common.cancel') }}</button>
              <button type="submit" class="btn-primary">{{ $t('common.save') }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- ── Delete confirmation modal ──────────────────────────── -->
    <Teleport to="body">
      <div v-if="deleteTarget" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-sm p-6 animate-fade-in">
          <div class="flex items-start gap-4 mb-6">
            <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-[#C62828]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-[#1A1A1A]">{{ $t('common.confirm') }}</h3>
              <p class="text-sm text-[#555555] mt-1">{{ $t('greenhouse.deleteConfirm') }}</p>
            </div>
          </div>
          <div class="flex justify-end gap-3">
            <button class="btn-secondary" @click="deleteTarget = null">{{ $t('common.cancel') }}</button>
            <button class="btn-danger" @click="executeDelete">{{ $t('common.delete') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

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
const deleteTarget = ref(null)
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

function confirmDelete(item) {
  deleteTarget.value = item
}

async function executeDelete() {
  if (!deleteTarget.value) return
  await store.remove(deleteTarget.value.id)
  deleteTarget.value = null
}
</script>
