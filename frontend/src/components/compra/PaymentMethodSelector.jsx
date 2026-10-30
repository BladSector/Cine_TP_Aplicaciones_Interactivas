import { Check, CreditCard } from 'lucide-react'
import { nextMonthValue, paymentIsExpired } from '../../utils/format'
import ExpiryMonthPicker from './ExpiryMonthPicker'

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

export default function PaymentMethodSelector({
  methods,
  mode,
  onModeChange,
  selectedMethodId,
  onSelectedMethodChange,
  card,
  onCardChange,
  saveCard,
  onSaveCardChange,
  forceSave = false,
}) {
  const newCardRequired = mode === 'new' || methods.length === 0

  function updateCard(field, value) {
    onCardChange((previous) => ({ ...previous, [field]: value }))
  }

  return (
    <>
      {methods.length > 0 && (
        <div className="auth-tabs">
          <button
            type="button"
            className={mode === 'saved' ? 'is-active' : ''}
            onClick={() => onModeChange('saved')}
          >
            Tarjeta guardada
          </button>
          <button
            type="button"
            className={mode === 'new' ? 'is-active' : ''}
            onClick={() => onModeChange('new')}
          >
            Nueva tarjeta
          </button>
        </div>
      )}

      {mode === 'saved' && methods.length > 0 ? (
        <div className="checkout-card-options" role="radiogroup" aria-label="Tarjetas guardadas">
          {methods.map((method) => {
            const expired = paymentIsExpired(method.fechaVencimiento)
            const selected = selectedMethodId === String(method.id)
            return (
              <label key={method.id} className="payment-card-option">
                <input
                  type="radio"
                  name="metodoDePago"
                  value={method.id}
                  checked={selected}
                  disabled={expired}
                  onChange={(event) => onSelectedMethodChange(event.target.value)}
                />
                <div className={`payment-card${expired ? ' is-expired' : ''}`}>
                  <div className="payment-card__top">
                    <span className="payment-card__chip" aria-hidden="true" />
                    <CreditCard size={22} aria-hidden="true" />
                  </div>
                  <div className="payment-card__number">•••• •••• •••• {method.ultimosNumeros}</div>
                  <div className="payment-card__details">
                    <div>
                      <span>Titular</span>
                      <strong>{method.titular || `${method.nombre} ${method.apellido || ''}`}</strong>
                    </div>
                    <div>
                      <span>Vence</span>
                      <strong>{formatExpiry(method.fechaVencimiento)}</strong>
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
                onChange={(event) => updateCard('nombreTitular', event.target.value)}
                required={newCardRequired}
              />
            </label>
            <label className="full">
              Número de tarjeta
              <input
                value={card.numero}
                onChange={(event) => updateCard('numero', formatCardNumber(event.target.value))}
                placeholder="0000 0000 0000 0000"
                inputMode="numeric"
                maxLength="19"
                required={newCardRequired}
              />
            </label>
            <label>
              Vencimiento (mes y año)
              <ExpiryMonthPicker
                min={nextMonthValue()}
                value={card.fechaVencimiento}
                onChange={(fechaVencimiento) => updateCard('fechaVencimiento', fechaVencimiento)}
              />
            </label>
            <label className="payment-card-grid__cvv">
              CVV
              <input
                type="password"
                value={card.cvv}
                onChange={(event) => updateCard('cvv', event.target.value.replace(/\D/g, '').slice(0, 4))}
                inputMode="numeric"
                maxLength="4"
                required={newCardRequired}
              />
            </label>
          </div>
          {!forceSave && (
            <label className="checkbox-row checkout-save-card">
              <input
                type="checkbox"
                checked={saveCard}
                onChange={(event) => onSaveCardChange(event.target.checked)}
              />
              <span>Guardar esta tarjeta para futuras compras.</span>
            </label>
          )}
        </div>
      )}
    </>
  )
}
