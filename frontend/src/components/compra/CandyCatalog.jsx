import { useMemo, useState } from 'react'
import { labelProductType, money } from '../../utils/format'

const TABS = [
  ['', 'Todos'],
  ['COMBO', 'Combos'],
  ['POCHOCLOS', 'Palomitas'],
  ['BEBIDA', 'Bebidas'],
  ['DULCE', 'Dulces'],
]

const DEFAULT_PRODUCT_IMAGES = {
  POCHOCLOS: '/images/consumibles/pochoclos.png',
  BEBIDA: '/images/consumibles/bebida.png',
  DULCE: '/images/consumibles/dulce.png',
  COMBO: '/images/consumibles/combo.png',
}

export default function CandyCatalog({ products, consumptions, onChange }) {
  const [tab, setTab] = useState('')
  const filtered = useMemo(
    () => products.filter((product) => !tab || product.tipo === tab),
    [products, tab],
  )

  return (
    <>
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
          const quantity = consumptions[product.id] || 0
          const imageUrl = product.imagenUrl || DEFAULT_PRODUCT_IMAGES[product.tipo]
          return (
            <article
              key={product.id}
              className={`consumption-card ${quantity > 0 ? 'is-selected' : ''}`}
            >
              <div className="consumption-card__image">
                {imageUrl ? (
                  <img src={imageUrl} alt={product.nombre} />
                ) : (
                  <span>Sin imagen</span>
                )}
              </div>
              <span className="movie-chip">{labelProductType(product.tipo)}</span>
              <h3>{product.nombre}</h3>
              <p className="muted">
                {product.tamano} · ${money(product.precio)}
              </p>
              <div className="quantity-control">
                <button
                  type="button"
                  onClick={() => onChange(product.id, quantity - 1)}
                  aria-label={`Quitar ${product.nombre}`}
                >
                  −
                </button>
                <strong>{quantity}</strong>
                <button
                  type="button"
                  onClick={() => onChange(product.id, quantity + 1)}
                  aria-label={`Agregar ${product.nombre}`}
                >
                  +
                </button>
              </div>
            </article>
          )
        })}
      </div>
    </>
  )
}
