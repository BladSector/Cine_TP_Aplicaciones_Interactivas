import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useState, useRef, useEffect } from 'react'

export default function SiteLayout() {
  const { spectator, isSpectator, logout } = useAuth()
  const navigate = useNavigate()

  // Estados para controlar el menú desplegable
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const menuRef = useRef(null)

  // Este efecto cierra el menú si el usuario hace clic fuera de él
  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  async function handleLogout() {
    await logout()
    setIsMenuOpen(false) // Cerramos el menú al salir
    navigate('/')
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
            <NavLink to="/" end>Cartelera</NavLink>
            <NavLink to="/proximamente">Próximamente</NavLink>
            <NavLink to="/experiencias">Experiencias</NavLink>
            <NavLink to="/club">Club Film Zone</NavLink>
          </nav>

          {/* Contenedor de acciones del usuario */}
          <div className="header-actions">
            {isSpectator ? (
              // Si el usuario ESTÁ logueado, mostramos el icono y el dropdown
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

                {/* El menú desplegable condicional */}
                {isMenuOpen && (
                  <div className="dropdown-menu">
                    <div className="dropdown-header">
                      Hola, <strong>{spectator.nombre}</strong>
                    </div>

                    {/* Usamos <a> normal para forzar el salto al ID */}
                    <a href="/cuenta#entradas" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Mis Entradas
                    </a>

                    <a href="/cuenta#perfil" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Editar Perfil
                    </a>

                    <a href="/cuenta#pagos" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                      Métodos de Pago
                    </a>

                    <hr className="dropdown-divider" />

                    <button type="button" className="dropdown-item text-danger" onClick={handleLogout}>
                      Cerrar sesión
                    </button>
                  </div>
                )}
              </div>
            ) : (
              // Si el usuario NO ESTÁ logueado, mostramos el botón de iniciar sesión
              <NavLink to="/cuenta" className="secondary-button">
                Iniciar sesión
              </NavLink>
            )}
          </div>

        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      {/* FOOTER (Se mantiene igual) */}
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
            <a href="/personal">Acceso personal</a>
          </div>
          <p className="site-footer__copy">
            © {new Date().getFullYear()} Film Zone. Todos los derechos reservados.
          </p>
        </div>
      </footer>
    </div>
  )
}