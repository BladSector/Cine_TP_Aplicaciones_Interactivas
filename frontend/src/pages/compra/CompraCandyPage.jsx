import { useMemo, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import OrderSummary from '../../components/compra/OrderSummary'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import { labelProductType, money } from '../../utils/format'

const TABS = [
  ['', 'Todos'],
  ['COMBO', 'Combos'],
  ['POCHOCLOS', 'Palomitas'],
  ['BEBIDA', 'Bebidas'],
  ['DULCE', 'Dulces'],
]

export default function CompraCandyPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { isSpectator } = useAuth()
  const { peliculas, funciones, productos } = useCatalog()
  const { draft, setConsumption, clearConsumptions } = usePurchase()
  const [tab, setTab] = useState('')

  const movie = peliculas.find((item) => item.id === Number(movieId))
  const funcion = funciones.find((item) => item.id === draft.functionId)

  const filtered = useMemo(
    () => productos.filter((product) => !tab || product.tipo === tab),
    [productos, tab],
  )

  if (!isSpectator) return <Navigate to={`/compra/${movieId}/acceso`} replace />
  if (!funcion || !movie || draft.selectedSeats.length === 0) {
    return <Navigate to={`/compra/${movieId}/butacas`} replace />
  }

  const totalTickets = draft.selectedSeats.length * funcion.precioEntrada
  const totalCandy = Object.entries(draft.consumptions).reduce((sum, [id, qty]) => {
    const product = productos.find((item) => item.id === Number(id))
    return sum + (product ? product.precio * qty : 0)
  }, 0)

  return (
    <div className="purchase-page">
      <Link to={`/compra/${movieId}/butacas`} className="back-link">
        Volver a las butacas
      </Link>
      <PurchaseStepper current="candy" />

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          <p className="eyebrow">Un extra para tu película · opcional</p>
          <h1>El cine sabe mejor así</h1>
          <p>Añade tus favoritos o continúa al pago solo con tus entradas. Tú eliges.</p>

          <div className="consumption-tabs">
            {TABS.map(([value, label]) => (
              <button
                key={value || 'all'}
                type="button"
                className={tab === value ? 'is-active' : ''}
                onClick={() => setTab(value)}
              >
                {label}
              </button>
            ))}
            <span className="count-pill">{filtered.length} productos</span>
          </div>

          <div className="consumption-grid">
            {filtered.map((product) => {
              const qty = draft.consumptions[product.id] || 0
              return (
                <article
                  key={product.id}
                  className={`consumption-card ${qty > 0 ? 'is-selected' : ''}`}
                >
                  <span className="movie-chip">{labelProductType(product.tipo)}</span>
                  <h3>{product.nombre}</h3>
                  <p className="muted">
                    {product.tamano} · ${money(product.precio)}
                  </p>
                  <div className="quantity-control">
                    <button
                      type="button"
                      onClick={() => setConsumption(product.id, qty - 1)}
                      aria-label="Quitar"
                    >
                      −
                    </button>
                    <strong>{qty}</strong>
                    <button
                      type="button"
                      onClick={() => setConsumption(product.id, qty + 1)}
                      aria-label="Agregar"
                    >
                      +
                    </button>
                  </div>
                </article>
              )
            })}
          </div>
        </section>

        <OrderSummary
          movie={movie}
          funcion={funcion}
          seats={draft.selectedSeats}
          products={productos}
          consumptions={draft.consumptions}
          onClearCandy={clearConsumptions}
          editSeatsTo={`/compra/${movieId}/butacas`}
          totalTickets={totalTickets}
          totalCandy={totalCandy}
          total={totalTickets + totalCandy}
          footer={
            <div className="summary-actions">
              <button
                type="button"
                className="primary-button"
                onClick={() => navigate(`/compra/${movieId}/pago`)}
              >
                Continuar al pago
              </button>
              <button
                type="button"
                className="secondary-button"
                onClick={() => {
                  clearConsumptions()
                  navigate(`/compra/${movieId}/pago`)
                }}
              >
                Continuar sin productos
              </button>
              <p className="muted tiny">Solo entradas: ${money(totalTickets)}</p>
            </div>
          }
        />
      </div>
    </div>
  )
}
