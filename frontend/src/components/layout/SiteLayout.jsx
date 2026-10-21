import { Search, X } from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import PurchaseAuthDrawer from '../cuenta/PurchaseAuthDrawer'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import {
  formatFunction,
  formatLanguage,
  functionHasStarted,
  normalizeText,
  shortTime,
} from '../../utils/format'
import { useEffect, useMemo, useRef, useState } from 'react'

function functionDateLabel(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
  })
}

export default function SiteLayout() {
  const { spectator, isSpectator, logout } = useAuth()
  const { peliculas, funciones, loading } = useCatalog()
  const navigate = useNavigate()

  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [isAuthOpen, setIsAuthOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [isSearchOpen, setIsSearchOpen] = useState(false)
  const [isSearchExpanded, setIsSearchExpanded] = useState(false)
  const menuRef = useRef(null)
  const searchRef = useRef(null)
  const searchInputRef = useRef(null)

  const searchResults = useMemo(() => {
    const query = normalizeText(searchQuery)
    if (!query) return []

    return peliculas
      .filter((movie) => normalizeText(movie.titulo).includes(query))
      .map((movie) => ({
        movie,
        upcomingFunctions: funciones
          .filter((fn) => fn.peliculaId === movie.id && !functionHasStarted(fn))
          .sort((a, b) =>
            `${a.fecha}T${a.horario}`.localeCompare(`${b.fecha}T${b.horario}`),
          )
          .slice(0, 3),
      }))
      .sort((a, b) => {
        const availabilityDifference = Number(b.upcomingFunctions.length > 0)
          - Number(a.upcomingFunctions.length > 0)
        return availabilityDifference || a.movie.titulo.localeCompare(b.movie.titulo, 'es')
      })
      .slice(0, 6)
  }, [peliculas, funciones, searchQuery])

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false)
      }
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setIsSearchOpen(false)
        setIsSearchExpanded(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    if (isSearchExpanded) {
      searchInputRef.current?.focus()
    }
  }, [isSearchExpanded])

  async function handleLogout() {
    if (!window.confirm('¿Querés cerrar la sesión?')) return
    await logout()
    setIsMenuOpen(false) // Cerramos el menú al salir
    navigate('/')
  }

  function openMovieResult(result) {
    setSearchQuery('')
    setIsSearchOpen(false)
    setIsSearchExpanded(false)
    navigate(`/pelicula/${result.movie.id}`, {
      state: { selectedDate: result.upcomingFunctions[0]?.fecha },
    })
  }

  return (
    <div className="site">
      <header className="site-header">
        <div className="site-header__inner">
          <NavLink to="/" className="brand">
            <span className="brand__word">Film</span>
            <span className="brand__word brand__word--accent">Zone</span>
          </NavLink>

          <nav className="site-nav" aria-label="Principal">
              <div className= "destacar_cartelera">
                  <NavLink to="/" end>Cartelera</NavLink>
              </div>
            <NavLink to="/proximamente">Próximamente</NavLink>
            <NavLink to="/experiencias">Experiencias</NavLink>
            <NavLink to="/club">Club Film Zone</NavLink>
          </nav>

          <div className="header-search" ref={searchRef}>
            {!isSearchExpanded ? (
              <button
                type="button"
                className="header-search__toggle"
                aria-label="Abrir buscador de películas"
                title="Buscar películas"
                onClick={() => setIsSearchExpanded(true)}
              >
                <Search size={20} aria-hidden="true" />
              </button>
            ) : (
              <div className="header-search__field">
                <Search className="header-search__icon" size={18} aria-hidden="true" />
                <input
                  ref={searchInputRef}
                  type="search"
                  value={searchQuery}
                  placeholder="Buscar películas"
                  aria-label="Buscar películas y funciones"
                  aria-expanded={isSearchOpen && Boolean(searchQuery.trim())}
                  aria-controls="movie-search-results"
                  onFocus={() => setIsSearchOpen(Boolean(searchQuery.trim()))}
                  onChange={(event) => {
                    setSearchQuery(event.target.value)
                    setIsSearchOpen(Boolean(event.target.value.trim()))
                  }}
                  onKeyDown={(event) => {
                    if (event.key === 'Escape') {
                      setIsSearchOpen(false)
                      setIsSearchExpanded(false)
                    }
                  }}
                />
                <button
                  type="button"
                  className="header-search__clear"
                  aria-label="Cerrar buscador"
                  title="Cerrar buscador"
                  onClick={() => {
                    setSearchQuery('')
                    setIsSearchOpen(false)
                    setIsSearchExpanded(false)
                  }}
                >
                  <X size={16} aria-hidden="true" />
                </button>
              </div>
            )}

            {isSearchOpen && searchQuery.trim() && (
              <div id="movie-search-results" className="header-search__results">
                {loading ? (
                  <p className="header-search__message">Buscando películas…</p>
                ) : searchResults.length === 0 ? (
                  <p className="header-search__message">No encontramos películas con ese nombre.</p>
                ) : (
                  searchResults.map((result) => (
                    <button
                      key={result.movie.id}
                      type="button"
                      className="header-search__result"
                      aria-label={`Ver funciones de ${result.movie.titulo}`}
                      onClick={() => openMovieResult(result)}
                    >
                      <span className="header-search__movie">
                        <strong>{result.movie.titulo}</strong>
                        <small>{result.movie.categoriaNombre}</small>
                      </span>
                      <span className="header-search__functions">
                        {result.upcomingFunctions.length > 0 ? (
                          result.upcomingFunctions.map((fn) => (
                            <span key={fn.id}>
                              {functionDateLabel(fn.fecha)} · {shortTime(fn.horario)} ·{' '}
                              {formatFunction(fn.formato)} · {formatLanguage(fn.idioma)}
                            </span>
                          ))
                        ) : (
                          <span>Sin funciones próximas</span>
                        )}
                      </span>
                      <span className="header-search__action">Ver todas las funciones</span>
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          <div className="header-actions">
            {isSpectator ? (
              <div className="user-menu-container" ref={menuRef}>
                <button
                  type="button"
                  className={`user-icon-button ${isMenuOpen ? 'is-active' : ''}`}
                  onClick={() => setIsMenuOpen(!isMenuOpen)}
                  title="Mi cuenta"
                >
                  {/* Icono de usuario SVG */}
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </button>

                {isMenuOpen && (
                  <div className="dropdown-menu">
                    <div className="dropdown-header">
                      Hola, <strong>{spectator.nombre}</strong>
                    </div>

                    <NavLink to="/cuenta#entradas" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Mis Entradas
                    </NavLink>

                    <NavLink to="/cuenta#perfil" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Editar Perfil
                    </NavLink>

                    <NavLink to="/cuenta#pagos" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Métodos de Pago
                    </NavLink>

                    <hr className="dropdown-divider" />

                    <button type="button" className="dropdown-item text-danger" onClick={handleLogout}>
                      Cerrar sesión
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <button type="button" className="secondary-button" onClick={() => setIsAuthOpen(true)}>
                Iniciar sesión
              </button>
            )}
          </div>

        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="site-footer__inner">
          <div className="brand brand--footer">
            <span className="brand__word">Film</span>
            <span className="brand__word brand__word--accent">Zone</span>
          </div>
          <p className="site-footer__tagline">La vida se ve mejor en pantalla grande.</p>
          <div className="site-footer__links">
            <span>Nuestros cines</span>
            <span>Preguntas frecuentes</span>
            <span>Contacto</span>
          </div>
          <p className="site-footer__copy">
            © {new Date().getFullYear()} Film Zone. Todos los derechos reservados.
          </p>
        </div>
      </footer>

      <PurchaseAuthDrawer
        open={isAuthOpen}
        purchase={false}
        onClose={() => setIsAuthOpen(false)}
        onComplete={() => setIsAuthOpen(false)}
      />
    </div>
  )
}
