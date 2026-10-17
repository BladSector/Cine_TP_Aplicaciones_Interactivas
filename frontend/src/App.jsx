import { Navigate, Route, Routes } from 'react-router-dom'
import SiteLayout from './components/layout/SiteLayout'
import CarteleraPage from './pages/CarteleraPage'
import PeliculaPage from './pages/PeliculaPage'
import CompraAccesoPage from './pages/compra/CompraAccesoPage'
import CompraButacasPage from './pages/compra/CompraButacasPage'
import CompraCandyPage from './pages/compra/CompraCandyPage'
import CompraPagoPage from './pages/compra/CompraPagoPage'
import CuentaPage from './pages/CuentaPage'
import PlaceholderPage from './pages/PlaceholderPage'

export default function App() {
  return (
    <Routes>
      <Route element={<SiteLayout />}>
        <Route index element={<CarteleraPage />} />
        <Route path="pelicula/:id" element={<PeliculaPage />} />
        <Route path="compra/:movieId/acceso" element={<CompraAccesoPage />} />
        <Route path="compra/:movieId/butacas" element={<CompraButacasPage />} />
        <Route path="compra/:movieId/candy" element={<CompraCandyPage />} />
        <Route path="compra/:movieId/pago" element={<CompraPagoPage />} />
        <Route path="cuenta" element={<CuentaPage />} />
        <Route
          path="proximamente"
          element={
            <PlaceholderPage
              title="Próximamente"
              subtitle="Los estrenos que vienen se revelan pronto en Noche Cine."
            />
          }
        />
        <Route
          path="experiencias"
          element={
            <PlaceholderPage
              title="Experiencias"
              subtitle="Salas inmersivas, noches temáticas y eventos especiales."
            />
          }
        />
        <Route
          path="club"
          element={
            <PlaceholderPage
              title="Club Noche"
              subtitle="Beneficios, preventas y noches exclusivas para socios."
            />
          }
        />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
