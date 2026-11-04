import { useCallback, useLayoutEffect, useRef } from 'react'

// Solo el dialogo superior captura el teclado cuando hay ventanas anidadas.
const dialogs = []
let previousOverflow = ''
const FOCUSABLE = 'a[href], button:not(:disabled), input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])'

function controls(panel) {
  return [...panel.querySelectorAll(FOCUSABLE)].filter((element) => element.getClientRects().length > 0)
}

export default function useDialog({ open = true, onClose, closeDisabled = false }) {
  const dialogRef = useRef(null)
  const optionsRef = useRef({ onClose, closeDisabled })
  const openerRef = useRef(null)
  const wasOpenRef = useRef(false)
  optionsRef.current = { onClose, closeDisabled }
  if (open && !wasOpenRef.current) openerRef.current = document.activeElement
  wasOpenRef.current = open

  const requestClose = useCallback(() => {
    if (!optionsRef.current.closeDisabled) optionsRef.current.onClose()
  }, [])

  useLayoutEffect(() => {
    if (!open) return undefined
    const panel = dialogRef.current
    const token = Symbol('dialog')
    if (!dialogs.length) {
      previousOverflow = document.body.style.overflow
      document.body.style.overflow = 'hidden'
    }
    dialogs.push(token)
    const isTop = () => dialogs.at(-1) === token
    const focusFirst = () => (controls(panel)[0] || panel).focus({ preventScroll: true })

    function handleKey(event) {
      if (!isTop()) return
      if (event.key === 'Escape') {
        event.preventDefault()
        event.stopImmediatePropagation()
        requestClose()
      } else if (event.key === 'Tab') {
        const items = controls(panel)
        const index = items.indexOf(document.activeElement)
        if (!items.length || (event.shiftKey ? index <= 0 : index === items.length - 1 || index < 0)) {
          event.preventDefault()
          const target = event.shiftKey ? items.at(-1) || panel : items[0] || panel
          target.focus({ preventScroll: true })
        }
      }
    }

    function handleFocus(event) {
      if (isTop() && !panel.contains(event.target)) focusFirst()
    }

    if (!panel.contains(document.activeElement)) focusFirst()
    document.addEventListener('keydown', handleKey, true)
    document.addEventListener('focusin', handleFocus)
    return () => {
      document.removeEventListener('keydown', handleKey, true)
      document.removeEventListener('focusin', handleFocus)
      const wasTop = isTop()
      dialogs.splice(dialogs.indexOf(token), 1)
      if (!dialogs.length) document.body.style.overflow = previousOverflow
      if (wasTop && openerRef.current?.isConnected) openerRef.current.focus({ preventScroll: true })
    }
  }, [open, requestClose])

  return { dialogRef, requestClose }
}
