import { Check } from 'lucide-react'
import { Link } from 'react-router-dom'
import CompraExitosaPage from '../../pages/compra/CompraExitosaPage'
import TicketDetails from '../tickets/TicketDetails'
import { formatDateShort, money } from '../../utils/format'

export default function PurchaseSuccess({
  ticket,
  subscription,
  spectator,
  movie,
  membership,
  candyOnly,
  isImmediateDifference,
  isImmediateFullPrice,
}) {
  if (subscription) {
    const hasScheduledChange = Boolean(subscription.membresiaProgramadaId)
    const benefits = hasScheduledChange
      ? membership?.beneficios || []
      : subscription.beneficios

    return (
      <CompraExitosaPage>
          <p className="eyebrow">
            {hasScheduledChange ? 'Cambio programado' : 'Membresía confirmada'}
          </p>
          <h1>
            {hasScheduledChange
              ? 'Tu próximo plan está confirmado'
              : 'Ya sos parte del Club Film Zone'}
          </h1>

          {hasScheduledChange ? (
            <p>
              <strong>{subscription.nombre}</strong> continúa activo hasta el{' '}
              {formatDateShort(subscription.fechaFin)}. El{' '}
              {formatDateShort(subscription.fechaCambioProgramado)} comenzará{' '}
              <strong>{subscription.membresiaProgramadaNombre}</strong> y se cobrarán{' '}
              <strong>${money(subscription.precioProgramado)}</strong>.
            </p>
          ) : (
            <p>
              Enviamos la confirmación a <strong>{spectator.email}</strong>.{' '}
              Tu plan <strong>{subscription.nombre}</strong> está activo hasta el{' '}
              {formatDateShort(subscription.fechaFin)}.
            </p>
          )}

          <ul className="membership-success__benefits">
            {benefits.map((benefit) => (
              <li key={benefit}>
                <Check size={16} aria-hidden="true" />
                <span>{benefit}</span>
              </li>
            ))}
          </ul>

          <p className="muted">
            {hasScheduledChange
              ? 'Hoy no se realizó ningún cobro.'
              : isImmediateDifference
                ? `Pagaste $${money(subscription.precioCobrado)} correspondientes a la diferencia entre planes.`
                : isImmediateFullPrice
                  ? `Pagaste $${money(subscription.precioCobrado)} por el nuevo mes de membresía.`
                  : `Pagaste $${money(subscription.precioCobrado)} por este mes.`}
          </p>

          <Link to="/club" className="primary-button">
            Volver al club
          </Link>
      </CompraExitosaPage>
    )
  }

  if (!ticket) return null

  return (
    <CompraExitosaPage>
        <p className="eyebrow">Compra confirmada</p>
        <h1>{candyOnly ? '¡Tu pedido está listo!' : '¡Listo para la gran pantalla!'}</h1>
        <p>
          Enviamos el comprobante a <strong>{spectator.email}</strong>.{' '}
          {candyOnly
            ? 'Presentá el QR en el candy para retirar tus productos.'
            : 'Tus entradas ya están listas. También podés verlas en Mi cuenta.'}
        </p>
        <TicketDetails ticket={ticket} movie={movie} />
    </CompraExitosaPage>
  )
}
