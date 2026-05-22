import api from './api'

export const dashboardService = {
  async getDashboard() {
    const { data } = await api.get('/api/dashboard')
    return data
  }
}
