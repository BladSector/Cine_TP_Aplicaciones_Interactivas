export default function RegisterForm({
  form,
  error,
  submitting,
  onChange,
  onSubmit,
  className = '',
  groupNameFields = false,
}) {
  const nameFields = (
    <>
      <label>
        Nombre
        <input name="nombre" value={form.nombre} onChange={onChange} required />
      </label>

      <label>
        Apellido
        <input name="apellido" value={form.apellido} onChange={onChange} required />
      </label>
    </>
  )

  return (
    <form className={`stack-form ${className}`.trim()} onSubmit={onSubmit}>
      {groupNameFields
        ? <div className="form-grid form-grid--two">{nameFields}</div>
        : nameFields}

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

      <label>
        Repetir contraseña
        <input
          type="password"
          name="contraseniaConfirmacion"
          value={form.contraseniaConfirmacion}
          onChange={onChange}
          minLength="4"
          required
        />
      </label>

      {error && <div className="info-box info-box--error">{error}</div>}

      <button type="submit" className="primary-button" disabled={submitting}>
        {submitting ? 'Procesando…' : 'Crear cuenta'}
      </button>
    </form>
  )
}
