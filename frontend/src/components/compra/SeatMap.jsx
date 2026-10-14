export default function SeatMap({ seats, selectedSeats, isUnavailable, onToggle }) {
  if (!seats.length) {
    return <div className="info-box">No hay butacas disponibles en esta sala.</div>
  }

  const filas = [...new Set(seats.map((seat) => seat.fila))]
  const maxNumero = Math.max(...seats.map((seat) => seat.numero))

  return (
    <div className="seat-map">
      <div className="screen-shape">Pantalla</div>
      <div className="seat-matrix">
        {filas.map((fila) => (
          <div key={fila} className="seat-row">
            <span className="seat-row-label">{fila}</span>
            {Array.from({ length: maxNumero }, (_, index) => {
              const numero = index + 1
              const butaca = seats.find((seat) => seat.fila === fila && seat.numero === numero)
              if (!butaca) return <span key={`${fila}-${numero}`} className="seat-gap" />

              const selected = selectedSeats.some((seat) => seat.id === butaca.id)
              const unavailable = isUnavailable(butaca)

              return (
                <button
                  key={butaca.id}
                  type="button"
                  className={`seat-button ${selected ? 'is-selected' : ''} ${unavailable ? 'is-blocked' : ''}`}
                  disabled={unavailable}
                  onClick={() => onToggle(butaca)}
                  aria-label={`Butaca ${butaca.fila}${butaca.numero}`}
                >
                  {butaca.numero}
                </button>
              )
            })}
          </div>
        ))}
      </div>
      <div className="seat-legend">
        <span>
          <i className="legend-seat available" /> Disponibles
        </span>
        <span>
          <i className="legend-seat selected" /> Seleccionadas
        </span>
        <span>
          <i className="legend-seat blocked" /> Ocupadas
        </span>
      </div>
    </div>
  )
}
