import { useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { api, errorMessage } from '../../api/client'
import { formatFunction, formatLanguage, money, shortTime } from '../../utils/format'
import ConfirmDialog from '../common/ConfirmDialog'

function weekdayName(value) {
  if (!value) return ''
  const label = new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', { weekday: 'long' })
  return label.charAt(0).toUpperCase() + label.slice(1)
}

function dateNumber(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

function statusLabel(value) {
  return String(value || '').replaceAll('_', ' ')
}

function cancellationDeadline(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function groupEntradas(entradas) {
  const groups = []
  entradas.forEach((entrada) => {
    const key = [
      entrada.pelicula,
      entrada.fecha,
      entrada.horario,
      entrada.sala,
      entrada.formato,
      entrada.idioma,
    ].join('|')
    const current = groups.find((group) => group.key === key)
    if (current) {
      current.butacas.push(entrada.butaca)
      current.precio += Number(entrada.precio || 0)
      return
    }
    groups.push({
      key,
      pelicula: entrada.pelicula,
      fecha: entrada.fecha,
      horario: entrada.horario,
      sala: entrada.sala,
      formato: entrada.formato,
      idioma: entrada.idioma,
      butacas: [entrada.butaca],
      precio: Number(entrada.precio || 0),
    })
  })
  return groups
}

function ReceiptRow({ label, value }) {
  return (
    <div className="ticket-receipt__row">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  )
}

export default function TicketDetails({ ticket, movie, allowCancellation = false, onCancelled }) {
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [cancelLoading, setCancelLoading] = useState(false)
  const [showCancelConfirm, setShowCancelConfirm] = useState(false)
  const [notice, setNotice] = useState('')

  async function downloadPdf() {
    setLoading(true)
    setError('')
    try {
      const blob = await api.getTicketPdf(ticket.id)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `ticket-${ticket.id}.pdf`
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function cancelPurchase() {
    setCancelLoading(true)
    setError('')
    try {
      const updatedTicket = await api.cancelarCompra(ticket.id)
      setShowCancelConfirm(false)
      setNotice('La compra fue cancelada correctamente.')
      await onCancelled?.(updatedTicket)
    } catch (err) {
      setError(errorMessage(err))
      setShowCancelConfirm(false)
    } finally {
      setCancelLoading(false)
    }
  }

  const entradas = ticket.entradas || []
  const consumos = ticket.itemsConsumo || []
  const funciones = groupEntradas(entradas)
  const titulo = movie?.titulo
    || entradas[0]?.pelicula
    || (consumos.length > 0 ? 'Compra del Kiosco' : 'Compra de cine')
  const tienePelicula = funciones.length > 0
  const compraSoloCandy = !tienePelicula && consumos.length > 0
  const limite = cancellationDeadline(ticket.limiteCancelacion)

  return (
    <div className="ticket-detail ticket-receipt">
      <div className="ticket-receipt__hero">
        <div className={`ticket-detail__movie${movie?.portadaUrl ? '' : ' ticket-detail__movie--without-poster'}`}>
          {movie?.portadaUrl && (
            <img
              className="ticket-detail__poster"
              src={movie.portadaUrl}
              alt={`Portada de ${titulo}`}
            />
          )}
          <div className={`ticket-receipt__identity${compraSoloCandy ? ' ticket-receipt__identity--candy' : ''}`}>
            <p className="eyebrow">{tienePelicula ? 'Película confirmada' : 'Pedido confirmado'}</p>
            <h3>{titulo}</h3>
            <p className="ticket-receipt__number">Ticket #{ticket.id}</p>
            <span className={`status-pill status-pill--${String(ticket.estado).toLowerCase()}`}>
              {statusLabel(ticket.estado)}
            </span>
          </div>
        </div>
        <div className="ticket-qr" aria-label="Código QR del ticket">
          <QRCodeSVG value={ticket.codigoQR} size={136} level="M" includeMargin />
        </div>
      </div>

      {funciones.map((funcion) => (
        <div key={funcion.key} className="ticket-receipt__function">
          <section className="ticket-receipt__section">
            <h4>Función</h4>
            <p className="ticket-receipt__highlight">
              <span>Horario</span>
              <strong>{shortTime(funcion.horario)}</strong>
            </p>
            <p className="ticket-receipt__when">
              <span>{weekdayName(funcion.fecha)}</span>
              <span>{dateNumber(funcion.fecha)}</span>
            </p>
          </section>

          <section className="ticket-receipt__section">
            <h4>Sala y butacas</h4>
            <p className="ticket-receipt__highlight">
              <span>Butacas</span>
              <strong>{funcion.butacas.join(', ')}</strong>
            </p>
            <dl className="ticket-receipt__rows">
              <ReceiptRow label="Sala" value={funcion.sala} />
              <ReceiptRow
                label="Formato"
                value={`${formatFunction(funcion.formato)} · ${formatLanguage(funcion.idioma)}`}
              />
              <ReceiptRow label="Valor" value={`$${money(funcion.precio)}`} />
            </dl>
          </section>
        </div>
      ))}

      <section className="ticket-receipt__section">
        <h4>Kiosco</h4>
        {consumos.length > 0 ? (
          <div className="ticket-detail__items">
            {consumos.map((item) => (
              <p key={item.id}>
                <span>{item.cantidad} × {item.producto}</span>
                <span>${money(item.subtotal)}</span>
              </p>
            ))}
          </div>
        ) : (
          <p className="muted ticket-receipt__empty">No se agregaron productos del kiosco.</p>
        )}
      </section>

      <div className="ticket-total">
        <span>Total a pagar</span>
        <strong>${money(ticket.total)}</strong>
      </div>
      {notice && <div className="info-box">{notice}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}
      {allowCancellation && ticket.cancelacionPermitida ? (
        <div className="ticket-cancellation-note">
          <strong>Podés cancelar esta compra.</strong>
          <span>
            {compraSoloCandy
              ? 'La opción estará disponible mientras el pedido no haya sido entregado.'
              : `Tenés tiempo hasta ${limite} (2 horas antes de la función).`}
          </span>
        </div>
      ) : allowCancellation && ticket.motivoCancelacion ? (
        <p className="muted ticket-cancellation-unavailable">{ticket.motivoCancelacion}</p>
      ) : null}
      <div className="summary-actions">
        <button type="button" className="primary-button" onClick={downloadPdf} disabled={loading}>
          {loading ? 'Descargando…' : 'Descargar PDF'}
        </button>
        {allowCancellation && ticket.cancelacionPermitida && (
          <button type="button" className="danger-button" onClick={() => setShowCancelConfirm(true)}>
            {compraSoloCandy ? 'Cancelar compra del Kiosco' : 'Cancelar compra'}
          </button>
        )}
      </div>

      {allowCancellation && showCancelConfirm && (
        <ConfirmDialog
          title="Cancelar compra"
          message={compraSoloCandy
            ? 'Se cancelarán todos los consumibles pendientes de este ticket.'
            : 'Se reembolsarán las entradas, se liberarán las butacas y se cancelarán los consumibles pendientes asociados.'}
          confirmLabel="Sí, cancelar compra"
          cancelLabel="Conservar compra"
          danger
          loading={cancelLoading}
          onConfirm={cancelPurchase}
          onCancel={() => setShowCancelConfirm(false)}
        />
      )}
    </div>
  )
}
