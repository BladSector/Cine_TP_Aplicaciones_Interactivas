import { useEffect, useState } from 'react'
import { Check } from 'lucide-react'
import { Link } from 'react-router-dom'
import { api, errorMessage } from '../../api/client'
import { formatDateShort, money } from '../../utils/format'

export default function MembresiaPanel() {
  const [suscripcion, setSuscripcion] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setSuscripcion(data)
      })
      .catch((err) => {
        if (!cancelled) setError(errorMessage(err))
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <section id="membresia" className="account-section membership-account">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Club Film Zone</p>
          <h2>Tu membresía</h2>
        </div>
      </div>

      {error && <div className="info-box info-box--error">{error}</div>}
      {suscripcion === undefined && !error && <div className="info-box">Cargando tu plan…</div>}
      {suscripcion === null && !error && (
        <div className="info-box">
          No tenés una membresía activa. <Link to="/club">Ver los planes</Link>
        </div>
      )}
      {suscripcion && (
        <div className="membership-account__body">
          <div>
            <p className="eyebrow">Plan actual</p>
            <h3>{suscripcion.nombre}</h3>
            <p className="muted">{suscripcion.descripcion}</p>
          </div>
          <p className="membership-account__price">
            ${money(suscripcion.precioCobrado)}
            <span> / mes</span>
          </p>
          <p>Activa hasta el {formatDateShort(suscripcion.fechaFin)}.</p>
          <ul className="membership-success__benefits">
            {suscripcion.beneficios.map((beneficio) => (
              <li key={beneficio}>
                <Check size={16} aria-hidden="true" />
                <span>{beneficio}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
