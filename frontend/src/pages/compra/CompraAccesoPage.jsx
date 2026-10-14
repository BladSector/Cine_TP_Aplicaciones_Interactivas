import { useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import PurchaseStepper from '../../components/compra/PurchaseStepper'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { usePurchase } from '../../context/PurchaseContext'

export default function CompraAccesoPage() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const { peliculas } = useCatalog()
  const { draft } = usePurchase()
  const { isSpectator, loginSpectator, registerSpectator, authError, setAuthError } = useAuth()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({
    email: '',
    contrasenia: '',
    nombre: '',
    apellido: '',
    contraseniaConfirmacion: '',
  })
  const [submitting, setSubmitting] = useState(false)

  const movie = peliculas.find((item) => item.id === Number(movieId))

  if (!draft.functionId) {
    return <Navigate to={`/pelicula/${movieId}`} replace />
  }

  if (isSpectator) {
    return <Navigate to={`/compra/${movieId}/butacas`} replace />
  }

  function updateField(event) {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setAuthError('')
    try {
      if (mode === 'login') {
        await loginSpectator(form.email, form.contrasenia)
      } else {
        await registerSpectator({
          nombre: form.nombre,
          apellido: form.apellido,
          email: form.email,
          contrasenia: form.contrasenia,
          contraseniaConfirmacion: form.contraseniaConfirmacion,
        })
      }
      navigate(`/compra/${movieId}/butacas`)
    } catch {
      // authError ya seteado
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="purchase-page">
      <Link to={`/pelicula/${movieId}`} className="back-link">
        Volver a la película
      </Link>
      <PurchaseStepper current="acceso" />

      <div className="purchase-grid">
        <section className="purchase-panel">
          <p className="eyebrow">Antes de elegir tu función</p>
          <h1>Tu noche empieza aquí</h1>
          <p>Inicia sesión para elegir una función y continuar con tu compra.</p>

          <div className="auth-tabs">
            <button
              type="button"
              className={mode === 'login' ? 'is-active' : ''}
              onClick={() => setMode('login')}
            >
              Iniciar sesión
            </button>
            <button
              type="button"
              className={mode === 'register' ? 'is-active' : ''}
              onClick={() => setMode('register')}
            >
              Crear cuenta
            </button>
          </div>

          <form className="stack-form" onSubmit={handleSubmit}>
            {mode === 'register' && (
              <>
                <label>
                  Nombre
                  <input name="nombre" value={form.nombre} onChange={updateField} required />
                </label>
                <label>
                  Apellido
                  <input name="apellido" value={form.apellido} onChange={updateField} required />
                </label>
              </>
            )}
            <label>
              Correo electrónico
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={updateField}
                required
              />
            </label>
            <label>
              Contraseña
              <input
                type="password"
                name="contrasenia"
                value={form.contrasenia}
                onChange={updateField}
                required
              />
            </label>
            {mode === 'register' && (
              <label>
                Repetir contraseña
                <input
                  type="password"
                  name="contraseniaConfirmacion"
                  value={form.contraseniaConfirmacion}
                  onChange={updateField}
                  required
                />
              </label>
            )}

            {authError && <div className="info-box info-box--error">{authError}</div>}

            <button type="submit" className="primary-button" disabled={submitting}>
              {submitting
                ? 'Ingresando…'
                : mode === 'login'
                  ? 'Iniciar sesión y elegir función'
                  : 'Crear cuenta y continuar'}
            </button>
          </form>
        </section>

        <aside className="order-summary">
          <p className="eyebrow">Una cuenta, tu reserva</p>
          <h2>{movie?.titulo || 'Película'}</h2>
          <p>
            Es necesario iniciar sesión para elegir o confirmar una función. No se puede continuar
            como invitado.
          </p>
          <p className="muted">
            Al entrar, volverás a la ficha para continuar con tus butacas.
          </p>
        </aside>
      </div>
    </div>
  )
}
