import { useEffect, useState } from 'react'
import { Check, CreditCard } from 'lucide-react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import ExpiryMonthPicker from '../../components/compra/ExpiryMonthPicker'
import SeatReservationTimer from '../../components/compra/SeatReservationTimer'
import TicketDetails from '../../components/tickets/TicketDetails'
import Loader from '../../components/common/Loader'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { formatDateShort, functionHasStarted, money, nextMonthValue, paymentIsExpired } from '../../utils/format'

function formatCardNumber(value) {
  return value
    .replace(/\D/g, '')
    .slice(0, 16)
    .replace(/(.{4})/g, '$1 ')
    .trim()
}

function formatExpiry(value) {
  if (!value || !value.includes('-')) return 'MM/AA'
  const [year, month] = value.split('-')
  return `${month}/${year.slice(-2)}`
}

function cardPreviewNumber(value) {
  if (!value) return '•••• •••• •••• ••••'
  return formatCardNumber(value).padEnd(19, '•')
}

export default function CompraPagoPage({ candyOnly = false, membershipOnly = false }) {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { spectator, isSpectator, refreshSpectator } = useAuth()
  const { peliculas, funciones, productos, reload } = useCatalog()
  const [membresias, setMembresias] = useState([])
  const [membresiasLoading, setMembresiasLoading] = useState(membershipOnly)
  const [planVigente, setPlanVigente] = useState(null)
  const [membershipQuote, setMembershipQuote] = useState(null)
  const [membershipQuoteLoading, setMembershipQuoteLoading] = useState(membershipOnly)
  const {
    draft,
    clearConsumptions,
    reset,
    reservationError,
    remainingSeconds,
  } = usePurchase()
  const [accepted, setAccepted] = useState(false)
  const [methodMode, setMethodMode] = useState('saved')
  const [selectedMethodId, setSelectedMethodId] = useState('')
  const [saveNewCard, setSaveNewCard] = useState(false)
  const [card, setCard] = useState({
    numero: '',
    fechaVencimiento: '',
    nombreTitular: '',
    cvv: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [ticket, setTicket] = useState(null)
  const [suscripcion, setSuscripcion] = useState(null)
  const [calculatedTickets, setCalculatedTickets] = useState(null)

  useEffect(() => {
    if (!membershipOnly) return undefined
    let cancelled = false
    api.getMembresias()
      .then((data) => {
        if (!cancelled) setMembresias(data || [])
      })
      .catch(() => {
        if (!cancelled) setMembresias([])
      })
      .finally(() => {
        if (!cancelled) setMembresiasLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [membershipOnly])

  useEffect(() => {
    if (!membershipOnly || !draft.membershipId || !isSpectator) return undefined
    let cancelled = false
    setMembershipQuoteLoading(true)
    setError('')
    api.cotizarMembresia(draft.membershipId)
      .then((data) => {
        if (!cancelled) setMembershipQuote(data)
      })
      .catch((err) => {
        if (!cancelled) setError(errorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setMembershipQuoteLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [draft.membershipId, isSpectator, membershipOnly])

  useEffect(() => {
    if (!membershipOnly) return undefined
    let cancelled = false
    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setPlanVigente(data)
      })
      .catch(() => {
        if (!cancelled) setPlanVigente(null)
      })
    return () => {
      cancelled = true
    }
  }, [membershipOnly])

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)
  const membresia = membresias.find((item) => item.id === Number(draft.membershipId))
  const esMejoraPorDiferencia = membershipQuote?.tipo === 'MEJORA_INMEDIATA_DIFERENCIA'
  const esMejoraPrecioCompleto = membershipQuote?.tipo === 'MEJORA_INMEDIATA_COMPLETA'
  const esMejoraInmediata = esMejoraPorDiferencia || esMejoraPrecioCompleto
  const esCambioProgramado = membershipQuote?.tipo === 'CAMBIO_PROGRAMADO'
  const metodos = spectator?.metodosDePago || []
  const sinFuncion = candyOnly || membershipOnly

  useEffect(() => {
    const firstValid = metodos.find((metodo) => !paymentIsExpired(metodo.fechaVencimiento))
    if (firstValid && !selectedMethodId) setSelectedMethodId(String(firstValid.id))
  }, [metodos, selectedMethodId])

  useEffect(() => {
    if (sinFuncion || !funcion || !spectator || draft.selectedSeats.length === 0) return
    api.calcularPrecio(
      funcion.id,
      spectator.id,
      funcion.precioEntrada,
      draft.selectedSeats.length,
    ).then((response) => setCalculatedTickets(response.total)).catch(() => setCalculatedTickets(null))
  }, [sinFuncion, funcion, spectator, draft.selectedSeats.length])

  useEffect(() => {
    if (ticket || suscripcion) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, [ticket, suscripcion])

  const candyItemCount = Object.values(draft.consumptions).reduce(
    (sum, quantity) => sum + Number(quantity),
    0,
  )

  if (!isSpectator) {
    return (
      <Navigate
        to={membershipOnly ? '/club' : candyOnly ? '/candy' : `/compra/${movieId}/acceso`}
        replace
      />
    )
  }
  if (membershipOnly) {
    if (!suscripcion && (draft.purchaseType !== 'membership' || !draft.membershipId)) {
      return <Navigate to="/club" replace />
    }
    if (!suscripcion && !membresia) {
      if (membresiasLoading) return <Loader />
      return <Navigate to="/club" replace />
    }
    if (!suscripcion && membershipQuoteLoading) return <Loader />
  } else if (candyOnly) {
    if (!ticket && (draft.purchaseType !== 'candy' || candyItemCount === 0)) {
      return <Navigate to="/candy" replace />
    }
  } else {
    if (!ticket && (!funcion || !movie || draft.selectedSeats.length === 0)) {
      return <Navigate to={`/compra/${movieId}/butacas`} replace />
    }
    if (!ticket && !draft.candyVisited) {
      return <Navigate to={`/compra/${movieId}/candy`} replace />
    }
  }

  const totalTickets = sinFuncion || ticket
    ? 0
    : calculatedTickets ?? draft.selectedSeats.length * funcion.precioEntrada
  const totalCandy = membershipOnly || ticket
    ? 0
    : Object.entries(draft.consumptions).reduce((sum, [id, qty]) => {
        const product = productos.find((item) => item.id === Number(id))
        return sum + (product ? product.precio * qty : 0)
      }, 0)
  const total = membershipOnly
    ? membershipQuote?.importeAhora ?? 0
    : totalTickets + totalCandy

  async function ensurePaymentMethod() {
    if (methodMode === 'saved' && selectedMethodId) {
      const metodo = metodos.find((item) => item.id === Number(selectedMethodId))
      if (!metodo || paymentIsExpired(metodo.fechaVencimiento)) {
        throw new Error('El método de pago seleccionado no es válido o está vencido.')
      }
      return { id: Number(selectedMethodId), isNew: false }
    }

    if (!card.numero || !card.fechaVencimiento || !card.nombreTitular || !card.cvv) {
      throw new Error('Completá todos los datos de la tarjeta.')
    }
    if (paymentIsExpired(card.fechaVencimiento)) {
      throw new Error('La tarjeta está vencida. Ingresá una fecha posterior a la actual.')
    }

    const created = await api.crearMetodoPago({
      numero: card.numero.replace(/\s/g, ''),
      fechaVencimiento: card.fechaVencimiento,
      nombreTitular: card.nombreTitular,
      cvv: card.cvv,
    })
    return { id: created.id, isNew: true }
  }

  async function handlePay(event) {
    event.preventDefault()
    if (!sinFuncion && functionHasStarted(funcion)) {
      setError('La función seleccionada ya comenzó. Elegí otro horario para continuar.')
      return
    }
    if (!accepted) {
      setError('Debés aceptar la política de privacidad y las normas del cine.')
      return
    }
    if (!spectator.emailVerificado) {
      setError('Debés verificar tu correo antes de realizar la compra.')
      return
    }

    setLoading(true)
    setError('')
    let temporaryPaymentMethodId = null
    try {
      const paymentMethod = await ensurePaymentMethod()
      const metodoDePagoId = paymentMethod.id
      const guardarMetodoPago = !paymentMethod.isNew || membershipOnly || saveNewCard
      if (paymentMethod.isNew && !guardarMetodoPago) {
        temporaryPaymentMethodId = metodoDePagoId
      }
      const items = Object.entries(draft.consumptions)
        .map(([productoId, cantidad]) => ({
          productoId: Number(productoId),
          cantidad: Number(cantidad),
        }))
        .filter((item) => item.cantidad > 0)

      const result = membershipOnly
        ? await api.comprarMembresia({
            espectadorId: spectator.id,
            metodoDePagoId,
            membresiaId: membresia.id,
          })
        : candyOnly
          ? await api.comprarCandy({
              espectadorId: spectator.id,
              metodoDePagoId,
              guardarMetodoPago,
              items,
            })
          : await api.comprar({
              espectadorId: spectator.id,
              metodoDePagoId,
              funcionId: funcion.id,
              butacasIds: draft.selectedSeats.map((seat) => seat.id),
              guardarMetodoPago,
              items,
            })

      if (membershipOnly) {
        setSuscripcion(result)
        window.dispatchEvent(new CustomEvent('filmzone:membresia', { detail: result }))
      }
      else setTicket(result)
      if (candyOnly) clearConsumptions()
      else if (!membershipOnly) reset()
      await reload()
      await refreshSpectator(spectator.id)
    } catch (err) {
      if (temporaryPaymentMethodId) {
        try {
          await api.eliminarMetodoPago(temporaryPaymentMethodId)
          await refreshSpectator(spectator.id)
        } catch {
          // La compra conserva su error original aunque falle la limpieza de la tarjeta temporal.
        }
      }
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  if (suscripcion) {
    const tieneCambioProgramado = Boolean(suscripcion.membresiaProgramadaId)
    return (
      <div className="purchase-page">
        <section className="success-panel">
          <p className="eyebrow">
            {tieneCambioProgramado ? 'Cambio programado' : 'Membresía confirmada'}
          </p>
          <h1>
            {tieneCambioProgramado ? 'Tu próximo plan está confirmado' : 'Ya sos parte del Club Film Zone'}
          </h1>
          {tieneCambioProgramado ? (
            <p>
              <strong>{suscripcion.nombre}</strong> continúa activo hasta el{' '}
              {formatDateShort(suscripcion.fechaFin)}. El{' '}
              {formatDateShort(suscripcion.fechaCambioProgramado)} comenzará{' '}
              <strong>{suscripcion.membresiaProgramadaNombre}</strong> y se cobrarán{' '}
              <strong>${money(suscripcion.precioProgramado)}</strong>.
            </p>
          ) : (
            <p>
              Enviamos la confirmación a <strong>{spectator.email}</strong>.{' '}
              Tu plan <strong>{suscripcion.nombre}</strong> está activo hasta el{' '}
              {formatDateShort(suscripcion.fechaFin)}.
            </p>
          )}
          <ul className="membership-success__benefits">
            {(tieneCambioProgramado ? membresia?.beneficios || [] : suscripcion.beneficios).map((beneficio) => (
              <li key={beneficio}>
                <Check size={16} aria-hidden="true" />
                <span>{beneficio}</span>
              </li>
            ))}
          </ul>
          <p className="muted">
            {tieneCambioProgramado
              ? 'Hoy no se realizó ningún cobro.'
              : esMejoraPorDiferencia
                ? `Pagaste $${money(suscripcion.precioCobrado)} correspondientes a la diferencia entre planes.`
                : esMejoraPrecioCompleto
                  ? `Pagaste $${money(suscripcion.precioCobrado)} por el nuevo mes de membresía.`
                : `Pagaste $${money(suscripcion.precioCobrado)} por este mes.`}
          </p>
          <Link to="/club" className="primary-button">
            Volver al club
          </Link>
        </section>
      </div>
    )
  }

  if (ticket) {
    return (
      <div className="purchase-page">
        <section className="success-panel">
          <p className="eyebrow">Compra confirmada</p>
          <h1>{candyOnly ? '¡Tu pedido está listo!' : '¡Listo para la gran pantalla!'}</h1>
          <p>
            Enviamos el comprobante a <strong>{spectator.email}</strong>.{' '}
            {candyOnly
              ? 'Presentá el QR en el Candy para retirar tus productos.'
              : 'También podés verlo en Mi cuenta.'}
          </p>
          <TicketDetails ticket={ticket} movie={movie} />
        </section>
      </div>
    )
  }

  if (loading) {
    return <Loader />
  }

  return (
    <div className="purchase-page purchase-page--checkout">
      {sinFuncion && (
        <Link
          to={membershipOnly ? '/club' : '/candy'}
          className="back-link"
        >
          {membershipOnly ? 'Volver al club' : 'Volver al Candy'}
        </Link>
      )}
      {!sinFuncion && <PurchaseStepper current="pago" />}

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">
            {membershipOnly
              ? 'Confirmá tu membresía'
              : candyOnly
                ? 'Confirmá tu pedido'
                : 'El último paso antes de la gran pantalla'}
          </p>
          <h1>
            {membershipOnly
              ? esCambioProgramado
                ? 'Programá tu próximo plan'
                : esMejoraInmediata
                  ? 'Mejorá tu membresía'
                  : 'Completá el pago'
              : sinFuncion
                ? 'Completá el pago'
                : 'Todo listo para tu noche'}
          </h1>
          <p>
            {membershipOnly
              ? membershipQuote?.mensaje
              : candyOnly
                ? 'Elegí cómo pagar tus productos. Enviaremos el QR a tu correo.'
                : 'Revisá tu pedido y completá el pago. Enviaremos las entradas a tu correo.'}
          </p>
          {membershipOnly && membershipQuote && (
            <div className="info-box membership-checkout-notice">
              {esCambioProgramado ? (
                <>
                  <strong>Hoy no se realizará ningún cobro.</strong>
                  <span>
                    {planVigente?.nombre} continuará hasta el {formatDateShort(planVigente?.fechaFin)}.
                    {' '}El {formatDateShort(membershipQuote.fechaInicio)} comenzará {membresia.nombre}
                    {' '}y se cobrarán ${money(membershipQuote.importeRenovacion)}.
                  </span>
                  {planVigente?.membresiaProgramadaId
                    && planVigente.membresiaProgramadaId !== membresia.id && (
                      <span>
                        Este cambio reemplazará la programación anterior a{' '}
                        <strong>{planVigente.membresiaProgramadaNombre}</strong>.
                      </span>
                    )}
                </>
              ) : esMejoraInmediata ? (
                <>
                  <strong>Hoy pagás ${money(membershipQuote.importeAhora)}.</strong>
                  <span>
                    {esMejoraPorDiferencia
                      ? `El plan cambia inmediatamente y conserva el vencimiento del ${formatDateShort(membershipQuote.fechaFin)}.`
                      : `El plan superior se activa inmediatamente y tendrá vigencia hasta el ${formatDateShort(membershipQuote.fechaFin)}.`}
                  </span>
                </>
              ) : (
                <>
                  <strong>Hoy pagás ${money(membershipQuote.importeAhora)}.</strong>
                  <span>La membresía estará activa hasta el {formatDateShort(membershipQuote.fechaFin)}.</span>
                </>
              )}
            </div>
          )}
          {!sinFuncion && <SeatReservationTimer remainingSeconds={remainingSeconds} />}

          <form className="stack-form" onSubmit={handlePay}>
            <div className="checkout-customer">
              <div>
                <span>Comprador</span>
                <strong>{spectator.nombre} {spectator.apellido}</strong>
              </div>
              <div>
                <span>Correo electrónico</span>
                <strong>{spectator.email}</strong>
              </div>
            </div>

            <h2>{esCambioProgramado ? 'Método de pago para la renovación' : 'Método de pago'}</h2>
            {metodos.length > 0 && (
              <div className="auth-tabs">
                <button
                  type="button"
                  className={methodMode === 'saved' ? 'is-active' : ''}
                  onClick={() => setMethodMode('saved')}
                >
                  Tarjeta guardada
                </button>
                <button
                  type="button"
                  className={methodMode === 'new' ? 'is-active' : ''}
                  onClick={() => setMethodMode('new')}
                >
                  Nueva tarjeta
                </button>
              </div>
            )}

            {methodMode === 'saved' && metodos.length > 0 ? (
              <div className="checkout-card-options" role="radiogroup" aria-label="Tarjetas guardadas">
                {metodos.map((metodo) => {
                  const expired = paymentIsExpired(metodo.fechaVencimiento)
                  const selected = selectedMethodId === String(metodo.id)
                  return (
                    <label key={metodo.id} className="payment-card-option">
                      <input
                        type="radio"
                        name="metodoDePago"
                        value={metodo.id}
                        checked={selected}
                        disabled={expired}
                        onChange={(event) => setSelectedMethodId(event.target.value)}
                      />
                      <div className={`payment-card${expired ? ' is-expired' : ''}`}>
                        <div className="payment-card__top">
                          <span className="payment-card__chip" aria-hidden="true" />
                          <CreditCard size={22} aria-hidden="true" />
                        </div>
                        <div className="payment-card__number">•••• •••• •••• {metodo.ultimosNumeros}</div>
                        <div className="payment-card__details">
                          <div>
                            <span>Titular</span>
                            <strong>{metodo.titular || `${metodo.nombre} ${metodo.apellido || ''}`}</strong>
                          </div>
                          <div>
                            <span>Vence</span>
                            <strong>{formatExpiry(metodo.fechaVencimiento)}</strong>
                          </div>
                        </div>
                        {expired && <span className="payment-card__status">Vencida</span>}
                        {selected && !expired && (
                          <span className="payment-card__selected">
                            <Check size={14} aria-hidden="true" />
                            Seleccionada
                          </span>
                        )}
                      </div>
                    </label>
                  )
                })}
              </div>
            ) : (
              <div className="checkout-new-card">
                <div className="payment-card payment-card--preview" aria-label="Vista previa de la tarjeta">
                  <div className="payment-card__top">
                    <span className="payment-card__chip" aria-hidden="true" />
                    <CreditCard size={22} aria-hidden="true" />
                  </div>
                  <div className="payment-card__number">{cardPreviewNumber(card.numero)}</div>
                  <div className="payment-card__details">
                    <div>
                      <span>Titular</span>
                      <strong>{card.nombreTitular || 'NOMBRE Y APELLIDO'}</strong>
                    </div>
                    <div>
                      <span>Vence</span>
                      <strong>{formatExpiry(card.fechaVencimiento)}</strong>
                    </div>
                  </div>
                </div>

                <div className="form-grid payment-card-grid">
                  <label className="full">
                    Titular de la tarjeta
                    <input
                      value={card.nombreTitular}
                      onChange={(e) => setCard((prev) => ({ ...prev, nombreTitular: e.target.value }))}
                      required={methodMode === 'new' || metodos.length === 0}
                    />
                  </label>
                  <label className="full">
                    Número de tarjeta
                    <input
                      value={card.numero}
                      onChange={(e) => setCard((prev) => ({ ...prev, numero: formatCardNumber(e.target.value) }))}
                      placeholder="0000 0000 0000 0000"
                      inputMode="numeric"
                      maxLength="19"
                      required={methodMode === 'new' || metodos.length === 0}
                    />
                  </label>
                  <label>
                    Vencimiento (mes y año)
                    <ExpiryMonthPicker
                      min={nextMonthValue()}
                      value={card.fechaVencimiento}
                      onChange={(fechaVencimiento) =>
                        setCard((prev) => ({ ...prev, fechaVencimiento }))
                      }
                    />
                  </label>
                  <label className="payment-card-grid__cvv">
                    CVV
                    <input
                      type="password"
                      value={card.cvv}
                      onChange={(e) => setCard((prev) => ({ ...prev, cvv: e.target.value.replace(/\D/g, '').slice(0, 4) }))}
                      inputMode="numeric"
                      maxLength="4"
                      required={methodMode === 'new' || metodos.length === 0}
                    />
                  </label>
                </div>
                {!membershipOnly && (
                  <label className="checkbox-row checkout-save-card">
                    <input
                      type="checkbox"
                      checked={saveNewCard}
                      onChange={(event) => setSaveNewCard(event.target.checked)}
                    />
                    <span>Guardar esta tarjeta para futuras compras.</span>
                  </label>
                )}
              </div>
            )}

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={accepted}
                onChange={(e) => setAccepted(e.target.checked)}
              />
              <span>
                He leído y acepto la{' '}
                <Link to="/privacidad" target="_blank" rel="noreferrer">
                  Política de privacidad
                </Link>{' '}
                y las{' '}
                <Link to="/normas" target="_blank" rel="noreferrer">
                  Normas del cine
                </Link>.
              </span>
            </label>

            {(error || reservationError) && (
              <div className="info-box info-box--error">{error || reservationError}</div>
            )}

            <button
              type="submit"
              className="primary-button"
              disabled={loading || (!sinFuncion && functionHasStarted(funcion)) || (membershipOnly && (!membresia || !membershipQuote))}
            >
              {loading
                ? 'Procesando…'
                : esCambioProgramado
                  ? 'Programar cambio'
                  : `Pagar $${money(total)}`}
            </button>
            <p className="muted tiny">Pago seguro · Datos cifrados</p>
          </form>
        </section>

        <OrderSummary
          candyOnly={candyOnly}
          membership={membershipOnly ? membresia : null}
          membershipQuote={membershipOnly ? membershipQuote : null}
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumptions={draft.consumptions}
          onClearCandy={clearConsumptions}
          editSeatsTo={sinFuncion ? undefined : `/compra/${movieId}/butacas`}
          editCandyTo={membershipOnly ? undefined : candyOnly ? '/candy' : `/compra/${movieId}/candy`}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={total}
        />
      </div>
    </div>
  )
}
