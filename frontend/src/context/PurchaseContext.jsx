import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'
import useSeatReservationHeartbeat from '../hooks/useSeatReservationHeartbeat'

const PurchaseContext = createContext(null)
const STORAGE_KEY = 'film-zone-purchase-draft'

const initialState = {
  purchaseType: null,
  movieId: null,
  functionId: null,
  selectedSeats: [],
  consumos: {},
  candyVisitado: false,
  membershipId: null,
}

function loadStoredDraft() {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY)
    if (!stored) return initialState

    const parsed = JSON.parse(stored)
    const {
      consumptions: consumosAnteriores,
      candyVisited: candyVisitadoAnterior,
      ...storedDraft
    } = parsed
    const consumos = parsed.consumos ?? consumosAnteriores

    return {
      ...initialState,
      ...storedDraft,
      purchaseType: parsed.purchaseType === 'kiosco' ? 'candy' : parsed.purchaseType,
      selectedSeats: Array.isArray(parsed.selectedSeats) ? parsed.selectedSeats : [],
      consumos: consumos && typeof consumos === 'object' ? consumos : {},
      candyVisitado: parsed.candyVisitado ?? parsed.kioscoVisitado ?? candyVisitadoAnterior ?? false,
    }
  } catch {
    return initialState
  }
}

export function PurchaseProvider({ children }) {
  const [draft, setDraft] = useState(loadStoredDraft)
  const [paymentInProgress, setPaymentInProgress] = useState(false)
  const [reservationExpiredNotice, setReservationExpiredNotice] = useState(false)
  const {
    reservationError,
    remainingSeconds,
    expired,
  } = useSeatReservationHeartbeat(
    draft.functionId,
    draft.selectedSeats,
    draft.purchaseType === 'movie' && draft.selectedSeats.length > 0 && !paymentInProgress,
  )

  const reset = useCallback(() => {
    setDraft(initialState)
    setReservationExpiredNotice(false)
  }, [])

  useEffect(() => {
    if (draft.purchaseType) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(draft))
    else sessionStorage.removeItem(STORAGE_KEY)
  }, [draft])

  const startMoviePurchase = useCallback((movieId, functionId) => {
    setDraft((previous) => ({
      ...initialState,
      purchaseType: 'movie',
      movieId,
      functionId,
      consumos: previous.consumos,
    }))
  }, [])

  const iniciarCompraCandy = useCallback(() => {
    setDraft((previous) => ({
      ...initialState,
      purchaseType: 'candy',
      consumos: previous.consumos,
      candyVisitado: true,
    }))
  }, [])

  const startMembership = useCallback((membershipId) => {
    setDraft({
      ...initialState,
      purchaseType: 'membership',
      membershipId,
    })
  }, [])

  const toggleSeat = useCallback((butaca) => {
    setDraft((previous) => {
      const exists = previous.selectedSeats.some((seat) => seat.id === butaca.id)
      return {
        ...previous,
        selectedSeats: exists
          ? previous.selectedSeats.filter((seat) => seat.id !== butaca.id)
          : [...previous.selectedSeats, butaca],
        candyVisitado: false,
      }
    })
  }, [])

  const actualizarConsumo = useCallback((productId, quantity) => {
    setDraft((previous) => {
      const next = { ...previous.consumos }
      if (quantity > 0) next[productId] = quantity
      else delete next[productId]
      return { ...previous, consumos: next }
    })
  }, [])

  const limpiarConsumos = useCallback(() => {
    setDraft((previous) => ({ ...previous, consumos: {} }))
  }, [])

  const marcarCandyVisitado = useCallback(() => {
    setDraft((previous) => ({ ...previous, candyVisitado: true }))
  }, [])

  const expireSeatReservation = useCallback(() => {
    setDraft((previous) => ({
      ...previous,
      selectedSeats: [],
      consumos: {},
      candyVisitado: false,
    }))
  }, [])

  useEffect(() => {
    if (paymentInProgress || !expired || draft.selectedSeats.length === 0) return
    setReservationExpiredNotice(true)
    expireSeatReservation()
  }, [draft.selectedSeats.length, expired, expireSeatReservation, paymentInProgress])

  const cancelActivePurchase = useCallback(async () => {
    const functionId = draft.functionId
    const selectedSeats = [...draft.selectedSeats]

    if (functionId && selectedSeats.length > 0) {
      await Promise.allSettled(
        selectedSeats.map((seat) => api.liberarReservaButaca(functionId, seat.id)),
      )
    }

    setDraft(initialState)
    setReservationExpiredNotice(false)
  }, [draft.functionId, draft.selectedSeats])

  const hasActivePurchase = draft.purchaseType === 'movie'
    && Boolean(draft.movieId)
    && Boolean(draft.functionId)
    && draft.selectedSeats.length > 0

  const cartItemCount = draft.selectedSeats.length
    + Object.values(draft.consumos).reduce((sum, quantity) => sum + Number(quantity), 0)

  const tieneCarritoCandy = draft.purchaseType === 'candy'
    && Object.keys(draft.consumos).length > 0

  const continuePath = hasActivePurchase
    ? draft.candyVisitado
      ? `/compra/${draft.movieId}/pago`
      : `/compra/${draft.movieId}/candy`
    : tieneCarritoCandy
      ? '/candy/pago'
      : null

  const value = useMemo(
    () => ({
      draft,
      reset,
      startMoviePurchase,
      iniciarCompraCandy,
      startMembership,
      toggleSeat,
      actualizarConsumo,
      limpiarConsumos,
      marcarCandyVisitado,
      expireSeatReservation,
      cancelActivePurchase,
      hasActivePurchase,
      tieneCarritoCandy,
      cartItemCount,
      continuePath,
      reservationError,
      remainingSeconds,
      paymentInProgress,
      setPaymentInProgress,
      reservationExpiredNotice,
      dismissReservationExpiredNotice: () => setReservationExpiredNotice(false),
    }),
    [
      draft,
      reset,
      startMoviePurchase,
      iniciarCompraCandy,
      startMembership,
      toggleSeat,
      actualizarConsumo,
      limpiarConsumos,
      marcarCandyVisitado,
      expireSeatReservation,
      cancelActivePurchase,
      hasActivePurchase,
      tieneCarritoCandy,
      cartItemCount,
      continuePath,
      reservationError,
      remainingSeconds,
      paymentInProgress,
      reservationExpiredNotice,
    ],
  )

  return <PurchaseContext.Provider value={value}>{children}</PurchaseContext.Provider>
}

export function usePurchase() {
  const context = useContext(PurchaseContext)
  if (!context) throw new Error('usePurchase debe usarse dentro de PurchaseProvider')
  return context
}
