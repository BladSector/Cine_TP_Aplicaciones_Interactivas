import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function AdminLoginPage() {
  const navigate = useNavigate()
  const { isPersonal, loginPersonal, authError, setAuthError } = useAuth()
  const [usuario, setUsuario] = useState('')
  const [contrasenia, setContrasenia] = useState('')
  const [loading, setLoading] = useState(false)

  if (isPersonal) return <Navigate to="/admin" replace />

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setAuthError('')
    try {
      await loginPersonal(usuario, contrasenia)
      navigate('/admin')
    } catch {
      // handled
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-login">
      <Link to="/" className="back-link">
        Volver al sitio
      </Link>
      <section className="admin-login__card">
        <p className="eyebrow">Personal Noche Cine</p>
        <h1>Acceso al panel</h1>
        <p className="muted">Administrá cartelera, salas, funciones y confitería.</p>
        <form className="stack-form" onSubmit={handleSubmit}>
          <label>
            Usuario
            <input value={usuario} onChange={(e) => setUsuario(e.target.value)} required />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              value={contrasenia}
              onChange={(e) => setContrasenia(e.target.value)}
              required
            />
          </label>
          {authError && <div className="info-box info-box--error">{authError}</div>}
          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? 'Ingresando…' : 'Ingresar'}
          </button>
        </form>
      </section>
    </div>
  )
}
