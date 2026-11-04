import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { addDays, formatDayLabel, formatDayNumber, formatMonthYear, todayValue } from '../../utils/format'

export default function DateStrip({ selectedDate, onChange, days = 14 }) {
  const today = todayValue()
  const dates = Array.from({ length: days }, (_, index) => addDays(today, index))
  const listRef = useRef(null)
  const dragRef = useRef(null)
  const suppressClickRef = useRef(false)
  const [dragging, setDragging] = useState(false)
  const [edges, setEdges] = useState({ start: true, end: true })

  useEffect(() => {
    const list = listRef.current
    const updateEdges = () => setEdges({
      start: list.scrollLeft <= 1,
      end: list.scrollLeft + list.clientWidth >= list.scrollWidth - 1,
    })
    const observer = new ResizeObserver(updateEdges)
    observer.observe(list)
    list.addEventListener('scroll', updateEdges, { passive: true })
    updateEdges()
    return () => {
      observer.disconnect()
      list.removeEventListener('scroll', updateEdges)
    }
  }, [days])

  function scrollDates(direction) {
    const list = listRef.current
    list.scrollBy({
      left: direction * list.clientWidth * 0.75,
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth',
    })
  }

  function startDrag(event) {
    suppressClickRef.current = false
    if (event.pointerType !== 'mouse' || event.button !== 0) return
    dragRef.current = { pointerId: event.pointerId, x: event.clientX, scrollLeft: event.currentTarget.scrollLeft }
  }

  function moveDrag(event) {
    const drag = dragRef.current
    if (!drag || drag.pointerId !== event.pointerId) return
    const distance = event.clientX - drag.x
    if (!suppressClickRef.current && Math.abs(distance) < 6) return
    // Capture only after a drag: a normal click must still select the day.
    suppressClickRef.current = true
    event.currentTarget.setPointerCapture(event.pointerId)
    setDragging(true)
    event.currentTarget.scrollLeft = drag.scrollLeft - distance
    event.preventDefault()
  }

  function endDrag(event) {
    dragRef.current = null
    setDragging(false)
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId)
    }
  }

  return (
    <section className="date-strip" aria-label="Elegí el día">
      <div className="date-strip__heading">
        <p className="eyebrow">Elegí el día</p>
        <h2>{formatMonthYear(selectedDate || today)}</h2>
      </div>
      <div className="date-strip__navigation">
        <button
          type="button"
          className="date-strip__arrow"
          aria-label="Ver días anteriores"
          title="Ver días anteriores"
          disabled={edges.start}
          onClick={() => scrollDates(-1)}
        >
          <ChevronLeft size={20} aria-hidden="true" />
        </button>
        <div
          ref={listRef}
          className={`date-strip__list ${dragging ? 'is-dragging' : ''}`}
          onPointerDown={startDrag}
          onPointerMove={moveDrag}
          onPointerUp={endDrag}
          onPointerCancel={endDrag}
          onLostPointerCapture={endDrag}
          onPointerLeave={(event) => {
            if (!event.currentTarget.hasPointerCapture(event.pointerId)) endDrag(event)
          }}
          onClickCapture={(event) => {
            if (suppressClickRef.current && event.detail !== 0) {
              event.preventDefault()
              event.stopPropagation()
            }
          }}
        >
          {dates.map((date, index) => {
            const active = date === selectedDate
            return (
              <button
                key={date}
                type="button"
                className={`date-chip ${active ? 'is-active' : ''}`}
                aria-pressed={active}
                onClick={() => onChange(date)}
              >
                <span className="date-chip__day">{index === 0 ? 'HOY' : formatDayLabel(date)}</span>
                <span className="date-chip__num">{formatDayNumber(date)}</span>
              </button>
            )
          })}
        </div>
        <button
          type="button"
          className="date-strip__arrow"
          aria-label="Ver días siguientes"
          title="Ver días siguientes"
          disabled={edges.end}
          onClick={() => scrollDates(1)}
        >
          <ChevronRight size={20} aria-hidden="true" />
        </button>
      </div>
    </section>
  )
}
