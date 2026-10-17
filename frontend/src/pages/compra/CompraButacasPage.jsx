import { useEffect } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import SeatMap from '../../components/compra/SeatMap'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { formatFunction, formatLanguage, functionHasStarted, money, seatLabel, shortTime } from '../../utils/format'

export default function CompraButacasPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { isSpectator } = useAuth()
  const { peliculas, funciones, butacas, entradas, productos, reload } = useCatalog()
  const { draft, toggleSeat } = usePurchase()

  useEffect(() => {
    if (isSpectator) reload()
  }, [isSpectator, reload])

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)

  if (!isSpectator) return <Navigate to={`/compra/${movieId}/acceso`} replace />
  if (!funcion || !movie) return <Navigate to={`/pelicula/${movieId}`} replace />
  if (functionHasStarted(funcion)) {
    return <Navigate to={`/pelicula/${movieId}`} replace state={{ expiredFunction: true }} />
  }

  const roomSeats = butacas
    .filter((seat) => seat.salaId === funcion.salaId)
    .sort((a, b) =>
      `${a.fila}${a.numero}`.localeCompare(`${b.fila}${b.numero}`, 'es', { numeric: true }),
    )

  function isUnavailable(butaca) {
    if (butaca.estado !== 'DISPONIBLE') return true
    return entradas.some(
      (entrada) =>
        entrada.funcionId === funcion.id &&
        entrada.butacaId === butaca.id &&
        entrada.estado !== 'REEMBOLSADA' &&
        entrada.estado !== 'CANCELADA',
    )
  }

  const totalTickets = draft.selectedSeats.length * funcion.precioEntrada

  return (
    <div className="purchase-page">
      <Link to={`/pelicula/${movieId}`} className="back-link">
        Cambiar función
      </Link>
      <PurchaseStepper current="butacas" />

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">Tu función está confirmada</p>
          <h1>Elige tu lugar en la historia</h1>
          <p>
            Selecciona tus butacas en el mapa. Cada entrada cuesta ${money(funcion.precioEntrada)}.
          </p>
          <p className="muted">
            {shortTime(funcion.horario)} · {formatFunction(funcion.formato)} ·{' '}
            {formatLanguage(funcion.idioma)} · {funcion.salaNombre}
          </p>

          <SeatMap
            seats={roomSeats}
            selectedSeats={draft.selectedSeats}
            isUnavailable={isUnavailable}
            onToggle={toggleSeat}
          />

          {draft.selectedSeats.length > 0 && (
            <p className="selection-note">
              {draft.selectedSeats.length} butaca
              {draft.selectedSeats.length > 1 ? 's' : ''} seleccionada
              {draft.selectedSeats.length > 1 ? 's' : ''} ·{' '}
              {draft.selectedSeats.map(seatLabel).join(', ')}
            </p>
          )}
        </section>

        <OrderSummary
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumptions={draft.consumptions}
          totalTickets={totalTickets}
          totalCandy={0}
          total={totalTickets}
          footer={
            <div className="summary-actions">
              <button
                type="button"
                className="primary-button"
                disabled={draft.selectedSeats.length === 0}
                onClick={() => navigate(`/compra/${movieId}/candy`)}
              >
                Añadir candy bar
              </button>
              <button
                type="button"
                className="secondary-button"
                disabled={draft.selectedSeats.length === 0}
                onClick={() => navigate(`/compra/${movieId}/pago`)}
              >
                Ir directamente a pagar
              </button>
            </div>
          }
        />
      </div>
    </div>
  )
}
