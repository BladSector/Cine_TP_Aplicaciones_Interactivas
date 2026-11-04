import { lazy } from 'react'
import { Navigate, Route, Routes, useParams } from 'react-router-dom'
import SiteLayout from '../components/layout/SiteLayout'
import CarteleraPage from '../pages/CarteleraPage'
import PlaceholderPage from '../pages/PlaceholderPage'
import CompraAccesoPage from '../pages/compra/CompraAccesoPage'

// Estas pantallas se descargan cuando se visitan, no al abrir la cartelera.
const AboutPage = lazy(() => import('../pages/AboutPage'))
const CandyPage = lazy(() => import('../pages/CandyPage'))
const ClubPage = lazy(() => import('../pages/ClubPage'))
const CuentaPage = lazy(() => import('../pages/CuentaPage'))
const LegalPage = lazy(() => import('../pages/LegalPage'))
const PeliculaPage = lazy(() => import('../pages/PeliculaPage'))
const CompraButacasPage = lazy(() => import('../pages/compra/CompraButacasPage'))
const CompraCandyPage = lazy(() => import('../pages/compra/CompraCandyPage'))
const CompraPagoPage = lazy(() => import('../pages/compra/CompraPagoPage'))

function LegacyCandyRedirect() {
  const { movieId } = useParams()
  return <Navigate to={`/compra/${movieId}/candy`} replace />
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<SiteLayout />}>
        <Route index element={<CarteleraPage />} />
        <Route path="pelicula/:id" element={<PeliculaPage />} />
        <Route path="candy" element={<CandyPage />} />
        <Route path="candy/pago" element={<CompraPagoPage candyOnly />} />
        <Route path="club" element={<ClubPage />} />
        <Route path="club/pago" element={<CompraPagoPage membershipOnly />} />
        <Route path="compra/:movieId/acceso" element={<CompraAccesoPage />} />
        <Route path="compra/:movieId/butacas" element={<CompraButacasPage />} />
        <Route path="compra/:movieId/candy" element={<CompraCandyPage />} />
        <Route path="compra/:movieId/pago" element={<CompraPagoPage />} />
        <Route path="cuenta" element={<CuentaPage />} />
        <Route path="privacidad" element={<LegalPage type="privacidad" />} />
        <Route path="normas" element={<LegalPage type="normas" />} />
        <Route path="nosotros" element={<AboutPage />} />
        {/* Compatibilidad con enlaces guardados antes del cambio de nombre. */}
        <Route path="kiosco" element={<Navigate to="/candy" replace />} />
        <Route path="kiosco/pago" element={<Navigate to="/candy/pago" replace />} />
        <Route path="compra/:movieId/kiosco" element={<LegacyCandyRedirect />} />
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
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
