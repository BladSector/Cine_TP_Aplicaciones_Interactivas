import { Check, CreditCard } from 'lucide-react'
import { paymentIsExpired } from '../../utils/format'
import { cardPreviewNumber, formatExpiry } from '../../utils/payment'

export default function PaymentCard({ as: Tag = 'div', method, card, selected = false, iconSize = 22, children }) {
  const preview = Boolean(card)
  const expired = !preview && paymentIsExpired(method.fechaVencimiento)
  const holder = preview
    ? card.nombreTitular || 'NOMBRE Y APELLIDO'
    : method.titular || [method.nombre, method.apellido].filter(Boolean).join(' ') || 'NOMBRE Y APELLIDO'

  return (
    <Tag
      className={`payment-card${preview ? ' payment-card--preview' : ''}${expired ? ' is-expired' : ''}`}
      aria-label={preview ? 'Vista previa de la tarjeta' : undefined}
    >
      <div className="payment-card__top">
        <span className="payment-card__chip" aria-hidden="true" />
        <CreditCard size={iconSize} aria-hidden="true" />
      </div>
      <div className="payment-card__number">
        {preview ? cardPreviewNumber(card.numero) : `•••• •••• •••• ${method.ultimosNumeros}`}
      </div>
      <div className="payment-card__details">
        <div>
          <span>Titular</span>
          <strong>{holder}</strong>
        </div>
        <div>
          <span>Vence</span>
          <strong>{formatExpiry(preview ? card.fechaVencimiento : method.fechaVencimiento)}</strong>
        </div>
      </div>
      {expired && <span className="payment-card__status">Vencida</span>}
      {selected && !expired && (
        <span className="payment-card__selected">
          <Check size={14} aria-hidden="true" />
          Seleccionada
        </span>
      )}
      {children}
    </Tag>
  )
}
