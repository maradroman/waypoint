import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

// Map locale codes to language resources
const localeMap: Record<string, string> = {
  'en': 'en',
  'en-US': 'en',
  'pl': 'pl',
  'pl-PL': 'pl',
  'uk': 'uk',
  'uk-UA': 'uk',
}

// Lazy load language resources
const loadLanguage = async (lang: string) => {
  switch (lang) {
    case 'en':
      return import('@/locales/en.json')
    case 'pl':
      return import('@/locales/pl.json')
    case 'uk':
      return import('@/locales/uk.json')
    default:
      return import('@/locales/en.json')
  }
}

i18n
  .use(initReactI18next)
  .init({
    resources: {}, // Start with empty resources
    lng: 'en', // default language
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false, // React already escapes
    },
  })

// Load default language
loadLanguage('en').then((module) => {
  i18n.addResourceBundle('en', 'translation', module.default)
})

export async function setLanguage(locale: string) {
  const lang = localeMap[locale] || localeMap[locale.split('-')[0]] || 'en'
  console.log('setLanguage called with locale:', locale, '-> mapped to:', lang)
  console.log('Current i18n language:', i18n.language)

  // Load the language if not already loaded
  if (!i18n.hasResourceBundle(lang, 'translation')) {
    const module = await loadLanguage(lang)
    i18n.addResourceBundle(lang, 'translation', module.default)
  }

  i18n.changeLanguage(lang)
  console.log('After change, i18n language:', i18n.language)
}

export default i18n
