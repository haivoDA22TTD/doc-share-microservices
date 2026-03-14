import { useLanguage } from '../contexts/LanguageContext'

const LanguageToggle = () => {
  const { language, toggleLanguage } = useLanguage()

  return (
    <button 
      onClick={toggleLanguage}
      className="language-toggle"
      title={language === 'en' ? 'Switch to Vietnamese' : 'Chuyển sang tiếng Anh'}
    >
      {language === 'en' ? '🇻🇳 VI' : '🇬🇧 EN'}
    </button>
  )
}

export default LanguageToggle
