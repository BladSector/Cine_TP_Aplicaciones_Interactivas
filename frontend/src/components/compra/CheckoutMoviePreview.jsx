import {
  formatDateShort,
  formatFunction,
  formatLanguage,
  shortTime,
} from '../../utils/format'

export default function CheckoutMoviePreview({ movie, funcion, confirmed = false }) {
  if (!movie) return null

  return (
    <section
      className={`checkout-movie-preview${confirmed ? ' checkout-movie-preview--confirmed' : ''}`}
      aria-label={`Película de la compra: ${movie.titulo}`}
    >
      <div className="checkout-movie-preview__poster">
        {movie.portadaUrl ? (
          <img src={movie.portadaUrl} alt={`Portada de ${movie.titulo}`} />
        ) : (
          <span>Sin portada</span>
        )}
      </div>
      <div className="checkout-movie-preview__details">
        <p className="eyebrow">{confirmed ? 'Película confirmada' : 'Tu película'}</p>
        <h2>{movie.titulo}</h2>
        {funcion && (
          <>
            <p>
              {formatDateShort(funcion.fecha)} · {shortTime(funcion.horario)} · {funcion.salaNombre}
            </p>
            <p className="muted">
              {formatFunction(funcion.formato)} · {formatLanguage(funcion.idioma)}
            </p>
          </>
        )}
      </div>
    </section>
  )
}
