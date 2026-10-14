import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, errorMessage } from '../api/client'

const CatalogContext = createContext(null)

export function CatalogProvider({ children }) {
  const [categorias, setCategorias] = useState([])
  const [peliculas, setPeliculas] = useState([])
  const [funciones, setFunciones] = useState([])
  const [salas, setSalas] = useState([])
  const [butacas, setButacas] = useState([])
  const [productos, setProductos] = useState([])
  const [entradas, setEntradas] = useState([])
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    setError('')
    setLoading(true)
    try {
      const [
        categoriasData,
        peliculasData,
        funcionesData,
        salasData,
        butacasData,
        productosData,
      ] = await Promise.all([
        api.getCategorias(),
        api.getPeliculas(),
        api.getFunciones(),
        api.getSalas(),
        api.getButacas(),
        api.getProductos(),
      ])

      setCategorias(categoriasData || [])
      setPeliculas(peliculasData || [])
      setFunciones(funcionesData || [])
      setSalas(salasData || [])
      setButacas(butacasData || [])
      setProductos(productosData || [])

      try {
        const [entradasData, ticketsData] = await Promise.all([
          api.getEntradas(),
          api.getTickets(),
        ])
        setEntradas(entradasData || [])
        setTickets(ticketsData || [])
      } catch {
        setEntradas([])
        setTickets([])
      }
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    reload()
  }, [reload])

  const value = useMemo(
    () => ({
      categorias,
      peliculas,
      funciones,
      salas,
      butacas,
      productos,
      entradas,
      tickets,
      loading,
      error,
      reload,
      setEntradas,
      setTickets,
    }),
    [
      categorias,
      peliculas,
      funciones,
      salas,
      butacas,
      productos,
      entradas,
      tickets,
      loading,
      error,
      reload,
    ],
  )

  return <CatalogContext.Provider value={value}>{children}</CatalogContext.Provider>
}

export function useCatalog() {
  const ctx = useContext(CatalogContext)
  if (!ctx) throw new Error('useCatalog debe usarse dentro de CatalogProvider')
  return ctx
}
