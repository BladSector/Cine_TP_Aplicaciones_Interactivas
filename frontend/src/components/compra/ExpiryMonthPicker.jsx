import { useEffect, useRef, useState } from 'react'
import { CalendarDays, ChevronLeft, ChevronRight } from 'lucide-react'

const MONTHS = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
]

function yearFrom(value, fallback) {
  const year = Number(String(value || '').split('-')[0])
  return Number.isFinite(year) && year > 0 ? year : fallback
}

export default function ExpiryMonthPicker({ value, min, onChange }) {
  const containerRef = useRef(null)
  const minimumYear = yearFrom(min, new Date().getFullYear())
  const maximumYear = minimumYear + 20
  const [open, setOpen] = useState(false)
  const [visibleYear, setVisibleYear] = useState(yearFrom(value, minimumYear))

  useEffect(() => {
    setVisibleYear(yearFrom(value, minimumYear))
  }, [value, minimumYear])

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (containerRef.current && !containerRef.current.contains(event.target)) setOpen(false)
    }

    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('mousedown', closeOnOutsideClick)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideClick)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [])

  const selectedYear = yearFrom(value, 0)
  const selectedMonth = Number(String(value || '').split('-')[1])
  const label = selectedYear && selectedMonth
    ? `${MONTHS[selectedMonth - 1]} ${selectedYear}`
    : 'Seleccionar mes'

  return (
    <div className="expiry-picker" ref={containerRef}>
      <button
        type="button"
        className="expiry-picker__trigger"
        aria-expanded={open}
        aria-haspopup="dialog"
        onClick={() => setOpen((current) => !current)}
      >
        <CalendarDays size={18} aria-hidden="true" />
        <span>{label}</span>
      </button>

      {open && (
        <div className="expiry-picker__popover" role="dialog" aria-label="Elegir vencimiento">
          <div className="expiry-picker__header">
            <button
              type="button"
              aria-label="Año anterior"
              disabled={visibleYear <= minimumYear}
              onClick={() => setVisibleYear((year) => year - 1)}
            >
              <ChevronLeft size={18} />
            </button>
            <strong>{visibleYear}</strong>
            <button
              type="button"
              aria-label="Año siguiente"
              disabled={visibleYear >= maximumYear}
              onClick={() => setVisibleYear((year) => year + 1)}
            >
              <ChevronRight size={18} />
            </button>
          </div>

          <div className="expiry-picker__months">
            {MONTHS.map((month, index) => {
              const monthValue = `${visibleYear}-${String(index + 1).padStart(2, '0')}`
              const expired = monthValue < min
              const selected = monthValue === value
              return (
                <button
                  key={month}
                  type="button"
                  className={selected ? 'is-selected' : ''}
                  disabled={expired}
                  onClick={() => {
                    onChange(monthValue)
                    setOpen(false)
                  }}
                >
                  {month.slice(0, 3)}
                </button>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
