import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function SiteLayout() {
  const { spectator, isSpectator, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/')
  }

  return (
    <div className="site">
      <header className="site-header">
        <div className="site-header__inner">
          <NavLink to="/" className="brand">
            <span className="brand__word">Noche</span>
            <span className="brand__word brand__word--accent">Cine</span>
          </NavLink>

          <nav className="site-nav" aria-label="Principal">
            <NavLink to="/" end>
              Cartelera
            </NavLink>
            <NavLink to="/proximamente">Próximamente</NavLink>
            <NavLink to="/experiencias">Experiencias</NavLink>
            <NavLink to="/club">Club Noche</NavLink>
            <NavLink to="/cuenta">
              {isSpectator ? `${spectator.nombre} ${spectator.apellido}` : 'Mi cuenta'}
            </NavLink>
          </nav>

          {isSpectator && (
            <button type="button" className="text-button" onClick={handleLogout}>
              Cerrar sesión
            </button>
          )}
        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="site-footer__inner">
          <div className="brand brand--footer">
            <span className="brand__word">Noche</span>
            <span className="brand__word brand__word--accent">Cine</span>
          </div>
          <p className="site-footer__tagline">La vida se ve mejor en pantalla grande.</p>
          <div className="site-footer__links">
            <span>Nuestros cines</span>
            <span>Preguntas frecuentes</span>
            <span>Contacto</span>
            <span>Accesibilidad</span>
            <a href="/personal">Acceso personal</a>
          </div>
          <p className="site-footer__copy">
            © {new Date().getFullYear()} Noche Cine. Todos los derechos reservados.
          </p>
        </div>
      </footer>
    </div>
  )
}
