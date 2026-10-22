import { Link } from 'react-router-dom'
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
  consumptions,
  onClearCandy,
  editSeatsTo,
  totalTickets,
  totalCandy,
  total,
  footer,
  candyOnly = false,
}) {
  const selectedProducts = Object.entries(consumptions || {})
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
          <h2>{candyOnly ? 'Candy bar' : movie?.titulo}</h2>
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
              <Link to={editSeatsTo} className="text-button">
                Editar
              </Link>
            )}
          </div>
        </div>
      )}

      <div className="order-summary__block">
        {selectedProducts.length === 0 ? (
          <p className="muted">
            {candyOnly
              ? 'Todavía no seleccionaste productos.'
              : 'Candy bar no añadido. Puedes añadir productos o ir directamente al pago.'}
          </p>
        ) : (
          <>
            <div className="order-summary__row">
              <span>{candyOnly ? 'Productos seleccionados' : 'Candy bar · opcional'}</span>
              <strong>${money(totalCandy)}</strong>
            </div>
            <ul className="order-summary__candy">
              {selectedProducts.map((item) => (
                <li key={item.id}>
                  {item.qty} × {item.nombre}
                </li>
              ))}
            </ul>
            {onClearCandy && (
              <button type="button" className="text-button" onClick={onClearCandy}>
                Quitar candy bar
              </button>
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
    </aside>
  )
}
