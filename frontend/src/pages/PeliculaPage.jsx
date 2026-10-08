import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import DateStrip from '../components/cartelera/DateStrip'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'
import {
  formatFunction,
  formatLanguage,
  money,
  shortTime,
  todayValue,
} from '../utils/format'

export default function PeliculaPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const movieId = Number(id)
  const { peliculas, funciones, loading } = useCatalog()
  const { setDraft } = usePurchase()
  const [selectedDate, setSelectedDate] = useState(todayValue())
  const [selectedFunctionId, setSelectedFunctionId] = useState(null)

  const movie = peliculas.find((item) => item.id === movieId)
  const dayFunctions = useMemo(
    () =>
      funciones
        .filter((fn) => fn.peliculaId === movieId && fn.fecha === selectedDate)
        .sort((a, b) => String(a.horario).localeCompare(String(b.horario))),
    [funciones, movieId, selectedDate],
  )

  const selected = dayFunctions.find((fn) => fn.id === selectedFunctionId) || dayFunctions[0]

  function continueToSeats() {
    if (!selected) return
    setDraft({
      movieId,
      functionId: selected.id,
      selectedSeats: [],
      consumptions: {},
      returnPath: null,
    })
    navigate(`/compra/${movieId}/acceso`)
  }

  if (loading) return <div className="page-loading">Cargando película…</div>
  if (!movie) return <div className="info-box">Película no encontrada.</div>

  return (
    <div className="movie-detail">
      <Link to="/" className="back-link">
        Volver a cartelera
      </Link>

      <div className="movie-detail__layout">
        <div className="movie-detail__poster">
          {movie.portadaUrl ? (
            <img src={movie.portadaUrl} alt={movie.titulo} />
          ) : (
            <div className="movie-card__placeholder">
              <span>{movie.titulo}</span>
            </div>
          )}
        </div>

        <div className="movie-detail__content">
          <p className="eyebrow">Ahora en pantalla</p>
          <h1>{movie.titulo}</h1>
          <p className="movie-detail__meta">
            {movie.duracion} min · {movie.categoriaNombre}
          </p>
          <h2>Sinopsis</h2>
          <p>{movie.descripcion}</p>

          <h2>Elige tu función</h2>
          <DateStrip selectedDate={selectedDate} onChange={setSelectedDate} days={7} />

          {dayFunctions.length === 0 ? (
            <div className="info-box">No hay funciones para este día.</div>
          ) : (
            <div className="function-picker">
              {dayFunctions.map((fn) => (
                <button
                  key={fn.id}
                  type="button"
                  className={`function-option ${selected?.id === fn.id ? 'is-selected' : ''}`}
                  onClick={() => setSelectedFunctionId(fn.id)}
                >
                  <strong>{shortTime(fn.horario)}</strong>
                  <span>
                    {formatFunction(fn.formato)} · {formatLanguage(fn.idioma)}
                  </span>
                  <span>{fn.salaNombre}</span>
                  <em>{selected?.id === fn.id ? 'Seleccionada' : 'Disponible'}</em>
                </button>
              ))}
            </div>
          )}

          <div className="movie-detail__cta">
            <button
              type="button"
              className="primary-button"
              disabled={!selected}
              onClick={continueToSeats}
            >
              Elegir butacas
            </button>
            {selected && <span>Desde ${money(selected.precioEntrada)}</span>}
          </div>
        </div>
      </div>
    </div>
  )
}
