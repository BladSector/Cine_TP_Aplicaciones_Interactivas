import { useState } from 'react'
import Modal from '../common/Modal'
import TicketDetails from '../tickets/TicketDetails'
import { useCatalog } from '../../context/CatalogContext'
import { formatDateShort, money, shortTime } from '../../utils/format'

export default function MisEntradasList() {
  const { tickets, peliculas, reload } = useCatalog()
  const [selected, setSelected] = useState(null)
  const myTickets = [...(tickets || [])].sort((a, b) => b.id - a.id)

  return (
    <section id="entradas" className="account-section">
      <div className="section-heading">
        <div><p className="eyebrow">Historial</p><h2>Mis tickets</h2></div>
      </div>
      {myTickets.length === 0 ? (
        <div className="info-box">Todavía no tenés compras. Elegí una función en cartelera.</div>
      ) : (
        <div className="ticket-list">
          {myTickets.map((ticket) => {
            const first = ticket.entradas?.[0]
            const items = ticket.itemsConsumo || []
            const candyOnly = !first && items.length > 0
            const candyQuantity = items.reduce(
              (total, item) => total + Number(item.cantidad || 0),
              0,
            )
            const candySummary = items
              .map((item) => `${item.cantidad} × ${item.producto}`)
              .join(' · ')
            return (
              <button key={ticket.id} type="button" className="ticket-list-item" onClick={() => setSelected(ticket)}>
                <div>
                  <p className="eyebrow">Ticket #{ticket.id}</p>
                  <h3 className={candyOnly ? 'ticket-list-item__title--candy' : undefined}>
                    {candyOnly ? 'Compra del Kiosco' : first?.pelicula || 'Compra'}
                  </h3>
                  {candyOnly ? (
                    <>
                      <p>{candyQuantity} consumible{candyQuantity !== 1 ? 's' : ''}</p>
                      <p>{candySummary}</p>
                    </>
                  ) : (
                    <>
                      <p>{formatDateShort(first?.fecha)} · {shortTime(first?.horario)}</p>
                      <p>{first?.sala} · {(ticket.entradas || []).map((e) => e.butaca).join(', ')}</p>
                    </>
                  )}
                </div>
                <div className="ticket-list-item__summary">
                  <span className={`status-pill status-pill--${String(ticket.estado).toLowerCase()}`}>{String(ticket.estado).replaceAll('_', ' ')}</span>
                  <strong>${money(ticket.total)}</strong>
                  <span>Ver ticket</span>
                </div>
              </button>
            )
          })}
        </div>
      )}
      {selected && (
        <Modal title="Detalle de la compra" onClose={() => setSelected(null)}>
          <TicketDetails
            ticket={selected}
            allowCancellation
            movie={(peliculas || []).find(
              (movie) => movie.titulo === selected.entradas?.[0]?.pelicula,
            )}
            onCancelled={async (updatedTicket) => {
              setSelected(updatedTicket)
              await reload()
            }}
          />
        </Modal>
      )}
    </section>
  )
}
