import { useCallback, useEffect, useRef, useState } from 'react'
import { Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import SeatMap from '../../components/compra/SeatMap'
import SeatReservationTimer from '../../components/compra/SeatReservationTimer'
import MobileCheckoutBar from '../../components/compra/MobileCheckoutBar'
import Loader from '../../components/common/Loader'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { api, errorMessage } from '../../api/client'
import { formatFunction, formatLanguage, functionHasStarted, money, seatLabel, shortTime } from '../../utils/format'

export default function CompraButacasPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { isSpectator } = useAuth()
  const { peliculas, funciones, butacas, productos, reload, loading: catalogLoading } = useCatalog()
  const {
    draft,
    toggleSeat,
    reservationError: timerError,
    remainingSeconds,
  } = usePurchase()
  const [reservations, setReservations] = useState([])
  const [occupiedSeatIds, setOccupiedSeatIds] = useState([])
  const [availabilityReady, setAvailabilityReady] = useState(false)
  const selectionRef = useRef(null)
  const availabilityRequestRef = useRef(0)
  const [pendingSeatIds, setPendingSeatIds] = useState([])
  const [reservationError, setReservationError] = useState('')

  useEffect(() => {
    if (isSpectator) reload()
  }, [isSpectator, reload])

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)

  useEffect(() => {
    const selection = { functionId: funcion?.id }
    selectionRef.current = selection
    setReservations([])
    setOccupiedSeatIds([])
    setAvailabilityReady(false)
    setPendingSeatIds([])
    return () => {
      selectionRef.current = null
    }
  }, [funcion?.id, movieId])

  const loadReservations = useCallback(async () => {
    if (!isSpectator || !funcion?.id) return
    const selection = selectionRef.current
    const requestId = ++availabilityRequestRef.current
    try {
      const [data, occupied] = await Promise.all([
        api.getReservasButacas(funcion.id),
        api.getButacasOcupadas(funcion.id),
      ])
      if (selectionRef.current !== selection || availabilityRequestRef.current !== requestId) return
      if (!Array.isArray(data) || !Array.isArray(occupied)) {
        throw new Error('No se pudo obtener la disponibilidad de las butacas.')
      }
      setReservations(data)
      setOccupiedSeatIds(occupied)
      setAvailabilityReady(true)
    } catch (error) {
      if (selectionRef.current === selection && availabilityRequestRef.current === requestId) {
        setReservationError(errorMessage(error))
      }
    }
  }, [isSpectator, funcion?.id])

  useEffect(() => {
    loadReservations()
    const timer = window.setInterval(loadReservations, 5_000)
    return () => window.clearInterval(timer)
  }, [loadReservations])

  if (!isSpectator) return <Navigate to={`/compra/${movieId}/acceso`} replace />
  if (catalogLoading && (!funcion || !movie)) return <Loader />
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
    if (!availabilityReady) return true
    if (butaca.estado !== 'DISPONIBLE') return true
    if (reservations.some((reservation) => reservation.butacaId === butaca.id && !reservation.propia)) {
      return true
    }
    return occupiedSeatIds.includes(butaca.id)
  }

  async function handleToggleSeat(butaca) {
    const selection = selectionRef.current
    if (!selection || selection.functionId !== funcion.id) return
    const selected = draft.selectedSeats.some((seat) => seat.id === butaca.id)
    setPendingSeatIds((ids) => [...ids, butaca.id])
    setReservationError('')
    try {
      if (selected) {
        await api.liberarReservaButaca(funcion.id, butaca.id)
        if (selectionRef.current !== selection) return
        setReservations((current) => current.filter((item) => item.butacaId !== butaca.id))
      } else {
        const reservation = await api.reservarButaca(funcion.id, butaca.id)
        if (selectionRef.current !== selection) return
        if (!reservation || typeof reservation !== 'object') {
          throw new Error('No se pudo reservar la butaca seleccionada.')
        }
        setReservations((current) => [
          ...current.filter((item) => item.butacaId !== butaca.id),
          reservation,
        ])
      }
      toggleSeat(butaca)
    } catch (error) {
      if (selectionRef.current !== selection) return
      setReservationError(errorMessage(error))
      await loadReservations()
    } finally {
      if (selectionRef.current === selection) {
        setPendingSeatIds((ids) => ids.filter((id) => id !== butaca.id))
      }
    }
  }

  const totalTickets = draft.selectedSeats.length * funcion.precioEntrada
  const totalCandy = Object.entries(draft.consumos).reduce((sum, [id, quantity]) => {
    const product = productos.find((item) => item.id === Number(id))
    return sum + (product ? product.precio * quantity : 0)
  }, 0)

  return (
    <div className="purchase-page purchase-page--seats">
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
          {location.state?.reservationExpired && (
            <div className="info-box info-box--error">
              Finalizó el tiempo de reserva. Volvé a seleccionar tus butacas.
            </div>
          )}

          {draft.selectedSeats.length > 0 ? (
            <SeatReservationTimer remainingSeconds={remainingSeconds} />
          ) : (
            <p className="reservation-note">
              Al seleccionar la primera butaca, comenzará una reserva de 10 minutos.
            </p>
          )}

          <SeatMap
            seats={roomSeats}
            selectedSeats={draft.selectedSeats}
            isUnavailable={isUnavailable}
            isPending={(seat) => pendingSeatIds.includes(seat.id)}
            onToggle={handleToggleSeat}
          />
          {(reservationError || timerError) && (
            <div className="info-box info-box--error">{reservationError || timerError}</div>
          )}

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
          consumos={draft.consumos}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={totalTickets + totalCandy}
          footer={
            <div className="summary-actions">
              <button
                type="button"
                className="primary-button"
                disabled={draft.selectedSeats.length === 0}
                onClick={() => navigate(`/compra/${movieId}/candy`)}
              >
                Continuar
              </button>
            </div>
          }
        />
      </div>

      <MobileCheckoutBar
        label={`${draft.selectedSeats.length} butaca${draft.selectedSeats.length === 1 ? '' : 's'}`}
        total={totalTickets + totalCandy}
        disabled={draft.selectedSeats.length === 0}
        onContinue={() => navigate(`/compra/${movieId}/candy`)}
        details={(
          <OrderSummary
            movie={movie}
            funcion={funcion}
            seats={draft.selectedSeats}
            products={productos}
            consumos={draft.consumos}
            totalTickets={totalTickets}
            totalCandy={totalCandy}
            total={totalTickets + totalCandy}
          />
        )}
      />
    </div>
  )
}
