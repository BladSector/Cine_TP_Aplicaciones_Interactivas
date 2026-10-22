import { useEffect } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import SiteLayout from './components/layout/SiteLayout'
import CarteleraPage from './pages/CarteleraPage'
import PeliculaPage from './pages/PeliculaPage'
import CompraAccesoPage from './pages/compra/CompraAccesoPage'
import CompraButacasPage from './pages/compra/CompraButacasPage'
import CompraCandyPage from './pages/compra/CompraCandyPage'
import CompraPagoPage from './pages/compra/CompraPagoPage'
import CuentaPage from './pages/CuentaPage'
import CandyPage from './pages/CandyPage'
import PlaceholderPage from './pages/PlaceholderPage'
import ClubPage from './pages/ClubPage'
import LegalPage from './pages/LegalPage'
import AboutPage from './pages/AboutPage'

function ScrollToTop() {
  const { pathname } = useLocation()

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [pathname])

  return null
}

export default function App() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route element={<SiteLayout />}>
          <Route index element={<CarteleraPage />} />
          <Route path="pelicula/:id" element={<PeliculaPage />} />
          <Route path="candy" element={<CandyPage />} />
          <Route path="candy/pago" element={<CompraPagoPage candyOnly />} />
          <Route path="club/pago" element={<CompraPagoPage membershipOnly />} />
          <Route path="compra/:movieId/acceso" element={<CompraAccesoPage />} />
          <Route path="compra/:movieId/butacas" element={<CompraButacasPage />} />
          <Route path="compra/:movieId/candy" element={<CompraCandyPage />} />
          <Route path="compra/:movieId/pago" element={<CompraPagoPage />} />
          <Route path="cuenta" element={<CuentaPage />} />
          <Route path="privacidad" element={<LegalPage type="privacidad" />} />
          <Route path="normas" element={<LegalPage type="normas" />} />
          <Route path="nosotros" element={<AboutPage />} />
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
          <Route path="club" element={<ClubPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
