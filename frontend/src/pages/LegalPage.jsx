const CONTENT = {
  privacidad: {
    eyebrow: 'Información legal',
    title: 'Política de privacidad',
    intro: 'En Film Zone cuidamos los datos que utilizás para crear tu cuenta y realizar compras.',
    sections: [
      ['Datos que recopilamos', 'Podemos registrar datos de identificación y contacto, información de la cuenta, métodos de pago asociados y el historial de tickets y consumos.'],
      ['Cómo usamos la información', 'Utilizamos estos datos para administrar tu cuenta, procesar compras, emitir tickets, enviar confirmaciones y brindar asistencia.'],
      ['Seguridad', 'El acceso a la cuenta requiere autenticación. En la interfaz, las tarjetas guardadas se identifican únicamente por sus últimos números.'],
      ['Comunicaciones', 'El correo registrado se utiliza para verificaciones de cuenta, recuperación de acceso y envío de comprobantes de compra.'],
      ['Actualización de datos', 'Podés consultar y modificar tus datos personales desde Mi cuenta. También podés eliminar métodos de pago que ya no utilices.'],
    ],
  },
  normas: {
    eyebrow: 'Antes de tu visita',
    title: 'Normas del cine',
    intro: 'Estas normas ayudan a que todas las personas disfruten la función de manera segura y ordenada.',
    sections: [
      ['Ingreso a la sala', 'Presentá el código QR de tu ticket al personal. Cada entrada puede validarse una sola vez y corresponde a la función, sala y butaca indicadas.'],
      ['Horarios', 'Recomendamos llegar con anticipación. Una vez iniciada la función, el ingreso podrá limitarse para no interrumpir la experiencia.'],
      ['Butacas', 'Respetá la ubicación asignada. Las butacas señaladas como fuera de servicio no pueden utilizarse.'],
      ['Convivencia', 'Mantené el teléfono en silencio, evitá conversaciones durante la película y seguí las indicaciones del personal.'],
      ['Consumos', 'Los productos comprados pueden retirarse presentando el mismo QR. La entrega queda registrada en el ticket.'],
      ['Cancelaciones y reembolsos', 'Las entradas pueden cancelarse desde Mis tickets hasta 2 horas antes de la función. Las compras de Candy pueden cancelarse mientras no hayan sido entregadas. No se admiten cancelaciones de entradas validadas ni de consumos entregados.'],
    ],
  },
}

export default function LegalPage({ type }) {
  const content = CONTENT[type]

  return (
    <article className="legal-page">
      <header className="legal-page__header">
        <p className="eyebrow">{content.eyebrow}</p>
        <h1>{content.title}</h1>
        <p>{content.intro}</p>
      </header>

      <div className="legal-page__content">
        {content.sections.map(([title, text]) => (
          <section key={title}>
            <h2>{title}</h2>
            <p>{text}</p>
          </section>
        ))}
      </div>
    </article>
  )
}
