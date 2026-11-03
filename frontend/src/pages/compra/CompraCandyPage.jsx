import { useEffect } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import CandyCatalog from '../../components/compra/CandyCatalog'
import MobileCheckoutBar from '../../components/compra/MobileCheckoutBar'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import SeatReservationTimer from '../../components/compra/SeatReservationTimer'
import Loader from '../../components/common/Loader'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'

export default function CompraCandyPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { isSpectator } = useAuth()
  const { peliculas, funciones, productos, loading: catalogLoading } = useCatalog()
  const {
    draft,
    actualizarConsumo,
    limpiarConsumos,
    marcarCandyVisitado,
    reservationError,
    remainingSeconds,
  } = usePurchase()

  useEffect(() => {
    marcarCandyVisitado()
  }, [marcarCandyVisitado])

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)
  if (!isSpectator) return <Navigate to={`/compra/${movieId}/acceso`} replace />
  if (catalogLoading && (!funcion || !movie)) return <Loader />
  if (!funcion || !movie || draft.selectedSeats.length === 0) {
    return <Navigate to={`/compra/${movieId}/butacas`} replace />
  }

  const totalTickets = draft.selectedSeats.length * funcion.precioEntrada
  const totalCandy = Object.entries(draft.consumos).reduce((sum, [id, quantity]) => {
    const product = productos.find((item) => item.id === Number(id))
    return sum + (product ? product.precio * quantity : 0)
  }, 0)
  const selectedItems = Object.values(draft.consumos).reduce(
    (total, quantity) => total + Number(quantity),
    0,
  )

  function continueToPayment() {
    navigate(`/compra/${movieId}/pago`)
  }

  return (
    <div className="purchase-page purchase-page--candy">
      <PurchaseStepper current="candy" />

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">Un extra para tu película · opcional</p>
          <h1>El cine sabe mejor así</h1>
          <p>Agregá tus favoritos o continuá al pago solo con tus entradas.</p>
          <SeatReservationTimer remainingSeconds={remainingSeconds} />
          {reservationError && (
            <div className="info-box info-box--error">{reservationError}</div>
          )}

          <CandyCatalog
            products={productos}
            consumos={draft.consumos}
            onChange={actualizarConsumo}
          />
        </section>

        <OrderSummary
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumos={draft.consumos}
          onLimpiarCandy={limpiarConsumos}
          editSeatsTo={`/compra/${movieId}/butacas`}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={totalTickets + totalCandy}
          footer={(
            <div className="summary-actions">
              <button type="button" className="primary-button" onClick={continueToPayment}>
                Continuar al pago
              </button>
            </div>
          )}
        />
      </div>

      <MobileCheckoutBar
        label={`${draft.selectedSeats.length} entrada${draft.selectedSeats.length === 1 ? '' : 's'}${selectedItems > 0 ? ` · ${selectedItems} producto${selectedItems === 1 ? '' : 's'}` : ''}`}
        total={totalTickets + totalCandy}
        onContinue={continueToPayment}
        details={(
          <OrderSummary
            movie={movie}
            funcion={funcion}
            seats={draft.selectedSeats}
            products={productos}
            consumos={draft.consumos}
            onLimpiarCandy={limpiarConsumos}
            editSeatsTo={`/compra/${movieId}/butacas`}
            totalTickets={totalTickets}
            totalCandy={totalCandy}
            total={totalTickets + totalCandy}
          />
        )}
      />
    </div>
  )
}
