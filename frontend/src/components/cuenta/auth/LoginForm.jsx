export default function LoginForm({
  form,
  error,
  notice,
  submitting,
  purchase,
  onChange,
  onSubmit,
  onForgotPassword,
}) {
  return (
    <>
      {notice && <div className="info-box">{notice}</div>}

      <form className="stack-form" onSubmit={onSubmit}>
        <label>
          Correo electrónico
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Contraseña
          <input
            type="password"
            name="contrasenia"
            value={form.contrasenia}
            onChange={onChange}
            minLength="4"
            required
          />
        </label>

        {error && <div className="info-box info-box--error">{error}</div>}

        <button type="submit" className="primary-button" disabled={submitting}>
          {submitting ? 'Procesando…' : purchase ? 'Ingresar y continuar' : 'Ingresar'}
        </button>

        <button
          type="button"
          className="text-button auth-recovery-link"
          onClick={onForgotPassword}
        >
          ¿Olvidaste la contraseña?
        </button>
      </form>
    </>
  )
}
