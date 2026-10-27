import { useEffect, useState } from 'react'
import { Check } from 'lucide-react'
import { Link } from 'react-router-dom'
import ConfirmDialog from '../common/ConfirmDialog'
import { api, errorMessage } from '../../api/client'
import { formatDateShort, money } from '../../utils/format'

export default function MembresiaPanel() {
  const [suscripcion, setSuscripcion] = useState(undefined)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [bajaConfirm, setBajaConfirm] = useState(false)
  const [bajaLoading, setBajaLoading] = useState(false)

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

  useEffect(() => {
    function handleMembresia(event) {
      setSuscripcion(event.detail ?? null)
      setError('')
    }
    window.addEventListener('filmzone:membresia', handleMembresia)
    return () => window.removeEventListener('filmzone:membresia', handleMembresia)
  }, [])

  async function confirmarBaja() {
    setBajaLoading(true)
    setError('')
    try {
      const nombre = suscripcion?.nombre
      await api.darDeBajaMembresia()
      setSuscripcion(null)
      window.dispatchEvent(new CustomEvent('filmzone:membresia', { detail: null }))
      setNotice(nombre ? `Diste de baja el plan ${nombre}.` : 'Diste de baja tu membresía.')
      setBajaConfirm(false)
    } catch (err) {
      setError(errorMessage(err))
      setBajaConfirm(false)
    } finally {
      setBajaLoading(false)
    }
  }

  return (
    <section id="membresia" className="account-section membership-account">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Club Film Zone</p>
          <h2>Tu membresía</h2>
        </div>
        {suscripcion && (
          <button
            type="button"
            className="membership-account__cancel"
            onClick={() => setBajaConfirm(true)}
          >
            Dar de baja
          </button>
        )}
      </div>

      {notice && <div className="info-box">{notice}</div>}
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

      {bajaConfirm && (
        <ConfirmDialog
          title="Dar de baja la membresía"
          message={`Se cancela ${suscripcion?.nombre || 'tu plan'} y dejás de tener los beneficios del club.`}
          confirmLabel="Dar de baja"
          danger
          loading={bajaLoading}
          onConfirm={confirmarBaja}
          onCancel={() => setBajaConfirm(false)}
        />
      )}
    </section>
  )
}
