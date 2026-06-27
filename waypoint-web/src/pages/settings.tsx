import { useState } from 'react'
import { useAuth } from '@/stores/auth'
import { useTranslation } from 'react-i18next'
import { api } from '@/lib/api'
import type { User } from '@/types/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'

const locales = [
  { value: 'en', label: 'English' },
  { value: 'pl', label: 'Polski' },
  { value: 'uk', label: 'Українська' },
]

const currencies = [
  { value: 'USD', label: 'USD ($)' },
  { value: 'EUR', label: 'EUR (€)' },
  { value: 'PLN', label: 'PLN (zł)' },
  { value: 'UAH', label: 'UAH (₴)' },
  { value: 'GBP', label: 'GBP (£)' },
]

export default function SettingsPage() {
  const { user, setUser, logout } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { t } = useTranslation()
  const [locale, setLocale] = useState(user?.locale ?? 'en')
  const [currency, setCurrency] = useState(user?.currency ?? 'USD')

  const updateMutation = useMutation({
    mutationFn: () =>
      api.patch('/auth/me', { locale, currency }),
    onSuccess: (data) => {
      setUser(data as User)
      queryClient.invalidateQueries()
    },
  })

  const exportMutation = useMutation({
    mutationFn: () => api.get('/goals'),
    onSuccess: (data) => {
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'waypoint-export.json'
      a.click()
      URL.revokeObjectURL(url)
    },
  })

  const handleReset = () => {
    if (confirm(t('settings.resetConfirm'))) {
      logout()
      navigate('/login')
    }
  }

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <h1 className="text-3xl font-bold tracking-tight">{t('settings.title')}</h1>

      <Card>
        <CardHeader>
          <CardTitle>{t('settings.profile')}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>{t('settings.name')}</Label>
            <Input value={user?.name ?? ''} disabled />
          </div>
          <div className="space-y-2">
            <Label>{t('settings.email')}</Label>
            <Input value={user?.email ?? ''} disabled />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t('settings.preferences')}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="locale">{t('settings.language')}</Label>
            <Select value={locale} onValueChange={setLocale}>
              <SelectTrigger id="locale">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {locales.map((l) => (
                  <SelectItem key={l.value} value={l.value}>{l.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="currency">{t('settings.currency')}</Label>
            <Select value={currency} onValueChange={setCurrency}>
              <SelectTrigger id="currency">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {currencies.map((c) => (
                  <SelectItem key={c.value} value={c.value}>{c.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <Button
            onClick={() => updateMutation.mutate()}
            disabled={updateMutation.isPending}
            className="w-full"
          >
            {updateMutation.isPending ? t('settings.saving') : t('settings.savePreferences')}
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t('settings.data')}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button variant="outline" className="w-full" onClick={() => exportMutation.mutate()}>
            {t('settings.exportData')}
          </Button>
          <Separator />
          <Button variant="destructive" className="w-full" onClick={handleReset}>
            {t('settings.resetData')}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
