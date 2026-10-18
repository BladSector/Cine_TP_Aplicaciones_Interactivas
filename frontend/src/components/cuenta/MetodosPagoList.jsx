import { useState } from 'react'
import { CreditCard, Plus, Trash2 } from 'lucide-react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { nextMonthValue, paymentIsExpired } from '../../utils/format'
import Modal from '../common/Modal'

const EMPTY_CARD = {
  numero: '',
  fechaVencimiento: '',
  nombreTitular: '',
  cvv: '',
}

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

export default function MetodosPagoList() {
  const { spectator, refreshSpectator } = useAuth()
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [showCardForm, setShowCardForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [card, setCard] = useState(EMPTY_CARD)

  function updateCard(event) {
    const { name, value } = event.target
    let nextValue = value
    if (name === 'numero') nextValue = formatCardNumber(value)
    if (name === 'cvv') nextValue = value.replace(/\D/g, '').slice(0, 4)
    if (name === 'nombreTitular') nextValue = value.replace(/[^A-Za-zÀ-ÿ' -]/g, '')
    setCard((prev) => ({ ...prev, [name]: nextValue }))
  }

  function closeCardForm() {
    setShowCardForm(false)
    setCard(EMPTY_CARD)
    setError('')
  }

  async function handleAddCard(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      if (paymentIsExpired(card.fechaVencimiento)) {
        throw new Error('La fecha de vencimiento debe ser posterior a la fecha actual.')
      }
      setSubmitting(true)
      await api.crearMetodoPago({
        numero: card.numero.replace(/\s/g, ''),
        fechaVencimiento: card.fechaVencimiento,
        nombreTitular: card.nombreTitular.trim(),
        cvv: card.cvv,
      })
      await refreshSpectator(spectator.id)
      closeCardForm()
      setMessage('Tarjeta agregada correctamente.')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteCard(id) {
    if (!window.confirm('¿Querés eliminar esta tarjeta de tus métodos de pago?')) return
    setError('')
    setMessage('')
    try {
      await api.eliminarMetodoPago(id)
      await refreshSpectator(spectator.id)
      setMessage('Tarjeta eliminada.')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  const paymentMethods = spectator.metodosDePago || []

  return (
    <section id="pagos" className="account-section account-section--wallet">
      <div className="wallet-heading">
        <div>
          <p className="eyebrow">Billetera</p>
          <h2>Métodos de pago</h2>
          <p className="muted">Elegí una tarjeta guardada al finalizar tu compra.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setShowCardForm(true)}>
          <Plus size={18} aria-hidden="true" />
          Agregar tarjeta
        </button>
      </div>

      {message && <div className="info-box">{message}</div>}
      {error && !showCardForm && <div className="info-box info-box--error">{error}</div>}

      {paymentMethods.length > 0 ? (
        <ul className="saved-card-grid">
          {paymentMethods.map((metodo) => {
            const expired = paymentIsExpired(metodo.fechaVencimiento)
            return (
              <li key={metodo.id} className={`payment-card${expired ? ' is-expired' : ''}`}>
                <div className="payment-card__top">
                  <span className="payment-card__chip" aria-hidden="true" />
                  <CreditCard size={23} aria-hidden="true" />
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
                <button
                  type="button"
                  className="payment-card__delete"
                  onClick={() => handleDeleteCard(metodo.id)}
                  aria-label={`Eliminar tarjeta terminada en ${metodo.ultimosNumeros}`}
                  title="Eliminar tarjeta"
                >
                  <Trash2 size={18} aria-hidden="true" />
                </button>
              </li>
            )
          })}
        </ul>
      ) : (
        <div className="wallet-empty">
          <CreditCard size={28} aria-hidden="true" />
          <div>
            <strong>Todavía no guardaste una tarjeta</strong>
            <p className="muted">Agregá una para pagar tus entradas más rápido.</p>
          </div>
        </div>
      )}

      {showCardForm && (
        <Modal title="Agregar método de pago" onClose={closeCardForm}>
          <div className="payment-card payment-card--preview" aria-label="Vista previa de la tarjeta">
            <div className="payment-card__top">
              <span className="payment-card__chip" aria-hidden="true" />
              <CreditCard size={23} aria-hidden="true" />
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

          <form className="stack-form payment-form" onSubmit={handleAddCard}>
            <label>
              Nombre y apellido del titular
              <input name="nombreTitular" value={card.nombreTitular} onChange={updateCard} autoComplete="cc-name" required />
            </label>
            <label>
              Número de tarjeta
              <input name="numero" inputMode="numeric" maxLength="19" value={card.numero} onChange={updateCard} autoComplete="cc-number" placeholder="0000 0000 0000 0000" required />
            </label>
            <div className="form-grid form-grid--two payment-form__row">
              <label>
                Vencimiento
                <input type="month" min={nextMonthValue()} name="fechaVencimiento" value={card.fechaVencimiento} onChange={updateCard} autoComplete="cc-exp" required />
              </label>
              <label>
                Código de seguridad
                <input type="password" name="cvv" inputMode="numeric" maxLength="4" value={card.cvv} onChange={updateCard} autoComplete="cc-csc" placeholder="CVV" required />
              </label>
            </div>
            {error && <div className="info-box info-box--error">{error}</div>}
            <button type="submit" className="primary-button" disabled={submitting}>
              {submitting ? 'Guardando…' : 'Guardar tarjeta'}
            </button>
          </form>
        </Modal>
      )}
    </section>
  )
}
