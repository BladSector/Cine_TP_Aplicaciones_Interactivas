import { useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmDialog from '../common/ConfirmDialog'
import {
  formatDateShort,
  formatFunction,
  formatLanguage,
  money,
  seatLabel,
  shortTime,
} from '../../utils/format'

export default function OrderSummary({
  movie,
  funcion,
  seats,
  products,
  consumos,
  onLimpiarCandy,
  editSeatsTo,
  editarCandyEn,
  totalTickets,
  totalCandy,
  total,
  footer,
  candyOnly = false,
  membership = null,
  membershipQuote = null,
}) {
  const [showClearCandyConfirm, setShowClearCandyConfirm] = useState(false)

  if (membership) {
    return (
      <aside className="order-summary">
        <div>
          <p className="eyebrow">Tu membresía</p>
          <h2>{membership.nombre}</h2>
          <p className="muted">{membership.descripcion}</p>
        </div>
        <div className="order-summary__block">
          <div className="order-summary__row">
            <span>Precio mensual</span>
            <strong>${money(membership.precioMensual)}</strong>
          </div>
          <ul className="order-summary__candy">
            {membership.beneficios.map((beneficio) => (
              <li key={beneficio}>{beneficio}</li>
            ))}
          </ul>
        </div>
        <div className="order-summary__total">
          <span>
            {membershipQuote?.tipo === 'CAMBIO_PROGRAMADO' ? 'A cobrar hoy' : 'Total a pagar'}
          </span>
          <strong>${money(total)}</strong>
        </div>
        {membershipQuote?.tipo === 'CAMBIO_PROGRAMADO' && (
          <div className="order-summary__scheduled">
            <span>Próximo cobro</span>
            <strong>${money(membershipQuote.importeRenovacion)}</strong>
            <small>{formatDateShort(membershipQuote.fechaInicio)}</small>
          </div>
        )}
        <p className="muted tiny">
          {membershipQuote?.tipo === 'MEJORA_INMEDIATA_DIFERENCIA'
            ? 'Importe correspondiente a la diferencia entre planes'
            : membershipQuote?.tipo === 'MEJORA_INMEDIATA_COMPLETA'
              ? 'El plan superior se activa hoy por un nuevo mes'
            : membershipQuote?.tipo === 'CAMBIO_PROGRAMADO'
              ? 'El plan actual continúa vigente hasta su vencimiento'
              : 'IVA incluido · Vigencia de un mes'}
        </p>
        {footer}
      </aside>
    )
  }

  const selectedProducts = Object.entries(consumos || {})
    .map(([id, qty]) => {
      const product = products.find((item) => item.id === Number(id))
      return product ? { ...product, qty } : null
    })
    .filter(Boolean)

  return (
    <aside className="order-summary">
      <div
        className={!candyOnly
          ? `order-summary__movie${movie?.portadaUrl ? '' : ' order-summary__movie--without-poster'}`
          : undefined}
      >
        {!candyOnly && movie?.portadaUrl && (
          <img
            className="order-summary__poster"
            src={movie.portadaUrl}
            alt={`Portada de ${movie.titulo}`}
          />
        )}
        <div>
          <p className="eyebrow">{candyOnly ? 'Tu pedido' : 'Tu noche de cine'}</p>
          <h2>{candyOnly ? 'Candy' : movie?.titulo}</h2>
          {!candyOnly && (
            <p className="muted">
              {movie?.duracion} min
              {funcion && (
                <>
                  {' · '}
                  {formatFunction(funcion.formato)} · {formatLanguage(funcion.idioma)}
                </>
              )}
            </p>
          )}
        </div>
      </div>

      {funcion && (
        <div className="order-summary__block">
          <p>
            <strong>{formatDateShort(funcion.fecha)}</strong>
          </p>
          <p>
            {shortTime(funcion.horario)} · Función confirmada · {funcion.salaNombre}
          </p>
        </div>
      )}

      {seats?.length > 0 && (
        <div className="order-summary__block">
          <div className="order-summary__row">
            <span>
              {seats.length} entrada{seats.length > 1 ? 's' : ''} × ${money(funcion?.precioEntrada)}
            </span>
            <strong>${money(totalTickets)}</strong>
          </div>
          <div className="order-summary__seats">
            {seats.map((seat) => (
              <span key={seat.id} className="seat-chip">
                {seatLabel(seat)}
              </span>
            ))}
            {editSeatsTo && (
              <Link to={editSeatsTo} className="order-summary__action-button">
                Editar butacas
              </Link>
            )}
          </div>
        </div>
      )}

      <div className="order-summary__block">
        {selectedProducts.length === 0 ? (
          <div className="order-summary__empty-candy">
            <p className="muted">
              {candyOnly
                ? 'Todavía no seleccionaste productos.'
                : 'No agregaste productos del candy.'}
            </p>
            {editarCandyEn && (
              <Link to={editarCandyEn} className="order-summary__action-button">
                Volver al candy
              </Link>
            )}
          </div>
        ) : (
          <>
            <div className="order-summary__row">
              <span>{candyOnly ? 'Productos seleccionados' : 'Candy · opcional'}</span>
              <strong>${money(totalCandy)}</strong>
            </div>
            <ul className="order-summary__candy">
              {selectedProducts.map((item) => (
                <li key={item.id}>
                  {item.qty} × {item.nombre}
                </li>
              ))}
            </ul>
            {(editarCandyEn || onLimpiarCandy) && (
              <div className="order-summary__candy-actions">
                {editarCandyEn && (
                  <Link to={editarCandyEn} className="order-summary__action-button">
                    Editar candy
                  </Link>
                )}
                {onLimpiarCandy && (
                  <button
                    type="button"
                    className="order-summary__action-button order-summary__action-button--danger"
                    onClick={() => setShowClearCandyConfirm(true)}
                  >
                    Quitar productos
                  </button>
                )}
              </div>
            )}
          </>
        )}
      </div>

      <div className="order-summary__total">
        <span>Total</span>
        <strong>${money(total)}</strong>
      </div>
      <p className="muted tiny">IVA incluido · Sin cargos adicionales</p>

      {footer}

      {showClearCandyConfirm && (
        <ConfirmDialog
          title="Quitar productos"
          message="¿Querés quitar todos los productos seleccionados del candy?"
          confirmLabel="Quitar productos"
          cancelLabel="Conservar productos"
          danger
          onConfirm={() => {
            onLimpiarCandy()
            setShowClearCandyConfirm(false)
          }}
          onCancel={() => setShowClearCandyConfirm(false)}
        />
      )}
    </aside>
  )
}
