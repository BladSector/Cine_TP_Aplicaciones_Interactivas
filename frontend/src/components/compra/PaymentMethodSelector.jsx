import { nextMonthValue, paymentIsExpired } from '../../utils/format'
import { formatCardNumber } from '../../utils/payment'
import PaymentCard from '../common/PaymentCard'
import ExpiryMonthPicker from './ExpiryMonthPicker'

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
                <PaymentCard method={method} selected={selected} />
              </label>
            )
          })}
        </div>
      ) : (
        <div className="checkout-new-card">
          <PaymentCard card={card} />

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
