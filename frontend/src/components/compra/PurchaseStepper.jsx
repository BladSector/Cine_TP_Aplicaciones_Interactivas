const STEPS = [
  { key: 'acceso', label: 'Acceso' },
  { key: 'funcion', label: 'Función' },
  { key: 'butacas', label: 'Butacas' },
  { key: 'candy', label: 'Kiosco · opcional' },
  { key: 'pago', label: 'Pago' },
]

export default function PurchaseStepper({ current }) {
  const currentIndex = STEPS.findIndex((step) => step.key === current)

  return (
    <ol className="purchase-stepper" aria-label="Pasos de compra">
      {STEPS.map((step, index) => {
        const state =
          index < currentIndex ? 'done' : index === currentIndex ? 'current' : 'todo'
        return (
          <li key={step.key} className={`purchase-stepper__item purchase-stepper__item--${state}`}>
            <span className="purchase-stepper__num">{index + 1}</span>
            <span className="purchase-stepper__label">{step.label}</span>
          </li>
        )
      })}
    </ol>
  )
}
