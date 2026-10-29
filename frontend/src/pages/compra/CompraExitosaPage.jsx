import Confetti from 'react-confetti'
import { useWindowSize } from 'react-use'
import './CompraExitosaPage.css'

function drawPopcorn(ctx) {
  ctx.font = '35px Arial'
  ctx.fillText('🍿', 0, 0)
}

export default function CompraExitosaPage({ children }) {
  const { width, height } = useWindowSize()

  return (
    <div className="purchase-page compra-exitosa-page">
      <Confetti
        className="compra-exitosa-page__confetti"
        width={width}
        height={height}
        numberOfPieces={30}
        recycle={false}
        gravity={0.7}
        drawShape={drawPopcorn}
      />

      <section className="mensaje-exito">
        {children}
      </section>
    </div>
  )
}
