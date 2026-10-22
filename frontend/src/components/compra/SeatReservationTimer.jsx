import { Clock3 } from 'lucide-react'

export default function SeatReservationTimer({ remainingSeconds }) {
  const remainingLabel = remainingSeconds === null
    ? null
    : `${Math.floor(remainingSeconds / 60)}:${String(remainingSeconds % 60).padStart(2, '0')}`
  const warning = remainingSeconds !== null && remainingSeconds <= 120

  return (
    <div
      className={`reservation-timer${warning ? ' is-warning' : ''}`}
      role="timer"
    >
      <Clock3 size={22} aria-hidden="true" />
      <div>
        <strong>
          {remainingLabel
            ? `${warning ? 'Tu reserva vence en' : 'Butacas reservadas por'} ${remainingLabel}`
            : 'Confirmando la reserva de tus butacas…'}
        </strong>
        <span>
          {warning
            ? 'Completá el pago para no perder tu selección.'
            : 'El tiempo comenzó al seleccionar la primera butaca.'}
        </span>
      </div>
    </div>
  )
}
