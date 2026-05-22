import api from './api'

const BASE = '/api/greenhouses'

export const greenhouseService = {
  async getAll()        { const { data } = await api.get(BASE);              return data },
  async getById(id)     { const { data } = await api.get(`${BASE}/${id}`);   return data },
  async create(dto)     { const { data } = await api.post(BASE, dto);         return data },
  async update(id, dto) { const { data } = await api.put(`${BASE}/${id}`, dto); return data },
  async remove(id)      { await api.delete(`${BASE}/${id}`) }
}
