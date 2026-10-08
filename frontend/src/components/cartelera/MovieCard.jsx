import { Link } from 'react-router-dom'
import { formatFunction, formatLanguage, money, shortTime } from '../../utils/format'

export default function MovieCard({ movie, functions, onSelectFunction }) {
  const grouped = functions.reduce((acc, fn) => {
    const key = `${fn.formato}|${fn.idioma}`
    if (!acc[key]) acc[key] = []
    acc[key].push(fn)
    return acc
  }, {})

  const minPrice = functions.reduce(
    (min, fn) => Math.min(min, fn.precioEntrada),
    functions[0]?.precioEntrada ?? 0,
  )

  return (
    <article className="movie-card">
      <div className="movie-card__poster">
        {movie.portadaUrl ? (
          <img src={movie.portadaUrl} alt={`Portada de ${movie.titulo}`} />
        ) : (
          <div className="movie-card__placeholder" aria-hidden="true">
            <span>{movie.titulo}</span>
          </div>
        )}
        <span className="movie-card__badge">Estreno</span>
      </div>

      <div className="movie-card__body">
        <div className="movie-card__meta">
          <span>{movie.duracion} min</span>
          <span>·</span>
          <span>{movie.categoriaNombre}</span>
        </div>
        <h3>{movie.titulo}</h3>
        <p className="movie-card__desc">{movie.descripcion}</p>

        {Object.entries(grouped).map(([key, list]) => {
          const [formato, idioma] = key.split('|')
          return (
            <div key={key} className="movie-card__sessions">
              <p className="movie-card__format">
                {formatFunction(formato)} · {formatLanguage(idioma)}
              </p>
              <div className="session-list">
                {list
                  .slice()
                  .sort((a, b) => String(a.horario).localeCompare(String(b.horario)))
                  .map((fn) => (
                    <button
                      key={fn.id}
                      type="button"
                      className="session-chip"
                      onClick={() => onSelectFunction(fn)}
                    >
                      {shortTime(fn.horario)}
                    </button>
                  ))}
              </div>
            </div>
          )
        })}

        <div className="movie-card__footer">
          <Link to={`/pelicula/${movie.id}`} className="text-button">
            Ver película
          </Link>
          <span className="movie-card__price">Desde ${money(minPrice)}</span>
        </div>
      </div>
    </article>
  )
}
