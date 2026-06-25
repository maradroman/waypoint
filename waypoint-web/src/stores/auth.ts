import { create } from 'zustand'
import { api, setAccessToken } from '@/lib/api'
import type { AuthResponse, User } from '@/types/api'

// Deduplicate concurrent refreshAuth calls (e.g. React Strict Mode double-mount)
let refreshPromise: Promise<void> | null = null

interface AuthState {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, name: string) => Promise<void>
  logout: () => void
  refreshAuth: () => Promise<void>
  setUser: (user: User) => void
}

export const useAuth = create<AuthState>((set) => ({
  user: null,
  isLoading: true,
  isAuthenticated: false,

  login: async (email, password) => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password })
    setAccessToken(res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)
    set({ user: res.user, isAuthenticated: true, isLoading: false })
  },

  register: async (email, password, name) => {
    const res = await api.post<AuthResponse>('/auth/register', {
      email,
      password,
      name,
    })
    setAccessToken(res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)
    set({ user: res.user, isAuthenticated: true, isLoading: false })
  },

  logout: () => {
    setAccessToken(null)
    localStorage.removeItem('refreshToken')
    set({ user: null, isAuthenticated: false, isLoading: false })
  },

  refreshAuth: async () => {
    // Deduplicate concurrent calls (React Strict Mode double-mount, etc.)
    if (refreshPromise) return refreshPromise

    const rt = localStorage.getItem('refreshToken')
    if (!rt) {
      set({ isLoading: false })
      return
    }

    refreshPromise = (async () => {
      try {
        const res = await api.post<AuthResponse>('/auth/refresh', {
          refreshToken: rt,
        })
        setAccessToken(res.accessToken)
        localStorage.setItem('refreshToken', res.refreshToken)
        set({ user: res.user, isAuthenticated: true, isLoading: false })
      } catch {
        localStorage.removeItem('refreshToken')
        set({ user: null, isAuthenticated: false, isLoading: false })
      } finally {
        refreshPromise = null
      }
    })()

    return refreshPromise
  },

  setUser: (user) => set({ user }),
}))
