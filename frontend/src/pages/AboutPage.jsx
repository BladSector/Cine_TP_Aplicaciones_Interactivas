import { useCatalog } from '../context/CatalogContext'

const TEAM_MEMBERS = [
  {
    nombre: 'Santiago López',
    legajo: '1207966',
    rol: 'Desarrollo Backend y API REST',
    iniciales: 'SL',
  },
  {
    nombre: 'Antonella Rayen López Brugorello',
    legajo: '1167557',
    rol: 'Desarrollo Frontend y diseño UX/UI',
    iniciales: 'AL',
  },
  {
    nombre: 'Martín Enrique Salas',
    legajo: '1209668',
    rol: 'Base de datos y pruebas del sistema',
    iniciales: 'MS',
  },
  {
    nombre: 'Lucas Iacobellis',
    legajo: '1217175',
    rol: 'Interfaz de escritorio Swing e integración',
    iniciales: 'LI',
  },
]

export default function AboutPage() {
  const { peliculas } = useCatalog()
  const posters = peliculas.filter((movie) => movie.portadaUrl).slice(0, 5)

  return (
    <article className="about-page">
      <header className="about-hero">
        <div className="about-hero__posters" aria-hidden="true">
          {posters.map((movie) => (
            <img key={movie.id} src={movie.portadaUrl} alt="" />
          ))}
        </div>
        <div className="about-hero__overlay" />
        <div className="about-hero__content">
          <p className="eyebrow">Sobre nosotros</p>
          <h1>Film Zone</h1>
          <p>Una experiencia de cine conectada desde la cartelera hasta el ingreso a la sala.</p>
        </div>
      </header>

      <section className="about-intro">
        <div>
          <p className="eyebrow">Nuestra empresa</p>
          <h2>El cine, organizado alrededor de cada función</h2>
        </div>
        <p>
          Film Zone integra la venta de entradas, la selección de butacas, el kiosco y la
          validación mediante QR en una misma plataforma. El sistema conecta la experiencia
          del espectador con la operación diaria del cine.
        </p>
      </section>

      <section className="about-values" aria-label="Principios de Film Zone">
        <div>
          <span>01</span>
          <h3>Experiencia</h3>
          <p>Un recorrido de compra claro, cómodo y pensado para disfrutar desde el primer paso.</p>
        </div>
        <div>
          <span>02</span>
          <h3>Organización</h3>
          <p>Información centralizada para coordinar funciones, salas, butacas y consumos.</p>
        </div>
        <div>
          <span>03</span>
          <h3>Confianza</h3>
          <p>Tickets trazables y operaciones registradas para clientes y personal del cine.</p>
        </div>
      </section>

      <section className="about-team">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Quiénes lo hacemos</p>
            <h2>Nuestro equipo</h2>
          </div>
        </div>

        <div className="about-team__grid">
          {TEAM_MEMBERS.map((member) => (
            <article key={member.legajo} className="team-member">
              <div className="team-member__photo" aria-hidden="true">
                <span>{member.iniciales}</span>
              </div>
              <div className="team-member__details">
                <h3>{member.nombre}</h3>
                <p>{member.rol}</p>
                <span>Legajo {member.legajo}</span>
              </div>
            </article>
          ))}
        </div>
      </section>
    </article>
  )
}
