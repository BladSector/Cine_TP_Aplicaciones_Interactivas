import { useEffect, useState } from 'react'
import { KeyRound, Mail } from 'lucide-react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import Modal from '../common/Modal'

const EMPTY_PASSWORD_FORM = {
  nuevaContrasenia: '',
  nuevaContraseniaConfirmacion: '',
}

export default function PerfilForm() {
  const { spectator, refreshSpectator, verifySpectatorEmail } = useAuth()
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [profile, setProfile] = useState({ nombre: '', apellido: '', email: '' })
  const [verificationEmail, setVerificationEmail] = useState('')
  const [verificationError, setVerificationError] = useState('')
  const [verificationLoading, setVerificationLoading] = useState(false)
  const [recoveryStep, setRecoveryStep] = useState(null)
  const [recoveryMail, setRecoveryMail] = useState(null)
  const [passwordForm, setPasswordForm] = useState(EMPTY_PASSWORD_FORM)
  const [recoveryError, setRecoveryError] = useState('')
  const [recoveryLoading, setRecoveryLoading] = useState(false)

  useEffect(() => {
    if (spectator) {
      setProfile({
        nombre: spectator.nombre,
        apellido: spectator.apellido,
        email: spectator.email,
      })
    }
  }, [spectator])

  function update(setter) {
    return (event) => setter((prev) => ({ ...prev, [event.target.name]: event.target.value }))
  }

  async function handleProfile(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const emailChanged = profile.email.trim().toLowerCase() !== spectator.email.trim().toLowerCase()
      const updated = await api.updateEspectador(spectator.id, profile)
      await refreshSpectator(updated.id)
      if (emailChanged) {
        setVerificationEmail(updated.email)
        setVerificationError('')
        setMessage('Tus datos se actualizaron. Confirmá el nuevo correo para volver a habilitar las compras.')
      } else {
        setMessage('Tus datos se actualizaron correctamente.')
      }
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function confirmNewEmail() {
    setVerificationLoading(true)
    setVerificationError('')
    try {
      await verifySpectatorEmail()
      setVerificationEmail('')
      setMessage('Tu nuevo correo fue verificado correctamente.')
    } catch (err) {
      setVerificationError(errorMessage(err))
    } finally {
      setVerificationLoading(false)
    }
  }

  function openRecovery() {
    setRecoveryStep('request')
    setRecoveryMail(null)
    setPasswordForm(EMPTY_PASSWORD_FORM)
    setRecoveryError('')
  }

  function closeRecovery() {
    setRecoveryStep(null)
    setRecoveryMail(null)
    setPasswordForm(EMPTY_PASSWORD_FORM)
    setRecoveryError('')
  }

  async function requestRecovery() {
    setRecoveryLoading(true)
    setRecoveryError('')
    try {
      const result = await api.solicitarRecuperacion(spectator.email)
      setRecoveryMail(result)
      setRecoveryStep('mail')
    } catch (err) {
      setRecoveryError(errorMessage(err))
    } finally {
      setRecoveryLoading(false)
    }
  }

  async function finishRecovery(event) {
    event.preventDefault()
    setRecoveryError('')
    if (passwordForm.nuevaContrasenia !== passwordForm.nuevaContraseniaConfirmacion) {
      setRecoveryError('Las contraseñas no coinciden.')
      return
    }

    setRecoveryLoading(true)
    try {
      await api.recuperarContrasenia({
        email: spectator.email,
        token: recoveryMail.token,
        ...passwordForm,
      })
      closeRecovery()
      setMessage('Tu contraseña se restableció correctamente.')
    } catch (err) {
      setRecoveryError(errorMessage(err))
    } finally {
      setRecoveryLoading(false)
    }
  }

  return (
    <section id="perfil" className="account-section">
      <h2>Editar perfil</h2>
      {message && <div className="info-box">{message}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}

      <form className="stack-form" onSubmit={handleProfile}>
        <label>
          Nombre
          <input name="nombre" value={profile.nombre} onChange={update(setProfile)} required />
        </label>
        <label>
          Apellido
          <input name="apellido" value={profile.apellido} onChange={update(setProfile)} required />
        </label>
        <label>
          Email
          <input type="email" name="email" value={profile.email} onChange={update(setProfile)} required />
        </label>
        <button type="submit" className="primary-button">
          Guardar datos
        </button>
      </form>

      <div className="profile-security">
        <div>
          <strong>Contraseña</strong>
          <p className="muted">Te enviaremos un correo para restablecerla de forma segura.</p>
        </div>
        <button type="button" className="secondary-button" onClick={openRecovery}>
          <KeyRound size={18} aria-hidden="true" />
          Cambiar contraseña
        </button>
      </div>

      {recoveryStep && (
        <Modal title="Restablecer contraseña" onClose={closeRecovery}>
          {recoveryStep === 'request' && (
            <div className="stack-form">
              <div className="recovery-recipient">
                <Mail size={20} aria-hidden="true" />
                <div>
                  <span>Enviar correo a</span>
                  <strong>{spectator.email}</strong>
                </div>
              </div>
              <p className="muted">Recibirás un enlace para elegir una contraseña nueva.</p>
              {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
              <button type="button" className="primary-button" disabled={recoveryLoading} onClick={requestRecovery}>
                {recoveryLoading ? 'Enviando…' : 'Enviar correo'}
              </button>
            </div>
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
              <p>Hola, {spectator.nombre}. Recibimos una solicitud para cambiar tu contraseña.</p>
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
                <input type="password" name="nuevaContrasenia" value={passwordForm.nuevaContrasenia} onChange={update(setPasswordForm)} minLength="4" required autoFocus />
              </label>
              <label>
                Repetir contraseña
                <input type="password" name="nuevaContraseniaConfirmacion" value={passwordForm.nuevaContraseniaConfirmacion} onChange={update(setPasswordForm)} minLength="4" required />
              </label>
              {recoveryError && <div className="info-box info-box--error">{recoveryError}</div>}
              <button type="submit" className="primary-button" disabled={recoveryLoading}>
                {recoveryLoading ? 'Actualizando…' : 'Guardar contraseña'}
              </button>
            </form>
          )}
        </Modal>
      )}

      {verificationEmail && (
        <Modal title="Confirmá tu nuevo correo" onClose={() => setVerificationEmail('')}>
          <div className="simulated-mail simulated-mail--inbox">
            <div className="simulated-mail__heading">
              <Mail size={22} aria-hidden="true" />
              <div>
                <p className="eyebrow">Correo simulado</p>
                <strong>Verificá tu nueva dirección</strong>
              </div>
            </div>
            <p>Enviamos la confirmación a <strong>{verificationEmail}</strong>.</p>
            <p>Confirmá que esta dirección te pertenece para volver a realizar compras.</p>
            {verificationError && <div className="info-box info-box--error">{verificationError}</div>}
            <button type="button" className="primary-button" disabled={verificationLoading} onClick={confirmNewEmail}>
              {verificationLoading ? 'Verificando…' : 'Confirmar nuevo correo'}
            </button>
            <small>Este correo es una simulación y no se envía fuera de la aplicación.</small>
          </div>
        </Modal>
      )}
    </section>
  )
}
