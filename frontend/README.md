# Film Zone: interfaz React

Aplicación web para espectadores. Permite consultar la cartelera, comprar entradas y productos del kiosco, contratar membresías y administrar la cuenta. Dueño y empleados utilizan exclusivamente Swing; no hay un panel administrativo web.

## Iniciar

Requiere Node.js 18 o posterior, npm y el backend iniciado en `http://localhost:8080`.

Desde PowerShell, dentro de esta carpeta (la que contiene `package.json`):

```powershell
npm.cmd install
npm.cmd run dev
```

Abrir `http://localhost:5173`, o el puerto que indique Vite si está ocupado. En macOS/Linux se usan `npm install` y `npm run dev`. La preparación de MySQL, backend y Swing está en [la guía de ejecución](../GUIA_EJECUCION.md).

`vite.config.js` redirige las rutas de la API y `/images` a Spring Boot. No hay que cambiar cada endpoint para desarrollar. `npm.cmd run build` genera `dist/`; `npm.cmd run preview` permite revisar esa compilación, pero no sustituye un despliegue de producción.

## Mapa de carpetas

Las rutas siguientes son relativas a `frontend/`.

```text
src/
  main.jsx                  Inicio y proveedores globales
  App.jsx                   Rutas y comportamiento de desplazamiento
  routes/AppRoutes.jsx      URL -> página
  api/client.js             HTTP, JSON, cookies y errores
  context/                  Sesión, catálogo y borrador de compra
  hooks/                    Pago y seguimiento de reservas
  pages/                    Pantallas asociadas a rutas
    compra/                 Acceso, butacas, kiosco y pago
  components/
    layout/                 Encabezado, carrito, usuario, avisos y footer
    cartelera/              Galería, tarjetas y selector de días
    compra/                 Sala, productos, pago y resúmenes
    cuenta/                 Perfil, tarjetas, tickets y membresía
      auth/                 Login, registro, recuperación y verificación
    tickets/                Detalle, QR y descarga del ticket
    common/                 Modal, confirmación, carga y desplazamiento
  utils/format.js           Fechas, dinero, butacas y normalización
  styles/                   Tema, base, layout, páginas y componentes
```

## Archivos que coordinan cada responsabilidad

| Archivo dentro de `src/` | Qué hace y a quién delega |
| --- | --- |
| `main.jsx` | Monta React y anida Router, sesión, catálogo y compra. |
| `routes/AppRoutes.jsx` | Define páginas y redirecciones de enlaces antiguos. |
| `components/layout/SiteLayout.jsx` | Compone navegación, `HeaderActions`, `Outlet`, avisos y footer. |
| `components/layout/HeaderActions.jsx` | Reúne búsqueda, carrito, tema y acceso lateral a la cuenta. |
| `components/layout/ActiveCart.jsx` | Permite retomar o cancelar la compra guardada al salir del flujo. |
| `components/layout/UserMenu.jsx` | Menú del espectador y confirmación de cierre de sesión. |
| `components/layout/ReservationExpiredNotice.jsx` | Avisos de compra conservada y reserva vencida. |
| `context/AuthContext.jsx` | Sesión del espectador, registro, verificación y actualización de sus datos. |
| `context/CatalogContext.jsx` | Carga y recarga datos compartidos del cine. |
| `context/PurchaseContext.jsx` | Borrador en `sessionStorage`, productos, butacas y continuidad del carrito. |
| `hooks/useSeatReservationHeartbeat.js` | Consulta reservas cada cinco segundos; calcula el contador sin renovar la reserva. |
| `hooks/useCheckout.js` | Estado de pago, comprobaciones visuales, totales y petición final de compra. |
| `pages/compra/CompraPagoPage.jsx` | Protege el paso y compone pago, resumen y resultado. |
| `components/compra/CheckoutSummary.jsx` | Conecta los Context con `OrderSummary` y sus acciones de edición. |
| `components/compra/MembershipCheckout.jsx` | Exporta `useMembershipCheckout` para consultar cotizaciones y un componente para mostrarlas. |
| `components/compra/PurchaseSuccess.jsx` | Muestra el ticket o la membresía confirmada por el servidor. |
| `components/cuenta/PurchaseAuthDrawer.jsx` | Coordina formularios pequeños de `cuenta/auth/` y la continuación tras autenticarse. |
| `components/tickets/TicketDetails.jsx` | Representa ticket y QR; solicita PDF o cancelación a la API. |

Un componente puede tener estado local sin ser un Context. El borrador no es un ticket guardado: las reservas sí se registran temporalmente en el backend y la compra se confirma al pagar. Los precios, permisos y disponibilidad definitivos los decide el backend.

## Rutas principales

| URL | Página |
| --- | --- |
| `/` | `CarteleraPage` |
| `/pelicula/:id` | `PeliculaPage` |
| `/compra/:movieId/acceso` | `CompraAccesoPage` |
| `/compra/:movieId/butacas` | `CompraButacasPage` |
| `/compra/:movieId/kiosco` | `CompraKioscoPage` |
| `/compra/:movieId/pago` | `CompraPagoPage` |
| `/kiosco` y `/kiosco/pago` | Compra de productos sin entradas |
| `/club` y `/club/pago` | Planes y pago de membresía |
| `/cuenta` | Perfil, métodos de pago, membresía y tickets |
| `/nosotros`, `/privacidad`, `/normas` | Información institucional y legal |
| `/proximamente`, `/experiencias` | Pantallas informativas temporales |

Las rutas con `/candy` solo redirigen a `/kiosco`. El endpoint `/compras/candy` conserva su nombre por compatibilidad con el backend; no es una página de React.

## Orden de estudio

1. `main.jsx` -> `App.jsx` -> `AppRoutes.jsx` -> `SiteLayout.jsx`.
2. `api/client.js` y los tres Context: de dónde llegan los datos y dónde se conservan.
3. `CarteleraPage` -> `PeliculaPage` -> páginas de compra -> `useCheckout` -> `PurchaseSuccess`.
4. `PurchaseAuthDrawer` -> formularios de `cuenta/auth/`; después perfil, tarjetas y tickets.
5. JSX -> clase CSS -> archivo correspondiente -> ajustes responsive.

La [guía de estudio completa](GUIA_ESTUDIO_FRONTEND.md) incluye recorridos React y Swing, preguntas y ejercicios. El [mapa de estilos](src/styles/README.md) explica cada hoja. El [mapa de Swing](../src/main/java/desktop/README.md) explica la otra interfaz.
