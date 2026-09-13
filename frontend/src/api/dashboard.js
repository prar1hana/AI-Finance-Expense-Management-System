import api from './axios'

export const getDashboardSummary = (params) => api.get('/dashboard/summary', { params })
export const getMonthlySpending = (params) => api.get('/dashboard/monthly-spending', { params })
export const getRecentTransactions = (params) => api.get('/dashboard/recent-transactions', { params })
