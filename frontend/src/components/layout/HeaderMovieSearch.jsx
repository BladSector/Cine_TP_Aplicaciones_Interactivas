import { Search, X } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  formatFunction,
  formatLanguage,
  functionHasStarted,
  normalizeText,
  shortTime,
} from '../../utils/format'

function functionDateLabel(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-AR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
  })
}

export default function HeaderMovieSearch({ movies, functions, loading }) {
  const navigate = useNavigate()
  const location = useLocation()
  const containerRef = useRef(null)
  const overlayRef = useRef(null)
  const inputRef = useRef(null)
  const [query, setQuery] = useState('')
  const [resultsOpen, setResultsOpen] = useState(false)
  const [expanded, setExpanded] = useState(false)
  const [anchor, setAnchor] = useState(null)

  const results = useMemo(() => {
    const normalizedQuery = normalizeText(query)
    if (!normalizedQuery) return []

    return movies
      .filter((movie) => normalizeText(movie.titulo).includes(normalizedQuery))
      .map((movie) => ({
        movie,
        upcomingFunctions: functions
          .filter((fn) => fn.peliculaId === movie.id && !functionHasStarted(fn))
          .sort((first, second) =>
            `${first.fecha}T${first.horario}`.localeCompare(`${second.fecha}T${second.horario}`),
          )
          .slice(0, 3),
      }))
      .sort((first, second) => {
        const availabilityDifference = Number(second.upcomingFunctions.length > 0)
          - Number(first.upcomingFunctions.length > 0)
        return availabilityDifference || first.movie.titulo.localeCompare(second.movie.titulo, 'es')
      })
      .slice(0, 6)
  }, [functions, movies, query])

  useEffect(() => {
    function closeOnOutsideClick(event) {
      const clickedToggle = containerRef.current?.contains(event.target)
      const clickedOverlay = overlayRef.current?.contains(event.target)
      if (!clickedToggle && !clickedOverlay) closeSearch()
    }

    document.addEventListener('mousedown', closeOnOutsideClick)
    return () => document.removeEventListener('mousedown', closeOnOutsideClick)
  }, [])

  useEffect(() => {
    if (expanded) inputRef.current?.focus({ preventScroll: true })
  }, [expanded])

  useEffect(() => {
    closeSearch()
  }, [location.pathname])

  function closeSearch() {
    setQuery('')
    setResultsOpen(false)
    setExpanded(false)
    setAnchor(null)
  }

  function openMovie(result) {
    closeSearch()
    navigate(`/pelicula/${result.movie.id}`, {
      state: { selectedDate: result.upcomingFunctions[0]?.fecha },
    })
  }

  return (
    <>
      <div className={`header-search${expanded ? ' is-searching' : ''}`} ref={containerRef}>
        <button
          type="button"
          className="header-search__toggle"
          aria-label="Abrir buscador de películas"
          title="Buscar películas"
          tabIndex={expanded ? -1 : 0}
          onClick={(event) => {
            const rect = event.currentTarget.getBoundingClientRect()
            setAnchor({ top: rect.top, right: window.innerWidth - rect.right })
            setExpanded(true)
          }}
        >
          <Search size={20} aria-hidden="true" />
        </button>
      </div>

      {expanded && createPortal(
        <div
          className="header-search-portal"
          ref={overlayRef}
          style={{
            '--search-field-top': `${anchor?.top ?? 17}px`,
            '--search-field-right': `${anchor?.right ?? 16}px`,
          }}
        >
          <div className="header-search__field">
            <Search className="header-search__icon" size={18} aria-hidden="true" />
            <input
              ref={inputRef}
              type="search"
              value={query}
              placeholder="Buscar películas"
              aria-label="Buscar películas y funciones"
              aria-expanded={resultsOpen && Boolean(query.trim())}
              aria-controls="movie-search-results"
              onFocus={() => setResultsOpen(Boolean(query.trim()))}
              onChange={(event) => {
                setQuery(event.target.value)
                setResultsOpen(Boolean(event.target.value.trim()))
              }}
              onKeyDown={(event) => {
                if (event.key === 'Escape') closeSearch()
              }}
            />
            <button
              type="button"
              className="header-search__clear"
              aria-label="Cerrar buscador"
              title="Cerrar buscador"
              onClick={closeSearch}
            >
              <X size={16} aria-hidden="true" />
            </button>
          </div>

          {resultsOpen && query.trim() && (
            <div id="movie-search-results" className="header-search__results">
              {loading ? (
                <p className="header-search__message">Buscando películas…</p>
              ) : results.length === 0 ? (
                <p className="header-search__message">No encontramos películas con ese nombre.</p>
              ) : (
                results.map((result) => (
                  <button
                    key={result.movie.id}
                    type="button"
                    className="header-search__result"
                    aria-label={`Ver funciones de ${result.movie.titulo}`}
                    onClick={() => openMovie(result)}
                  >
                    <span className="header-search__poster" aria-hidden="true">
                      {result.movie.portadaUrl ? (
                        <img src={result.movie.portadaUrl} alt="" />
                      ) : (
                        <span>{result.movie.titulo.slice(0, 1)}</span>
                      )}
                    </span>
                    <span className="header-search__result-body">
                      <span className="header-search__movie">
                        <strong>{result.movie.titulo}</strong>
                        <small>{result.movie.categoriaNombre}</small>
                      </span>
                      <span className="header-search__functions">
                        {result.upcomingFunctions.length > 0 ? (
                          result.upcomingFunctions.map((fn) => (
                            <span key={fn.id}>
                              {functionDateLabel(fn.fecha)} · {shortTime(fn.horario)} ·{' '}
                              {formatFunction(fn.formato)} · {formatLanguage(fn.idioma)}
                            </span>
                          ))
                        ) : (
                          <span>Sin funciones próximas</span>
                        )}
                      </span>
                      <span className="header-search__action">Ver todas las funciones</span>
                    </span>
                  </button>
                ))
              )}
            </div>
          )}
        </div>,
        document.body,
      )}
    </>
  )
}
