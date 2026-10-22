import Modal from './Modal'

export default function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  danger = false,
  loading = false,
  onConfirm,
  onCancel,
}) {
  return (
    <Modal
      title={title}
      compact
      onClose={onCancel}
      actions={(
        <>
          <button type="button" className="secondary-button" onClick={onCancel} disabled={loading}>
            {cancelLabel}
          </button>
          <button
            type="button"
            className={danger ? 'danger-button' : 'primary-button'}
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Procesando…' : confirmLabel}
          </button>
        </>
      )}
    >
      <p className="confirm-dialog__message">{message}</p>
    </Modal>
  )
}
