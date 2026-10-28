import { useMemo, useState } from 'react'
import { labelProductType, money, normalizeText } from '../../utils/format'

const TABS = [
  ['', 'Todos'],
  ['COMBO', 'Combos'],
  ['POCHOCLOS', 'Pochoclos'],
  ['BEBIDA', 'Bebidas'],
  ['DULCE', 'Dulces'],
]

const DEFAULT_PRODUCT_IMAGES = {
  POCHOCLOS: '/images/consumibles/pochoclos.png',
  BEBIDA: '/images/consumibles/bebida.png',
  DULCE: '/images/consumibles/dulce.png',
  COMBO: '/images/consumibles/combo.png',
}

const POPCORN_SIZE_LABELS = {
  CHICO: 'chicos',
  MEDIANO: 'medianos',
  GRANDE: 'grandes',
  UNICO: '',
}

const COMBO_DESCRIPTIONS = {
  'combo clasico': 'Pochoclos medianos y una gaseosa mediana.',
  'combo pareja': 'Pochoclos grandes y dos gaseosas medianas.',
  'combo familiar': 'Dos pochoclos grandes y cuatro gaseosas medianas.',
  'combo infantil': 'Pochoclos chicos, gaseosa chica y una golosina.',
}

function popcornFlavor(product) {
  const name = normalizeText(product.nombre)
  if (name.includes('dulce')) return 'Dulces'
  if (name.includes('salado')) return 'Salados'
  return product.nombre.replace(/^pochoclos\s*/i, '') || 'Clásicos'
}

function productDescription(product) {
  if (product.descripcion) return product.descripcion
  if (product.tipo === 'COMBO') return COMBO_DESCRIPTIONS[normalizeText(product.nombre)] || ''
  return ''
}

function buildCatalogItems(products, tab) {
  const items = []
  const popcornGroups = new Map()

  products
    .filter((product) => !tab || product.tipo === tab)
    .forEach((product) => {
      if (product.tipo !== 'POCHOCLOS') {
        items.push({
          key: `product-${product.id}`,
          title: product.nombre,
          variants: [product],
        })
        return
      }

      const key = `popcorn-${product.tamano}`
      let group = popcornGroups.get(key)
      if (!group) {
        const size = POPCORN_SIZE_LABELS[product.tamano] || String(product.tamano || '').toLowerCase()
        group = {
          key,
          title: `Pochoclos${size ? ` ${size}` : ''}`,
          variants: [],
        }
        popcornGroups.set(key, group)
        items.push(group)
      }
      group.variants.push(product)
    })

  return items
}

export default function CandyCatalog({ products, consumptions, onChange }) {
  const [tab, setTab] = useState('')
  const [selectedVariants, setSelectedVariants] = useState({})
  const catalogItems = useMemo(() => buildCatalogItems(products, tab), [products, tab])

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
        <span className="count-pill">
          {catalogItems.length} {catalogItems.length === 1 ? 'opción' : 'opciones'}
        </span>
      </div>

      <div className="consumption-grid">
        {catalogItems.map((item) => {
          const selectedId = selectedVariants[item.key]
          const activeProduct = item.variants.find((variant) => variant.id === selectedId)
            || item.variants.find((variant) => Number(consumptions[variant.id]) > 0)
            || item.variants[0]
          const quantity = Number(consumptions[activeProduct.id]) || 0
          const totalSelected = item.variants.reduce(
            (total, variant) => total + (Number(consumptions[variant.id]) || 0),
            0,
          )
          const imageUrl = activeProduct.imagenUrl || DEFAULT_PRODUCT_IMAGES[activeProduct.tipo]
          const description = productDescription(activeProduct)
          return (
            <article
              key={item.key}
              className={`consumption-card ${totalSelected > 0 ? 'is-selected' : ''}`}
            >
              <div className="consumption-card__image">
                {imageUrl ? (
                  <img src={imageUrl} alt={item.title} />
                ) : (
                  <span>Sin imagen</span>
                )}
              </div>
              <span className="movie-chip">{labelProductType(activeProduct.tipo)}</span>
              <h3>{item.title}</h3>
              {description && <p className="consumption-card__description">{description}</p>}

              {item.variants.length > 1 && (
                <div className="flavor-options" role="radiogroup" aria-label={`Sabor de ${item.title}`}>
                  {item.variants.map((variant) => {
                    const variantQuantity = Number(consumptions[variant.id]) || 0
                    return (
                      <button
                        key={variant.id}
                        type="button"
                        role="radio"
                        aria-checked={variant.id === activeProduct.id}
                        className={variant.id === activeProduct.id ? 'is-active' : ''}
                        onClick={() => setSelectedVariants((current) => ({
                          ...current,
                          [item.key]: variant.id,
                        }))}
                      >
                        {popcornFlavor(variant)}
                        {variantQuantity > 0 && <span>{variantQuantity}</span>}
                      </button>
                    )
                  })}
                </div>
              )}

              <p className="muted consumption-card__price">
                {activeProduct.tamano} · ${money(activeProduct.precio)}
              </p>
              <div className="quantity-control">
                <button
                  type="button"
                  onClick={() => onChange(activeProduct.id, quantity - 1)}
                  aria-label={`Quitar ${activeProduct.nombre}`}
                >
                  −
                </button>
                <strong>{quantity}</strong>
                <button
                  type="button"
                  onClick={() => onChange(activeProduct.id, quantity + 1)}
                  aria-label={`Agregar ${activeProduct.nombre}`}
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
