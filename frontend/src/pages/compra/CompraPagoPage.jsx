import { useEffect, useState } from 'react'
import { Check, CreditCard } from 'lucide-react'
import { Link, Navigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import ExpiryMonthPicker from '../../components/compra/ExpiryMonthPicker'
import TicketDetails from '../../components/tickets/TicketDetails'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { functionHasStarted, money, nextMonthValue, paymentIsExpired } from '../../utils/format'
import useSeatReservationHeartbeat from '../../hooks/useSeatReservationHeartbeat'

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

export default function CompraPagoPage({ candyOnly = false }) {
  const { movieId } = useParams()
  const { spectator, isSpectator, refreshSpectator } = useAuth()
  const { peliculas, funciones, productos, reload } = useCatalog()
  const { draft, clearConsumptions } = usePurchase()
  const [accepted, setAccepted] = useState(false)
  const [methodMode, setMethodMode] = useState('saved')
  const [selectedMethodId, setSelectedMethodId] = useState('')
  const [card, setCard] = useState({
    numero: '',
    fechaVencimiento: '',
    nombreTitular: '',
    cvv: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [ticket, setTicket] = useState(null)
  const [calculatedTickets, setCalculatedTickets] = useState(null)
  const reservationError = useSeatReservationHeartbeat(
    draft.functionId,
    draft.selectedSeats,
    !candyOnly && !ticket,
  )

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)
  const metodos = spectator?.metodosDePago || []

  useEffect(() => {
    const firstValid = metodos.find((metodo) => !paymentIsExpired(metodo.fechaVencimiento))
    if (firstValid && !selectedMethodId) setSelectedMethodId(String(firstValid.id))
  }, [metodos, selectedMethodId])

  useEffect(() => {
    if (candyOnly || !funcion || !spectator || draft.selectedSeats.length === 0) return
    api.calcularPrecio(
      funcion.id,
      spectator.id,
      funcion.precioEntrada,
      draft.selectedSeats.length,
    ).then((response) => setCalculatedTickets(response.total)).catch(() => setCalculatedTickets(null))
  }, [candyOnly, funcion, spectator, draft.selectedSeats.length])

  useEffect(() => {
    if (ticket) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, [ticket])

  const candyItemCount = Object.values(draft.consumptions).reduce(
    (sum, quantity) => sum + Number(quantity),
    0,
  )

  if (!isSpectator) {
    return <Navigate to={candyOnly ? '/candy' : `/compra/${movieId}/acceso`} replace />
  }
  if (candyOnly) {
    if (!ticket && (draft.purchaseType !== 'candy' || candyItemCount === 0)) {
      return <Navigate to="/candy" replace />
    }
  } else {
    if (!funcion || !movie || draft.selectedSeats.length === 0) {
      return <Navigate to={`/compra/${movieId}/butacas`} replace />
    }
    if (!draft.candyVisited) {
      return <Navigate to={`/compra/${movieId}/candy`} replace />
    }
  }

  const totalTickets = candyOnly
    ? 0
    : calculatedTickets ?? draft.selectedSeats.length * funcion.precioEntrada
  const totalCandy = Object.entries(draft.consumptions).reduce((sum, [id, qty]) => {
    const product = productos.find((item) => item.id === Number(id))
    return sum + (product ? product.precio * qty : 0)
  }, 0)
  const total = totalTickets + totalCandy

  async function ensurePaymentMethod() {
    if (methodMode === 'saved' && selectedMethodId) {
      const metodo = metodos.find((item) => item.id === Number(selectedMethodId))
      if (!metodo || paymentIsExpired(metodo.fechaVencimiento)) {
        throw new Error('El método de pago seleccionado no es válido o está vencido.')
      }
      return Number(selectedMethodId)
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
    await refreshSpectator(spectator.id)
    return created.id
  }

  async function handlePay(event) {
    event.preventDefault()
    if (!candyOnly && functionHasStarted(funcion)) {
      setError('La función seleccionada ya comenzó. Elegí otro horario para continuar.')
      return
    }
    if (!accepted) {
      setError('Debés aceptar las condiciones de compra.')
      return
    }
    if (!spectator.emailVerificado) {
      setError('Debés verificar tu correo antes de realizar la compra.')
      return
    }

    setLoading(true)
    setError('')
    try {
      const metodoDePagoId = await ensurePaymentMethod()
      const items = Object.entries(draft.consumptions)
        .map(([productoId, cantidad]) => ({
          productoId: Number(productoId),
          cantidad: Number(cantidad),
        }))
        .filter((item) => item.cantidad > 0)

      const result = candyOnly
        ? await api.comprarCandy({
            espectadorId: spectator.id,
            metodoDePagoId,
            items,
          })
        : await api.comprar({
            espectadorId: spectator.id,
            metodoDePagoId,
            funcionId: funcion.id,
            butacasIds: draft.selectedSeats.map((seat) => seat.id),
            items,
          })

      await reload()
      await refreshSpectator(spectator.id)
      setTicket(result)
      if (candyOnly) clearConsumptions()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
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
          <TicketDetails ticket={ticket} />
        </section>
      </div>
    )
  }

  return (
    <div className="purchase-page">
      <Link to={candyOnly ? '/candy' : `/compra/${movieId}/candy`} className="back-link">
        {candyOnly ? 'Volver al Candy' : 'Volver al candy bar'}
      </Link>
      {!candyOnly && <PurchaseStepper current="pago" />}

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">
            {candyOnly ? 'Confirmá tu pedido' : 'El último paso antes de la gran pantalla'}
          </p>
          <h1>{candyOnly ? 'Completá el pago' : 'Todo listo para tu noche'}</h1>
          <p>
            {candyOnly
              ? 'Elegí cómo pagar tus productos. Enviaremos el QR a tu correo.'
              : 'Revisá tu pedido y completá el pago. Enviaremos las entradas a tu correo.'}
          </p>

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

            <h2>Método de pago</h2>
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
              </div>
            )}

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={accepted}
                onChange={(e) => setAccepted(e.target.checked)}
              />
              He leído y acepto las condiciones de compra y la política de privacidad.
            </label>

            {(error || reservationError) && (
              <div className="info-box info-box--error">{error || reservationError}</div>
            )}

            <button
              type="submit"
              className="primary-button"
              disabled={loading || (!candyOnly && functionHasStarted(funcion))}
            >
              {loading ? 'Procesando…' : `Pagar $${money(total)}`}
            </button>
            <p className="muted tiny">Pago seguro · Datos cifrados</p>
          </form>
        </section>

        <OrderSummary
          candyOnly={candyOnly}
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumptions={draft.consumptions}
          onClearCandy={clearConsumptions}
          editSeatsTo={candyOnly ? undefined : `/compra/${movieId}/butacas`}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={total}
        />
      </div>
    </div>
  )
}
