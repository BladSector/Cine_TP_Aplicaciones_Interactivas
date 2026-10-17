import { useState } from 'react'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { nextMonthValue, paymentIsExpired } from '../../utils/format'

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
      if (paymentIsExpired(card.fechaVencimiento)) {
        throw new Error('La fecha de vencimiento debe ser posterior a la fecha actual.')
      }
      await api.crearMetodoPago({
        numero: card.numero.replace(/\s/g, ''),
        fechaVencimiento: card.fechaVencimiento,
        nombreTitular: card.nombreTitular,
        cvv: card.cvv,
      })
      await refreshSpectator(spectator.id)
      setCard({ numero: '', fechaVencimiento: '', nombreTitular: '', cvv: '' })
      setMessage('Tarjeta agregada.')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function handleDeleteCard(id) {
    if (!window.confirm('¿Querés eliminar esta tarjeta de tus métodos de pago?')) return
    setError('')
    setMessage('')
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
      {(spectator.metodosDePago || []).length === 0 && (
        <div className="info-box">No tenés tarjetas activas asociadas.</div>
      )}
      <h3>Agregar una tarjeta</h3>
      <form className="stack-form" onSubmit={handleAddCard}>
        <label>
          Titular
          <input name="nombreTitular" value={card.nombreTitular} onChange={update(setCard)} required />
        </label>
        <label>
          Número
          <input name="numero" inputMode="numeric" maxLength="19" value={card.numero} onChange={update(setCard)} placeholder="16 dígitos" required />
        </label>
        <label>
          Vencimiento (mes y año)
          <input type="month" min={nextMonthValue()} name="fechaVencimiento" value={card.fechaVencimiento} onChange={update(setCard)} required />
        </label>
        <label>
          CVV
          <input type="password" name="cvv" inputMode="numeric" maxLength="4" value={card.cvv} onChange={update(setCard)} required />
        </label>
        <button type="submit" className="secondary-button">
          Agregar tarjeta
        </button>
      </form>
    </section>
  )
}
