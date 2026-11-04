import { useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { Link } from 'react-router-dom'
import { formatFunction, formatLanguage, formatScheduleDay, money, shortTime } from '../../utils/format'

function compactLanguage(idioma) {
  if (idioma === 'ESPANIOL') return 'Doblada'
  if (idioma === 'SUBTITULADA') return 'Subtitulada'
  return formatLanguage(idioma)
}

export default function MovieCard({ movie, functions, onSelectFunction, showDates = false }) {
  const [scheduleOpen, setScheduleOpen] = useState(false)
  const grouped = functions.reduce((acc, fn) => {
    const key = `${showDates ? fn.fecha : ''}|${fn.formato}|${fn.idioma}`
    if (!acc[key]) acc[key] = []
    acc[key].push(fn)
    return acc
  }, {})

  const minPrice = functions.reduce(
    (min, fn) => Math.min(min, fn.precioEntrada),
    functions[0]?.precioEntrada ?? 0,
  )

  const groupedSessions = Object.entries(grouped).sort(([firstKey], [secondKey]) => {
    const [firstDate, firstFormat, firstLanguage] = firstKey.split('|')
    const [secondDate, secondFormat, secondLanguage] = secondKey.split('|')
    const formatOrder = { DOS_D: 0, TRES_D: 1 }
    const languageOrder = { ESPANIOL: 0, SUBTITULADA: 1 }

    return (
      firstDate.localeCompare(secondDate) ||
      (formatOrder[firstFormat] ?? 9) - (formatOrder[secondFormat] ?? 9) ||
      (languageOrder[firstLanguage] ?? 9) - (languageOrder[secondLanguage] ?? 9)
    )
  })

  return (
    <article className="movie-card">
      <Link
        to={`/pelicula/${movie.id}`}
        className="movie-card__poster"
        aria-label={`Ver ${movie.titulo}`}
      >
        {movie.portadaUrl ? (
          <img src={movie.portadaUrl} alt={`Portada de ${movie.titulo}`} />
        ) : (
          <div className="movie-card__placeholder" aria-hidden="true">
            <span>{movie.titulo}</span>
          </div>
        )}
      </Link>

      <div className="movie-card__body">
        <div className="movie-card__meta">
          <span>{movie.duracion} min</span>
          <span>·</span>
          <span>{movie.categoriaNombre}</span>
        </div>
        <h3>{movie.titulo}</h3>
        <p className="movie-card__desc">{movie.descripcion}</p>

        <button
          type="button"
          className="movie-card__schedule-toggle"
          aria-expanded={scheduleOpen}
          aria-controls={`movie-schedule-${movie.id}`}
          onClick={() => setScheduleOpen((open) => !open)}
        >
          <span>{scheduleOpen ? 'Ocultar funciones' : showDates ? 'Ver funciones' : 'Ver funciones y horarios'}</span>
          <ChevronDown size={18} aria-hidden="true" />
        </button>

        <div
          id={`movie-schedule-${movie.id}`}
          className={`movie-card__schedule${scheduleOpen ? ' is-expanded' : ''}`}
        >
          <p className="movie-card__schedule-label">
            {functions.length === 0 ? 'Sin funciones programadas.' : 'Horarios:'}
          </p>
          {groupedSessions.map(([key, list], index) => {
            const [fecha, formato, idioma] = key.split('|')
            const startsDay = showDates && (
              index === 0 || groupedSessions[index - 1][0].split('|')[0] !== fecha
            )
            const endsDay = showDates && (
              index === groupedSessions.length - 1 || groupedSessions[index + 1][0].split('|')[0] !== fecha
            )
            return (
              <div key={key}>
                {startsDay && <h4 className="movie-card__day">{formatScheduleDay(fecha)}</h4>}
                <div className={`movie-card__sessions${endsDay ? ' movie-card__sessions--day-end' : ''}`}>
                  <div className="movie-card__format">
                    <span>{formatFunction(formato)}</span>
                    <span>{compactLanguage(idioma)}</span>
                  </div>
                  <div className="session-list">
                    {list
                      .slice()
                      .sort((a, b) => String(a.horario).localeCompare(String(b.horario)))
                      .map((fn) => (
                        <button
                          key={fn.id}
                          type="button"
                          className="session-chip"
                          title={`${shortTime(fn.horario)} · ${fn.salaNombre || 'Sala a confirmar'}`}
                          onClick={() => onSelectFunction(fn)}
                        >
                          {shortTime(fn.horario)}
                        </button>
                      ))}
                  </div>
                </div>
              </div>
            )
          })}
        </div>

        <div className="movie-card__footer">
          <span className="movie-card__price">{functions.length > 0 ? `Desde $${money(minPrice)}` : 'Sin funciones programadas'}</span>
        </div>
      </div>
    </article>
  )
}
