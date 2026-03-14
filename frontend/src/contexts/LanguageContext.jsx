import { createContext, useState, useContext, useEffect } from 'react'
import { getCurrentLanguage, setLanguage as setLang, t } from '../i18n/translations'

const LanguageContext = createContext(null)

export const useLanguage = () => {
  const context = useContext(LanguageContext)
  if (!context) {
    throw new Error('useLanguage must be used within LanguageProvider')
  }
  return context
}

export const LanguageProvider = ({ children }) => {
  const [language, setLanguageState] = useState(getCurrentLanguage())

  const setLanguage = (lang) => {
    setLang(lang)
    setLanguageState(lang)
  }

  const toggleLanguage = () => {
    const newLang = language === 'en' ? 'vi' : 'en'
    setLanguage(newLang)
  }

  const value = {
    language,
    setLanguage,
    toggleLanguage,
    t
  }

  return (
    <LanguageContext.Provider value={value}>
      {children}
    </LanguageContext.Provider>
  )
}
