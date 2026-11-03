import { UserRound } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { NavLink, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { usePurchase } from '../../context/PurchaseContext'
import ConfirmDialog from '../common/ConfirmDialog'

export default function UserMenu({ onLogin }) {
  const { spectator, isSpectator, logout } = useAuth()
  const { hasActivePurchase, cancelActivePurchase } = usePurchase()
  const navigate = useNavigate()
  const location = useLocation()
  const menuRef = useRef(null)
  const [open, setOpen] = useState(false)
  const [subscription, setSubscription] = useState(null)
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)

  useEffect(() => {
    if (!isSpectator) {
      setSubscription(null)
      return undefined
    }
    let cancelled = false

    api.getMembresiaActual()
      .then((data) => {
        if (!cancelled) setSubscription(data)
      })
      .catch(() => {
        if (!cancelled) setSubscription(null)
      })

    return () => {
      cancelled = true
    }
  }, [isSpectator, spectator?.id, location.pathname])

  useEffect(() => {
    function handleMembership(event) {
      setSubscription(event.detail ?? null)
    }
    window.addEventListener('filmzone:membresia', handleMembership)
    return () => window.removeEventListener('filmzone:membresia', handleMembership)
  }, [])

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) setOpen(false)
    }
    document.addEventListener('mousedown', closeOnOutsideClick)
    return () => document.removeEventListener('mousedown', closeOnOutsideClick)
  }, [])

  async function toggleMenu() {
    const next = !open
    setOpen(next)
    if (!next) return

    try {
      setSubscription(await api.getMembresiaActual())
    } catch {
      setSubscription(null)
    }
  }

  async function handleLogout() {
    setLoggingOut(true)
    try {
      if (hasActivePurchase) await cancelActivePurchase()
      await logout()
      setShowLogoutConfirm(false)
      setOpen(false)
      navigate('/')
    } finally {
      setLoggingOut(false)
    }
  }

  if (!isSpectator) {
    return (
      <button type="button" className="secondary-button" onClick={onLogin}>
        Iniciar sesión
      </button>
    )
  }

  return (
    <>
      <div className="user-menu-container" ref={menuRef}>
        <button
          type="button"
          className={`user-icon-button ${open ? 'is-active' : ''}`}
          aria-label="Abrir menú de mi cuenta"
          aria-expanded={open}
          title="Mi cuenta"
          onClick={toggleMenu}
        >
          <UserRound size={22} aria-hidden="true" />
        </button>

        {open && (
          <div className="dropdown-menu">
            <div className="dropdown-header">
              Hola, <strong>{spectator.nombre}</strong>
            </div>

            <NavLink to="/cuenta#entradas" className="dropdown-item" onClick={() => setOpen(false)}>
              Mis tickets
            </NavLink>

            {subscription && (
              <NavLink to="/cuenta#membresia" className="dropdown-item" onClick={() => setOpen(false)}>
                Membresía
              </NavLink>
            )}

            <NavLink to="/cuenta#perfil" className="dropdown-item" onClick={() => setOpen(false)}>
              Editar Perfil
            </NavLink>

            <NavLink to="/cuenta#pagos" className="dropdown-item" onClick={() => setOpen(false)}>
              Métodos de Pago
            </NavLink>

            <hr className="dropdown-divider" />

            <button
              type="button"
              className="dropdown-item text-danger"
              onClick={() => {
                setOpen(false)
                setShowLogoutConfirm(true)
              }}
            >
              Cerrar sesión
            </button>
          </div>
        )}
      </div>

      {showLogoutConfirm && (
        <ConfirmDialog
          title="Cerrar sesión"
          message="¿Querés cerrar tu sesión en Film Zone?"
          confirmLabel="Cerrar sesión"
          danger
          loading={loggingOut}
          onConfirm={handleLogout}
          onCancel={() => setShowLogoutConfirm(false)}
        />
      )}
    </>
  )
}
