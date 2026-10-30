import { ArrowUpRight } from 'lucide-react'
import { NavLink } from 'react-router-dom'

export default function SiteFooter() {
  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="brand brand--footer">
          <span className="brand__word">Film</span>
          <span className="brand__word brand__word--accent">Zone</span>
        </div>
        <p className="site-footer__tagline">La vida se ve mejor en pantalla grande.</p>
        <div className="site-footer__links">
          <div className="site-footer__links-row">
            <span>Nuestros cines</span>
            <span>Preguntas frecuentes</span>
            <span>Contacto</span>
            <NavLink to="/privacidad">Política de privacidad</NavLink>
            <NavLink to="/normas">Normas del cine</NavLink>
          </div>
          <NavLink to="/nosotros" className="site-footer__about-link">
            Sobre nosotros
            <ArrowUpRight size={16} aria-hidden="true" />
          </NavLink>
        </div>
        <p className="site-footer__copy">
          © {new Date().getFullYear()} Film Zone. Todos los derechos reservados.
        </p>
      </div>
    </footer>
  )
}
