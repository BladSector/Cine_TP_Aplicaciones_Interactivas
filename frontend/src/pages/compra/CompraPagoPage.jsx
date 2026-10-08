import { useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { money, paymentIsExpired } from '../../utils/format'

export default function CompraPagoPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { spectator, isSpectator, refreshSpectator } = useAuth()
  const { peliculas, funciones, productos, reload } = useCatalog()
  const { draft, reset, clearConsumptions } = usePurchase()
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

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)
  const metodos = spectator?.metodosDePago || []

  if (!isSpectator) return <Navigate to={`/compra/${movieId}/acceso`} replace />
  if (!funcion || !movie || draft.selectedSeats.length === 0) {
    return <Navigate to={`/compra/${movieId}/butacas`} replace />
  }

  const totalTickets = draft.selectedSeats.length * funcion.precioEntrada
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
    if (!accepted) {
      setError('Debés aceptar las condiciones de compra.')
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

      const result = await api.comprar({
        espectadorId: spectator.id,
        metodoDePagoId,
        funcionId: funcion.id,
        butacasIds: draft.selectedSeats.map((seat) => seat.id),
        items,
      })

      await reload()
      await refreshSpectator(spectator.id)
      setTicket(result)
      reset()
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
          <h1>¡Listo para la gran pantalla!</h1>
          <p>
            Enviamos el comprobante a <strong>{spectator.email}</strong>. También podés verlo en Mi
            cuenta.
          </p>
          <div className="ticket-card">
            <p className="eyebrow">Ticket #{ticket.id}</p>
            <h2>{ticket.entradas?.[0]?.pelicula || movie.titulo}</h2>
            <p>QR: {ticket.codigoQR}</p>
            <p>Total: ${money(ticket.total)}</p>
            <p>
              Butacas:{' '}
              {(ticket.entradas || []).map((entrada) => entrada.butaca).join(', ') || '-'}
            </p>
          </div>
          <div className="summary-actions">
            <button type="button" className="primary-button" onClick={() => navigate('/cuenta')}>
              Ir a Mi cuenta
            </button>
            <button type="button" className="secondary-button" onClick={() => navigate('/')}>
              Volver a cartelera
            </button>
          </div>
        </section>
      </div>
    )
  }

  return (
    <div className="purchase-page">
      <Link to={`/compra/${movieId}/candy`} className="back-link">
        Volver al candy bar
      </Link>
      <PurchaseStepper current="pago" />

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">El último paso antes de la gran pantalla</p>
          <h1>Todo listo para tu noche</h1>
          <p>Revisa tu pedido y completa el pago. Enviaremos las entradas a tu correo.</p>

          <form className="stack-form" onSubmit={handlePay}>
            <div className="form-grid">
              <label>
                Nombre
                <input value={spectator.nombre} readOnly />
              </label>
              <label>
                Apellidos
                <input value={spectator.apellido} readOnly />
              </label>
            </div>
            <label>
              Correo electrónico
              <input value={spectator.email} readOnly />
            </label>

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
              <label>
                Tarjeta
                <select
                  value={selectedMethodId}
                  onChange={(e) => setSelectedMethodId(e.target.value)}
                  required
                >
                  <option value="">Seleccionar…</option>
                  {metodos.map((metodo) => (
                    <option
                      key={metodo.id}
                      value={metodo.id}
                      disabled={paymentIsExpired(metodo.fechaVencimiento)}
                    >
                      **** {metodo.ultimosNumeros} · vence {metodo.fechaVencimiento}
                      {paymentIsExpired(metodo.fechaVencimiento) ? ' (vencida)' : ''}
                    </option>
                  ))}
                </select>
              </label>
            ) : (
              <div className="form-grid">
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
                    onChange={(e) => setCard((prev) => ({ ...prev, numero: e.target.value }))}
                    placeholder="16 dígitos"
                    required={methodMode === 'new' || metodos.length === 0}
                  />
                </label>
                <label>
                  Fecha de caducidad
                  <input
                    type="month"
                    value={card.fechaVencimiento}
                    onChange={(e) =>
                      setCard((prev) => ({ ...prev, fechaVencimiento: e.target.value }))
                    }
                    required={methodMode === 'new' || metodos.length === 0}
                  />
                </label>
                <label>
                  CVV
                  <input
                    type="password"
                    value={card.cvv}
                    onChange={(e) => setCard((prev) => ({ ...prev, cvv: e.target.value }))}
                    required={methodMode === 'new' || metodos.length === 0}
                  />
                </label>
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

            {error && <div className="info-box info-box--error">{error}</div>}

            <button type="submit" className="primary-button" disabled={loading}>
              {loading ? 'Procesando…' : `Pagar $${money(total)}`}
            </button>
            <p className="muted tiny">Pago seguro · Datos cifrados</p>
          </form>
        </section>

        <OrderSummary
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumptions={draft.consumptions}
          onClearCandy={clearConsumptions}
          editSeatsTo={`/compra/${movieId}/butacas`}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={total}
        />
      </div>
    </div>
  )
}
