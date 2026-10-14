import { useCatalog } from '../../context/CatalogContext'
import { formatDateShort, money, shortTime } from '../../utils/format'

export default function MisEntradasList() {
  const { tickets } = useCatalog()
  const myTickets = tickets || []

  return (
    // El ID "entradas" es clave para el link del menú
    <section id="entradas" className="account-section">
      <h2>Mis entradas</h2>
      {myTickets.length === 0 ? (
        <div className="info-box">Todavía no tenés compras. Elegí una función en cartelera.</div>
      ) : (
        <div className="ticket-list">
          {myTickets.map((ticket) => {
            const first = ticket.entradas?.[0]
            return (
              <article key={ticket.id} className="ticket-card">
                <div>
                  <p className="eyebrow">Ticket #{ticket.id}</p>
                  <h3>{first?.pelicula || 'Entrada'}</h3>
                  <p>
                    {formatDateShort(first?.fecha)} · {shortTime(first?.horario)}
                  </p>
                  <p>
                    Sala {first?.sala} · Butacas{' '}
                    {(ticket.entradas || []).map((e) => e.butaca).join(', ')}
                  </p>
                  <p>Estado: {ticket.estado}</p>
                  <p>Total: ${money(ticket.total)}</p>
                </div>
                <p className="qr-code">QR {ticket.codigoQR}</p>
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}