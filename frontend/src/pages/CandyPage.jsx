import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import CandyCatalog from '../components/compra/CandyCatalog'
import OrderSummary from '../components/compra/OrderSummary'
import PurchaseAuthDrawer from '../components/cuenta/PurchaseAuthDrawer'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'

export default function CandyPage() {
  const navigate = useNavigate()
  const { spectator, isSpectator } = useAuth()
  const { productos, loading, error } = useCatalog()
  const {
    draft,
    startCandyOnly,
    setConsumption,
    clearConsumptions,
  } = usePurchase()
  const [authOpen, setAuthOpen] = useState(false)
  const [selectionError, setSelectionError] = useState('')

  useEffect(() => {
    if (draft.purchaseType !== 'candy') {
      startCandyOnly()
    }
  }, [draft.purchaseType, startCandyOnly])

  const selectedItems = Object.values(draft.consumptions).reduce(
    (total, quantity) => total + Number(quantity),
    0,
  )
  const total = Object.entries(draft.consumptions).reduce((sum, [id, quantity]) => {
    const product = productos.find((item) => item.id === Number(id))
    return sum + (product ? product.precio * quantity : 0)
  }, 0)

  function continueToPayment() {
    if (selectedItems === 0) {
      setSelectionError('Seleccioná al menos un producto para continuar.')
      return
    }
    setSelectionError('')
    if (!isSpectator || !spectator.emailVerificado) {
      setAuthOpen(true)
      return
    }
    navigate('/candy/pago')
  }

  return (
    <div className="purchase-page candy-page">
      <header className="candy-page__heading">
        <p className="eyebrow">Candy Film Zone</p>
        <h1>Tu película favorita también se disfruta en casa</h1>
        <p>Elegí tus productos, pagá en línea y presentá el QR para retirarlos en el cine.</p>
      </header>

      <div className="purchase-grid purchase-grid--wide">
        <section className="purchase-panel">
          {loading && <div className="info-box">Cargando productos…</div>}
          {error && <div className="info-box info-box--error">{error}</div>}
          {!loading && !error && (
            <CandyCatalog
              products={productos}
              consumptions={draft.consumptions}
              onChange={(productId, quantity) => {
                setSelectionError('')
                setConsumption(productId, quantity)
              }}
            />
          )}
          {selectionError && <div className="info-box info-box--error">{selectionError}</div>}
        </section>

        <OrderSummary
          candyOnly
          products={productos}
          consumptions={draft.consumptions}
          onClearCandy={clearConsumptions}
          totalTickets={0}
          totalCandy={total}
          total={total}
          footer={
            <div className="summary-actions">
              <button
                type="button"
                className="primary-button"
                disabled={selectedItems === 0}
                onClick={continueToPayment}
              >
                Continuar al pago
              </button>
            </div>
          }
        />
      </div>

      <PurchaseAuthDrawer
        open={authOpen}
        purchaseDescription="Necesitás una cuenta para comprar productos del Candy."
        onClose={() => setAuthOpen(false)}
        onComplete={() => {
          setAuthOpen(false)
          navigate('/candy/pago')
        }}
      />
    </div>
  )
}
