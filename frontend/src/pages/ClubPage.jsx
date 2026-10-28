import { useEffect, useRef, useState } from 'react'
import { Check, Crown, Star, Ticket } from 'lucide-react'
import { gsap } from 'gsap'
import { useNavigate } from 'react-router-dom'
import ConfirmDialog from '../components/common/ConfirmDialog'
import PurchaseAuthDrawer from '../components/cuenta/PurchaseAuthDrawer'
import { api, errorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { usePurchase } from '../context/PurchaseContext'
import { formatDateShort, money } from '../utils/format'

const PLAN_ICONS = [Ticket, Star, Crown]

export default function ClubPage() {
  const navigate = useNavigate()
  const gridRef = useRef(null)
  const { spectator, isSpectator } = useAuth()
  const { startMembership } = usePurchase()
  const [authOpen, setAuthOpen] = useState(false)
  const [pendingId, setPendingId] = useState(null)
  const [suscripcion, setSuscripcion] = useState(null)
  const [membresias, setMembresias] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [bajaConfirm, setBajaConfirm] = useState(false)
  const [bajaLoading, setBajaLoading] = useState(false)

  useEffect(() => {
    let cancelled = false
    api.getMembresias()
      .then((data) => {
        if (!cancelled) setMembresias(data || [])
      })
      .catch((err) => {
        if (!cancelled) setError(errorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setSuscripcion(data)
      })
      .catch(() => {
        if (!cancelled) setSuscripcion(null)
      })
    return () => {
      cancelled = true
    }
  }, [spectator?.id])

  useEffect(() => {
    function handleMembresia(event) {
      setSuscripcion(event.detail ?? null)
    }
    window.addEventListener('filmzone:membresia', handleMembresia)
    return () => window.removeEventListener('filmzone:membresia', handleMembresia)
  }, [])

  useEffect(() => {
    const grid = gridRef.current
    if (!grid || membresias.length === 0) return undefined
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduceMotion) return undefined

    const cards = grid.querySelectorAll('.membership-card')
    const tween = gsap.fromTo(
      cards,
      { y: 42, autoAlpha: 0 },
      {
        y: 0,
        autoAlpha: 1,
        duration: 0.75,
        stagger: 0.14,
        ease: 'power3.out',
        clearProps: 'transform,opacity,visibility',
      },
    )
    return () => tween.kill()
  }, [membresias])

  function liftCard(event) {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    gsap.to(event.currentTarget, { y: -10, duration: 0.28, ease: 'power2.out' })
  }

  function dropCard(event) {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    gsap.to(event.currentTarget, { y: 0, duration: 0.28, ease: 'power2.out' })
  }

  function goToPayment(membershipId) {
    startMembership(membershipId)
    navigate('/club/pago')
  }

  function adquirir(membershipId) {
    setNotice('')
    setPendingId(membershipId)
    if (!isSpectator || !spectator.emailVerificado) {
      setAuthOpen(true)
      return
    }
    goToPayment(membershipId)
  }

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
    <section className="club-page">
      <header className="club-page__heading">
        <p className="eyebrow">Club Film Zone</p>
        <h1>Membresías disponibles</h1>
        <p>
          Elegí un plan y pagalo con el mismo checkout que usás para tus entradas.
          Si ya tenés uno, podés cambiarlo o darlo de baja.
        </p>
      </header>

      {notice && <div className="info-box">{notice}</div>}
      {suscripcion && (
        <div className="info-box">
          Tu plan <strong>{suscripcion.nombre}</strong> está activo hasta el{' '}
          {formatDateShort(suscripcion.fechaFin)}.{' '}
          {suscripcion.membresiaProgramadaId ? (
            <>
              El {formatDateShort(suscripcion.fechaCambioProgramado)} comenzará{' '}
              <strong>{suscripcion.membresiaProgramadaNombre}</strong> por{' '}
              <strong>${money(suscripcion.precioProgramado)}</strong>.
            </>
          ) : (
            'Elegí otro para reemplazarlo.'
          )}
        </div>
      )}
      {suscripcion && !suscripcion.membresiaProgramadaId && (
        <div className="membership-change-rule">
          <strong>¿Querés cambiar de plan?</strong>
          <span>
            Durante los primeros 7 días, una mejora se cobra solo por la diferencia.
            Después de ese plazo, se cobra el precio completo y el plan superior se activa en el momento.
            Si elegís un plan menor, el cambio comienza al vencer el actual.
          </span>
        </div>
      )}

      {loading && <div className="info-box">Cargando membresías…</div>}
      {error && <div className="info-box info-box--error">{error}</div>}
      {!loading && !error && membresias.length === 0 && (
        <div className="info-box">Todavía no hay membresías cargadas.</div>
      )}

      <div className="membership-grid" ref={gridRef}>
        {membresias.map((membresia, index) => {
          const Icon = PLAN_ICONS[index % PLAN_ICONS.length]
          const esActual = suscripcion?.membresiaId === membresia.id
          const esProgramada = suscripcion?.membresiaProgramadaId === membresia.id
          return (
            <article
              key={membresia.id}
              className={`membership-card${membresia.destacada ? ' membership-card--featured' : ''}${esActual ? ' membership-card--current' : ''}`}
              onMouseEnter={liftCard}
              onMouseLeave={dropCard}
            >
              {membresia.destacada && <span className="membership-card__badge">Recomendada</span>}
              <div className="membership-card__icon" aria-hidden="true">
                <Icon size={22} />
              </div>
              <h2>{membresia.nombre}</h2>
              <p className="membership-card__price">
                ${money(membresia.precioMensual)}
                <span> / mes</span>
              </p>
              <p className="muted">{membresia.descripcion}</p>
              <ul>
                {membresia.beneficios.map((beneficio) => (
                  <li key={beneficio}>
                    <Check size={16} aria-hidden="true" />
                    <span>{beneficio}</span>
                  </li>
                ))}
              </ul>
              <button
                type="button"
                className={`${esActual ? 'secondary-button' : 'primary-button'} membership-card__action`}
                disabled={esProgramada}
                onClick={() => (esActual ? setBajaConfirm(true) : adquirir(membresia.id))}
              >
                {esActual
                  ? 'Dar de baja'
                  : esProgramada
                    ? 'Cambio programado'
                    : suscripcion
                      ? 'Cambiar a este plan'
                      : 'Adquirir'}
              </button>
            </article>
          )
        })}
      </div>

      <PurchaseAuthDrawer
        open={authOpen}
        purchaseDescription="Necesitás una cuenta para adquirir una membresía del Club Film Zone."
        onClose={() => setAuthOpen(false)}
        onComplete={() => {
          setAuthOpen(false)
          if (pendingId) goToPayment(pendingId)
        }}
      />

      {bajaConfirm && (
        <ConfirmDialog
          title="Dar de baja la membresía"
          message={suscripcion?.membresiaProgramadaId
            ? `Se cancela ${suscripcion.nombre}, el cambio programado a ${suscripcion.membresiaProgramadaNombre} y los beneficios del club.`
            : `Se cancela ${suscripcion?.nombre || 'tu plan'} y dejás de tener los beneficios del club.`}
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
