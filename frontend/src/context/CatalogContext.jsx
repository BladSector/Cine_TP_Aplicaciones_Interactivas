import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { api, errorMessage } from '../api/client'

const CatalogContext = createContext(null)

export function CatalogProvider({ children }) {
  const [categorias, setCategorias] = useState([])
  const [peliculas, setPeliculas] = useState([])
  const [funciones, setFunciones] = useState([])
  const [salas, setSalas] = useState([])
  const [butacas, setButacas] = useState([])
  const [productos, setProductos] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const requestVersion = useRef(0)

  const reload = useCallback(async () => {
    const version = ++requestVersion.current
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

      if (version !== requestVersion.current) return
      setCategorias(categoriasData || [])
      setPeliculas(peliculasData || [])
      setFunciones(funcionesData || [])
      setSalas(salasData || [])
      setButacas(butacasData || [])
      setProductos(productosData || [])
    } catch (err) {
      if (version === requestVersion.current) setError(errorMessage(err))
    } finally {
      if (version === requestVersion.current) setLoading(false)
    }
  }, [])

  useEffect(() => {
    reload()
    return () => { requestVersion.current += 1 }
  }, [reload])

  const value = useMemo(
    () => ({
      categorias,
      peliculas,
      funciones,
      salas,
      butacas,
      productos,
      loading,
      error,
      reload,
    }),
    [
      categorias,
      peliculas,
      funciones,
      salas,
      butacas,
      productos,
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
