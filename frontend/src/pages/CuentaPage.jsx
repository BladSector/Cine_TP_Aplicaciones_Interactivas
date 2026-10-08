import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, errorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'
import { formatDateShort, money, paymentIsExpired, shortTime } from '../utils/format'

export default function CuentaPage() {
  const { spectator, isSpectator, loginSpectator, registerSpectator, logout, refreshSpectator, authError, setAuthError } =
    useAuth()
  const { tickets, reload } = useCatalog()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({
    email: '',
    contrasenia: '',
    nombre: '',
    apellido: '',
    contraseniaConfirmacion: '',
  })
  const [profile, setProfile] = useState({
    nombre: '',
    apellido: '',
    email: '',
    contraseniaActual: '',
    nuevaContrasenia: '',
    nuevaContraseniaConfirmacion: '',
  })
  const [card, setCard] = useState({
    numero: '',
    fechaVencimiento: '',
    nombreTitular: '',
    cvv: '',
  })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (spectator) {
      setProfile((prev) => ({
        ...prev,
        nombre: spectator.nombre,
        apellido: spectator.apellido,
        email: spectator.email,
      }))
      reload()
    }
  }, [spectator, reload])

  function update(setter) {
    return (event) => setter((prev) => ({ ...prev, [event.target.name]: event.target.value }))
  }

  async function handleAuth(event) {
    event.preventDefault()
    setAuthError('')
    try {
      if (mode === 'login') await loginSpectator(form.email, form.contrasenia)
      else {
        await registerSpectator({
          nombre: form.nombre,
          apellido: form.apellido,
          email: form.email,
          contrasenia: form.contrasenia,
          contraseniaConfirmacion: form.contraseniaConfirmacion,
        })
      }
    } catch {
      // handled
    }
  }

  async function handleProfile(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const updated = await api.updateEspectador(spectator.id, profile)
      await refreshSpectator(updated.id)
      setMessage('Perfil actualizado.')
      setProfile((prev) => ({
        ...prev,
        contraseniaActual: '',
        nuevaContrasenia: '',
        nuevaContraseniaConfirmacion: '',
      }))
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function handleAddCard(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const created = await api.crearMetodoPago({
        numero: card.numero.replace(/\s/g, ''),
        fechaVencimiento: card.fechaVencimiento,
        nombreTitular: card.nombreTitular,
        cvv: card.cvv,
      })
      await api.asociarMetodoPago(spectator.id, created.id)
      await refreshSpectator(spectator.id)
      setCard({ numero: '', fechaVencimiento: '', nombreTitular: '', cvv: '' })
      setMessage('Tarjeta agregada.')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function handleDeleteCard(id) {
    try {
      await api.eliminarMetodoPago(id)
      await refreshSpectator(spectator.id)
      setMessage('Tarjeta eliminada.')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  if (!isSpectator) {
    return (
      <section className="account-gate">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Ingresá para ver tus entradas</h1>
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'is-active' : ''} onClick={() => setMode('login')}>
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
        <form className="stack-form account-form" onSubmit={handleAuth}>
          {mode === 'register' && (
            <>
              <label>
                Nombre
                <input name="nombre" value={form.nombre} onChange={update(setForm)} required />
              </label>
              <label>
                Apellido
                <input name="apellido" value={form.apellido} onChange={update(setForm)} required />
              </label>
            </>
          )}
          <label>
            Email
            <input type="email" name="email" value={form.email} onChange={update(setForm)} required />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              name="contrasenia"
              value={form.contrasenia}
              onChange={update(setForm)}
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
                onChange={update(setForm)}
                required
              />
            </label>
          )}
          {authError && <div className="info-box info-box--error">{authError}</div>}
          <button type="submit" className="primary-button">
            {mode === 'login' ? 'Ingresar' : 'Crear e ingresar'}
          </button>
        </form>
      </section>
    )
  }

  const myTickets = tickets || []

  return (
    <div className="account-page">
      <div className="account-header">
        <div>
          <p className="eyebrow">Mi cuenta</p>
          <h1>
            Hola, {spectator.nombre}
          </h1>
          <p className="muted">{spectator.email}</p>
        </div>
        <div className="summary-actions">
          <Link to="/" className="secondary-button">
            Ir a cartelera
          </Link>
          <button type="button" className="text-button" onClick={logout}>
            Cerrar sesión
          </button>
        </div>
      </div>

      {message && <div className="info-box">{message}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}

      <section className="account-section">
        <h2>Mis entradas</h2>
        {myTickets.length === 0 ? (
          <div className="info-box">Todavía no tenés compras. Elegí una función en cartelera.</div>
        ) : (
          <div className="ticket-list">
            {myTickets.map((ticket) => {
              const first = ticket.entradas?.[0]
              return (
                <article key={ticket.id} className="ticket-card">
                  <div>
                    <p className="eyebrow">Ticket #{ticket.id}</p>
                    <h3>{first?.pelicula || 'Entrada'}</h3>
                    <p>
                      {formatDateShort(first?.fecha)} · {shortTime(first?.horario)}
                    </p>
                    <p>
                      Sala {first?.sala} · Butacas{' '}
                      {(ticket.entradas || []).map((e) => e.butaca).join(', ')}
                    </p>
                    <p>Estado: {ticket.estado}</p>
                    <p>Total: ${money(ticket.total)}</p>
                  </div>
                  <p className="qr-code">QR {ticket.codigoQR}</p>
                </article>
              )
            })}
          </div>
        )}
      </section>

      <div className="account-grid">
        <section className="account-section">
          <h2>Editar perfil</h2>
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
              <input
                type="email"
                name="email"
                value={profile.email}
                onChange={update(setProfile)}
                required
              />
            </label>
            <label>
              Contraseña actual
              <input
                type="password"
                name="contraseniaActual"
                value={profile.contraseniaActual}
                onChange={update(setProfile)}
              />
            </label>
            <label>
              Nueva contraseña
              <input
                type="password"
                name="nuevaContrasenia"
                value={profile.nuevaContrasenia}
                onChange={update(setProfile)}
              />
            </label>
            <label>
              Confirmar nueva contraseña
              <input
                type="password"
                name="nuevaContraseniaConfirmacion"
                value={profile.nuevaContraseniaConfirmacion}
                onChange={update(setProfile)}
              />
            </label>
            <button type="submit" className="primary-button">
              Guardar cambios
            </button>
          </form>
        </section>

        <section className="account-section">
          <h2>Métodos de pago</h2>
          <ul className="payment-list">
            {(spectator.metodosDePago || []).map((metodo) => (
              <li key={metodo.id}>
                <span>
                  **** {metodo.ultimosNumeros} · {metodo.fechaVencimiento}
                  {paymentIsExpired(metodo.fechaVencimiento) ? ' (vencida)' : ''}
                </span>
                <button type="button" className="text-button" onClick={() => handleDeleteCard(metodo.id)}>
                  Eliminar
                </button>
              </li>
            ))}
          </ul>
          <form className="stack-form" onSubmit={handleAddCard}>
            <label>
              Titular
              <input
                name="nombreTitular"
                value={card.nombreTitular}
                onChange={update(setCard)}
                required
              />
            </label>
            <label>
              Número
              <input name="numero" value={card.numero} onChange={update(setCard)} required />
            </label>
            <label>
              Vencimiento
              <input
                type="month"
                name="fechaVencimiento"
                value={card.fechaVencimiento}
                onChange={update(setCard)}
                required
              />
            </label>
            <label>
              CVV
              <input
                type="password"
                name="cvv"
                value={card.cvv}
                onChange={update(setCard)}
                required
              />
            </label>
            <button type="submit" className="secondary-button">
              Agregar tarjeta
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}
