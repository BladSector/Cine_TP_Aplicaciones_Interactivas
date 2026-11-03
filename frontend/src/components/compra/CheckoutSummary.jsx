import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import OrderSummary from './OrderSummary'

export default function CheckoutSummary({
  candyOnly,
  membershipOnly,
  movieId,
  movie,
  funcion,
  membership,
  membershipQuote,
  totalTickets,
  totalCandy,
  total,
}) {
  const { productos } = useCatalog()
  const { draft, limpiarConsumos } = usePurchase()
  const withoutFunction = candyOnly || membershipOnly

  return (
    <OrderSummary
      candyOnly={candyOnly}
      membership={membershipOnly ? membership : null}
      membershipQuote={membershipOnly ? membershipQuote : null}
      movie={movie}
      funcion={funcion}
      seats={draft.selectedSeats}
      products={productos}
      consumos={draft.consumos}
      onLimpiarCandy={limpiarConsumos}
      editSeatsTo={withoutFunction ? undefined : `/compra/${movieId}/butacas`}
      editarCandyEn={membershipOnly
        ? undefined
        : candyOnly
          ? '/candy'
          : `/compra/${movieId}/candy`}
      totalTickets={totalTickets}
      totalCandy={totalCandy}
      total={total}
    />
  )
}
