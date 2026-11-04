import { Menu, X } from 'lucide-react'
import { Suspense, useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import Loader from '../common/Loader'
import RouteErrorBoundary from '../common/RouteErrorBoundary'
import HeaderActions from './HeaderActions'
import ReservationExpiredNotice from './ReservationExpiredNotice'
import SiteFooter from './SiteFooter'

export default function SiteLayout() {
  const { loading: authLoading } = useAuth()
  const { peliculas, funciones, loading } = useCatalog()
  const location = useLocation()
  const [navOpen, setNavOpen] = useState(false)

  useEffect(() => {
    setNavOpen(false)
  }, [location.pathname])

  if (authLoading) return <Loader />

  return (
    <div className="site">
      <header className="site-header">
        <div className="site-header__inner">
          <NavLink to="/" className="brand">
            <span className="brand__word">Film</span>
            <span className="brand__word brand__word--accent">Zone</span>
          </NavLink>

          <button
            type="button"
            className="mobile-nav-toggle"
            aria-label={navOpen ? 'Cerrar menú principal' : 'Abrir menú principal'}
            aria-expanded={navOpen}
            aria-controls="site-navigation"
            onClick={() => setNavOpen((current) => !current)}
          >
            {navOpen
              ? <X size={22} aria-hidden="true" />
              : <Menu size={22} aria-hidden="true" />}
          </button>

          <nav
            id="site-navigation"
            className={`site-nav ${navOpen ? 'is-open' : ''}`}
            aria-label="Principal"
          >
            <NavLink to="/" className="btn-cartelera" onClick={() => setNavOpen(false)} end>
              Cartelera
            </NavLink>
            <NavLink to="/candy" onClick={() => setNavOpen(false)}>Candy</NavLink>
            <NavLink to="/club" onClick={() => setNavOpen(false)}>Club Film Zone</NavLink>
          </nav>

          <HeaderActions movies={peliculas} functions={funciones} loading={loading} />
        </div>
      </header>

      <main className="site-main">
        <RouteErrorBoundary key={location.pathname}>
          <Suspense fallback={<Loader />}>
            <Outlet />
          </Suspense>
        </RouteErrorBoundary>
      </main>

      <ReservationExpiredNotice />
      <SiteFooter />
    </div>
  )
}
