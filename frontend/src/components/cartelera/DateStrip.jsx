import { addDays, formatDayLabel, formatDayNumber, formatMonthYear, todayValue } from '../../utils/format'

export default function DateStrip({ selectedDate, onChange, days = 14 }) {
  const today = todayValue()
  const dates = Array.from({ length: days }, (_, index) => addDays(today, index))

  return (
    <section className="date-strip" aria-label="Elegí el día">
      <div className="date-strip__heading">
        <p className="eyebrow">Elegí el día</p>
        <h2>{formatMonthYear(selectedDate || today)}</h2>
      </div>
      <div className="date-strip__list">
        {dates.map((date, index) => {
          const active = date === selectedDate
          return (
            <button
              key={date}
              type="button"
              className={`date-chip ${active ? 'is-active' : ''}`}
              onClick={() => onChange(date)}
            >
              <span className="date-chip__day">{index === 0 ? 'HOY' : formatDayLabel(date)}</span>
              <span className="date-chip__num">{formatDayNumber(date)}</span>
            </button>
          )
        })}
      </div>
    </section>
  )
}
