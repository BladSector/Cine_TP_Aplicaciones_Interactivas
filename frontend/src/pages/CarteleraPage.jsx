import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import DateStrip from '../components/cartelera/DateStrip'
import MovieCard from '../components/cartelera/MovieCard'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'
import { formatFunction, todayValue } from '../utils/format'

// 1. El import con la ruta correcta hacia la carpeta components/cartelera
import AccordionGallery from '../components/cartelera/AccordionGallery'

// 2. Las imágenes estáticas (fuera de la función principal)
const items = [
  { image: 'https://picsum.photos/id/1015/900/1200', label: 'Canyon', link: '#' },
  { image: 'https://picsum.photos/id/1018/900/1200', label: 'Ridgeline', link: '#' },
  { image: 'https://picsum.photos/id/1039/900/1200', label: 'Falls', link: '#' },
  { image: 'https://picsum.photos/id/1043/900/1200', label: 'Harbour', link: '#' },
  { image: 'https://picsum.photos/id/1044/900/1200', label: 'Skyline', link: '#' }
];

export default function CarteleraPage() {
  const navigate = useNavigate()
  const { peliculas, funciones, categorias, loading, error } = useCatalog()
  const { setDraft } = usePurchase()
  const [selectedDate, setSelectedDate] = useState(todayValue())
  const [categoryId, setCategoryId] = useState('')
  const [formato, setFormato] = useState('')
  const [idioma, setIdioma] = useState('')
  const [query, setQuery] = useState('')

  const moviesWithSessions = useMemo(() => {
    return peliculas
      .map((movie) => {
        const movieFunctions = funciones.filter((fn) => {
          if (fn.peliculaId !== movie.id) return false
          if (fn.fecha !== selectedDate) return false
          if (categoryId && String(movie.categoriaId) !== categoryId) return false
          if (formato && fn.formato !== formato) return false
          if (idioma && fn.idioma !== idioma) return false
          if (query && !movie.titulo.toLowerCase().includes(query.toLowerCase())) return false
          return true
        })
        return { movie, functions: movieFunctions }
      })
      .filter((item) => item.functions.length > 0)
  }, [peliculas, funciones, selectedDate, categoryId, formato, idioma, query])

  function handleSelectFunction(movie, fn) {
    setDraft({
      movieId: movie.id,
      functionId: fn.id,
      selectedSeats: [],
      consumptions: {},
      returnPath: null,
    })
    navigate(`/compra/${movie.id}/acceso`)
  }

  return (
    <div className="cartelera-page">
      <section className="hero">
        <div className="hero__content">
          <h1 className="hero__brand">
             <span>Film Zone</span>
          </h1>
          <p className="hero__headline">
              Elegí tu película. Encontrá tu función. Viví el cine.
          </p>
        </div>

        {/* contenedor de la galería*/}
        <div className="hero__gallery">
          <AccordionGallery
            items={items}
            defaultIndex={2}
            expandRatio={0.52}
            trigger="hover"
          />
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

        {loading && <div className="info-box">Cargando cartelera…</div>}
        {error && <div className="info-box info-box--error">{error}</div>}
        {!loading && !error && moviesWithSessions.length === 0 && (
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
    </div>
  )
}