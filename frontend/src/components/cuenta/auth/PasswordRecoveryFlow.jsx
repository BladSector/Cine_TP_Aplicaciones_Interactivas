import { Mail } from 'lucide-react'
import { useEffect, useState } from 'react'
import { api, errorMessage } from '../../../api/client'

const EMPTY_FORM = {
  email: '',
  nuevaContrasenia: '',
  nuevaContraseniaConfirmacion: '',
}

export default function PasswordRecoveryFlow({
  initialEmail = '',
  fixedEmail = false,
  cancelLabel = 'Volver a iniciar sesión',
  onCancel,
  onComplete,
  onBusyChange,
}) {
  const [step, setStep] = useState('request')
  const [mail, setMail] = useState(null)
  const [form, setForm] = useState({ ...EMPTY_FORM, email: initialEmail })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    onBusyChange?.(submitting)
    return () => onBusyChange?.(false)
  }, [submitting, onBusyChange])

  function updateField(field, value) {
    setForm((previous) => ({ ...previous, [field]: value }))
  }

  async function requestRecovery(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      const result = await api.solicitarRecuperacion(form.email)
      setMail(result)
      setStep('mail')
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  async function finishRecovery(event) {
    event.preventDefault()
    setError('')

    if (form.nuevaContrasenia !== form.nuevaContraseniaConfirmacion) {
      setError('Las contraseñas no coinciden.')
      return
    }

    setSubmitting(true)
    try {
      await api.recuperarContrasenia({
        email: mail.email,
        token: mail.token,
        nuevaContrasenia: form.nuevaContrasenia,
        nuevaContraseniaConfirmacion: form.nuevaContraseniaConfirmacion,
      })
      onComplete(mail.email)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  if (step === 'request') {
    return (
      <form className="stack-form" onSubmit={requestRecovery}>
        {fixedEmail ? (
          <>
            <div className="recovery-recipient">
              <Mail size={20} aria-hidden="true" />
              <div>
                <span>Enviar correo a</span>
                <strong>{form.email}</strong>
              </div>
            </div>
            <p className="muted">Recibirás un enlace para elegir una contraseña nueva.</p>
          </>
        ) : (
          <>
            <p>Ingresá el correo asociado a tu cuenta.</p>
            <label>
              Correo electrónico
              <input
                type="email"
                value={form.email}
                onChange={(event) => updateField('email', event.target.value)}
                required
                autoFocus
              />
            </label>
          </>
        )}

        {error && <div className="info-box info-box--error">{error}</div>}

        <button type="submit" className="primary-button" disabled={submitting}>
          {submitting ? 'Enviando…' : 'Enviar correo'}
        </button>
        <button type="button" className="text-button auth-recovery-link" onClick={onCancel} disabled={submitting}>
          {cancelLabel}
        </button>
      </form>
    )
  }

  if (step === 'mail') {
    return (
      <div className="simulated-mail simulated-mail--inbox">
        <div className="simulated-mail__heading">
          <Mail size={22} aria-hidden="true" />
          <div>
            <p className="eyebrow">Correo simulado</p>
            <strong>Restablecé tu contraseña</strong>
          </div>
        </div>
        <p>Enviamos un correo a <strong>{mail.email}</strong>.</p>
        <p>Usá el enlace del mensaje para elegir una contraseña nueva.</p>
        <button type="button" className="primary-button" onClick={() => setStep('password')}>
          Restablecer contraseña
        </button>
        <small>Este correo es una simulación y no se envía fuera de la aplicación.</small>
      </div>
    )
  }

  return (
    <form className="stack-form" onSubmit={finishRecovery}>
      <label>
        Nueva contraseña
        <input
          type="password"
          value={form.nuevaContrasenia}
          onChange={(event) => updateField('nuevaContrasenia', event.target.value)}
          minLength="4"
          required
          autoFocus
        />
      </label>

      <label>
        Repetir contraseña
        <input
          type="password"
          value={form.nuevaContraseniaConfirmacion}
          onChange={(event) => updateField('nuevaContraseniaConfirmacion', event.target.value)}
          minLength="4"
          required
        />
      </label>

      {error && <div className="info-box info-box--error">{error}</div>}

      <button type="submit" className="primary-button" disabled={submitting}>
        {submitting ? 'Actualizando…' : 'Guardar contraseña'}
      </button>
    </form>
  )
}
