import api from './axios'

export const categorizeExpense = (description) =>
  api.post('/ai/categorize', { description })

export const chatWithAI = (message, conversationHistory = []) =>
  api.post('/ai/chat', { message, conversationHistory })
