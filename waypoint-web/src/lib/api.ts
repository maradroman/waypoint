import type { Goal } from '@/types/api'

const BASE_URL = import.meta.env.VITE_API_URL ?? (import.meta.env.DEV ? 'http://localhost:8080/api/v1' : '/api/v1')

let accessToken: string | null = null

// Map backend icon values to emojis
function mapIconToEmoji(icon: string | null | undefined): string | null {
  if (!icon) return null
  if (icon === 'target') return '🎯'
  return icon
}

// Transform goal objects to map icons
function transformGoal(goal: Goal): Goal {
  return {
    ...goal,
    icon: mapIconToEmoji(goal.icon)
  }
}

function transformResponse<T>(data: T): T {
  // Handle single goal
  if (data && typeof data === 'object' && 'icon' in data && 'title' in data) {
    return transformGoal(data as unknown as Goal) as unknown as T
  }

  // Handle array of goals
  if (Array.isArray(data) && data.length > 0 && 'icon' in data[0] && 'title' in data[0]) {
    return data.map(item => transformGoal(item as unknown as Goal)) as unknown as T
  }

  return data
}

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

class ApiError extends Error {
  status: number
  body: unknown

  constructor(status: number, body: unknown) {
    super(`API error: ${status}`)
    this.status = status
    this.body = body
  }
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  const parsed = await res.json().catch(() => null)

  if (!res.ok) {
    throw new ApiError(res.status, parsed)
  }

  if (res.status === 204) return undefined as T

  // Unwrap ResponseEnvelope: all backend success responses are { data: T, meta: {...} }
  if (parsed && typeof parsed === 'object' && 'data' in parsed) {
    return transformResponse(parsed.data as T)
  }

  return transformResponse(parsed as T)
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, body),
  delete: <T>(path: string) => request<T>('DELETE', path),
}

export function uploadWithProgress<T>(
  path: string,
  formData: FormData,
  onProgress: (percent: number) => void,
): Promise<T> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', `${BASE_URL}${path}`)

    if (accessToken) {
      xhr.setRequestHeader('Authorization', `Bearer ${accessToken}`)
    }

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    }

    xhr.onload = () => {
      const parsed = JSON.parse(xhr.responseText || '{}')
      if (xhr.status >= 200 && xhr.status < 300) {
        if (parsed && typeof parsed === 'object' && 'data' in parsed) {
          resolve(parsed.data as T)
        } else {
          resolve(parsed as T)
        }
      } else {
        reject(new ApiError(xhr.status, parsed))
      }
    }

    xhr.onerror = () => reject(new ApiError(0, null))
    xhr.send(formData)
  })
}

// Types
export interface PlannedFund {
  id: string
  goalId: string
  date: string // YYYY-MM-DD format
  amount: number // in cents
}

// Planned Funds API
export const plannedFunds = {
  list: (goalId: string) =>
    api.get<PlannedFund[]>(`/goals/${goalId}/planned-funds`),

  upsert: (goalId: string, date: string, amount: number) =>
    api.put<PlannedFund>(`/goals/${goalId}/planned-funds/${date}`, {
      date,
      amount,
    }),

  delete: (goalId: string, date: string) =>
    api.delete<void>(`/goals/${goalId}/planned-funds/${date}`),
}

export { ApiError }
