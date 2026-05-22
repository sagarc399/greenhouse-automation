import api from './api'

const BASE = '/api/sensor-readings'

export const sensorReadingService = {
  async getBySensor(sensorId, page = 0, size = 20) {
    const { data } = await api.get(BASE, { params: { sensorId, page, size } })
    return data
  },
  async getById(id)  { const { data } = await api.get(`${BASE}/${id}`);  return data },
  async create(dto)  { const { data } = await api.post(BASE, dto);        return data },
  async remove(id)   { await api.delete(`${BASE}/${id}`) }
}
