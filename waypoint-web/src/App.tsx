import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useEffect } from 'react'
import { useAuth } from '@/stores/auth'
import { Toaster } from '@/components/ui/sonner'
import AppLayout from '@/components/layout'
import LoginPage from '@/pages/login'
import RegisterPage from '@/pages/register'
import DashboardPage from '@/pages/dashboard'
import GoalsPage from '@/pages/goals'
import GoalDetailPage from '@/pages/goal-detail'
import SettingsPage from '@/pages/settings'
import AdminPage from '@/pages/admin'
import { Skeleton } from '@/components/ui/skeleton'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Skeleton className="h-8 w-48" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <AppLayout>{children}</AppLayout>
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Skeleton className="h-8 w-48" />
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/goals" replace />
  }

  return <>{children}</>
}

function AuthGate({ children }: { children: React.ReactNode }) {
  const refreshAuth = useAuth((s) => s.refreshAuth)
  const isAuthenticated = useAuth((s) => s.isAuthenticated)
  const isLoading = useAuth((s) => s.isLoading)

  useEffect(() => {
    refreshAuth()
  }, [refreshAuth])

  if (isLoading && !isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Skeleton className="h-8 w-48" />
      </div>
    )
  }

  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthGate>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/goals"
            element={
              <ProtectedRoute>
                <GoalsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/goals/:goalId"
            element={
              <ProtectedRoute>
                <GoalDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <SettingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
        <Toaster />
      </AuthGate>
    </BrowserRouter>
  )
}
