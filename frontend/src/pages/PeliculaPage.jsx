import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import DateStrip from '../components/cartelera/DateStrip'
import ConfirmDialog from '../components/common/ConfirmDialog'
import PurchaseAuthDrawer from '../components/cuenta/PurchaseAuthDrawer'
import { useAuth } from '../context/AuthContext'
import { useCatalog } from '../context/CatalogContext'
import { usePurchase } from '../context/PurchaseContext'
import {
  formatFunction,
  formatLanguage,
  functionHasStarted,
  money,
  shortTime,
  todayValue,
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
    setDraft,
    hasActivePurchase,
    continuePath,
    cancelActivePurchase,
  } = usePurchase()
  const [selectedDate, setSelectedDate] = useState(location.state?.selectedDate || todayValue())
  const [selectedFunctionId, setSelectedFunctionId] = useState(null)
  const [authOpen, setAuthOpen] = useState(Boolean(location.state?.openPurchaseAuth))
  const [showReplaceConfirm, setShowReplaceConfirm] = useState(false)
  const [replacingPurchase, setReplacingPurchase] = useState(false)

  const movie = peliculas.find((item) => item.id === movieId)
  const dayFunctions = useMemo(
    () =>
      funciones
        .filter(
          (fn) =>
            fn.peliculaId === movieId &&
            fn.fecha === selectedDate &&
            !functionHasStarted(fn),
        )
        .sort((a, b) => String(a.horario).localeCompare(String(b.horario))),
    [funciones, movieId, selectedDate],
  )

  const selected = dayFunctions.find((fn) => fn.id === selectedFunctionId) || dayFunctions[0]

  useEffect(() => {
    setSelectedDate(location.state?.selectedDate || todayValue())
    setSelectedFunctionId(null)
  }, [movieId, location.state?.selectedDate])

  function beginSelectedPurchase() {
    if (!selected) return
    setDraft((prev) => ({
      purchaseType: 'movie',
      movieId,
      functionId: selected.id,
      selectedSeats: [],
      consumptions: prev.consumptions,
      candyVisited: false,
      returnPath: null,
      membershipId: null,
    }))
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
      <Link to="/" className="back-link">
        Volver a cartelera
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
            <DateStrip selectedDate={selectedDate} onChange={setSelectedDate} days={14} />

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
