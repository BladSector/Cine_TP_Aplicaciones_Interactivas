import { Moon, Sun } from 'lucide-react'
import { useEffect, useState } from 'react'
import PurchaseAuthDrawer from '../cuenta/PurchaseAuthDrawer'
import ActiveCart from './ActiveCart'
import HeaderMovieSearch from './HeaderMovieSearch'
import UserMenu from './UserMenu'

export default function HeaderActions({ movies, functions, loading }) {
  const [theme, setTheme] = useState(
    () => document.documentElement.dataset.theme || 'light',
  )
  const [authOpen, setAuthOpen] = useState(false)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    localStorage.setItem('film-zone-theme', theme)
  }, [theme])

  return (
    <>
      <div className="header-utility">
        <HeaderMovieSearch movies={movies} functions={functions} loading={loading} />
        <ActiveCart />

        <button
          type="button"
          className="theme-toggle"
          aria-label={theme === 'dark' ? 'Activar modo claro' : 'Activar modo oscuro'}
          title={theme === 'dark' ? 'Modo claro' : 'Modo oscuro'}
          onClick={() => setTheme((current) => (current === 'dark' ? 'light' : 'dark'))}
        >
          {theme === 'dark'
            ? <Sun size={20} aria-hidden="true" />
            : <Moon size={20} aria-hidden="true" />}
        </button>

        <div className="header-actions">
          <UserMenu onLogin={() => setAuthOpen(true)} />
        </div>
      </div>

      <PurchaseAuthDrawer
        open={authOpen}
        purchase={false}
        onClose={() => setAuthOpen(false)}
        onComplete={() => setAuthOpen(false)}
      />
    </>
  )
}
