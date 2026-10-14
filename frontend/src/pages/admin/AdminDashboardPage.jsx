import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, errorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { useCatalog } from '../../context/CatalogContext'
import { formatFunction, formatLanguage, money, shortTime } from '../../utils/format'

const TABS = [
  ['catalogo', 'Catálogo'],
  ['salas', 'Salas'],
  ['funciones', 'Funciones'],
  ['confiteria', 'Confitería'],
]

export default function AdminDashboardPage() {
  const navigate = useNavigate()
  const { personal, logout, isAdmin } = useAuth()
  const { categorias, peliculas, salas, funciones, productos, reload } = useCatalog()
  const [tab, setTab] = useState('catalogo')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const [categoriaNombre, setCategoriaNombre] = useState('')
  const [movieForm, setMovieForm] = useState({
    titulo: '',
    duracion: 120,
    descripcion: '',
    portadaUrl: '',
    categoriaId: '',
  })
  const [salaForm, setSalaForm] = useState({ nombre: '', filas: 8, butacasPorFila: 12 })
  const [funcionForm, setFuncionForm] = useState({
    fecha: '',
    horario: '',
    peliculaId: '',
    salaId: '',
    formato: 'DOS_D',
    idioma: 'ESPANIOL',
    precioEntrada: 8900,
  })
  const [productForm, setProductForm] = useState({
    nombre: '',
    precio: 0,
    tipo: 'COMBO',
    tamano: 'MEDIANO',
  })

  async function run(action, successMessage) {
    setError('')
    setMessage('')
    try {
      await action()
      await reload()
      setMessage(successMessage)
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/personal')
  }

  return (
    <div className="admin-shell">
      <header className="admin-header">
        <div>
          <p className="eyebrow">Panel · {personal?.rol}</p>
          <h1>Hola, {personal?.nombre}</h1>
        </div>
        <div className="summary-actions">
          <Link to="/" className="secondary-button">
            Ver sitio
          </Link>
          <button type="button" className="text-button" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </header>

      <nav className="admin-tabs">
        {TABS.map(([key, label]) => (
          <button
            key={key}
            type="button"
            className={tab === key ? 'is-active' : ''}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </nav>

      {message && <div className="info-box">{message}</div>}
      {error && <div className="info-box info-box--error">{error}</div>}
      {!isAdmin && (
        <div className="info-box">
          Algunas altas requieren rol administrador. Podés operar según tus permisos.
        </div>
      )}

      {tab === 'catalogo' && (
        <section className="admin-section">
          <div className="admin-grid">
            <form
              className="stack-form"
              onSubmit={(e) => {
                e.preventDefault()
                run(
                  () => api.crearCategoria({ nombre: categoriaNombre }),
                  'Categoría creada.',
                ).then(() => setCategoriaNombre(''))
              }}
            >
              <h2>Nueva categoría</h2>
              <label>
                Nombre
                <input
                  value={categoriaNombre}
                  onChange={(e) => setCategoriaNombre(e.target.value)}
                  required
                />
              </label>
              <button className="primary-button" type="submit">
                Crear
              </button>
            </form>

            <form
              className="stack-form"
              onSubmit={(e) => {
                e.preventDefault()
                run(
                  () =>
                    api.crearPelicula({
                      ...movieForm,
                      duracion: Number(movieForm.duracion),
                      categoriaId: Number(movieForm.categoriaId),
                    }),
                  'Película creada.',
                ).then(() =>
                  setMovieForm({
                    titulo: '',
                    duracion: 120,
                    descripcion: '',
                    portadaUrl: '',
                    categoriaId: '',
                  }),
                )
              }}
            >
              <h2>Nueva película</h2>
              <label>
                Título
                <input
                  value={movieForm.titulo}
                  onChange={(e) => setMovieForm((p) => ({ ...p, titulo: e.target.value }))}
                  required
                />
              </label>
              <label>
                Duración (min)
                <input
                  type="number"
                  value={movieForm.duracion}
                  onChange={(e) => setMovieForm((p) => ({ ...p, duracion: e.target.value }))}
                  required
                />
              </label>
              <label>
                Categoría
                <select
                  value={movieForm.categoriaId}
                  onChange={(e) => setMovieForm((p) => ({ ...p, categoriaId: e.target.value }))}
                  required
                >
                  <option value="">Seleccionar…</option>
                  {categorias.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.nombre}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Portada URL
                <input
                  value={movieForm.portadaUrl}
                  onChange={(e) => setMovieForm((p) => ({ ...p, portadaUrl: e.target.value }))}
                />
              </label>
              <label>
                Descripción
                <textarea
                  value={movieForm.descripcion}
                  onChange={(e) => setMovieForm((p) => ({ ...p, descripcion: e.target.value }))}
                  required
                />
              </label>
              <button className="primary-button" type="submit">
                Crear película
              </button>
            </form>
          </div>

          <h2>Películas</h2>
          <div className="admin-table">
            {peliculas.map((movie) => (
              <div key={movie.id} className="admin-row">
                <strong>{movie.titulo}</strong>
                <span>
                  {movie.duracion} min · {movie.categoriaNombre}
                </span>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => run(() => api.eliminarPelicula(movie.id), 'Película eliminada.')}
                >
                  Eliminar
                </button>
              </div>
            ))}
          </div>
        </section>
      )}

      {tab === 'salas' && (
        <section className="admin-section">
          <form
            className="stack-form admin-narrow"
            onSubmit={(e) => {
              e.preventDefault()
              run(
                () =>
                  api.crearSalaConButacas({
                    nombre: salaForm.nombre,
                    filas: Number(salaForm.filas),
                    butacasPorFila: Number(salaForm.butacasPorFila),
                  }),
                'Sala creada con butacas.',
              ).then(() => setSalaForm({ nombre: '', filas: 8, butacasPorFila: 12 }))
            }}
          >
            <h2>Nueva sala</h2>
            <label>
              Nombre
              <input
                value={salaForm.nombre}
                onChange={(e) => setSalaForm((p) => ({ ...p, nombre: e.target.value }))}
                required
              />
            </label>
            <label>
              Filas
              <input
                type="number"
                value={salaForm.filas}
                onChange={(e) => setSalaForm((p) => ({ ...p, filas: e.target.value }))}
                required
              />
            </label>
            <label>
              Butacas por fila
              <input
                type="number"
                value={salaForm.butacasPorFila}
                onChange={(e) => setSalaForm((p) => ({ ...p, butacasPorFila: e.target.value }))}
                required
              />
            </label>
            <button className="primary-button" type="submit">
              Crear sala
            </button>
          </form>

          <div className="admin-table">
            {salas.map((sala) => (
              <div key={sala.id} className="admin-row">
                <strong>{sala.nombre}</strong>
                <span>
                  Capacidad {sala.capacidad} · {sala.estado}
                </span>
              </div>
            ))}
          </div>
        </section>
      )}

      {tab === 'funciones' && (
        <section className="admin-section">
          <form
            className="stack-form admin-narrow"
            onSubmit={(e) => {
              e.preventDefault()
              run(
                () =>
                  api.crearFuncion({
                    ...funcionForm,
                    peliculaId: Number(funcionForm.peliculaId),
                    salaId: Number(funcionForm.salaId),
                    precioEntrada: Number(funcionForm.precioEntrada),
                    horario:
                      funcionForm.horario.length === 5
                        ? `${funcionForm.horario}:00`
                        : funcionForm.horario,
                  }),
                'Función creada.',
              )
            }}
          >
            <h2>Nueva función</h2>
            <label>
              Película
              <select
                value={funcionForm.peliculaId}
                onChange={(e) => setFuncionForm((p) => ({ ...p, peliculaId: e.target.value }))}
                required
              >
                <option value="">Seleccionar…</option>
                {peliculas.map((movie) => (
                  <option key={movie.id} value={movie.id}>
                    {movie.titulo}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Sala
              <select
                value={funcionForm.salaId}
                onChange={(e) => setFuncionForm((p) => ({ ...p, salaId: e.target.value }))}
                required
              >
                <option value="">Seleccionar…</option>
                {salas.map((sala) => (
                  <option key={sala.id} value={sala.id}>
                    {sala.nombre}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Fecha
              <input
                type="date"
                value={funcionForm.fecha}
                onChange={(e) => setFuncionForm((p) => ({ ...p, fecha: e.target.value }))}
                required
              />
            </label>
            <label>
              Horario
              <input
                type="time"
                value={funcionForm.horario}
                onChange={(e) => setFuncionForm((p) => ({ ...p, horario: e.target.value }))}
                required
              />
            </label>
            <label>
              Formato
              <select
                value={funcionForm.formato}
                onChange={(e) => setFuncionForm((p) => ({ ...p, formato: e.target.value }))}
              >
                <option value="DOS_D">2D</option>
                <option value="TRES_D">3D</option>
              </select>
            </label>
            <label>
              Idioma
              <select
                value={funcionForm.idioma}
                onChange={(e) => setFuncionForm((p) => ({ ...p, idioma: e.target.value }))}
              >
                <option value="ESPANIOL">Doblada</option>
                <option value="SUBTITULADA">Subtitulada</option>
              </select>
            </label>
            <label>
              Precio
              <input
                type="number"
                step="0.01"
                value={funcionForm.precioEntrada}
                onChange={(e) => setFuncionForm((p) => ({ ...p, precioEntrada: e.target.value }))}
                required
              />
            </label>
            <button className="primary-button" type="submit">
              Crear función
            </button>
          </form>

          <div className="admin-table">
            {funciones.map((fn) => (
              <div key={fn.id} className="admin-row">
                <strong>{fn.peliculaTitulo}</strong>
                <span>
                  {fn.fecha} {shortTime(fn.horario)} · {fn.salaNombre} ·{' '}
                  {formatFunction(fn.formato)} · {formatLanguage(fn.idioma)} · $
                  {money(fn.precioEntrada)}
                </span>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => run(() => api.eliminarFuncion(fn.id), 'Función eliminada.')}
                >
                  Eliminar
                </button>
              </div>
            ))}
          </div>
        </section>
      )}

      {tab === 'confiteria' && (
        <section className="admin-section">
          <form
            className="stack-form admin-narrow"
            onSubmit={(e) => {
              e.preventDefault()
              run(
                () =>
                  api.crearProducto({
                    ...productForm,
                    precio: Number(productForm.precio),
                  }),
                'Producto creado.',
              ).then(() =>
                setProductForm({ nombre: '', precio: 0, tipo: 'COMBO', tamano: 'MEDIANO' }),
              )
            }}
          >
            <h2>Nuevo producto</h2>
            <label>
              Nombre
              <input
                value={productForm.nombre}
                onChange={(e) => setProductForm((p) => ({ ...p, nombre: e.target.value }))}
                required
              />
            </label>
            <label>
              Precio
              <input
                type="number"
                step="0.01"
                value={productForm.precio}
                onChange={(e) => setProductForm((p) => ({ ...p, precio: e.target.value }))}
                required
              />
            </label>
            <label>
              Tipo
              <select
                value={productForm.tipo}
                onChange={(e) => setProductForm((p) => ({ ...p, tipo: e.target.value }))}
              >
                <option value="COMBO">Combo</option>
                <option value="POCHOCLOS">Palomitas</option>
                <option value="BEBIDA">Bebida</option>
                <option value="DULCE">Dulce</option>
              </select>
            </label>
            <label>
              Tamaño
              <select
                value={productForm.tamano}
                onChange={(e) => setProductForm((p) => ({ ...p, tamano: e.target.value }))}
              >
                <option value="CHICO">Chico</option>
                <option value="MEDIANO">Mediano</option>
                <option value="GRANDE">Grande</option>
              </select>
            </label>
            <button className="primary-button" type="submit">
              Crear producto
            </button>
          </form>

          <div className="admin-table">
            {productos.map((product) => (
              <div key={product.id} className="admin-row">
                <strong>{product.nombre}</strong>
                <span>
                  {product.tipo} · {product.tamano} · ${money(product.precio)}
                </span>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
