import { Link, Navigate, useParams } from 'react-router-dom'
import CheckoutSummary from '../../components/compra/CheckoutSummary'
import MembershipCheckout from '../../components/compra/MembershipCheckout'
import PaymentMethodSelector from '../../components/compra/PaymentMethodSelector'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import PurchaseSuccess from '../../components/compra/PurchaseSuccess'
import SeatReservationTimer from '../../components/compra/SeatReservationTimer'
import Loader from '../../components/common/Loader'
import { useCheckout } from '../../hooks/useCheckout'
import { functionHasStarted, money } from '../../utils/format'

export default function CompraPagoPage({ candyOnly = false, membershipOnly = false }) {
  const { movieId } = useParams()
  const checkout = useCheckout({ movieId, candyOnly, membershipOnly })
  const {
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
    membership,
    membershipsLoading,
    currentPlan,
    quote,
    quoteLoading,
    isImmediateDifference,
    isImmediateFullPrice,
    isImmediateUpgrade,
    isScheduledChange,
  } = checkout

  if (loading && !ticket && !subscription) return <Loader />

  if (!isSpectator) {
    return (
      <Navigate
        to={membershipOnly ? '/club' : candyOnly ? '/candy' : `/compra/${movieId}/acceso`}
        replace
      />
    )
  }

  if (membershipOnly) {
    if (!subscription && (draft.purchaseType !== 'membership' || !draft.membershipId)) {
      return <Navigate to="/club" replace />
    }
    if (!subscription && !membership) {
      if (membershipsLoading) return <Loader />
      return <Navigate to="/club" replace />
    }
    if (!subscription && quoteLoading) return <Loader />
  } else if (candyOnly) {
    if (!ticket && (draft.purchaseType !== 'candy' || cantidadItemsCandy === 0)) {
      return <Navigate to="/candy" replace />
    }
  } else {
    if (!ticket && catalogLoading && (!funcion || !movie)) return <Loader />
    if (!ticket && (!funcion || !movie || draft.selectedSeats.length === 0)) {
      return <Navigate to={`/compra/${movieId}/butacas`} replace />
    }
    if (!ticket && !draft.candyVisitado) {
      return <Navigate to={`/compra/${movieId}/candy`} replace />
    }
  }

  if (ticket || subscription) {
    return (
      <PurchaseSuccess
        ticket={ticket}
        subscription={subscription}
        spectator={spectator}
        movie={movie}
        membership={membership}
        candyOnly={candyOnly}
        isImmediateDifference={isImmediateDifference}
        isImmediateFullPrice={isImmediateFullPrice}
      />
    )
  }

  if (loading) return <Loader />

  return (
    <div className="purchase-page purchase-page--checkout">
      {withoutFunction && (
        <Link
          to={membershipOnly ? '/club' : '/candy'}
          className="order-summary__action-button checkout-back-button"
        >
          {membershipOnly ? 'Volver al club' : 'Volver al candy'}
        </Link>
      )}
      {!withoutFunction && <PurchaseStepper current="pago" />}

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          {membershipOnly ? (
            <MembershipCheckout
              membership={membership}
              quote={quote}
              currentPlan={currentPlan}
              isImmediateDifference={isImmediateDifference}
              isImmediateUpgrade={isImmediateUpgrade}
              isScheduledChange={isScheduledChange}
            />
          ) : (
            <>
              <p className="eyebrow">
                {candyOnly
                  ? 'Confirmá tu pedido'
                  : 'El último paso antes de la gran pantalla'}
              </p>
              <h1>{withoutFunction ? 'Completá el pago' : 'Todo listo para tu noche'}</h1>
              <p>
                {candyOnly
                  ? 'Elegí cómo pagar tus productos. Enviaremos el QR a tu correo.'
                  : 'Revisá tu pedido y completá el pago. Enviaremos las entradas a tu correo.'}
              </p>
            </>
          )}

          {!withoutFunction && <SeatReservationTimer remainingSeconds={remainingSeconds} />}

          <form className="stack-form" onSubmit={handlePay}>
            <div className="checkout-customer">
              <div>
                <span>Comprador</span>
                <strong>{spectator.nombre} {spectator.apellido}</strong>
              </div>
              <div>
                <span>Correo electrónico</span>
                <strong>{spectator.email}</strong>
              </div>
            </div>

            <h2>{isScheduledChange ? 'Método de pago para la renovación' : 'Método de pago'}</h2>
            <PaymentMethodSelector
              methods={methods}
              mode={methodMode}
              onModeChange={setMethodMode}
              selectedMethodId={selectedMethodId}
              onSelectedMethodChange={setSelectedMethodId}
              card={card}
              onCardChange={setCard}
              saveCard={saveNewCard}
              onSaveCardChange={setSaveNewCard}
              forceSave={membershipOnly}
            />

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={accepted}
                onChange={(event) => setAccepted(event.target.checked)}
              />
              <span>
                He leído y acepto la{' '}
                <Link to="/privacidad" target="_blank" rel="noreferrer">
                  Política de privacidad
                </Link>{' '}
                y las{' '}
                <Link to="/normas" target="_blank" rel="noreferrer">
                  Normas del cine
                </Link>.
              </span>
            </label>

            {(error || reservationError) && (
              <div className="info-box info-box--error">{error || reservationError}</div>
            )}

            <button
              type="submit"
              className="primary-button"
              disabled={
                loading
                || (!withoutFunction && functionHasStarted(funcion))
                || (membershipOnly && (!membership || !quote))
              }
            >
              {loading
                ? 'Procesando…'
                : isScheduledChange
                  ? 'Programar cambio'
                  : `Pagar $${money(total)}`}
            </button>
            <p className="muted tiny">Pago de demostración · Usá datos de prueba</p>
          </form>
        </section>

        <CheckoutSummary
          candyOnly={candyOnly}
          membershipOnly={membershipOnly}
          movieId={movieId}
          movie={movie}
          funcion={funcion}
          membership={membership}
          membershipQuote={quote}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={total}
        />
      </div>
    </div>
  )
}
