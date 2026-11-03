import { ChevronDown, ChevronUp } from 'lucide-react'
import { useId, useState } from 'react'
import { money } from '../../utils/format'

export default function MobileCheckoutBar({ label, total, disabled = false, onContinue, details }) {
  const [open, setOpen] = useState(false)
  const detailsId = useId()

  return (
    <>
      {details && open && (
        <div id={detailsId} className="mobile-checkout-drawer">
          {details}
        </div>
      )}
      <div className={`mobile-checkout-bar${open ? ' is-open' : ''}`} aria-label="Resumen de compra">
        <button
          type="button"
          className="mobile-checkout-bar__summary-toggle"
          aria-expanded={open}
          aria-controls={details ? detailsId : undefined}
          disabled={!details}
          onClick={() => details && setOpen((current) => !current)}
        >
          <span className="mobile-checkout-bar__total" aria-live="polite">
            <span>{label}</span>
            <strong>${money(total)}</strong>
          </span>
          {details && (open
            ? <ChevronDown size={18} aria-hidden="true" />
            : <ChevronUp size={18} aria-hidden="true" />)}
        </button>
        <button
          type="button"
          className="primary-button"
          disabled={disabled}
          onClick={onContinue}
        >
          Continuar
        </button>
      </div>
    </>
  )
}
