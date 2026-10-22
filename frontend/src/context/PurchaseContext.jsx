import { createContext, useCallback, useContext, useMemo, useState } from 'react'

const PurchaseContext = createContext(null)

const initialState = {
  purchaseType: null,
  movieId: null,
  functionId: null,
  selectedSeats: [],
  consumptions: {},
  candyVisited: false,
  returnPath: null,
}

export function PurchaseProvider({ children }) {
  const [draft, setDraft] = useState(initialState)

  const reset = useCallback(() => setDraft(initialState), [])

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

  const value = useMemo(
    () => ({
      draft,
      reset,
      startFromMovie,
      startCandyOnly,
      selectFunction,
      toggleSeat,
      setConsumption,
      clearConsumptions,
      markCandyVisited,
      expireSeatReservation,
      setDraft,
    }),
    [
      draft,
      reset,
      startFromMovie,
      startCandyOnly,
      selectFunction,
      toggleSeat,
      setConsumption,
      clearConsumptions,
      markCandyVisited,
      expireSeatReservation,
    ],
  )

  return <PurchaseContext.Provider value={value}>{children}</PurchaseContext.Provider>
}

export function usePurchase() {
  const ctx = useContext(PurchaseContext)
  if (!ctx) throw new Error('usePurchase debe usarse dentro de PurchaseProvider')
  return ctx
}
