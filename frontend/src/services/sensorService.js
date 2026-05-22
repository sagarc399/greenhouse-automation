import api from './api'

const BASE = '/api/sensors'

export const sensorService = {
  async getAll(zoneId)  {
    const params = zoneId ? { zoneId } : {}
    const { data } = await api.get(BASE, { params })
    return data
  },
  async getById(id)     { const { data } = await api.get(`${BASE}/${id}`);      return data },
  async create(dto)     { const { data } = await api.post(BASE, dto);            return data },
  async update(id, dto) { const { data } = await api.put(`${BASE}/${id}`, dto);  return data },
  async remove(id)      { await api.delete(`${BASE}/${id}`) }
}
