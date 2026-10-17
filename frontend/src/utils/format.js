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
  return year < now.getFullYear() || (year === now.getFullYear() && month <= now.getMonth() + 1)
}

export function nextMonthValue() {
  const date = new Date()
  date.setDate(1)
  date.setMonth(date.getMonth() + 1)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

export function functionHasStarted(funcion, now = new Date()) {
  if (!funcion?.fecha || !funcion?.horario) return false
  const time = String(funcion.horario).slice(0, 8).padEnd(8, ':00')
  const start = new Date(`${funcion.fecha}T${time}`)
  return !Number.isNaN(start.getTime()) && start <= now
}

export function seatLabel(seat) {
  return seat?.butacaNombre || `${seat?.fila || ''}${seat?.numero || ''}`
}

export function normalizeText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

export function formatStatus(value) {
  const labels = {
    DISPONIBLE: 'Disponible',
    LIMPIEZA: 'Limpieza',
    CERRADA: 'Cerrada',
    PROBLEMA_PARTICULAR: 'Problema particular',
    PAGADO: 'Pagado',
    PROCESADO: 'Procesado',
    REEMBOLSADO: 'Reembolsado',
    PENDIENTE: 'Pendiente',
    ENTREGADO: 'Entregado',
    PAGADA: 'Pagada',
    ESCANEADA: 'Escaneada',
  }
  return labels[value] || String(value || '').replaceAll('_', ' ')
}
