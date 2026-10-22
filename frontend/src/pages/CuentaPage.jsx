import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, errorMessage } from '../api/client'
import Modal from '../components/common/Modal'
import MembresiaPanel from '../components/cuenta/MembresiaPanel'
import MetodosPagoList from '../components/cuenta/MetodosPagoList'
import MisEntradasList from '../components/cuenta/MisEntradasList'
import PerfilForm from '../components/cuenta/PerfilForm'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'

const EMPTY_FORM = {
  email: '',
  contrasenia: '',
  nombre: '',
  apellido: '',
  contraseniaConfirmacion: '',
}

export default function CuentaPage() {
  const {
    spectator,
    isSpectator,
    loginSpectator,
    registerSpectator,
    verifySpectatorEmail,
    logout,
    authError,
    setAuthError,
  } = useAuth()
  const { reload } = useCatalog()
  const location = useLocation()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState('')
  const [showVerification, setShowVerification] = useState(false)
  const [recovery, setRecovery] = useState(null)
  const [recoveryForm, setRecoveryForm] = useState({
    email: '',
    token: '',
    nuevaContrasenia: '',
    nuevaContraseniaConfirmacion: '',
  })
  const [recoveryError, setRecoveryError] = useState('')

  useEffect(() => {
    if (spectator) reload()
  }, [spectator, reload])

  useEffect(() => {
    if (!location.hash) return
    const element = document.getElementById(location.hash.replace('#', ''))
    if (element) setTimeout(() => element.scrollIntoView({ behavior: 'smooth', block: 'start' }), 100)
  }, [location])

  function updateForm(event) {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }))
  }

  function changeMode(nextMode) {
    setMode(nextMode)
    setAuthError('')
    setNotice('')
  }

  async function handleAuth(event) {
    event.preventDefault()
    setSubmitting(true)
    setAuthError('')
    setNotice('')
    try {
      if (mode === 'login') {
        const logged = await loginSpectator(form.email, form.contrasenia)
        if (!logged.emailVerificado) setShowVerification(true)
      } else {
        if (form.contrasenia !== form.contraseniaConfirmacion) {
          throw new Error('Las contraseñas no coinciden.')
        }
        await registerSpectator(form)
        setShowVerification(true)
      }
      setForm(EMPTY_FORM)
    } catch (error) {
      if (!authError) setAuthError(errorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleVerify() {
    setSubmitting(true)
    setAuthError('')
    try {
      await verifySpectatorEmail()
      setShowVerification(false)
      setNotice('Correo verificado correctamente. Ya podés realizar compras.')
    } catch (error) {
      setAuthError(errorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  async function requestRecovery(event) {
    event.preventDefault()
    setRecoveryError('')
    try {
      const result = await api.solicitarRecuperacion(recoveryForm.email)
      setRecovery(result)
      setRecoveryForm((prev) => ({ ...prev, token: result.token }))
    } catch (error) {
      setRecoveryError(errorMessage(error))
    }
  }

  async function finishRecovery(event) {
    event.preventDefault()
    setRecoveryError('')
    if (recoveryForm.nuevaContrasenia !== recoveryForm.nuevaContraseniaConfirmacion) {
      setRecoveryError('Las contraseñas no coinciden.')
      return
    }
    try {
      await api.recuperarContrasenia(recoveryForm)
      closeRecovery()
      setNotice('Contraseña actualizada. Ya podés iniciar sesión.')
      setMode('login')
    } catch (error) {
      setRecoveryError(errorMessage(error))
    }
  }

  function closeRecovery() {
    setRecovery(null)
    setRecoveryError('')
    setRecoveryForm({ email: '', token: '', nuevaContrasenia: '', nuevaContraseniaConfirmacion: '' })
  }

  if (!isSpectator) {
    return (
      <section className="account-gate">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Tu cine, en un solo lugar</h1>
        <p className="muted">Ingresá para comprar, administrar tus tarjetas y consultar tus entradas.</p>
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'is-active' : ''} onClick={() => changeMode('login')}>Iniciar sesión</button>
          <button type="button" className={mode === 'register' ? 'is-active' : ''} onClick={() => changeMode('register')}>Nueva cuenta</button>
        </div>
        {notice && <div className="info-box">{notice}</div>}
        <form className="stack-form account-form" onSubmit={handleAuth}>
          {mode === 'register' && (
            <div className="form-grid form-grid--two">
              <label>Nombre<input name="nombre" value={form.nombre} onChange={updateForm} required /></label>
              <label>Apellido<input name="apellido" value={form.apellido} onChange={updateForm} required /></label>
            </div>
          )}
          <label>Email<input type="email" name="email" value={form.email} onChange={updateForm} required /></label>
          <label>Contraseña<input type="password" name="contrasenia" value={form.contrasenia} onChange={updateForm} minLength="4" required /></label>
          {mode === 'register' && (
            <label>Repetir contraseña<input type="password" name="contraseniaConfirmacion" value={form.contraseniaConfirmacion} onChange={updateForm} minLength="4" required /></label>
          )}
          {authError && <div className="info-box info-box--error">{authError}</div>}
          <button type="submit" className="primary-button" disabled={submitting}>
            {submitting ? 'Procesando…' : mode === 'login' ? 'Ingresar' : 'Crear cuenta'}
          </button>
          {mode === 'login' && (
            <button type="button" className="text-button auth-recovery-link" onClick={() => setRecovery({})}>¿Olvidaste la contraseña?</button>
          )}
        </form>

        {recovery && (
          <Modal title="Recuperar contraseña" onClose={closeRecovery}>
            {!recovery.token ? (
              <form className="stack-form" onSubmit={requestRecovery}>
                <p>Ingresá el correo asociado a tu cuenta.</p>
                <label>Email<input type="email" value={recoveryForm.email} onChange={(e) => setRecoveryForm((p) => ({ ...p, email: e.target.value }))} required /></label>
                {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
                <button className="primary-button" type="submit">Enviar correo</button>
              </form>
            ) : (
              <form className="stack-form" onSubmit={finishRecovery}>
                <div className="simulated-mail">
                  <p className="eyebrow">Correo simulado</p>
                  <strong>{recovery.email}</strong>
                  <p>Usá este código para restablecer tu contraseña:</p>
                  <code>{recovery.token}</code>
                </div>
                <label>Código<input value={recoveryForm.token} onChange={(e) => setRecoveryForm((p) => ({ ...p, token: e.target.value }))} required /></label>
                <label>Nueva contraseña<input type="password" value={recoveryForm.nuevaContrasenia} onChange={(e) => setRecoveryForm((p) => ({ ...p, nuevaContrasenia: e.target.value }))} minLength="4" required /></label>
                <label>Repetir contraseña<input type="password" value={recoveryForm.nuevaContraseniaConfirmacion} onChange={(e) => setRecoveryForm((p) => ({ ...p, nuevaContraseniaConfirmacion: e.target.value }))} minLength="4" required /></label>
                {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
                <button className="primary-button" type="submit">Cambiar contraseña</button>
              </form>
            )}
          </Modal>
        )}
      </section>
    )
  }

  const activeSection = location.hash || '#entradas'
  return (
    <div className="account-page">
      <div className="account-header">
        <div>
          <p className="eyebrow">Mi cuenta</p>
          <h1>Hola, {spectator.nombre}</h1>
        </div>
      </div>

      {!spectator.emailVerificado && (
        <div className="verification-banner">
          <div><strong>Tu correo todavía no está verificado.</strong><p>Verificalo antes de realizar una compra.</p></div>
          <button type="button" className="primary-button" onClick={() => setShowVerification(true)}>Verificar ahora</button>
        </div>
      )}
      {notice && <div className="info-box">{notice}</div>}
      {activeSection === '#entradas' && <MisEntradasList />}
      {activeSection === '#membresia' && <MembresiaPanel />}
      {activeSection === '#perfil' && <div className="account-grid account-grid--profile"><PerfilForm /></div>}
      {activeSection === '#pagos' && <div className="account-grid account-grid--wallet"><MetodosPagoList /></div>}

      {showVerification && (
        <Modal title="Confirmá tu correo" onClose={() => setShowVerification(false)}>
          <div className="simulated-mail">
            <p className="eyebrow">Correo simulado</p>
            <strong>{spectator.email}</strong>
            <p>Confirmá que esta dirección te pertenece para habilitar tus compras.</p>
          </div>
          {authError && <div className="info-box info-box--error">{authError}</div>}
          <button type="button" className="primary-button" disabled={submitting} onClick={handleVerify}>
            {submitting ? 'Verificando…' : 'Confirmar correo'}
          </button>
        </Modal>
      )}
    </div>
  )
}
