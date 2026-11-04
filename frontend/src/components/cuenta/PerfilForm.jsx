import { useEffect, useState } from 'react'
import { KeyRound, Mail } from 'lucide-react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import Modal from '../common/Modal'
import PasswordRecoveryFlow from './auth/PasswordRecoveryFlow'

export default function PerfilForm() {
  const { spectator, refreshSpectator, verifySpectatorEmail } = useAuth()
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [profile, setProfile] = useState({ nombre: '', apellido: '', email: '' })
  const [verificationEmail, setVerificationEmail] = useState('')
  const [verificationError, setVerificationError] = useState('')
  const [verificationLoading, setVerificationLoading] = useState(false)
  const [recoveryOpen, setRecoveryOpen] = useState(false)
  const [recoveryBusy, setRecoveryBusy] = useState(false)

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
        <button type="button" className="secondary-button" onClick={() => setRecoveryOpen(true)}>
          <KeyRound size={18} aria-hidden="true" />
          Cambiar contraseña
        </button>
      </div>

      {recoveryOpen && (
        <Modal title="Restablecer contraseña" onClose={() => setRecoveryOpen(false)} closeDisabled={recoveryBusy}>
          <PasswordRecoveryFlow
            initialEmail={spectator.email}
            fixedEmail
            cancelLabel="Cancelar"
            onBusyChange={setRecoveryBusy}
            onCancel={() => setRecoveryOpen(false)}
            onComplete={() => {
              setRecoveryOpen(false)
              setMessage('Tu contraseña se restableció correctamente.')
            }}
          />
        </Modal>
      )}

      {verificationEmail && (
        <Modal title="Confirmá tu nuevo correo" onClose={() => setVerificationEmail('')} closeDisabled={verificationLoading}>
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
