import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'

const EMPTY_FORM = {
  email: '',
  contrasenia: '',
  nombre: '',
  apellido: '',
  contraseniaConfirmacion: '',
}

export default function PurchaseAuthDrawer({ open, onClose, onComplete, purchase = true }) {
  const {
    spectator,
    isSpectator,
    loginSpectator,
    registerSpectator,
    verifySpectatorEmail,
    authError,
    setAuthError,
  } = useAuth()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open) return undefined

    function handleKey(event) {
      if (event.key === 'Escape') onClose()
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', handleKey)

    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', handleKey)
    }
  }, [open, onClose])

  if (!open) return null

  function updateField(event) {
    setForm((previous) => ({ ...previous, [event.target.name]: event.target.value }))
  }

  function changeMode(nextMode) {
    setMode(nextMode)
    setAuthError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setAuthError('')

    try {
      if (mode === 'login') {
        const logged = await loginSpectator(form.email, form.contrasenia)
        if (logged.emailVerificado) onComplete()
      } else {
        if (form.contrasenia !== form.contraseniaConfirmacion) {
          throw new Error('Las contraseñas no coinciden.')
        }
        await registerSpectator(form)
      }
    } catch (error) {
      if (!authError) setAuthError(error.message || 'No se pudo completar el acceso.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleVerify() {
    setSubmitting(true)
    setAuthError('')
    try {
      await verifySpectatorEmail()
      onComplete()
    } catch {
      // El contexto muestra el mensaje devuelto por el backend.
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-drawer-backdrop" role="presentation" onMouseDown={onClose}>
      <aside
        className="auth-drawer"
        role="dialog"
        aria-modal="true"
        aria-label="Ingresar para continuar la compra"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="auth-drawer__header">
          <div>
            <p className="eyebrow">{purchase ? 'Continuar compra' : 'Mi cuenta'}</p>
            <h2>{isSpectator ? 'Verificá tu correo' : 'Ingresá a tu cuenta'}</h2>
          </div>
          <button type="button" className="modal-close" onClick={onClose} aria-label="Cerrar">
            &times;
          </button>
        </header>

        <div className="auth-drawer__body">
          {isSpectator ? (
            <div className="stack-form">
              <p>Confirmá tu correo para habilitar la compra.</p>
              <div className="simulated-mail">
                <p className="eyebrow">Correo simulado</p>
                <strong>{spectator.email}</strong>
              </div>
              {authError && <div className="info-box info-box--error">{authError}</div>}
              <button type="button" className="primary-button" onClick={handleVerify} disabled={submitting}>
                {submitting ? 'Verificando…' : 'Confirmar correo y continuar'}
              </button>
            </div>
          ) : (
            <>
              <p className="muted">
                {purchase
                  ? 'Necesitás una cuenta para reservar tus butacas.'
                  : 'Ingresá para consultar tus entradas, tarjetas y datos personales.'}
              </p>
              <div className="auth-tabs">
                <button type="button" className={mode === 'login' ? 'is-active' : ''} onClick={() => changeMode('login')}>
                  Iniciar sesión
                </button>
                <button type="button" className={mode === 'register' ? 'is-active' : ''} onClick={() => changeMode('register')}>
                  Nueva cuenta
                </button>
              </div>

              <form className="stack-form" onSubmit={handleSubmit}>
                {mode === 'register' && (
                  <>
                    <label>Nombre<input name="nombre" value={form.nombre} onChange={updateField} required /></label>
                    <label>Apellido<input name="apellido" value={form.apellido} onChange={updateField} required /></label>
                  </>
                )}
                <label>Correo electrónico<input type="email" name="email" value={form.email} onChange={updateField} required /></label>
                <label>Contraseña<input type="password" name="contrasenia" value={form.contrasenia} onChange={updateField} minLength="4" required /></label>
                {mode === 'register' && (
                  <label>Repetir contraseña<input type="password" name="contraseniaConfirmacion" value={form.contraseniaConfirmacion} onChange={updateField} minLength="4" required /></label>
                )}
                {authError && <div className="info-box info-box--error">{authError}</div>}
                <button type="submit" className="primary-button" disabled={submitting}>
                  {submitting
                    ? 'Procesando…'
                    : mode === 'login'
                      ? purchase ? 'Ingresar y continuar' : 'Ingresar'
                      : 'Crear cuenta'}
                </button>
              </form>
            </>
          )}
        </div>
      </aside>
    </div>
  )
}
