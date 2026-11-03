import { ShoppingCart, X } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { usePurchase } from '../../context/PurchaseContext'

export default function ReservationExpiredNotice() {
  const location = useLocation()
  const {
    hasActivePurchase,
    reservationExpiredNotice,
    dismissReservationExpiredNotice,
  } = usePurchase()
  const previousPathRef = useRef(location.pathname)
  const [showResumeNotice, setShowResumeNotice] = useState(false)

  useEffect(() => {
    const previousWasPurchaseFlow = previousPathRef.current.startsWith('/compra/')
    const isPurchaseFlow = location.pathname.startsWith('/compra/')

    if (!hasActivePurchase) setShowResumeNotice(false)

    if (previousWasPurchaseFlow && !isPurchaseFlow && hasActivePurchase) {
      setShowResumeNotice(true)
      previousPathRef.current = location.pathname
      const timer = window.setTimeout(() => setShowResumeNotice(false), 6_000)
      return () => window.clearTimeout(timer)
    }

    previousPathRef.current = location.pathname
    return undefined
  }, [hasActivePurchase, location.pathname])

  return (
    <>
      {showResumeNotice && (
        <div className="cart-resume-toast" role="status">
          <ShoppingCart size={20} aria-hidden="true" />
          <div>
            <strong>Tu compra sigue en curso</strong>
            <span>Podés retomarla desde el carrito antes de que venza la reserva.</span>
          </div>
          <button
            type="button"
            className="reservation-expired-toast__close"
            aria-label="Cerrar aviso"
            onClick={() => setShowResumeNotice(false)}
          >
            <X size={18} aria-hidden="true" />
          </button>
        </div>
      )}

      {reservationExpiredNotice && (
        <div className="reservation-expired-toast" role="status">
          <div>
            <strong>La reserva venció</strong>
            <span>La compra se canceló y las butacas volvieron a estar disponibles.</span>
          </div>
          <button
            type="button"
            className="reservation-expired-toast__close"
            aria-label="Cerrar aviso"
            onClick={dismissReservationExpiredNotice}
          >
            <X size={18} aria-hidden="true" />
          </button>
        </div>
      )}
    </>
  )
}
