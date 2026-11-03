export default function EmailVerification({
  email,
  error,
  submitting,
  onVerify,
}) {
  return (
    <div className="stack-form">
      <p>Confirmá tu correo para habilitar la compra.</p>

      <div className="simulated-mail">
        <p className="eyebrow">Correo simulado</p>
        <strong>{email}</strong>
      </div>

      {error && <div className="info-box info-box--error">{error}</div>}

      <button
        type="button"
        className="primary-button"
        onClick={onVerify}
        disabled={submitting}
      >
        {submitting ? 'Verificando…' : 'Confirmar correo y continuar'}
      </button>
    </div>
  )
}
