# Organización de estilos

`main.jsx` importa únicamente `global.css`. Este archivo actúa como índice y carga el resto de las hojas en un orden controlado.

```text
styles/
  global.css
  theme.css
  base.css
  responsive.css
  layout/
    header.css
    footer.css
  pages/
    about.css
    cartelera.css
    compra.css
    cuenta.css
    club.css
    legal.css
  components/
    cards.css
    modals.css
    tickets.css
```

## Archivos principales

### `global.css`

Es el punto de entrada. Solo contiene los `@import`; no se agregan reglas visuales directamente. El orden debe conservarse porque los estilos más específicos se apoyan en reglas cargadas anteriormente.

### `theme.css`

Define las variables CSS de los temas claro y oscuro: colores, superficies, bordes, sombras, tipografías y fondos. Un cambio aquí afecta la identidad visual completa.

### `base.css`

Contiene el reset básico y las reglas generales de HTML: `body`, títulos, enlaces, imágenes, formularios y clases reutilizables como `.eyebrow`, `.muted` y `.tiny`.

### `responsive.css`

Contiene los ajustes globales para tablet y celular: navegación móvil, grillas, flujo de compra compacto, tamaños, footer y barra inferior. Las adaptaciones pequeñas que pertenecen exclusivamente a una página pueden permanecer junto a esa página.

## `layout/`

### `header.css`

Encabezado compartido: marca Film Zone, navegación, buscador, modo oscuro, menú del usuario, carrito activo, avisos y contenedor principal del sitio.

### `footer.css`

Pie de página, enlaces institucionales, texto legal y espaciado inferior para escritorio y dispositivos móviles.

## `pages/`

### `about.css`

Hero institucional, presentación de la empresa, valores y tarjetas de los integrantes.

### `cartelera.css`

Presentación principal, selector de fechas, filtros, grilla de películas, portadas, funciones desplegables y botones de la cartelera.

### `compra.css`

Detalle de película y flujo de compra: selección de función, resumen, mapa de butacas, reserva temporal, formularios de pago y confirmación.

### `cuenta.css`

Estructura de Mi cuenta: perfil, recuperación, verificación de correo, formularios y administración de métodos de pago.

### `club.css`

Planes del Club Film Zone, tarjetas de membresía, beneficios, plan actual y acciones de cambio o baja.

### `legal.css`

Política de privacidad, normas del cine y aceptación de condiciones durante la compra.

## `components/`

### `cards.css`

Componentes visuales repetibles con forma de tarjeta. Actualmente contiene tarjetas bancarias y productos del kiosco, junto con sus estados seleccionados y controles de cantidad.

### `modals.css`

Ventanas emergentes, diálogos de confirmación y panel lateral de autenticación. Incluye fondo, cabecera, acciones, cierre y animaciones.

### `tickets.css`

Historial y detalle del ticket: datos de función, entradas, consumos, QR, código manual, estados y acciones de cancelación.

## CSS que permanece junto al componente

- `components/cartelera/AccordionGallery.css`: animación y distribución de la galería destacada.
- `components/common/Loader.css`: animación de la pantalla de carga.

Estos archivos permanecen junto a sus componentes porque son independientes y no se reutilizan en otras secciones.

## Cómo encontrar una regla

1. Buscar la clase utilizada por el JSX.
2. Identificar si pertenece al layout, a una página o a un componente reutilizable.
3. Abrir el archivo correspondiente.
4. Revisar `responsive.css` si el comportamiento cambia solamente en celular o tablet.
