import api from './axios'

export const getIncomes = (params) => api.get('/incomes', { params })
export const createIncome = (data) => api.post('/incomes', data)
export const updateIncome = (id, data) => api.put(`/incomes/${id}`, data)
export const deleteIncome = (id) => api.delete(`/incomes/${id}`)
