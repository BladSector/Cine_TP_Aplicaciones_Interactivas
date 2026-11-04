import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { errorMessage } from '../api/client'
import Modal from '../components/common/Modal'
import MembresiaPanel from '../components/cuenta/MembresiaPanel'
import MetodosPagoList from '../components/cuenta/MetodosPagoList'
import MisEntradasList from '../components/cuenta/MisEntradasList'
import PerfilForm from '../components/cuenta/PerfilForm'
import LoginForm from '../components/cuenta/auth/LoginForm'
import RegisterForm from '../components/cuenta/auth/RegisterForm'
import PasswordRecoveryFlow from '../components/cuenta/auth/PasswordRecoveryFlow'
import { useAuth } from '../context/AuthContext'

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
    authError,
    setAuthError,
  } = useAuth()
  const location = useLocation()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState('')
  const [showVerification, setShowVerification] = useState(false)
  const [recoveryOpen, setRecoveryOpen] = useState(false)
  const [recoveryBusy, setRecoveryBusy] = useState(false)

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
    setForm((previous) => ({ ...EMPTY_FORM, email: previous.email }))
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

  function completeRecovery(email) {
    setRecoveryOpen(false)
    setForm({ ...EMPTY_FORM, email })
    setNotice('Contraseña actualizada. Ya podés iniciar sesión.')
    setMode('login')
  }

  if (!isSpectator) {
    return (
      <section className="account-gate">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Tu cine, en un solo lugar</h1>
        <p className="muted">Ingresá para comprar, administrar tus tarjetas y consultar tus entradas.</p>
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'is-active' : ''} onClick={() => changeMode('login')} disabled={submitting}>Iniciar sesión</button>
          <button type="button" className={mode === 'register' ? 'is-active' : ''} onClick={() => changeMode('register')} disabled={submitting}>Nueva cuenta</button>
        </div>
        {notice && <div className="info-box">{notice}</div>}
        {mode === 'login' ? (
          <LoginForm
            form={form}
            error={authError}
            submitting={submitting}
            className="account-form"
            onChange={updateForm}
            onSubmit={handleAuth}
            onForgotPassword={() => setRecoveryOpen(true)}
          />
        ) : (
          <RegisterForm
            form={form}
            error={authError}
            submitting={submitting}
            className="account-form"
            groupNameFields
            onChange={updateForm}
            onSubmit={handleAuth}
          />
        )}

        {recoveryOpen && (
          <Modal title="Recuperar contraseña" onClose={() => setRecoveryOpen(false)} closeDisabled={recoveryBusy}>
            <PasswordRecoveryFlow
              initialEmail={form.email}
              onCancel={() => setRecoveryOpen(false)}
              onComplete={completeRecovery}
              onBusyChange={setRecoveryBusy}
            />
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
      {activeSection === '#entradas' && <MisEntradasList key={spectator.id} />}
      {activeSection === '#membresia' && <MembresiaPanel />}
      {activeSection === '#perfil' && <div className="account-grid account-grid--profile"><PerfilForm /></div>}
      {activeSection === '#pagos' && <div className="account-grid account-grid--wallet"><MetodosPagoList /></div>}

      {showVerification && (
        <Modal title="Confirmá tu correo" onClose={() => setShowVerification(false)} closeDisabled={submitting}>
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
