export function money(value) {
  return Number(value || 0).toLocaleString('es-AR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

export function formatDateShort(value) {
  if (!value) return ''
  const date = new Date(`${value}T00:00:00`)
  return date.toLocaleDateString('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

export function formatDayLabel(value) {
  if (!value) return ''
  const date = new Date(`${value}T00:00:00`)
  return date.toLocaleDateString('es-AR', { weekday: 'short' }).replace('.', '').toUpperCase()
}

export function formatDayNumber(value) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).getDate()
}

export function formatMonthYear(value) {
  if (!value) return ''
  const date = new Date(`${value}T00:00:00`)
  return date.toLocaleDateString('es-AR', { month: 'long', year: 'numeric' })
}

export function shortTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
}

export function todayValue() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function addDays(base, days) {
  const date = new Date(`${base}T00:00:00`)
  date.setDate(date.getDate() + days)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function formatFunction(formato) {
  if (formato === 'TRES_D') return '3D'
  if (formato === 'DOS_D') return '2D'
  return formato || ''
}

export function formatLanguage(idioma) {
  if (idioma === 'ESPANIOL') return 'Doblada'
  if (idioma === 'SUBTITULADA') return 'Versión original subtitulada'
  return idioma || ''
}

export function labelProductType(tipo) {
  const map = {
    POCHOCLOS: 'Palomitas',
    BEBIDA: 'Bebidas',
    DULCE: 'Dulces',
    COMBO: 'Combos',
  }
  return map[tipo] || tipo
}

export function paymentIsExpired(fechaVencimiento) {
  if (!fechaVencimiento) return true
  const [year, month] = String(fechaVencimiento).split('-').map(Number)
  if (!year || !month) return true
  const now = new Date()
  const exp = new Date(year, month, 0, 23, 59, 59)
  return exp < now
}
