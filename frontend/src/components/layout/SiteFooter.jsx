import { ArrowUpRight } from 'lucide-react'
import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import Modal from '../common/Modal'

export default function SiteFooter() {
  const [contactOpen, setContactOpen] = useState(false)

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
            <button type="button" className="site-footer__contact-link" onClick={() => setContactOpen(true)}>
              Contacto
            </button>
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
      {contactOpen && (
        <Modal title="Contacto" compact onClose={() => setContactOpen(false)}>
          <address className="site-footer__contact-details">
            <p><strong>Correo electrónico</strong><br /><a href="mailto:contacto@filmzone.example">contacto@filmzone.example</a></p>
            <p><strong>Teléfono</strong><br />011 0000-0000</p>
            <p><strong>Dirección</strong><br />Avenida del Cine 1234, Buenos Aires</p>
            <p><strong>Horario de atención</strong><br />Todos los días, de 13:00 a 23:00.</p>
          </address>
          <p className="muted tiny">Datos ficticios para la demostración del proyecto.</p>
        </Modal>
      )}
    </footer>
  )
}
