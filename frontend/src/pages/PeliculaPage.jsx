import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import ConfirmDialog from '../components/common/ConfirmDialog'
import PurchaseAuthDrawer from '../components/cuenta/PurchaseAuthDrawer'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'
import {
  formatFunction,
  formatLanguage,
  formatScheduleDay,
  functionHasStarted,
  money,
  shortTime,
} from '../utils/format'

export default function PeliculaPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const movieId = Number(id)
  const { peliculas, funciones, loading } = useCatalog()
  const { spectator, isSpectator } = useAuth()
  const {
    draft,
    startMoviePurchase,
    hasActivePurchase,
    continuePath,
    cancelActivePurchase,
  } = usePurchase()
  const [selectedFunctionId, setSelectedFunctionId] = useState(null)
  const [authOpen, setAuthOpen] = useState(Boolean(location.state?.openPurchaseAuth))
  const [showReplaceConfirm, setShowReplaceConfirm] = useState(false)
  const [replacingPurchase, setReplacingPurchase] = useState(false)

  const movie = peliculas.find((item) => item.id === movieId)
  const availableFunctions = useMemo(
    () =>
      funciones
        .filter(
          (fn) =>
            fn.peliculaId === movieId &&
            !functionHasStarted(fn),
        )
        .sort((a, b) => String(a.fecha).localeCompare(String(b.fecha))
          || String(a.horario).localeCompare(String(b.horario))),
    [funciones, movieId],
  )

  const functionsByDay = availableFunctions.reduce((days, fn) => {
    if (!days[fn.fecha]) days[fn.fecha] = []
    days[fn.fecha].push(fn)
    return days
  }, {})

  const selected = availableFunctions.find((fn) => fn.id === selectedFunctionId)
    || availableFunctions.find((fn) => fn.fecha === location.state?.selectedDate)
    || availableFunctions[0]

  useEffect(() => {
    setSelectedFunctionId(null)
  }, [movieId, location.state?.selectedDate])

  function beginSelectedPurchase() {
    if (!selected) return
    startMoviePurchase(movieId, selected.id)
    if (!isSpectator || !spectator.emailVerificado) {
      setAuthOpen(true)
      return
    }
    navigate(`/compra/${movieId}/butacas`)
  }

  function continueToSeats() {
    if (!selected) return
    if (hasActivePurchase && draft.functionId === selected.id) {
      navigate(continuePath)
      return
    }
    if (hasActivePurchase) {
      setShowReplaceConfirm(true)
      return
    }
    beginSelectedPurchase()
  }

  async function replaceActivePurchase() {
    setReplacingPurchase(true)
    try {
      await cancelActivePurchase()
      setShowReplaceConfirm(false)
      beginSelectedPurchase()
    } finally {
      setReplacingPurchase(false)
    }
  }

  if (loading) return <div className="page-loading">Cargando película…</div>
  if (!movie) return <div className="info-box">Película no encontrada.</div>

  return (
    <div className="movie-detail">
      <Link to="/" className="back-link" aria-label="Volver a cartelera" title="Volver a cartelera">
        <ArrowLeft size={24} aria-hidden="true" />
      </Link>

      {location.state?.expiredFunction && (
        <div className="info-box info-box--error">
          La función seleccionada ya comenzó. Elegí otro horario disponible.
        </div>
      )}

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
          <header className="movie-detail__summary">
            <p className="eyebrow">Ahora en pantalla</p>
            <h1>{movie.titulo}</h1>
            <p className="movie-detail__meta">
              {movie.duracion} min · {movie.categoriaNombre}
            </p>
            <p className="movie-detail__description">{movie.descripcion}</p>
          </header>

          <section className="movie-detail__booking" aria-labelledby="function-title">
            <h2 id="function-title">Elegí tu función</h2>
            {availableFunctions.length === 0 ? (
              <div className="info-box">No hay próximas funciones programadas para esta película.</div>
            ) : (
              Object.entries(functionsByDay).map(([fecha, dayFunctions]) => (
              <section key={fecha} className="movie-detail__day" aria-label={formatScheduleDay(fecha)}>
                <h3>{formatScheduleDay(fecha)}</h3>
              <div className="function-picker">
                {dayFunctions.map((fn) => (
                  <button
                    key={fn.id}
                    type="button"
                    className={`function-option ${selected?.id === fn.id ? 'is-selected' : ''}`}
                    aria-pressed={selected?.id === fn.id}
                    onClick={() => setSelectedFunctionId(fn.id)}
                  >
                    <strong className="function-option__time">{shortTime(fn.horario)}</strong>
                    <span className="function-option__format">
                      {formatFunction(fn.formato)} · {formatLanguage(fn.idioma)}
                    </span>
                    <span className="function-option__room">{fn.salaNombre}</span>
                    <span className="function-option__price">${money(fn.precioEntrada)}</span>
                    {selected?.id === fn.id && <em>Elegida</em>}
                  </button>
                ))}
              </div>
              </section>
              ))
            )}

            {selected && (
              <div className="movie-detail__cta">
                <button type="button" className="primary-button" onClick={continueToSeats}>
                  Elegir asientos
                </button>
              </div>
            )}
          </section>
        </div>
      </div>

      <PurchaseAuthDrawer
        open={authOpen}
        onClose={() => setAuthOpen(false)}
        onComplete={() => {
          setAuthOpen(false)
          navigate(`/compra/${movieId}/butacas`)
        }}
      />

      {showReplaceConfirm && (
        <ConfirmDialog
          title="Ya tenés una compra en curso"
          message="Si elegís esta función se cancelará la compra actual y se liberarán sus butacas."
          confirmLabel="Descartar y continuar"
          cancelLabel="Conservar compra"
          danger
          loading={replacingPurchase}
          onConfirm={replaceActivePurchase}
          onCancel={() => setShowReplaceConfirm(false)}
        />
      )}
    </div>
  )
}
