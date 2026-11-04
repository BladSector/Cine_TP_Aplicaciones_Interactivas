import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { api, errorMessage } from '../api/client'
import { useAuth } from './AuthContext'

const AccountContext = createContext(null)

export function AccountProvider({ children }) {
  const { spectator } = useAuth()
  const ownerId = spectator?.id ?? null
  const [history, setHistory] = useState({ ownerId: null, tickets: [], loading: false, error: '' })
  const requestRef = useRef({ version: 0, controller: null })

  const reloadTickets = useCallback(async () => {
    const request = requestRef.current
    request.controller?.abort()
    const version = ++request.version
    if (!ownerId) {
      setHistory({ ownerId: null, tickets: [], loading: false, error: '' })
      return
    }

    const controller = new AbortController()
    request.controller = controller
    setHistory((previous) => ({
      ownerId,
      tickets: previous.ownerId === ownerId ? previous.tickets : [],
      loading: true,
      error: '',
    }))
    try {
      const tickets = await api.getTickets({ signal: controller.signal })
      if (request.version === version) {
        setHistory({ ownerId, tickets: tickets || [], loading: false, error: '' })
      }
    } catch (error) {
      if (request.version === version && error.name !== 'AbortError') {
        setHistory({ ownerId, tickets: [], loading: false, error: errorMessage(error) })
      }
    }
  }, [ownerId])

  useEffect(() => {
    reloadTickets()
    return () => {
      requestRef.current.version += 1
      requestRef.current.controller?.abort()
    }
  }, [reloadTickets])

  // Oculta la cuenta anterior incluso antes de ejecutar el efecto de recarga.
  const value = useMemo(() => ({
    tickets: history.ownerId === ownerId ? history.tickets : [],
    loading: Boolean(ownerId) && (history.ownerId !== ownerId || history.loading),
    error: history.ownerId === ownerId ? history.error : '',
    reloadTickets,
  }), [history, ownerId, reloadTickets])

  return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>
}

export function useAccount() {
  const context = useContext(AccountContext)
  if (!context) throw new Error('useAccount debe usarse dentro de AccountProvider')
  return context
}
