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
  consumptions: {},
  candyVisited: false,
  returnPath: null,
  membershipId: null,
}

function loadStoredDraft() {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY)
    if (!stored) return initialState
    const parsed = JSON.parse(stored)
    return {
      ...initialState,
      ...parsed,
      selectedSeats: Array.isArray(parsed.selectedSeats) ? parsed.selectedSeats : [],
      consumptions: parsed.consumptions && typeof parsed.consumptions === 'object'
        ? parsed.consumptions
        : {},
    }
  } catch {
    return initialState
  }
}

export function PurchaseProvider({ children }) {
  const [draft, setDraft] = useState(loadStoredDraft)
  const [reservationExpiredNotice, setReservationExpiredNotice] = useState(false)
  const {
    reservationError,
    remainingSeconds,
    expired,
  } = useSeatReservationHeartbeat(
    draft.functionId,
    draft.selectedSeats,
    draft.purchaseType === 'movie' && draft.selectedSeats.length > 0,
  )

  const reset = useCallback(() => {
    setDraft(initialState)
    setReservationExpiredNotice(false)
  }, [])

  useEffect(() => {
    if (draft.purchaseType) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(draft))
    else sessionStorage.removeItem(STORAGE_KEY)
  }, [draft])

  const startFromMovie = useCallback((movieId, returnPath = null) => {
    setDraft({
      ...initialState,
      purchaseType: 'movie',
      movieId,
      returnPath,
    })
  }, [])

  const startCandyOnly = useCallback(() => {
    setDraft({
      ...initialState,
      purchaseType: 'candy',
      candyVisited: true,
    })
  }, [])

  const startMembership = useCallback((membershipId) => {
    setDraft({
      ...initialState,
      purchaseType: 'membership',
      membershipId,
    })
  }, [])

  const selectFunction = useCallback((functionId) => {
    setDraft((prev) => ({
      ...prev,
      functionId,
      selectedSeats: [],
      consumptions: {},
      candyVisited: false,
    }))
  }, [])

  const toggleSeat = useCallback((butaca) => {
    setDraft((prev) => {
      const exists = prev.selectedSeats.some((seat) => seat.id === butaca.id)
      return {
        ...prev,
        selectedSeats: exists
          ? prev.selectedSeats.filter((seat) => seat.id !== butaca.id)
          : [...prev.selectedSeats, butaca],
        candyVisited: false,
      }
    })
  }, [])

  const setConsumption = useCallback((productId, quantity) => {
    setDraft((prev) => {
      const next = { ...prev.consumptions }
      if (quantity > 0) next[productId] = quantity
      else delete next[productId]
      return { ...prev, consumptions: next }
    })
  }, [])

  const clearConsumptions = useCallback(() => {
    setDraft((prev) => ({ ...prev, consumptions: {} }))
  }, [])

  const markCandyVisited = useCallback(() => {
    setDraft((prev) => ({ ...prev, candyVisited: true }))
  }, [])

  const expireSeatReservation = useCallback(() => {
    setDraft((prev) => ({
      ...prev,
      selectedSeats: [],
      consumptions: {},
      candyVisited: false,
    }))
  }, [])

  useEffect(() => {
    if (!expired || draft.selectedSeats.length === 0) return
    setReservationExpiredNotice(true)
    expireSeatReservation()
  }, [draft.selectedSeats.length, expired, expireSeatReservation])

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
    + Object.values(draft.consumptions).reduce((sum, quantity) => sum + Number(quantity), 0)

  const continuePath = hasActivePurchase
    ? draft.candyVisited
      ? `/compra/${draft.movieId}/pago`
      : `/compra/${draft.movieId}/candy`
    : null

  const value = useMemo(
    () => ({
      draft,
      reset,
      startFromMovie,
      startCandyOnly,
      startMembership,
      selectFunction,
      toggleSeat,
      setConsumption,
      clearConsumptions,
      markCandyVisited,
      expireSeatReservation,
      cancelActivePurchase,
      hasActivePurchase,
      cartItemCount,
      continuePath,
      reservationError,
      remainingSeconds,
      reservationExpiredNotice,
      dismissReservationExpiredNotice: () => setReservationExpiredNotice(false),
      setDraft,
    }),
    [
      draft,
      reset,
      startFromMovie,
      startCandyOnly,
      startMembership,
      selectFunction,
      toggleSeat,
      setConsumption,
      clearConsumptions,
      markCandyVisited,
      expireSeatReservation,
      cancelActivePurchase,
      hasActivePurchase,
      cartItemCount,
      continuePath,
      reservationError,
      remainingSeconds,
      reservationExpiredNotice,
    ],
  )

  return <PurchaseContext.Provider value={value}>{children}</PurchaseContext.Provider>
}

export function usePurchase() {
  const ctx = useContext(PurchaseContext)
  if (!ctx) throw new Error('usePurchase debe usarse dentro de PurchaseProvider')
  return ctx
}
