import { useEffect, useState } from 'react'
import { api, errorMessage } from '../api/client'

export default function useSeatReservationHeartbeat(functionId, selectedSeats, enabled = true) {
  const [reservationError, setReservationError] = useState('')
  const seatIds = selectedSeats.map((seat) => seat.id).sort((a, b) => a - b).join(',')

  useEffect(() => {
    if (!enabled || !functionId || !seatIds) {
      setReservationError('')
      return undefined
    }

    let active = true
    const ids = seatIds.split(',').map(Number)

    async function renew() {
      try {
        await Promise.all(ids.map((butacaId) => api.reservarButaca(functionId, butacaId)))
        if (active) setReservationError('')
      } catch (error) {
        if (active) setReservationError(errorMessage(error))
      }
    }

    renew()
    const timer = window.setInterval(renew, 60_000)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [enabled, functionId, seatIds])

  return reservationError
}
