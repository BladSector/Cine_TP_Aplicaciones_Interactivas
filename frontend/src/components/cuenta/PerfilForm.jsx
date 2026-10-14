import { useState, useEffect } from 'react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'

export default function PerfilForm() {
  const { spectator, refreshSpectator } = useAuth()
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [profile, setProfile] = useState({
    nombre: '',
    apellido: '',
    email: '',
    contraseniaActual: '',
    nuevaContrasenia: '',
    nuevaContraseniaConfirmacion: '',
  })

  useEffect(() => {
    if (spectator) {
      setProfile((prev) => ({
        ...prev,
        nombre: spectator.nombre,
        apellido: spectator.apellido,
        email: spectator.email,
      }))
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
        <label>
          Contraseña actual
          <input type="password" name="contraseniaActual" value={profile.contraseniaActual} onChange={update(setProfile)} />
        </label>
        <label>
          Nueva contraseña
          <input type="password" name="nuevaContrasenia" value={profile.nuevaContrasenia} onChange={update(setProfile)} />
        </label>
        <label>
          Confirmar nueva contraseña
          <input type="password" name="nuevaContraseniaConfirmacion" value={profile.nuevaContraseniaConfirmacion} onChange={update(setProfile)} />
        </label>
        <button type="submit" className="primary-button">
          Guardar cambios
        </button>
      </form>
    </section>
  )
}