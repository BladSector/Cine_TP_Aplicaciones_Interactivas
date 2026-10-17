import { useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { api, errorMessage } from '../../api/client'
import { formatDateShort, formatFunction, formatLanguage, money, shortTime } from '../../utils/format'

export default function TicketDetails({ ticket, allowEmail = true }) {
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

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

  async function resendEmail() {
    setLoading(true)
    setError('')
    setMessage('')
    try {
      await api.reenviarTicket(ticket.id)
      setMessage('Ticket enviado nuevamente al correo de la cuenta.')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="ticket-detail">
      <div className="ticket-detail__heading">
        <div>
          <p className="eyebrow">Ticket #{ticket.id}</p>
          <h3>{ticket.entradas?.[0]?.pelicula || 'Compra de cine'}</h3>
          <span className={`status-pill status-pill--${String(ticket.estado).toLowerCase()}`}>
            {String(ticket.estado).replaceAll('_', ' ')}
          </span>
        </div>
        <div className="ticket-qr">
          <QRCodeSVG value={ticket.codigoQR} size={136} level="M" includeMargin />
          <code>{ticket.codigoQR}</code>
        </div>
      </div>

      <div className="ticket-detail__entries">
        {(ticket.entradas || []).map((entrada) => (
          <article key={entrada.id} className="ticket-entry">
            <strong>{entrada.pelicula}</strong>
            <span>{formatDateShort(entrada.fecha)} · {shortTime(entrada.horario)}</span>
            <span>{entrada.sala} · Butaca {entrada.butaca}</span>
            <span>{formatFunction(entrada.formato)} · {formatLanguage(entrada.idioma)}</span>
            <span>${money(entrada.precio)} · {String(entrada.estado).replaceAll('_', ' ')}</span>
          </article>
        ))}
      </div>

      {(ticket.itemsConsumo || []).length > 0 && (
        <div className="ticket-detail__items">
          <h4>Consumos</h4>
          {ticket.itemsConsumo.map((item) => (
            <p key={item.id}>
              <span>{item.cantidad} × {item.producto}</span>
              <span>${money(item.subtotal)} · {String(item.estado).replaceAll('_', ' ')}</span>
            </p>
          ))}
        </div>
      )}

      <div className="ticket-total"><span>Total</span><strong>${money(ticket.total)}</strong></div>
      <p className="muted tiny">Pago: {ticket.metodoDePagoResumen || 'Método registrado en la compra'}</p>
      {message && <div className="info-box">{message}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}
      <div className="summary-actions">
        <button type="button" className="primary-button" onClick={downloadPdf} disabled={loading}>Descargar PDF</button>
        {allowEmail && <button type="button" className="secondary-button" onClick={resendEmail} disabled={loading}>Reenviar por correo</button>}
      </div>
    </div>
  )
}
