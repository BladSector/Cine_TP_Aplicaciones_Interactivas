import { createPortal } from 'react-dom'
import useDialog from '../../hooks/useDialog'

export default function Modal({ title, children, onClose, actions, compact = false, closeDisabled = false }) {
  const { dialogRef, requestClose } = useDialog({ onClose, closeDisabled })

  return createPortal(
    <div className="modal-backdrop" role="presentation" onMouseDown={requestClose}>
      <section
        ref={dialogRef}
        tabIndex={-1}
        className={`modal-panel${compact ? ' modal-panel--compact' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={requestClose} disabled={closeDisabled} aria-label="Cerrar">
            &times;
          </button>
        </header>
        <div className="modal-body">{children}</div>
        {actions && <footer className="modal-actions">{actions}</footer>}
      </section>
    </div>,
    document.body,
  )
}
