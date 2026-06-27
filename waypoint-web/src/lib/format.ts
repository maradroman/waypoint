import { useAuth } from '@/stores/auth'
import { useEffect } from 'react'
import { setLanguage } from './i18n'

export function formatMoney(cents: number, locale: string = 'en-US', currency: string = 'USD'): string {
  const amount = cents / 100
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

export function useFormatMoney() {
  const user = useAuth((s) => s.user)
  const locale = user?.locale || 'en-US'
  const currency = user?.currency || 'USD'

  // Update i18n language when user locale changes
  useEffect(() => {
    if (user?.locale) {
      setLanguage(user.locale)
    }
  }, [user?.locale])

  return (cents: number) => formatMoney(cents, locale, currency)
}
