import { useEffect, useState } from 'react'
import Modal from '../common/Modal'
import TicketDetails from '../tickets/TicketDetails'
import { useCatalog } from '../../context/CatalogContext'
import { useAccount } from '../../context/AccountContext'
import { formatDateShort, money, shortTime } from '../../utils/format'

export default function MisEntradasList() {
  const { peliculas } = useCatalog()
  const { tickets, loading, error, reloadTickets } = useAccount()
  const [selected, setSelected] = useState(null)
  const myTickets = [...(tickets || [])].sort((a, b) => b.id - a.id)

  useEffect(() => { reloadTickets() }, [reloadTickets])

  return (
    <section id="entradas" className="account-section">
      <div className="section-heading">
        <div><p className="eyebrow">Historial</p><h2>Mis tickets</h2></div>
      </div>
      {error ? (
        <div className="info-box info-box--error" role="alert">
          {error}
          <button type="button" className="secondary-button" onClick={reloadTickets}>Reintentar</button>
        </div>
      ) : loading && myTickets.length === 0 ? (
        <p role="status">Cargando tus tickets…</p>
      ) : myTickets.length === 0 ? (
        <div className="info-box">Todavía no tenés compras. Elegí una función en cartelera.</div>
      ) : (
        <div className="ticket-list">
          {myTickets.map((ticket) => {
            const first = ticket.entradas?.[0]
            const items = ticket.itemsConsumo || []
            const compraSoloCandy = !first && items.length > 0
            const cantidadProductosCandy = items.reduce(
              (total, item) => total + Number(item.cantidad || 0),
              0,
            )
            const resumenCandy = items
              .map((item) => `${item.cantidad} × ${item.producto}`)
              .join(' · ')
            return (
              <button key={ticket.id} type="button" className="ticket-list-item" onClick={() => setSelected(ticket)}>
                <div>
                  <p className="eyebrow">Ticket #{ticket.id}</p>
                  <h3 className={compraSoloCandy ? 'ticket-list-item__title--candy' : undefined}>
                    {compraSoloCandy ? 'Compra del Candy' : first?.pelicula || 'Compra'}
                  </h3>
                  {compraSoloCandy ? (
                    <>
                      <p>{cantidadProductosCandy} consumible{cantidadProductosCandy !== 1 ? 's' : ''}</p>
                      <p>{resumenCandy}</p>
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
              await reloadTickets()
            }}
          />
        </Modal>
      )}
    </section>
  )
}
