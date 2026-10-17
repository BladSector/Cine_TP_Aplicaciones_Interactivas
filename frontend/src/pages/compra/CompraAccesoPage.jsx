import { Navigate, useParams } from 'react-router-dom'

export default function CompraAccesoPage() {
  const { movieId } = useParams()
  return <Navigate to={`/pelicula/${movieId}`} replace state={{ openPurchaseAuth: true }} />
}
