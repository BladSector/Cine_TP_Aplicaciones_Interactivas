import { useEffect, useRef, useState } from 'react'
import { api, errorMessage } from '../api/client'
import { useMembershipCheckout } from '../components/compra/MembershipCheckout'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'
import { useAccount } from '../context/AccountContext'
import { usePurchase } from '../context/PurchaseContext'
import { functionHasStarted, paymentIsExpired } from '../utils/format'
import { EMPTY_CARD } from '../utils/payment'

export function useCheckout({ movieId, candyOnly, membershipOnly }) {
  const { spectator, isSpectator, refreshSpectator } = useAuth()
  const { peliculas, funciones, productos, reload, loading: catalogLoading } = useCatalog()
  const { reloadTickets } = useAccount()
  const {
    draft,
    limpiarConsumos,
    reset,
    reservationError,
    remainingSeconds,
    paymentInProgress,
    setPaymentInProgress,
  } = usePurchase()

  const [accepted, setAccepted] = useState(false)
  const [methodMode, setMethodMode] = useState('saved')
  const [selectedMethodId, setSelectedMethodId] = useState('')
  const [saveNewCard, setSaveNewCard] = useState(false)
  const [card, setCard] = useState(EMPTY_CARD)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [ticket, setTicket] = useState(null)
  const [subscription, setSubscription] = useState(null)
  const [calculatedTickets, setCalculatedTickets] = useState(null)
  const submittingRef = useRef(false)

  const membershipCheckout = useMembershipCheckout({
    enabled: membershipOnly,
    membershipId: draft.membershipId,
    isSpectator,
    onError: setError,
  })

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)
  const methods = spectator?.metodosDePago || []
  const withoutFunction = candyOnly || membershipOnly

  useEffect(() => {
    const firstValid = methods.find((method) => !paymentIsExpired(method.fechaVencimiento))
    if (firstValid && !selectedMethodId) setSelectedMethodId(String(firstValid.id))
  }, [methods, selectedMethodId])

  useEffect(() => {
    if (withoutFunction || !funcion || !spectator || draft.selectedSeats.length === 0) return

    api.calcularPrecio(
      funcion.id,
      spectator.id,
      funcion.precioEntrada,
      draft.selectedSeats.length,
    )
      .then((response) => setCalculatedTickets(response.total))
      .catch(() => setCalculatedTickets(null))
  }, [withoutFunction, funcion, spectator, draft.selectedSeats.length])

  useEffect(() => {
    if (ticket || subscription) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }, [ticket, subscription])

  const cantidadItemsCandy = Object.values(draft.consumos).reduce(
    (sum, quantity) => sum + Number(quantity),
    0,
  )

  const totalTickets = withoutFunction || ticket
    ? 0
    : calculatedTickets ?? draft.selectedSeats.length * Number(funcion?.precioEntrada || 0)

  const totalCandy = membershipOnly || ticket
    ? 0
    : Object.entries(draft.consumos).reduce((sum, [id, quantity]) => {
        const product = productos.find((item) => item.id === Number(id))
        return sum + (product ? product.precio * quantity : 0)
      }, 0)

  const total = membershipOnly
    ? membershipCheckout.quote?.importeAhora ?? 0
    : totalTickets + totalCandy

  async function ensurePaymentMethod() {
    if (methodMode === 'saved' && selectedMethodId) {
      const method = methods.find((item) => item.id === Number(selectedMethodId))
      if (!method || paymentIsExpired(method.fechaVencimiento)) {
        throw new Error('El método de pago seleccionado no es válido o está vencido.')
      }
      return { id: Number(selectedMethodId), isNew: false }
    }

    if (!card.numero || !card.fechaVencimiento || !card.nombreTitular || !card.cvv) {
      throw new Error('Completá todos los datos de la tarjeta.')
    }
    if (paymentIsExpired(card.fechaVencimiento)) {
      throw new Error('La tarjeta está vencida. Ingresá una fecha posterior a la actual.')
    }

    const created = await api.crearMetodoPago({
      numero: card.numero.replace(/\s/g, ''),
      fechaVencimiento: card.fechaVencimiento,
      nombreTitular: card.nombreTitular,
      cvv: card.cvv,
    })
    return { id: created.id, isNew: true }
  }

  async function handlePay(event) {
    event.preventDefault()
    if (submittingRef.current || paymentInProgress) return

    if (!withoutFunction && functionHasStarted(funcion)) {
      setError('La función seleccionada ya comenzó. Elegí otro horario para continuar.')
      return
    }
    if (!accepted) {
      setError('Debés aceptar la política de privacidad y las normas del cine.')
      return
    }
    if (!spectator.emailVerificado) {
      setError('Debés verificar tu correo antes de realizar la compra.')
      return
    }

    submittingRef.current = true
    setPaymentInProgress(true)
    setLoading(true)
    setError('')
    let temporaryPaymentMethodId = null

    try {
      const paymentMethod = await ensurePaymentMethod()
      const metodoDePagoId = paymentMethod.id
      const guardarMetodoPago = !paymentMethod.isNew || membershipOnly || saveNewCard
      if (paymentMethod.isNew && !guardarMetodoPago) {
        temporaryPaymentMethodId = metodoDePagoId
      }

      const items = Object.entries(draft.consumos)
        .map(([productoId, cantidad]) => ({
          productoId: Number(productoId),
          cantidad: Number(cantidad),
        }))
        .filter((item) => item.cantidad > 0)

      const result = membershipOnly
        ? await api.comprarMembresia({
            espectadorId: spectator.id,
            metodoDePagoId,
            membresiaId: membershipCheckout.membership.id,
          })
        : candyOnly
          ? await api.comprarCandy({
              espectadorId: spectator.id,
              metodoDePagoId,
              guardarMetodoPago,
              items,
            })
          : await api.comprar({
              espectadorId: spectator.id,
              metodoDePagoId,
              funcionId: funcion.id,
              butacasIds: draft.selectedSeats.map((seat) => seat.id),
              guardarMetodoPago,
              items,
            })

      if (membershipOnly) {
        setSubscription(result)
        window.dispatchEvent(new CustomEvent('filmzone:membresia', { detail: result }))
      } else {
        setTicket(result)
      }

      if (candyOnly) limpiarConsumos()
      else if (!membershipOnly) reset()

      await reload()
      await reloadTickets()
      await refreshSpectator(spectator.id)
    } catch (err) {
      if (temporaryPaymentMethodId) {
        try {
          await api.eliminarMetodoPago(temporaryPaymentMethodId)
          await refreshSpectator(spectator.id)
        } catch {
          // Se conserva el error original de la compra aunque falle esta limpieza.
        }
      }
      setError(errorMessage(err))
    } finally {
      submittingRef.current = false
      setPaymentInProgress(false)
      setLoading(false)
    }
  }

  return {
    spectator,
    isSpectator,
    draft,
    movie,
    funcion,
    methods,
    withoutFunction,
    cantidadItemsCandy,
    totalTickets,
    totalCandy,
    total,
    accepted,
    setAccepted,
    methodMode,
    setMethodMode,
    selectedMethodId,
    setSelectedMethodId,
    saveNewCard,
    setSaveNewCard,
    card,
    setCard,
    loading,
    catalogLoading,
    error,
    ticket,
    subscription,
    reservationError,
    remainingSeconds,
    handlePay,
    ...membershipCheckout,
  }
}
