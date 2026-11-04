# Organización de estilos

`main.jsx` importa únicamente `global.css`. Este archivo actúa como índice y carga el resto de las hojas en un orden controlado.

```text
styles/
  global.css
  theme.css
  base.css
  responsive.css
  responsive/
    tablet.css
    mobile-header.css
    mobile-cartelera.css
    mobile-pelicula.css
    mobile-compra.css
    mobile-club.css
    mobile-candy.css
    mobile-summary.css
    mobile-overlays-footer.css
    small-phone.css
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

El fondo usa `--body-background`, `--body-image-filter` y `--body-image-opacity`. Estas dos últimas ajustan el color y la intensidad de la imagen cinematográfica en cada tema, sin modificar el archivo original.

### `base.css`

Contiene el reset básico y las reglas generales de HTML: `body`, títulos, enlaces, imágenes, formularios y clases reutilizables como `.eyebrow`, `.muted` y `.tiny`.

`body::before` muestra detrás del contenido `src/assets/cinema-background.png`, sin repetirla y cubriendo la pantalla. En celular se encuadra hacia la derecha para conservar visible la cinta. Es decorativa: no ocupa espacio, no intercepta clics y no cambia el tamaño de las páginas.

### `responsive.css`

Es un índice de importaciones, no una hoja grande de reglas. Carga `responsive/` en el mismo orden que tenían las reglas antes de separarlas. No reordenar los imports alfabéticamente: algunas reglas posteriores completan otras anteriores. Las adaptaciones locales que ya estaban en una página permanecen allí.

## `responsive/`

| Archivo | Ancho | Qué contiene |
| --- | --- | --- |
| `tablet.css` | Hasta 900 px y una regla base del menú | Primera adaptación de navegación, grillas, formularios y compra. |
| `mobile-header.css` | Hasta 640 px | Base del documento móvil, contenedores, encabezado, iconos, búsqueda y menús. |
| `mobile-cartelera.css` | Hasta 640 px | Presentación, días, filtros, tarjetas y reglas compartidas de formularios/encabezados de sección. |
| `mobile-pelicula.css` | Hasta 640 px | Ficha de película, lista de funciones y botón de elegir asientos. |
| `mobile-compra.css` | Hasta 640 px | Pasos, sala, resumen, pago, tarjetas bancarias y compra confirmada. |
| `mobile-club.css` | Hasta 640 px | Planes de membresía y selector de vencimiento. |
| `mobile-candy.css` | Hasta 640 px | Productos compactos, variantes y controles de cantidad. |
| `mobile-summary.css` | Hasta 640 px | Barra inferior y detalles desplegables de la compra; ajustes finales de productos. |
| `mobile-overlays-footer.css` | Hasta 640 px | Tickets, QR, modales, avisos, footer y Sobre nosotros. |
| `small-phone.css` | Hasta 390 px | Ajustes finales del buscador en pantallas pequeñas. Las funciones se ajustan en `mobile-pelicula.css`. |

Los grupos conservan bandas consecutivas de la hoja anterior. Por eso algunas reglas compartidas aparecen junto a una sección concreta: moverlas de orden podría cambiar la cascada. En la separación inicial se comprobó que las 260 reglas conservaban contenido y orden originales; los ajustes visuales posteriores se realizan en la hoja de su sección.

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

Las reglas `.function-picker` y `.function-option` pertenecen a esta hoja, no a la cartelera. La grilla iguala alturas y permite que el texto ocupe más de una línea cuando hace falta. La selección usa una sombra interior naranja sobre todo el marco para no cambiar las dimensiones. `.movie-detail` añade separación antes del footer solo en el detalle de película.

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
4. Si cambia solamente en celular o tablet, seguir los imports de `responsive.css` hasta la hoja de la sección. Usar la búsqueda de clases para encontrar reglas compartidas.
