import { Clock3, Menu, Moon, ShoppingCart, Sun, X } from 'lucide-react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import PurchaseAuthDrawer from '../cuenta/PurchaseAuthDrawer'
import ConfirmDialog from '../common/ConfirmDialog'
import Loader from '../common/Loader'
import { api } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'
import {
  money,
  seatLabel,
  shortTime,
} from '../../utils/format'
import { useEffect, useRef, useState } from 'react'
import HeaderMovieSearch from './HeaderMovieSearch'
import SiteFooter from './SiteFooter'

function functionDateLabel(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
  })
}

function timerLabel(seconds) {
  if (seconds === null || seconds === undefined) return '--:--'
  const minutes = Math.floor(seconds / 60)
  return `${minutes}:${String(seconds % 60).padStart(2, '0')}`
}

export default function SiteLayout() {
  const { spectator, isSpectator, logout, loading: authLoading } = useAuth()
  const { peliculas, funciones, productos, loading } = useCatalog()
  const {
    draft,
    hasActivePurchase,
    hasCandyCart,
    cartItemCount,
    continuePath,
    cancelActivePurchase,
    reservationError,
    remainingSeconds,
    reservationExpiredNotice,
    dismissReservationExpiredNotice,
  } = usePurchase()
  const navigate = useNavigate()
  const location = useLocation()
  const [suscripcion, setSuscripcion] = useState(null)

  const [theme, setTheme] = useState(() => document.documentElement.dataset.theme || 'light')
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [isNavOpen, setIsNavOpen] = useState(false)
  const [isAuthOpen, setIsAuthOpen] = useState(false)
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [showCartNotice, setShowCartNotice] = useState(false)
  const [showCancelPurchaseConfirm, setShowCancelPurchaseConfirm] = useState(false)
  const [isCancellingPurchase, setIsCancellingPurchase] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const menuRef = useRef(null)
  const cartRef = useRef(null)
  const previousPathRef = useRef(location.pathname)

  const activeMovie = peliculas.find((movie) => movie.id === draft.movieId)
  const activeFunction = funciones.find((funcion) => funcion.id === draft.functionId)
  const activeConsumptions = Object.entries(draft.consumptions)
    .map(([productId, quantity]) => ({
      product: productos.find((item) => item.id === Number(productId)),
      quantity: Number(quantity),
    }))
    .filter((item) => item.product && item.quantity > 0)
  const cartTicketsTotal = activeFunction
    ? draft.selectedSeats.length * Number(activeFunction.precioEntrada || 0)
    : 0
  const cartCandyTotal = activeConsumptions.reduce(
    (total, item) => total + Number(item.product.precio || 0) * item.quantity,
    0,
  )
  const isPurchaseFlow = location.pathname.startsWith('/compra/')
  const hasCart = hasActivePurchase || hasCandyCart
  const showCart = hasCart && !isPurchaseFlow && location.pathname !== '/candy/pago'

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    localStorage.setItem('film-zone-theme', theme)
  }, [theme])

  useEffect(() => {
    if (!isSpectator) {
      setSuscripcion(null)
      return undefined
    }
    let cancelled = false
    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setSuscripcion(data)
      })
      .catch(() => {
        if (!cancelled) setSuscripcion(null)
      })
    return () => {
      cancelled = true
    }
  }, [isSpectator, spectator?.id, location.pathname])

  useEffect(() => {
    function handleMembresia(event) {
      setSuscripcion(event.detail ?? null)
    }
    window.addEventListener('filmzone:membresia', handleMembresia)
    return () => window.removeEventListener('filmzone:membresia', handleMembresia)
  }, [])

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false)
      }
      if (cartRef.current && !cartRef.current.contains(event.target)) {
        setIsCartOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    setIsNavOpen(false)
  }, [location.pathname])

  useEffect(() => {
    const wasInPurchaseFlow = previousPathRef.current.startsWith('/compra/')
    if (!hasActivePurchase) {
      setShowCartNotice(false)
    }
    if (isPurchaseFlow) {
      setIsCartOpen(false)
    }
    if (wasInPurchaseFlow && !isPurchaseFlow && hasActivePurchase) {
      setShowCartNotice(true)
      const timer = window.setTimeout(() => setShowCartNotice(false), 6_000)
      previousPathRef.current = location.pathname
      return () => window.clearTimeout(timer)
    }
    previousPathRef.current = location.pathname
    return undefined
  }, [hasActivePurchase, isPurchaseFlow, location.pathname])

  async function handleLogout() {
    setIsLoggingOut(true)
    try {
      if (hasActivePurchase) await cancelActivePurchase()
      await logout()
      setShowLogoutConfirm(false)
      setIsMenuOpen(false)
      navigate('/')
    } finally {
      setIsLoggingOut(false)
    }
  }

  async function handleCancelPurchase() {
    setIsCancellingPurchase(true)
    try {
      await cancelActivePurchase()
      setShowCancelPurchaseConfirm(false)
      setIsCartOpen(false)
      setShowCartNotice(false)
    } finally {
      setIsCancellingPurchase(false)
    }
  }

  function continuePurchase() {
    if (!continuePath) return
    setIsCartOpen(false)
    setShowCartNotice(false)
    navigate(continuePath)
  }

  if (authLoading) return <Loader />

  return (
    <div className="site">
      <header className="site-header">
        <div className="site-header__inner">
          <NavLink to="/" className="brand">
            <span className="brand__word">Film</span>
            <span className="brand__word brand__word--accent">Zone</span>
          </NavLink>

          <button
            type="button"
            className="mobile-nav-toggle"
            aria-label={isNavOpen ? 'Cerrar menú principal' : 'Abrir menú principal'}
            aria-expanded={isNavOpen}
            aria-controls="site-navigation"
            onClick={() => setIsNavOpen((open) => !open)}
          >
            {isNavOpen ? <X size={22} aria-hidden="true" /> : <Menu size={22} aria-hidden="true" />}
          </button>

          <nav
            id="site-navigation"
            className={`site-nav ${isNavOpen ? 'is-open' : ''}`}
            aria-label="Principal"
          >
            <NavLink to="/" className="btn-cartelera" onClick={() => setIsNavOpen(false)} end>Cartelera</NavLink>
            <NavLink to="/candy" onClick={() => setIsNavOpen(false)}>Kiosco</NavLink>
            <NavLink to="/proximamente" onClick={() => setIsNavOpen(false)}>Próximamente</NavLink>
            <NavLink to="/experiencias" onClick={() => setIsNavOpen(false)}>Experiencias</NavLink>
            <NavLink to="/club" onClick={() => setIsNavOpen(false)}>Club Film Zone</NavLink>
          </nav>

          <div className="header-utility">
            <HeaderMovieSearch movies={peliculas} functions={funciones} loading={loading} />

            {showCart && <div className="header-cart" ref={cartRef}>
              <button
                type="button"
                className={`header-cart__toggle ${isCartOpen ? 'is-active' : ''}`}
                aria-label={hasCart ? `Compra en curso, ${cartItemCount} productos` : 'Carrito vacío'}
                title={hasCart ? 'Continuar compra' : 'No hay una compra en curso'}
                disabled={!hasCart}
                onClick={() => hasCart && setIsCartOpen((open) => !open)}
              >
                <ShoppingCart size={20} aria-hidden="true" />
                {hasCart && <span className="header-cart__badge">{cartItemCount}</span>}
              </button>

              {isCartOpen && hasCart && (
                <div className="header-cart__panel">
                  <div className="header-cart__heading">
                    <div>
                      <span>Compra en curso</span>
                      <strong>{hasActivePurchase ? activeMovie?.titulo || 'Tu película' : 'Kiosco'}</strong>
                    </div>
                    {hasActivePurchase && (
                      <span className={`header-cart__timer ${remainingSeconds !== null && remainingSeconds <= 120 ? 'is-warning' : ''}`}>
                        <Clock3 size={16} aria-hidden="true" />
                        {timerLabel(remainingSeconds)}
                      </span>
                    )}
                  </div>

                  {activeFunction && (
                    <div className="header-cart__function">
                      <span>
                        {functionDateLabel(activeFunction.fecha)} · {shortTime(activeFunction.horario)}
                      </span>
                      <strong>
                        {activeFunction.salaNombre} · Butacas {draft.selectedSeats.map(seatLabel).join(', ')}
                      </strong>
                    </div>
                  )}

                  {hasActivePurchase && (
                    <div className="header-cart__line">
                      <span>{draft.selectedSeats.length} entrada{draft.selectedSeats.length === 1 ? '' : 's'}</span>
                      <strong>${money(cartTicketsTotal)}</strong>
                    </div>
                  )}

                  {activeConsumptions.map(({ product, quantity }) => (
                    <div key={product.id} className="header-cart__line">
                      <span>{quantity} × {product.nombre}</span>
                      <strong>${money(product.precio * quantity)}</strong>
                    </div>
                  ))}

                  {reservationError && (
                    <p className="header-cart__error">{reservationError}</p>
                  )}

                  <div className="header-cart__total">
                    <span>Subtotal estimado</span>
                    <strong>${money(cartTicketsTotal + cartCandyTotal)}</strong>
                  </div>

                  <div className="header-cart__actions">
                    <button
                      type="button"
                      className="text-button text-button--danger"
                      onClick={() => setShowCancelPurchaseConfirm(true)}
                    >
                      Cancelar compra
                    </button>
                    <button type="button" className="primary-button" onClick={continuePurchase}>
                      Continuar
                    </button>
                  </div>
                </div>
              )}
            </div>}

            <button
              type="button"
              className="theme-toggle"
              aria-label={theme === 'dark' ? 'Activar modo claro' : 'Activar modo oscuro'}
              title={theme === 'dark' ? 'Modo claro' : 'Modo oscuro'}
              onClick={() => setTheme((current) => current === 'dark' ? 'light' : 'dark')}
            >
              {theme === 'dark'
                ? <Sun size={20} aria-hidden="true" />
                : <Moon size={20} aria-hidden="true" />}
            </button>

            <div className="header-actions">
                  {isSpectator ? (
                    <div className="user-menu-container" ref={menuRef}>
                    <button
                      type="button"
                      className={`user-icon-button ${isMenuOpen ? 'is-active' : ''}`}
                      onClick={() => {
                        const next = !isMenuOpen
                        setIsMenuOpen(next)
                        if (next && isSpectator) {
                          api.getMembresiaActual()
                            .then((data) => setSuscripcion(data))
                            .catch(() => setSuscripcion(null))
                        }
                      }}
                      title="Mi cuenta"
                    >
                      {/* Icono de usuario SVG */}
                      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                    </button>

                    {isMenuOpen && (
                      <div className="dropdown-menu">
                        <div className="dropdown-header">
                          Hola, <strong>{spectator.nombre}</strong>
                        </div>

                        <NavLink to="/cuenta#entradas" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                          Mis tickets
                        </NavLink>

                        {suscripcion && (
                          <NavLink to="/cuenta#membresia" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                            Membresía
                          </NavLink>
                        )}

                        <NavLink to="/cuenta#perfil" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                          Editar Perfil
                        </NavLink>

                        <NavLink to="/cuenta#pagos" className="dropdown-item" onClick={() => setIsMenuOpen(false)}>
                          Métodos de Pago
                        </NavLink>

                        <hr className="dropdown-divider" />

                        <button
                          type="button"
                          className="dropdown-item text-danger"
                          onClick={() => {
                            setIsMenuOpen(false)
                            setShowLogoutConfirm(true)
                          }}
                        >
                          Cerrar sesión
                        </button>
                      </div>
                    )}
                    </div>
                  ) : (
                    <button type="button" className="secondary-button" onClick={() => setIsAuthOpen(true)}>
                      Iniciar sesión
                    </button>
                  )}
              </div>
          </div>

        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      {showCartNotice && (
        <div className="cart-resume-toast" role="status">
          <ShoppingCart size={20} aria-hidden="true" />
          <div>
            <strong>Tu compra sigue en curso</strong>
            <span>Podés retomarla desde el carrito antes de que venza la reserva.</span>
          </div>
          <button
            type="button"
            className="reservation-expired-toast__close"
            aria-label="Cerrar aviso"
            onClick={() => setShowCartNotice(false)}
          >
            <X size={18} aria-hidden="true" />
          </button>
        </div>
      )}

      {reservationExpiredNotice && (
        <div className="reservation-expired-toast" role="status">
          <div>
            <strong>La reserva venció</strong>
            <span>La compra se canceló y las butacas volvieron a estar disponibles.</span>
          </div>
          <button
            type="button"
            className="reservation-expired-toast__close"
            aria-label="Cerrar aviso"
            onClick={dismissReservationExpiredNotice}
          >
            <X size={18} aria-hidden="true" />
          </button>
        </div>
      )}

      <SiteFooter />

      <PurchaseAuthDrawer
        open={isAuthOpen}
        purchase={false}
        onClose={() => setIsAuthOpen(false)}
        onComplete={() => setIsAuthOpen(false)}
      />

      {showLogoutConfirm && (
        <ConfirmDialog
          title="Cerrar sesión"
          message="¿Querés cerrar tu sesión en Film Zone?"
          confirmLabel="Cerrar sesión"
          danger
          loading={isLoggingOut}
          onConfirm={handleLogout}
          onCancel={() => setShowLogoutConfirm(false)}
        />
      )}

      {showCancelPurchaseConfirm && (
        <ConfirmDialog
          title="Cancelar compra"
          message="Se vaciará el carrito y las butacas seleccionadas volverán a quedar disponibles."
          confirmLabel="Cancelar compra"
          cancelLabel="Conservar compra"
          danger
          loading={isCancellingPurchase}
          onConfirm={handleCancelPurchase}
          onCancel={() => setShowCancelPurchaseConfirm(false)}
        />
      )}
    </div>
  )
}
