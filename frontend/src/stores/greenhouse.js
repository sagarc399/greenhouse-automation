import { defineStore } from 'pinia'
import { ref } from 'vue'
import { greenhouseService } from '@/services/greenhouseService'

/**
 * Pinia store for greenhouse CRUD state.
 */
export const useGreenhouseStore = defineStore('greenhouse', () => {
  const items = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      items.value = await greenhouseService.getAll()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function create(dto) {
    const created = await greenhouseService.create(dto)
    items.value.push(created)
    return created
  }

  async function update(id, dto) {
    const updated = await greenhouseService.update(id, dto)
    const idx = items.value.findIndex(g => g.id === id)
    if (idx !== -1) items.value[idx] = updated
    return updated
  }

  async function remove(id) {
    await greenhouseService.remove(id)
    items.value = items.value.filter(g => g.id !== id)
  }

  return { items, loading, error, fetchAll, create, update, remove }
})
