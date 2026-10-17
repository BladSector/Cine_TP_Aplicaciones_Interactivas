import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, errorMessage } from '../api/client'

const AuthContext = createContext(null)
const SPECTATOR_KEY = 'noche-cine-spectator-id'

export function AuthProvider({ children }) {
  const [spectator, setSpectator] = useState(null)
  const [loading, setLoading] = useState(true)
  const [authError, setAuthError] = useState('')

  const refreshSpectator = useCallback(async (id) => {
    const data = await api.getEspectador(id)
    setSpectator(data)
    localStorage.setItem(SPECTATOR_KEY, String(data.id))
    return data
  }, [])

  useEffect(() => {
    async function bootstrap() {
      setLoading(true)
      try {
        const savedId = localStorage.getItem(SPECTATOR_KEY)

        if (savedId) {
          await refreshSpectator(Number(savedId))
        }
      } catch {
        localStorage.removeItem(SPECTATOR_KEY)
        setSpectator(null)
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
      return await refreshSpectator(data.id)
    } catch (error) {
      setAuthError(errorMessage(error))
      throw error
    }
  }, [refreshSpectator])

  const verifySpectatorEmail = useCallback(async () => {
    if (!spectator?.id) throw new Error('No hay una cuenta para verificar.')
    setAuthError('')
    try {
      await api.verificarMail(spectator.id)
      return await refreshSpectator(spectator.id)
    } catch (error) {
      setAuthError(errorMessage(error))
      throw error
    }
  }, [spectator, refreshSpectator])

  const logout = useCallback(async () => {
    try {
      await api.logout()
    } catch {
      // Sesión ya cerrada
    }
    setSpectator(null)
    localStorage.removeItem(SPECTATOR_KEY)
  }, [])

  const value = useMemo(
    () => ({
      spectator,
      loading,
      authError,
      setAuthError,
      setSpectator,
      refreshSpectator,
      loginSpectator,
      registerSpectator,
      verifySpectatorEmail,
      logout,
      isSpectator: Boolean(spectator),
    }),
    [
      spectator,
      loading,
      authError,
      refreshSpectator,
      loginSpectator,
      registerSpectator,
      verifySpectatorEmail,
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
