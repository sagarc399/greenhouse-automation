import api from './api'

const BASE = '/api/actuators'

export const actuatorService = {
  async getAll(zoneId)  {
    const params = zoneId ? { zoneId } : {}
    const { data } = await api.get(BASE, { params })
    return data
  },
  async getById(id)     { const { data } = await api.get(`${BASE}/${id}`);               return data },
  async create(dto)     { const { data } = await api.post(BASE, dto);                     return data },
  async update(id, dto) { const { data } = await api.put(`${BASE}/${id}`, dto);           return data },
  async setState(id, state) {
    const { data } = await api.patch(`${BASE}/${id}/state`, null, { params: { state } })
    return data
  },
  async remove(id)      { await api.delete(`${BASE}/${id}`) }
}
