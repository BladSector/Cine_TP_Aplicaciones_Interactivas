import { Clock3, ShoppingCart } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { money, seatLabel, shortTime } from '../../utils/format'
import ConfirmDialog from '../common/ConfirmDialog'

function functionDateLabel(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
  })
}

function timerLabel(seconds) {
  if (seconds === null || seconds === undefined) return '--:--'
  const minutes = Math.floor(seconds / 60)
  return `${minutes}:${String(seconds % 60).padStart(2, '0')}`
}

export default function ActiveCart() {
  const { peliculas, funciones, productos } = useCatalog()
  const {
    draft,
    hasActivePurchase,
    tieneCarritoCandy,
    cartItemCount,
    continuePath,
    cancelActivePurchase,
    reservationError,
    remainingSeconds,
  } = usePurchase()
  const navigate = useNavigate()
  const location = useLocation()
  const cartRef = useRef(null)
  const [open, setOpen] = useState(false)
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)
  const [cancelling, setCancelling] = useState(false)

  const movie = peliculas.find((item) => item.id === draft.movieId)
  const funcion = funciones.find((item) => item.id === draft.functionId)
  const consumosSeleccionados = Object.entries(draft.consumos)
    .map(([productId, quantity]) => ({
      product: productos.find((item) => item.id === Number(productId)),
      quantity: Number(quantity),
    }))
    .filter((item) => item.product && item.quantity > 0)

  const ticketsTotal = funcion
    ? draft.selectedSeats.length * Number(funcion.precioEntrada || 0)
    : 0
  const totalCandy = consumosSeleccionados.reduce(
    (total, item) => total + Number(item.product.precio || 0) * item.quantity,
    0,
  )
  const hasCart = hasActivePurchase || tieneCarritoCandy
  const inPurchaseFlow = location.pathname.startsWith('/compra/')
  const visible = hasCart && !inPurchaseFlow && location.pathname !== '/candy/pago'

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (cartRef.current && !cartRef.current.contains(event.target)) setOpen(false)
    }
    document.addEventListener('mousedown', closeOnOutsideClick)
    return () => document.removeEventListener('mousedown', closeOnOutsideClick)
  }, [])

  useEffect(() => {
    if (inPurchaseFlow) setOpen(false)
  }, [inPurchaseFlow])

  async function cancelPurchase() {
    setCancelling(true)
    try {
      await cancelActivePurchase()
      setShowCancelConfirm(false)
      setOpen(false)
    } finally {
      setCancelling(false)
    }
  }

  function continuePurchase() {
    if (!continuePath) return
    setOpen(false)
    navigate(continuePath)
  }

  if (!visible) return null

  return (
    <>
      <div className="header-cart" ref={cartRef}>
        <button
          type="button"
          className={`header-cart__toggle ${open ? 'is-active' : ''}`}
          aria-label={`Compra en curso, ${cartItemCount} productos`}
          title="Continuar compra"
          onClick={() => setOpen((current) => !current)}
        >
          <ShoppingCart size={20} aria-hidden="true" />
          <span className="header-cart__badge">{cartItemCount}</span>
        </button>

        {open && (
          <div className="header-cart__panel">
            <div className="header-cart__heading">
              <div>
                <span>Compra en curso</span>
                <strong>{hasActivePurchase ? movie?.titulo || 'Tu película' : 'Candy'}</strong>
              </div>
              {hasActivePurchase && (
                <span className={`header-cart__timer ${remainingSeconds !== null && remainingSeconds <= 120 ? 'is-warning' : ''}`}>
                  <Clock3 size={16} aria-hidden="true" />
                  {timerLabel(remainingSeconds)}
                </span>
              )}
            </div>

            {funcion && (
              <div className="header-cart__function">
                <span>{functionDateLabel(funcion.fecha)} · {shortTime(funcion.horario)}</span>
                <strong>
                  {funcion.salaNombre} · Butacas {draft.selectedSeats.map(seatLabel).join(', ')}
                </strong>
              </div>
            )}

            {hasActivePurchase && (
              <div className="header-cart__line">
                <span>
                  {draft.selectedSeats.length} entrada{draft.selectedSeats.length === 1 ? '' : 's'}
                </span>
                <strong>${money(ticketsTotal)}</strong>
              </div>
            )}

            {consumosSeleccionados.map(({ product, quantity }) => (
              <div key={product.id} className="header-cart__line">
                <span>{quantity} × {product.nombre}</span>
                <strong>${money(product.precio * quantity)}</strong>
              </div>
            ))}

            {reservationError && <p className="header-cart__error">{reservationError}</p>}

            <div className="header-cart__total">
              <span>Subtotal estimado</span>
              <strong>${money(ticketsTotal + totalCandy)}</strong>
            </div>

            <div className="header-cart__actions">
              <button
                type="button"
                className="text-button text-button--danger"
                onClick={() => setShowCancelConfirm(true)}
              >
                Cancelar compra
              </button>
              <button type="button" className="primary-button" onClick={continuePurchase}>
                Continuar
              </button>
            </div>
          </div>
        )}
      </div>

      {showCancelConfirm && (
        <ConfirmDialog
          title="Cancelar compra"
          message="Se vaciará el carrito y las butacas seleccionadas volverán a quedar disponibles."
          confirmLabel="Cancelar compra"
          cancelLabel="Conservar compra"
          danger
          loading={cancelling}
          onConfirm={cancelPurchase}
          onCancel={() => setShowCancelConfirm(false)}
        />
      )}
    </>
  )
}
