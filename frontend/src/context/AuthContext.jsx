import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, errorMessage } from '../api/client'

const AuthContext = createContext(null)
const SPECTATOR_KEY = 'noche-cine-spectator-id'
const ROLE_KEY = 'noche-cine-role'

export function AuthProvider({ children }) {
  const [spectator, setSpectator] = useState(null)
  const [personal, setPersonal] = useState(null)
  const [role, setRole] = useState(null)
  const [loading, setLoading] = useState(true)
  const [authError, setAuthError] = useState('')

  const refreshSpectator = useCallback(async (id) => {
    const data = await api.getEspectador(id)
    setSpectator(data)
    setRole('ESPECTADOR')
    localStorage.setItem(SPECTATOR_KEY, String(data.id))
    localStorage.setItem(ROLE_KEY, 'ESPECTADOR')
    return data
  }, [])

  useEffect(() => {
    async function bootstrap() {
      setLoading(true)
      try {
        const savedRole = localStorage.getItem(ROLE_KEY)
        const savedId = localStorage.getItem(SPECTATOR_KEY)

        if (savedRole === 'ESPECTADOR' && savedId) {
          await refreshSpectator(Number(savedId))
        } else if (savedRole === 'PERSONAL') {
          const data = await api.getPersonal()
          setPersonal(data)
          setRole('PERSONAL')
        }
      } catch {
        localStorage.removeItem(SPECTATOR_KEY)
        localStorage.removeItem(ROLE_KEY)
        setSpectator(null)
        setPersonal(null)
        setRole(null)
      } finally {
        setLoading(false)
      }
    }

    bootstrap()
  }, [refreshSpectator])

  const loginSpectator = useCallback(async (email, contrasenia) => {
    setAuthError('')
    try {
      const data = await api.loginEspectador({ email, contrasenia })
      setPersonal(null)
      return await refreshSpectator(data.id)
    } catch (error) {
      setAuthError(errorMessage(error))
      throw error
    }
  }, [refreshSpectator])

  const registerSpectator = useCallback(async (payload) => {
    setAuthError('')
    try {
      const data = await api.registrarEspectador(payload)
      setPersonal(null)
      return await refreshSpectator(data.id)
    } catch (error) {
      setAuthError(errorMessage(error))
      throw error
    }
  }, [refreshSpectator])

  const loginPersonal = useCallback(async (usuario, contrasenia) => {
    setAuthError('')
    try {
      const data = await api.loginPersonal({ usuario, contrasenia })
      setSpectator(null)
      setPersonal(data)
      setRole('PERSONAL')
      localStorage.removeItem(SPECTATOR_KEY)
      localStorage.setItem(ROLE_KEY, 'PERSONAL')
      return data
    } catch (error) {
      setAuthError(errorMessage(error))
      throw error
    }
  }, [])

  const logout = useCallback(async () => {
    try {
      await api.logout()
    } catch {
      // Sesión ya cerrada
    }
    setSpectator(null)
    setPersonal(null)
    setRole(null)
    localStorage.removeItem(SPECTATOR_KEY)
    localStorage.removeItem(ROLE_KEY)
  }, [])

  const value = useMemo(
    () => ({
      spectator,
      personal,
      role,
      loading,
      authError,
      setAuthError,
      setSpectator,
      refreshSpectator,
      loginSpectator,
      registerSpectator,
      loginPersonal,
      logout,
      isSpectator: role === 'ESPECTADOR' && Boolean(spectator),
      isPersonal: role === 'PERSONAL' && Boolean(personal),
      isAdmin: personal?.rol === 'DUENIO',
    }),
    [
      spectator,
      personal,
      role,
      loading,
      authError,
      refreshSpectator,
      loginSpectator,
      registerSpectator,
      loginPersonal,
      logout,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
