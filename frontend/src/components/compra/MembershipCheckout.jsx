import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../api/client'
import { formatDateShort, money } from '../../utils/format'

export function useMembershipCheckout({ enabled, membershipId, isSpectator, onError }) {
  const [memberships, setMemberships] = useState([])
  const [membershipsLoading, setMembershipsLoading] = useState(enabled)
  const [currentPlan, setCurrentPlan] = useState(null)
  const [quote, setQuote] = useState(null)
  const [quoteLoading, setQuoteLoading] = useState(enabled)

  useEffect(() => {
    if (!enabled) return undefined
    let cancelled = false

    setMembershipsLoading(true)
    api.getMembresias()
      .then((data) => {
        if (!cancelled) setMemberships(data || [])
      })
      .catch(() => {
        if (!cancelled) setMemberships([])
      })
      .finally(() => {
        if (!cancelled) setMembershipsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [enabled])

  useEffect(() => {
    if (!enabled || !membershipId || !isSpectator) return undefined
    let cancelled = false

    setQuoteLoading(true)
    onError('')
    api.cotizarMembresia(membershipId)
      .then((data) => {
        if (!cancelled) setQuote(data)
      })
      .catch((error) => {
        if (!cancelled) onError(errorMessage(error))
      })
      .finally(() => {
        if (!cancelled) setQuoteLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [enabled, isSpectator, membershipId, onError])

  useEffect(() => {
    if (!enabled) return undefined
    let cancelled = false

    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setCurrentPlan(data)
      })
      .catch(() => {
        if (!cancelled) setCurrentPlan(null)
      })

    return () => {
      cancelled = true
    }
  }, [enabled])

  const membership = memberships.find((item) => item.id === Number(membershipId))
  const isImmediateDifference = quote?.tipo === 'MEJORA_INMEDIATA_DIFERENCIA'
  const isImmediateFullPrice = quote?.tipo === 'MEJORA_INMEDIATA_COMPLETA'

  return {
    membership,
    membershipsLoading,
    currentPlan,
    quote,
    quoteLoading,
    isImmediateDifference,
    isImmediateFullPrice,
    isImmediateUpgrade: isImmediateDifference || isImmediateFullPrice,
    isScheduledChange: quote?.tipo === 'CAMBIO_PROGRAMADO',
  }
}

export default function MembershipCheckout({
  membership,
  quote,
  currentPlan,
  isImmediateDifference,
  isImmediateUpgrade,
  isScheduledChange,
}) {
  return (
    <>
      <p className="eyebrow">Confirmá tu membresía</p>
      <h1>
        {isScheduledChange
          ? 'Programá tu próximo plan'
          : isImmediateUpgrade
            ? 'Mejorá tu membresía'
            : 'Completá el pago'}
      </h1>
      <p>{quote?.mensaje}</p>

      {quote && (
        <div className="info-box membership-checkout-notice">
          {isScheduledChange ? (
            <>
              <strong>Hoy no se realizará ningún cobro.</strong>
              <span>
                {currentPlan?.nombre} continuará hasta el {formatDateShort(currentPlan?.fechaFin)}.
                {' '}El {formatDateShort(quote.fechaInicio)} comenzará {membership?.nombre}
                {' '}y se cobrarán ${money(quote.importeRenovacion)}.
              </span>
              {currentPlan?.membresiaProgramadaId
                && currentPlan.membresiaProgramadaId !== membership?.id && (
                  <span>
                    Este cambio reemplazará la programación anterior a{' '}
                    <strong>{currentPlan.membresiaProgramadaNombre}</strong>.
                  </span>
                )}
            </>
          ) : isImmediateUpgrade ? (
            <>
              <strong>Hoy pagás ${money(quote.importeAhora)}.</strong>
              <span>
                {isImmediateDifference
                  ? `El plan cambia inmediatamente y conserva el vencimiento del ${formatDateShort(quote.fechaFin)}.`
                  : `El plan superior se activa inmediatamente y tendrá vigencia hasta el ${formatDateShort(quote.fechaFin)}.`}
              </span>
            </>
          ) : (
            <>
              <strong>Hoy pagás ${money(quote.importeAhora)}.</strong>
              <span>La membresía estará activa hasta el {formatDateShort(quote.fechaFin)}.</span>
            </>
          )}
        </div>
      )}
    </>
  )
}
