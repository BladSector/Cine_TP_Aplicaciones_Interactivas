import { useState } from 'react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { paymentIsExpired } from '../../utils/format'

export default function MetodosPagoList() {
  const { spectator, refreshSpectator } = useAuth()
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [card, setCard] = useState({
    numero: '',
    fechaVencimiento: '',
    nombreTitular: '',
    cvv: '',
  })

  function update(setter) {
    return (event) => setter((prev) => ({ ...prev, [event.target.name]: event.target.value }))
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

  return (
    <section id="pagos" className="account-section">
      <h2>Métodos de pago</h2>
      {message && <div className="info-box">{message}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}

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
          <input name="nombreTitular" value={card.nombreTitular} onChange={update(setCard)} required />
        </label>
        <label>
          Número
          <input name="numero" value={card.numero} onChange={update(setCard)} required />
        </label>
        <label>
          Vencimiento
          <input type="month" name="fechaVencimiento" value={card.fechaVencimiento} onChange={update(setCard)} required />
        </label>
        <label>
          CVV
          <input type="password" name="cvv" value={card.cvv} onChange={update(setCard)} required />
        </label>
        <button type="submit" className="secondary-button">
          Agregar tarjeta
        </button>
      </form>
    </section>
  )
}