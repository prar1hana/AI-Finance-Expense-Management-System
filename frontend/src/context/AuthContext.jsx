import { createContext, useState, useEffect, useCallback } from 'react'
import { login as loginApi, register as registerApi } from '../api/auth'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const user = localStorage.getItem('user')
    if (token && user) {
      try { setCurrentUser(JSON.parse(user)) } catch { /* ignore */ }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    const { data } = await loginApi({ email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ id: data.userId, name: data.name, email: data.email }))
    setCurrentUser({ id: data.userId, name: data.name, email: data.email })
    return data
  }, [])

  const register = useCallback(async (name, email, password) => {
    const { data } = await registerApi({ name, email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ id: data.userId, name: data.name, email: data.email }))
    setCurrentUser({ id: data.userId, name: data.name, email: data.email })
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setCurrentUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ currentUser, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
