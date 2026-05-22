import api from './api'

const BASE = '/api/alerts'

export const alertService = {
  async getAll(pending = false) {
    const { data } = await api.get(BASE, { params: { pending } })
    return data
  },
  async getById(id)      { const { data } = await api.get(`${BASE}/${id}`);          return data },
  async create(dto)      { const { data } = await api.post(BASE, dto);                return data },
  async updateStatus(id, status) {
    const { data } = await api.put(`${BASE}/${id}/status`, null, { params: { status } })
    return data
  },
  async remove(id)       { await api.delete(`${BASE}/${id}`) }
}
