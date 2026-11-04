export const EMPTY_CARD = {
  numero: '',
  fechaVencimiento: '',
  nombreTitular: '',
  cvv: '',
}

export function formatCardNumber(value) {
  return value
    .replace(/\D/g, '')
    .slice(0, 16)
    .replace(/(.{4})/g, '$1 ')
    .trim()
}

export function formatExpiry(value) {
  if (!value || !value.includes('-')) return 'MM/AA'
  const [year, month] = value.split('-')
  return `${month}/${year.slice(-2)}`
}

export function cardPreviewNumber(value) {
  if (!value) return '•••• •••• •••• ••••'
  return formatCardNumber(value).padEnd(19, '•')
}
