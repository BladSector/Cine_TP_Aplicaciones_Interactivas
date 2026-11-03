import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { useAuth } from '../../context/AuthContext'
import EmailVerification from './auth/EmailVerification'
import LoginForm from './auth/LoginForm'
import PasswordRecoveryFlow from './auth/PasswordRecoveryFlow'
import RegisterForm from './auth/RegisterForm'

const EMPTY_FORM = {
  email: '',
  contrasenia: '',
  nombre: '',
  apellido: '',
  contraseniaConfirmacion: '',
}

export default function PurchaseAuthDrawer({
  open,
  onClose,
  onComplete,
  purchase = true,
  purchaseDescription,
}) {
  const {
    spectator,
    loginSpectator,
    registerSpectator,
    verifySpectatorEmail,
    authError,
    setAuthError,
    loading: authLoading,
  } = useAuth()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState('')
  const [recoveryOpen, setRecoveryOpen] = useState(false)
  const verifiedCompletionRef = useRef(false)
  const requiresEmailVerification = Boolean(spectator && !spectator.emailVerificado)

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

  useEffect(() => {
    if (!open) {
      verifiedCompletionRef.current = false
      return
    }
    if (authLoading || !spectator?.emailVerificado || verifiedCompletionRef.current) return

    verifiedCompletionRef.current = true
    onComplete()
  }, [open, authLoading, spectator?.emailVerificado, onComplete])

  if (!open) return null

  function updateField(event) {
    setForm((previous) => ({ ...previous, [event.target.name]: event.target.value }))
  }

  function changeMode(nextMode) {
    setMode(nextMode)
    setAuthError('')
    setNotice('')
    setRecoveryOpen(false)
  }

  function openRecovery() {
    setRecoveryOpen(true)
    setAuthError('')
    setNotice('')
  }

  function completeRecovery(email) {
    setForm((previous) => ({ ...previous, email, contrasenia: '' }))
    setRecoveryOpen(false)
    setMode('login')
    setNotice('Contraseña actualizada. Ya podés iniciar sesión.')
  }

  async function handleLogin(event) {
    event.preventDefault()
    setSubmitting(true)
    setAuthError('')

    try {
      const logged = await loginSpectator(form.email, form.contrasenia)
      if (logged.emailVerificado) {
        verifiedCompletionRef.current = true
        onComplete()
      }
    } catch (error) {
      setAuthError((current) => current || error.message || 'No se pudo iniciar sesión.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRegister(event) {
    event.preventDefault()
    setAuthError('')

    if (form.contrasenia !== form.contraseniaConfirmacion) {
      setAuthError('Las contraseñas no coinciden.')
      return
    }

    setSubmitting(true)
    try {
      await registerSpectator(form)
    } catch (error) {
      setAuthError((current) => current || error.message || 'No se pudo crear la cuenta.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleVerify() {
    setSubmitting(true)
    setAuthError('')
    try {
      await verifySpectatorEmail()
      verifiedCompletionRef.current = true
      onComplete()
    } catch {
      // AuthContext conserva el mensaje enviado por el backend.
    } finally {
      setSubmitting(false)
    }
  }

  const title = requiresEmailVerification
    ? 'Verificá tu correo'
    : recoveryOpen
      ? 'Restablecé tu contraseña'
      : 'Ingresá a tu cuenta'

  return createPortal(
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
            <h2>{title}</h2>
          </div>
          <button type="button" className="modal-close" onClick={onClose} aria-label="Cerrar">
            &times;
          </button>
        </header>

        <div className="auth-drawer__body">
          {requiresEmailVerification ? (
            <EmailVerification
              email={spectator.email}
              error={authError}
              submitting={submitting}
              onVerify={handleVerify}
            />
          ) : recoveryOpen ? (
            <PasswordRecoveryFlow
              initialEmail={form.email}
              onCancel={() => setRecoveryOpen(false)}
              onComplete={completeRecovery}
            />
          ) : (
            <>
              <p className="muted">
                {purchase
                  ? purchaseDescription || 'Necesitás una cuenta para reservar tus butacas.'
                  : 'Ingresá para consultar tus entradas, tarjetas y datos personales.'}
              </p>

              <div className="auth-tabs">
                <button
                  type="button"
                  className={mode === 'login' ? 'is-active' : ''}
                  onClick={() => changeMode('login')}
                >
                  Iniciar sesión
                </button>
                <button
                  type="button"
                  className={mode === 'register' ? 'is-active' : ''}
                  onClick={() => changeMode('register')}
                >
                  Nueva cuenta
                </button>
              </div>

              {mode === 'login' ? (
                <LoginForm
                  form={form}
                  error={authError}
                  notice={notice}
                  submitting={submitting}
                  purchase={purchase}
                  onChange={updateField}
                  onSubmit={handleLogin}
                  onForgotPassword={openRecovery}
                />
              ) : (
                <RegisterForm
                  form={form}
                  error={authError}
                  submitting={submitting}
                  onChange={updateField}
                  onSubmit={handleRegister}
                />
              )}
            </>
          )}
        </div>
      </aside>
    </div>,
    document.body,
  )
}
