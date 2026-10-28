import { useEffect, useState } from 'react'
import { Mail } from 'lucide-react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'

const EMPTY_FORM = {
  email: '',
  contrasenia: '',
  nombre: '',
  apellido: '',
  contraseniaConfirmacion: '',
}

const EMPTY_RECOVERY_FORM = {
  email: '',
  nuevaContrasenia: '',
  nuevaContraseniaConfirmacion: '',
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
  const [notice, setNotice] = useState('')
  const [recoveryStep, setRecoveryStep] = useState(null)
  const [recoveryMail, setRecoveryMail] = useState(null)
  const [recoveryForm, setRecoveryForm] = useState(EMPTY_RECOVERY_FORM)
  const [recoveryError, setRecoveryError] = useState('')

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
    setNotice('')
    closeRecovery()
  }

  function openRecovery() {
    setRecoveryStep('request')
    setRecoveryMail(null)
    setRecoveryForm({ ...EMPTY_RECOVERY_FORM, email: form.email })
    setRecoveryError('')
    setAuthError('')
    setNotice('')
  }

  function closeRecovery() {
    setRecoveryStep(null)
    setRecoveryMail(null)
    setRecoveryForm(EMPTY_RECOVERY_FORM)
    setRecoveryError('')
  }

  async function requestRecovery(event) {
    event.preventDefault()
    setSubmitting(true)
    setRecoveryError('')
    try {
      const result = await api.solicitarRecuperacion(recoveryForm.email)
      setRecoveryMail(result)
      setRecoveryStep('mail')
    } catch (error) {
      setRecoveryError(errorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  async function finishRecovery(event) {
    event.preventDefault()
    setRecoveryError('')
    if (recoveryForm.nuevaContrasenia !== recoveryForm.nuevaContraseniaConfirmacion) {
      setRecoveryError('Las contraseñas no coinciden.')
      return
    }

    setSubmitting(true)
    try {
      await api.recuperarContrasenia({
        email: recoveryMail.email,
        token: recoveryMail.token,
        nuevaContrasenia: recoveryForm.nuevaContrasenia,
        nuevaContraseniaConfirmacion: recoveryForm.nuevaContraseniaConfirmacion,
      })
      setForm((previous) => ({ ...previous, email: recoveryMail.email, contrasenia: '' }))
      closeRecovery()
      setMode('login')
      setNotice('Contraseña actualizada. Ya podés iniciar sesión.')
    } catch (error) {
      setRecoveryError(errorMessage(error))
    } finally {
      setSubmitting(false)
    }
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
            <h2>
              {isSpectator
                ? 'Verificá tu correo'
                : recoveryStep
                  ? 'Restablecé tu contraseña'
                  : 'Ingresá a tu cuenta'}
            </h2>
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
          ) : recoveryStep ? (
            <>
              {recoveryStep === 'request' && (
                <form className="stack-form" onSubmit={requestRecovery}>
                  <p>Ingresá el correo asociado a tu cuenta.</p>
                  <label>
                    Correo electrónico
                    <input
                      type="email"
                      value={recoveryForm.email}
                      onChange={(event) => setRecoveryForm((previous) => ({
                        ...previous,
                        email: event.target.value,
                      }))}
                      required
                      autoFocus
                    />
                  </label>
                  {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
                  <button type="submit" className="primary-button" disabled={submitting}>
                    {submitting ? 'Enviando…' : 'Enviar correo'}
                  </button>
                  <button type="button" className="text-button auth-recovery-link" onClick={closeRecovery}>
                    Volver a iniciar sesión
                  </button>
                </form>
              )}

              {recoveryStep === 'mail' && (
                <div className="simulated-mail simulated-mail--inbox">
                  <div className="simulated-mail__heading">
                    <Mail size={22} aria-hidden="true" />
                    <div>
                      <p className="eyebrow">Correo simulado</p>
                      <strong>Restablecé tu contraseña</strong>
                    </div>
                  </div>
                  <p>Enviamos un correo a <strong>{recoveryMail.email}</strong>.</p>
                  <p>Usá el enlace del mensaje para elegir una contraseña nueva.</p>
                  <button type="button" className="primary-button" onClick={() => setRecoveryStep('password')}>
                    Restablecer contraseña
                  </button>
                  <small>Este correo es una simulación y no se envía fuera de la aplicación.</small>
                </div>
              )}

              {recoveryStep === 'password' && (
                <form className="stack-form" onSubmit={finishRecovery}>
                  <label>
                    Nueva contraseña
                    <input
                      type="password"
                      value={recoveryForm.nuevaContrasenia}
                      onChange={(event) => setRecoveryForm((previous) => ({
                        ...previous,
                        nuevaContrasenia: event.target.value,
                      }))}
                      minLength="4"
                      required
                      autoFocus
                    />
                  </label>
                  <label>
                    Repetir contraseña
                    <input
                      type="password"
                      value={recoveryForm.nuevaContraseniaConfirmacion}
                      onChange={(event) => setRecoveryForm((previous) => ({
                        ...previous,
                        nuevaContraseniaConfirmacion: event.target.value,
                      }))}
                      minLength="4"
                      required
                    />
                  </label>
                  {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
                  <button type="submit" className="primary-button" disabled={submitting}>
                    {submitting ? 'Actualizando…' : 'Guardar contraseña'}
                  </button>
                </form>
              )}
            </>
          ) : (
            <>
              <p className="muted">
                {purchase
                  ? purchaseDescription || 'Necesitás una cuenta para reservar tus butacas.'
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

              {notice && <div className="info-box">{notice}</div>}
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
                {mode === 'login' && (
                  <button type="button" className="text-button auth-recovery-link" onClick={openRecovery}>
                    ¿Olvidaste la contraseña?
                  </button>
                )}
              </form>
            </>
          )}
        </div>
      </aside>
    </div>
  )
}
