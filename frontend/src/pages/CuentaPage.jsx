import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'

// Importamos los nuevos componentes
import MisEntradasList from '../components/cuenta/MisEntradasList'
import PerfilForm from '../components/cuenta/PerfilForm'
import MetodosPagoList from '../components/cuenta/MetodosPagoList'

export default function CuentaPage() {
  const { spectator, isSpectator, loginSpectator, registerSpectator, logout, authError, setAuthError } = useAuth()
  const { reload } = useCatalog()

  // Hook para leer la URL actual (para el scroll suave)
  const location = useLocation()

  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({
    email: '',
    contrasenia: '',
    nombre: '',
    apellido: '',
    contraseniaConfirmacion: '',
  })

  // Efecto 1: Recargar datos del catálogo si el usuario está logueado
  useEffect(() => {
    if (spectator) reload()
  }, [spectator, reload])

  // Efecto 2: Manejar el scroll automático si hay un hash en la URL (#pagos, #perfil)
  useEffect(() => {
    if (location.hash) {
      const targetId = location.hash.replace('#', '')
      const element = document.getElementById(targetId)

      if (element) {
        setTimeout(() => {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }, 100)
      }
    }
  }, [location])

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

  // SI NO ESTÁ LOGUEADO: Mostramos el formulario de ingreso
  if (!isSpectator) {
    return (
      <section className="account-gate">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Ingresá para ver tus entradas</h1>
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'is-active' : ''} onClick={() => setMode('login')}>
            Iniciar sesión
          </button>
          <button type="button" className={mode === 'register' ? 'is-active' : ''} onClick={() => setMode('register')}>
            Crear cuenta
          </button>
        </div>
        <form className="stack-form account-form" onSubmit={handleAuth}>
          {mode === 'register' && (
            <>
              <label>Nombre <input name="nombre" value={form.nombre} onChange={update(setForm)} required /></label>
              <label>Apellido <input name="apellido" value={form.apellido} onChange={update(setForm)} required /></label>
            </>
          )}
          <label>Email <input type="email" name="email" value={form.email} onChange={update(setForm)} required /></label>
          <label>Contraseña <input type="password" name="contrasenia" value={form.contrasenia} onChange={update(setForm)} required /></label>
          {mode === 'register' && (
            <label>Repetir contraseña <input type="password" name="contraseniaConfirmacion" value={form.contraseniaConfirmacion} onChange={update(setForm)} required /></label>
          )}
          {authError && <div className="info-box info-box--error">{authError}</div>}
          <button type="submit" className="primary-button">
            {mode === 'login' ? 'Ingresar' : 'Crear e ingresar'}
          </button>
        </form>
      </section>
    )
  }

  // SI ESTÁ LOGUEADO: Mostramos el dashboard modular
 // Leemos qué sección pide la URL. Si no hay ninguna (es solo /cuenta), mostramos #entradas por defecto.
   const seccionActiva = location.hash || '#entradas';

   // SI ESTÁ LOGUEADO: Mostramos el dashboard dinámico
   return (
     <div className="account-page">
       <div className="account-header">
         <div>
           <p className="eyebrow">Mi cuenta</p>
           <h1>Hola, {spectator.nombre}</h1>
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

       {/* RENDERIZADO CONDICIONAL: Solo se dibuja el que cumpla la condición */}

       {seccionActiva === '#entradas' && (
         <MisEntradasList />
       )}

       {seccionActiva === '#perfil' && (
         <div className="account-grid">
           <PerfilForm />
         </div>
       )}

       {seccionActiva === '#pagos' && (
         <div className="account-grid">
           <MetodosPagoList />
         </div>
       )}

     </div>
   )
 }