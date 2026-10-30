import { UserRound } from 'lucide-react'
import { useCatalog } from '../context/CatalogContext'

// Agregar objetos con nombre, legajo, rol y foto cuando el equipo defina sus datos.
const TEAM_MEMBERS = []

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

        {TEAM_MEMBERS.length === 0 ? (
          <div className="about-team__empty">
            <UserRound size={34} aria-hidden="true" />
            <div>
              <strong>Información del equipo pendiente</strong>
              <p>Las fotos, nombres, legajos y roles se incorporarán próximamente.</p>
            </div>
          </div>
        ) : (
          <div className="about-team__grid">
            {TEAM_MEMBERS.map((member) => (
              <article key={member.legajo} className="team-member">
                <img src={member.foto} alt={`Foto de ${member.nombre}`} />
                <div>
                  <h3>{member.nombre}</h3>
                  <p>{member.rol}</p>
                  <span>Legajo {member.legajo}</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </article>
  )
}
