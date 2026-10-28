import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import DateStrip from '../components/cartelera/DateStrip'
import MovieCard from '../components/cartelera/MovieCard'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'
import { formatFunction, functionHasStarted, normalizeText, todayValue } from '../utils/format'

import AccordionGallery from '../components/cartelera/AccordionGallery'
import PurchaseAuthDrawer from '../components/cuenta/PurchaseAuthDrawer'
import ConfirmDialog from '../components/common/ConfirmDialog'
import { useAuth } from '../context/AuthContext'
import Loader from '../components/common/Loader'

export default function CarteleraPage() {
  const navigate = useNavigate()

  const { peliculas, funciones, categorias, loading, error } = useCatalog()
  const { spectator, isSpectator } = useAuth()
  const {
    draft,
    setDraft,
    hasActivePurchase,
    continuePath,
    cancelActivePurchase,
  } = usePurchase()
  const [selectedDate, setSelectedDate] = useState(todayValue())
  const [categoryId, setCategoryId] = useState('')
  const [formato, setFormato] = useState('')
  const [idioma, setIdioma] = useState('')
  const [query, setQuery] = useState('')
  const [pendingMovieId, setPendingMovieId] = useState(null)
  const [pendingReplacement, setPendingReplacement] = useState(null)
  const [replacingPurchase, setReplacingPurchase] = useState(false)

  // 1. Calculamos las películas destacadas para el carrusel
  const featuredMovies = useMemo(() => {
    const availableMovieIds = new Set(
      funciones
        .filter((fn) => fn.fecha === selectedDate && !functionHasStarted(fn))
        .map((fn) => fn.peliculaId),
    )

    return peliculas
      .filter((movie) => availableMovieIds.has(movie.id) && movie.portadaUrl)
      .slice(0, 5)
      .map((movie) => ({
        image: movie.portadaUrl,
        label: movie.titulo,
        alt: `Portada de ${movie.titulo}`,
        link: `/pelicula/${movie.id}`,
      }))
  }, [peliculas, funciones, selectedDate])

  // 2. Calculamos la lista principal de películas filtradas
  const moviesWithSessions = useMemo(() => {
    return peliculas
      .map((movie) => {
        const movieFunctions = funciones.filter((fn) => {
          if (fn.peliculaId !== movie.id) return false
          if (fn.fecha !== selectedDate) return false
          if (functionHasStarted(fn)) return false
          if (categoryId && String(movie.categoriaId) !== categoryId) return false
          if (formato && fn.formato !== formato) return false
          if (idioma && fn.idioma !== idioma) return false
          if (query && !normalizeText(movie.titulo).includes(normalizeText(query))) return false
          return true
        })
        return { movie, functions: movieFunctions }
      })
      .filter((item) => item.functions.length > 0)
  }, [peliculas, funciones, selectedDate, categoryId, formato, idioma, query])

  function beginPurchase(movie, fn) {
    setDraft({
      purchaseType: 'movie',
      movieId: movie.id,
      functionId: fn.id,
      selectedSeats: [],
      consumptions: {},
      candyVisited: false,
      returnPath: null,
      membershipId: null,
    })
    if (!isSpectator || !spectator.emailVerificado) {
      setPendingMovieId(movie.id)
      return
    }
    navigate(`/compra/${movie.id}/butacas`)
  }

  function handleSelectFunction(movie, fn) {
    if (hasActivePurchase && draft.functionId === fn.id) {
      navigate(continuePath)
      return
    }
    if (hasActivePurchase) {
      setPendingReplacement({ movie, fn })
      return
    }
    beginPurchase(movie, fn)
  }

  async function replaceActivePurchase() {
    if (!pendingReplacement) return
    setReplacingPurchase(true)
    try {
      await cancelActivePurchase()
      const nextPurchase = pendingReplacement
      setPendingReplacement(null)
      beginPurchase(nextPurchase.movie, nextPurchase.fn)
    } finally {
      setReplacingPurchase(false)
    }
  }

  // 3. LA BARRERA DEL HÁMSTER
  // Si los datos están cargando, detenemos el renderizado normal y mostramos la pantalla completa
  if (loading) {
    return <Loader />
  }

  // 4. Si ya cargó todo, mostramos la página normal
  return (
    <div className="cartelera-page">
      <section className="hero">
        <div className="hero__content">
          <h1 className="hero__brand">
             <span>Film Zone</span>
          </h1>
          <p className="hero__headline">
            <span>Elegí tu película.</span>
            <span>Encontrá tu función.</span>
            <span>Viví el cine.</span>
          </p>
        </div>

        <div className="hero__gallery">
          {featuredMovies.length > 0 ? (
            <AccordionGallery
              items={featuredMovies}
              defaultIndex={Math.min(2, featuredMovies.length - 1)}
              expandRatio={0.52}
              trigger="hover"
            />
          ) : (
            <div className="hero__empty-gallery">No hay películas disponibles para esta fecha.</div>
          )}
        </div>

        <div className="hero__glow" aria-hidden="true" />
      </section>

      <section className="catalog-panel">
        <DateStrip selectedDate={selectedDate} onChange={setSelectedDate} />

        <div className="filters">
          <label>
            Buscar
            <input
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Nombre de película"
            />
          </label>
          <label>
            Género
            <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">Todos</option>
              {categorias.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.nombre}
                </option>
              ))}
            </select>
          </label>
          <label>
            Formato
            <select value={formato} onChange={(e) => setFormato(e.target.value)}>
              <option value="">Todos los formatos</option>
              <option value="DOS_D">{formatFunction('DOS_D')}</option>
              <option value="TRES_D">{formatFunction('TRES_D')}</option>
            </select>
          </label>
          <label>
            Idioma
            <select value={idioma} onChange={(e) => setIdioma(e.target.value)}>
              <option value="">Todos los idiomas</option>
              <option value="ESPANIOL">Doblada</option>
              <option value="SUBTITULADA">Subtitulada</option>
            </select>
          </label>
        </div>

        <div className="catalog-heading">
          <div>
            <p className="eyebrow">En pantalla</p>
            <h2>Sesiones para {selectedDate === todayValue() ? 'hoy' : selectedDate}</h2>
          </div>
          <span className="count-pill">{moviesWithSessions.length} películas</span>
        </div>

        {error && <div className="info-box info-box--error">{error}</div>}
        {!error && moviesWithSessions.length === 0 && (
          <div className="info-box">No hay funciones para los filtros seleccionados.</div>
        )}

        <div className="movie-grid">
          {moviesWithSessions.map(({ movie, functions }) => (
            <MovieCard
              key={movie.id}
              movie={movie}
              functions={functions}
              onSelectFunction={(fn) => handleSelectFunction(movie, fn)}
            />
          ))}
        </div>

        <p className="catalog-note">
          Selecciona un horario para elegir tus butacas. Los precios pueden variar según la función y
          el formato.
        </p>
      </section>

      <PurchaseAuthDrawer
        open={pendingMovieId !== null}
        onClose={() => setPendingMovieId(null)}
        onComplete={() => {
          const movieId = pendingMovieId
          setPendingMovieId(null)
          navigate(`/compra/${movieId}/butacas`)
        }}
      />

      {pendingReplacement && (
        <ConfirmDialog
          title="Ya tenés una compra en curso"
          message="Si elegís esta función se cancelará la compra actual y se liberarán sus butacas."
          confirmLabel="Descartar y continuar"
          cancelLabel="Conservar compra"
          danger
          loading={replacingPurchase}
          onConfirm={replaceActivePurchase}
          onCancel={() => setPendingReplacement(null)}
        />
      )}
    </div>
  )
}
