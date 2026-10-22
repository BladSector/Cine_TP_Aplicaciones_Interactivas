import { useEffect, useMemo, useState } from 'react'
import { api, errorMessage } from '../api/client'

export default function useSeatReservationHeartbeat(functionId, selectedSeats, enabled = true) {
  const [reservationError, setReservationError] = useState('')
  const [expiresAt, setExpiresAt] = useState(null)
  const [currentTime, setCurrentTime] = useState(Date.now())
  const [expired, setExpired] = useState(false)
  const seatIds = selectedSeats.map((seat) => seat.id).sort((a, b) => a - b).join(',')

  useEffect(() => {
    if (!enabled || !functionId || !seatIds) {
      setReservationError('')
      setExpiresAt(null)
      setExpired(false)
      return undefined
    }

    let active = true
    const ids = seatIds.split(',').map(Number)

    async function checkReservations() {
      try {
        const reservations = await api.getReservasButacas(functionId)
        if (!active) return

        const ownReservations = (reservations || []).filter(
          (reservation) => reservation.propia && ids.includes(reservation.butacaId),
        )
        if (ownReservations.length !== ids.length) {
          setExpired(true)
          setReservationError('La reserva de tus butacas venció.')
          return
        }

        const expirations = ownReservations
          .map((reservation) => new Date(reservation.venceEn).getTime())
          .filter(Number.isFinite)
        if (expirations.length !== ids.length) {
          throw new Error('No se pudo obtener el vencimiento de la reserva.')
        }

        setExpiresAt(Math.min(...expirations))
        setCurrentTime(Date.now())
        setExpired(false)
        setReservationError('')
      } catch (error) {
        if (active) setReservationError(errorMessage(error))
      }
    }

    checkReservations()
    const timer = window.setInterval(checkReservations, 5_000)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [enabled, functionId, seatIds])

  useEffect(() => {
    if (!enabled || expiresAt === null || expired) return undefined

    const timer = window.setInterval(() => {
      const now = Date.now()
      setCurrentTime(now)
      if (now >= expiresAt) setExpired(true)
    }, 1_000)
    return () => window.clearInterval(timer)
  }, [enabled, expiresAt, expired])

  const remainingSeconds = useMemo(() => {
    if (expiresAt === null) return null
    return Math.max(0, Math.ceil((expiresAt - currentTime) / 1_000))
  }, [currentTime, expiresAt])

  return {
    reservationError,
    remainingSeconds,
    expired,
  }
}
