# Guía de estudio de las interfaces de Film Zone

Esta guía refleja la separación actual de componentes React y módulos Swing. Para una consulta rápida: [mapa React](README.md), [mapa Swing](../src/main/java/desktop/README.md), [mapa CSS](src/styles/README.md) e [inicio del proyecto](../GUIA_EJECUCION.md).

## Índice de lectura

| Bloque | Secciones |
| --- | --- |
| Panorama y orden de estudio | [1. Idea general](#1-idea-general), [2. Orden](#2-orden-recomendado-para-estudiar), [3. Carpetas](#3-carpetas-y-responsabilidades). |
| Inicio, rutas y estado React | [4. Inicio](#4-inicio-de-la-aplicación), [5. Rutas](#5-enrutamiento), [6. Estado](#6-estado-global). |
| Compra y comunicación | [7. Compra](#7-flujo-completo-de-compra), [8. Reservas](#8-reservas-y-prevención-de-doble-compra), [9. HTTP](#9-comunicación-con-el-backend). |
| Conceptos y práctica React | [10. Hooks](#10-hooks-de-react-utilizados), [11. Datos](#11-props-estado-y-datos-derivados), [12. Formularios](#12-formularios-y-validación), [13. CSS](#13-css-y-responsive), [14. Preguntas](#14-preguntas-posibles-y-respuestas-breves), [15. Práctica](#15-práctica-antes-de-la-exposición). |
| Mapa compartido y archivos React | [16. Interfaces](#16-las-dos-interfaces-del-sistema), [17. Proyecto](#17-mapa-general-del-proyecto), [18. Archivos React](#18-estructura-de-react-archivo-por-archivo). |
| Swing y sus recorridos | [19. Inicio](#19-inicio-de-la-aplicación-swing), [20. Archivos](#20-archivos-principales-de-swing), [21. Dueño](#21-módulos-del-dueño-en-swing), [22. Empleado](#22-módulos-del-empleado-en-swing), [23. Conceptos](#23-conceptos-de-swing-que-hay-que-poder-explicar), [24. Flujos](#24-flujos-para-estudiar-en-swing). |
| Comparación, ejecución y exposición | [25. Comparación](#25-react-y-swing-comparación-rápida), [26. Ejecutar](#26-cómo-iniciar-todo-en-una-pc-nueva), [27. Equipo](#27-reparto-sugerido-para-estudiar-en-equipo), [28. Guion](#28-guion-corto-para-la-exposición), [29. Preguntas Swing](#29-preguntas-posibles-sobre-swing), [30. Repaso](#30-lista-final-de-repaso). |
| Detalle de la separación de Swing | [31. Formularios](#31-formularios-administrativos-de-swing), [32. Diálogos](#32-diálogos-del-personal), [33. Componentes](#33-componentes-comunes-de-swing), [34. API](#34-clientes-de-la-api-para-swing), [35. Estado y módulos](#35-estado-y-coordinación-por-módulo). |
| Práctica por archivo y límites de responsabilidad | [36. Recorridos guiados](#36-recorridos-guiados-por-archivo). |

Las rutas `src/...` de los apartados React son relativas a `frontend/`; las rutas `desktop/...` son relativas a `src/main/java/`. Las rutas que empiezan por `frontend/` o `src/main/java/` parten de la raíz del repositorio. Una ruta web como `/candy` no es una carpeta Java ni un endpoint de la API.

## 1. Idea general

Film Zone tiene dos interfaces. La aplicación web está desarrollada con React y Vite para los espectadores. La aplicación de escritorio está desarrollada con Java Swing para el dueño y los empleados del cine.

Las dos interfaces consumen la misma API Spring Boot. React y Swing no se conectan directamente entre sí: cada una envía peticiones HTTP al backend, que aplica las reglas de negocio y trabaja con MySQL.

Ninguna interfaz accede directamente a MySQL. React centraliza sus peticiones en `frontend/src/api/client.js` y Swing lo hace en `src/main/java/desktop/comun/ApiClient.java`.

Flujo general:

```text
Usuario
  -> página React
  -> componente visual
  -> Context o función de la página
  -> api/client.js
  -> backend Spring Boot
  -> respuesta JSON
  -> actualización del estado
  -> React vuelve a renderizar la interfaz
```

## 2. Orden recomendado para estudiar

1. `src/main.jsx`: inicio de React y proveedores globales.
2. `src/App.jsx`: composición principal de la aplicación.
3. `src/routes/AppRoutes.jsx`: URL asociada a cada página.
4. `src/components/layout/SiteLayout.jsx`: estructura compartida y composición de los componentes del encabezado.
5. `src/api/client.js`: comunicación con el backend.
6. `src/context/AuthContext.jsx`: sesión del espectador.
7. `src/context/CatalogContext.jsx`: catálogo global.
8. `src/context/PurchaseContext.jsx`: compra en curso.
9. Páginas de cartelera y película.
10. Flujo de compra: acceso, butacas, candy y pago.
11. Cuenta, tickets y métodos de pago.
12. Componentes reutilizables, utilidades y CSS.

No conviene comenzar por `responsive.css`: primero hay que entender qué componente genera cada elemento y cuál es su estilo base.

### Convención de vocabulario

- **Candy** se usa en textos, rutas, páginas, componentes y nombres derivados, por ejemplo `CandyPage`, `CandyCatalog` y `totalCandy`.
- **Consumos** representa internamente el mapa de productos y cantidades guardado en `draft.consumos`.
- `itemsConsumo` es el detalle que devuelve el backend; `draft.consumos` es el mapa de cantidades del carrito. No representan exactamente la misma estructura.
- Las rutas antiguas con `/kiosco` redirigen a `/candy` para mantener compatibles los enlaces guardados.

## 3. Carpetas y responsabilidades

### `src/api`

Contiene la capa de comunicación HTTP.

- `client.js`: define `request`, procesa respuestas y errores, incluye cookies de sesión con `credentials: 'include'` y expone métodos como `getPeliculas`, `reservarButaca` o `comprar`.

### `src/routes`

- `AppRoutes.jsx`: reúne todas las rutas. Permite saber rápidamente qué página se abre para cada URL.

### `src/pages`

Una página representa una pantalla asociada a una ruta.

- `CarteleraPage.jsx`: fechas, filtros y películas disponibles.
- `PeliculaPage.jsx`: detalle de película y elección de función.
- `CandyPage.jsx`: compra exclusiva de productos del candy.
- `ClubPage.jsx`: membresías.
- `CuentaPage.jsx`: acceso, registro y secciones de la cuenta.
- `AboutPage.jsx`, `LegalPage.jsx` y `PlaceholderPage.jsx`: contenido institucional.
- `pages/compra`: etapas del flujo de compra.

### `src/components`

Los componentes son partes reutilizables de una página.

- `cartelera`: tarjetas de películas, selector de fechas y galería.
- `compra`: sala, butacas, productos, resumen, pago y contador.
- `cuenta`: perfil, métodos de pago, tickets y autenticación.
- `tickets`: representación detallada del ticket y QR.
- `layout`: encabezado, buscador, pie y estructura general.
- `common`: modal, diálogo de confirmación, carga y comportamiento común.

### `src/context`

Los Context almacenan información que varias páginas necesitan compartir sin pasarla manualmente por muchos niveles de props.

- `AuthContext`: usuario autenticado y operaciones de sesión.
- `CatalogContext`: películas, funciones, salas, butacas y productos.
- `AccountContext`: tickets privados, carga, errores y reintento por cuenta autenticada.
- `PurchaseContext`: borrador de compra, reservas y carrito.

### `src/hooks`

- `useSeatReservationHeartbeat.js`: consulta periódicamente si las reservas siguen vigentes y calcula el tiempo restante.
- `useCheckout.js`: coordina el estado del pago, totales, comprobaciones visuales y la petición de compra. Delega la cotización de membresías a `useMembershipCheckout`, exportado desde `components/compra/MembershipCheckout.jsx`.
- `useDialog.js`: coordina foco, teclado, desplazamiento y cierre de las ventanas, incluyendo ventanas anidadas y operaciones pendientes.

### `src/utils`

- `format.js`: fechas, dinero, formatos, idiomas, nombres de butacas y normalización de texto.

### `src/styles`

- `global.css`: índice que importa todas las hojas en el orden correcto.
- `theme.css` y `base.css`: variables de tema y estilos generales.
- `layout/`: encabezado y footer compartidos.
- `pages/`: estilos separados por pantalla o caso de uso.
- `components/`: tarjetas, modales y tickets reutilizables.
- `responsive.css`: índice ordenado de diez hojas en `responsive/`, separadas por sección y tamaño. La división conserva las mismas reglas y su orden.
- `README.md`: mapa detallado de la carpeta y criterio para ubicar cada regla.
- Algunos componentes complejos conservan un CSS propio, como `AccordionGallery` y `Loader`.

## 4. Inicio de la aplicación

`main.jsx` busca el elemento `#root` de `index.html` y monta React con `createRoot`.

Los proveedores se anidan de esta manera:

```jsx
<BrowserRouter>
  <AuthProvider>
    <CatalogProvider>
      <AccountProvider>
        <PurchaseProvider>
          <App />
        </PurchaseProvider>
      </AccountProvider>
    </CatalogProvider>
  </AuthProvider>
</BrowserRouter>
```

- `BrowserRouter` habilita la navegación por URL.
- `AuthProvider` administra la sesión.
- `CatalogProvider` carga datos del cine.
- `AccountProvider` carga únicamente los tickets de la cuenta activa.
- `PurchaseProvider` conserva el estado de la compra.
- `App` muestra las rutas.

## 5. Enrutamiento

`AppRoutes.jsx` usa React Router:

- `Route`: relaciona una URL con un componente.
- `:id` o `:movieId`: parámetro dinámico de la URL.
- `Navigate`: redirección controlada.
- `SiteLayout`: ruta contenedora que compone encabezado, avisos, contenido y footer.
- `Outlet`: lugar donde se renderiza la página hija actual.

Ejemplo:

```text
/pelicula/3
```

Abre `PeliculaPage` y `useParams()` obtiene el ID `3`.

### Carga diferida y errores de página

`AppRoutes.jsx` declara las pantallas secundarias con `lazy(() => import(...))`. `import()` devuelve una promesa del módulo y `lazy` permite usar su exportación por defecto como componente. Vite genera paquetes separados que el navegador descarga cuando se visita esa pantalla; no se cambia su URL ni su lógica de negocio.

```jsx
const PeliculaPage = lazy(() => import('../pages/PeliculaPage'))
```

`SiteLayout` mantiene encabezado y footer fuera del límite de carga. Dentro del contenido, `Suspense` muestra `Loader` mientras llega el módulo y luego representa `Outlet`. Esto no espera automáticamente las consultas HTTP: los Context y formularios conservan sus propios estados de carga.

`RouteErrorBoundary` captura un fallo de descarga o render y ofrece recarga manual en lugar de dejar la web en blanco. Es un componente de clase React con `getDerivedStateFromError`, no una clase del backend. Se reinicia al cambiar de ruta mediante su `key`. Recargar no envía una compra automáticamente.

El paquete inicial bajó aproximadamente de 505,6 a 402,9 kB sin aumentar el límite de advertencia de Vite. La mejora corresponde al JavaScript inicial; no implica que todas las imágenes o la suma de paquetes de todas las pantallas pesen menos.

## 6. Estado global

### Autenticación

`AuthContext.jsx`:

- Recupera la sesión guardada al iniciar.
- Ejecuta login y registro.
- Verifica el correo.
- Actualiza los datos del espectador.
- Cierra la sesión.
- Expone `spectator` e `isSpectator`.

El ID se conserva en `localStorage`, mientras que la sesión real también depende de la cookie enviada por el backend.

### Catálogo

`CatalogContext.jsx` carga en paralelo categorías, películas, funciones, salas, butacas y productos mediante `Promise.all`.

`reload()` vuelve a solicitar los datos después de una operación que puede modificar el sistema, por ejemplo una compra.

### Historial privado

`AccountContext.jsx` usa la sesión de `AuthContext` y consulta `GET /tickets` solo si hay espectador. `MisEntradasList` consume `useAccount`, no el catálogo público. `reloadTickets()` actualiza el historial tras una compra o cancelación.

Al cambiar o cerrar sesión se ocultan los tickets anteriores inmediatamente. `AbortController` cancela la petición pendiente y una versión descarta respuestas tardías. Si falla la carga se muestra el error y la opción de reintentar, en lugar de afirmar que no hay compras. La API mantiene la autorización efectiva.

### Compra

`PurchaseContext.jsx` contiene un `draft` con:

```js
{
  purchaseType,
  movieId,
  functionId,
  selectedSeats,
  consumos,
  candyVisitado,
  membershipId,
}
```

El borrador se guarda en `sessionStorage`. Sobrevive a una recarga de la pestaña, pero no representa una compra confirmada en la base de datos.

La compra real se crea solamente en la etapa de pago, cuando se ejecuta `api.comprar`, `api.comprarCandy` o `api.comprarMembresia`. Esto no significa que no haya escrituras anteriores: la cuenta, el método de pago y las reservas temporales pueden guardarse antes. El borrador no crea entradas ni un ticket pagado.

## 7. Flujo completo de compra

### Paso 1: cartelera

`CarteleraPage.jsx` obtiene películas y funciones desde `CatalogContext`. Con `useMemo` filtra por fecha, género, formato e idioma.

`MovieCard.jsx` agrupa funciones por formato e idioma y permite desplegar horarios.

### Paso 2: película y función

`PeliculaPage.jsx` obtiene el ID con `useParams`, busca la película y filtra funciones del día.

Al continuar:

- Guarda película y función en `PurchaseContext`.
- Si el usuario no está autenticado o no verificó su correo, abre `PurchaseAuthDrawer`.
- Si todo está correcto, navega a la selección de butacas.

### Paso 3: butacas

`CompraButacasPage.jsx`:

- Obtiene las butacas de la sala correspondiente.
- Marca como no disponibles las ocupadas, fuera de servicio o reservadas por otra sesión.
- Al seleccionar una butaca llama a `api.reservarButaca`.
- Al quitarla llama a `api.liberarReservaButaca`.
- Solo actualiza el estado local si el backend confirma la reserva.

La primera butaca inicia una reserva de 10 minutos, duración definida por `MINUTOS_RESERVA` en `service/ventas/ReservaButacaService.java`. `useSeatReservationHeartbeat` consulta las reservas cada 5 segundos y actualiza el contador cada segundo. Consultar no renueva el vencimiento. Si vence, `PurchaseContext` limpia las butacas y los consumos de esa compra y activa el aviso correspondiente.

### Paso 4: candy

`CompraCandyPage.jsx` usa `CandyCatalog` para modificar las cantidades guardadas en `draft.consumos`.

Los productos son opcionales, pero la etapa debe visitarse antes del pago. Eso se controla con `candyVisitado`.

### Paso 5: pago

`CompraPagoPage.jsx` controla las redirecciones y compone la pantalla final. La operación se divide así:

- `useCheckout.js` valida la función, correo, términos y método de pago; calcula totales, construye la petición y ejecuta la compra.
- `MembershipCheckout.jsx` carga la cotización y muestra las reglas específicas de cambios de membresía.
- `CheckoutSummary.jsx` conecta el resumen con catálogo y compra en curso.
- `PurchaseSuccess.jsx` muestra el ticket o la membresía confirmada.

`PaymentMethodSelector.jsx` se ocupa únicamente de elegir una tarjeta guardada o completar una nueva.

## 8. Reservas y prevención de doble compra

La interfaz no considera una butaca seleccionada como definitivamente comprada.

1. El usuario hace clic.
2. React envía la reserva al backend.
3. El backend decide si todavía está disponible.
4. Si responde correctamente, se agrega a `selectedSeats`.
5. Otras sesiones reciben esa reserva al consultar disponibilidad.
6. Al pagar se generan entrada y ticket.
7. Si vence o se cancela, se libera la reserva.

Por eso la protección principal no depende solamente del color mostrado en React: el backend confirma cada operación.

## 9. Comunicación con el backend

`api/client.js` concentra `fetch` para evitar repetir configuración.

Aspectos importantes:

- `credentials: 'include'`: envía cookies de sesión.
- Convierte objetos JavaScript a JSON con `JSON.stringify`.
- Detecta JSON usando `Content-Type`.
- Si la respuesta no es exitosa, lanza un `Error` con mensaje y estado.
- `errorMessage` transforma distintos errores en un texto presentable.

Durante desarrollo, `vite.config.js` usa `proxy` para enviar rutas como `/peliculas` o `/compras` al backend de `localhost:8080`.

## 10. Hooks de React utilizados

### `useState`

Guarda estado local: formulario, pestaña activa, modal abierto, tarjeta seleccionada o función elegida.

### `useEffect`

Ejecuta efectos externos: cargar información, registrar intervalos, guardar en storage o reaccionar a un cambio de ruta.

Todo intervalo o listener creado en un efecto debe eliminarse en la función de limpieza.

### `useMemo`

Calcula un valor derivado y evita repetir el cálculo mientras no cambien sus dependencias. Se usa en filtros y búsquedas.

### `useCallback`

Mantiene estable una función utilizada por efectos o componentes. Aparece en los Context y en la carga de reservas.

### `useContext`

Permite consumir sesión, catálogo, historial privado o compra mediante `useAuth`, `useCatalog`, `useAccount` y `usePurchase`.

### `useRef`

Conserva una referencia sin provocar renderizados. Se usa para enfocar inputs, detectar clics fuera y recordar una ruta previa.

## 11. Props, estado y datos derivados

- **Props:** información que un componente padre entrega a un hijo.
- **Estado:** información que puede cambiar y provoca un nuevo renderizado.
- **Dato derivado:** valor calculado a partir de props o estado; no debe duplicarse innecesariamente en otro `useState`.

Ejemplo: el total del candy se calcula a partir de productos y cantidades. No hace falta guardarlo como estado independiente.

## 12. Formularios y validación

Los inputs son controlados: su `value` proviene del estado y `onChange` actualiza ese estado.

Existen dos niveles de validación:

- HTML/React: `required`, `type="email"`, `minLength`, formato y coincidencia de contraseñas.
- Backend: reglas definitivas como correo existente, tarjeta vencida, función pasada o butaca ocupada.

El frontend ayuda al usuario, pero no reemplaza las reglas del backend.

## 13. CSS y responsive

La carpeta `styles` utiliza:

- Variables CSS en `:root` para colores, superficies y sombras.
- `data-theme='dark'` para sobrescribir variables en modo oscuro.
- Nombres de clase similares a BEM: `movie-card__poster`, `ticket-receipt__row`.
- Grid para estructuras de página y Flexbox para grupos lineales.
- Media queries principales para escritorio, tablet y celular.
- `:focus-visible` para navegación con teclado.
- `@media (hover: hover)` para efectos exclusivos de dispositivos con mouse.

Al estudiar un estilo, buscar primero la clase utilizada por el JSX con la búsqueda global de IntelliJ.

## 14. Preguntas posibles y respuestas breves

### ¿Por qué se usa Context?

Porque la sesión, el catálogo y la compra son utilizados por varias páginas. Context evita pasar esa información manualmente por todos los componentes intermedios.

### ¿Cuándo se guarda una compra?

Solo al confirmar el pago. Antes existe un borrador en `PurchaseContext` y reservas temporales de butacas.

### ¿Por qué se usa `sessionStorage` para la compra?

Para conservar el flujo si se recarga la pestaña, sin convertir el borrador en información permanente del usuario.

### ¿Cómo se evita comprar dos veces una butaca?

La selección llama al backend para crear una reserva temporal. La interfaz solo confirma la selección si el servidor la acepta.

### ¿Cómo se mantiene la sesión?

El frontend usa una cookie de sesión mediante `credentials: 'include'` y guarda el ID del espectador para reconstruir sus datos al iniciar.

### ¿Qué diferencia hay entre página y componente?

Una página está asociada a una ruta y coordina un caso de uso. Un componente representa una parte reutilizable y recibe información mediante props.

### ¿Por qué no se llama a `fetch` desde todos los componentes?

Porque `api/client.js` centraliza URLs, métodos, JSON, cookies y tratamiento de errores.

### ¿Qué provoca un nuevo renderizado?

Un cambio en estado, props o Context consumido por el componente.

### ¿Qué hace `Navigate`?

Renderiza una redirección. Se usa para impedir el acceso a una etapa si falta sesión o información previa.

## 15. Práctica antes de la exposición

Explicar siguiendo estos recorridos:

1. Inicio: `index.html` -> `main.jsx` -> Providers -> `App` -> `AppRoutes` -> `SiteLayout`.
2. Datos: `CatalogContext` -> `api/client.js` -> backend -> estado -> página.
3. Compra: `PeliculaPage` -> `PurchaseContext` -> butacas -> candy -> pago -> ticket.
4. Sesión: formulario -> `AuthContext` -> API -> `spectator` -> interfaz autenticada.
5. Responsive: componente -> clase CSS -> regla base -> media query.

Para cada archivo conviene poder responder tres cosas:

1. Qué responsabilidad tiene.
2. Qué datos recibe o consume.
3. Qué acción produce o a qué componente delega.

## 16. Las dos interfaces del sistema

```text
Espectador
  -> Frontend React (localhost:5173)
  -> API Spring Boot (localhost:8080)
  -> Services y repositories
  -> MySQL

Dueño o empleado
  -> Interfaz Java Swing
  -> API Spring Boot (localhost:8080)
  -> Services y repositories
  -> MySQL
```

React está orientado a la experiencia de compra del cliente. Swing está orientado a la operación interna del cine. El backend es la única capa que decide si una acción está permitida y guarda los cambios.

## 17. Mapa general del proyecto

### Raíz del proyecto

- `pom.xml`: dependencias de Spring Boot, JPA, MySQL, PDFBox, ZXing y configuración para ejecutar backend y Swing.
- `GUIA_EJECUCION.md`: instrucciones generales para iniciar el proyecto.
- `database/`: scripts de preparación y actualización de MySQL.
- `frontend/`: aplicación web React para espectadores.
- `src/main/java/`: backend Spring Boot, modelos y aplicación Swing.
- `src/main/resources/application.properties`: conexión a MySQL y configuración del backend.
- `src/main/resources/static/images/`: portadas y fotos de productos servidas por Spring Boot.
- `target/`: archivos compilados por Maven. Se genera automáticamente y no se estudia ni se edita.

### Backend compartido

- `controller/`: endpoints HTTP. Recibe las peticiones de React y Swing.
- `service/`: reglas de negocio y coordinación de operaciones.
- `repository/`: acceso a la base de datos con Spring Data JPA.
- `modelo/entidades/`: clases principales que representan las tablas.
- `modelo/enums/`: estados y opciones cerradas, como estado de ticket, idioma o formato.
- `desktop/`: interfaz Swing. Es una interfaz cliente, no reemplaza a controllers ni services.
- `com/tp/cine/TpCineApiApplication.java`: punto de entrada del backend Spring Boot.

### Base de datos

- `database/seed_demo.sql`: elimina la base existente, crea tablas y carga categorías, películas, salas, butacas, productos y membresías. No carga cuentas ni compras de prueba.
- `database/completar_funciones_2_semanas.sql`: agrega funciones para dos semanas sin borrar compras ni usuarios.
- `database/descripcionPeliculas.txt`: textos usados para las películas.
- `database/portadasPeliculas/`: archivos originales de las portadas.

## 18. Estructura de React archivo por archivo

### Inicio y navegación

- `frontend/src/main.jsx`: monta React, define el tema inicial y registra los cuatro Context globales.
- `frontend/src/App.jsx`: agrega el comportamiento de desplazamiento y las rutas.
- `frontend/src/routes/AppRoutes.jsx`: relaciona cada URL con su página.
- `frontend/src/components/common/ScrollToTop.jsx`: sube la página al cambiar de ruta cuando corresponde.

### Comunicación y estado

- `frontend/src/api/client.js`: único punto de comunicación HTTP con Spring Boot.
- `frontend/src/context/AuthContext.jsx`: restaura la sesión, inicia y cierra sesión, registra al espectador y verifica el correo.
- `frontend/src/context/CatalogContext.jsx`: carga categorías, películas, funciones, salas, butacas y productos públicos.
- `frontend/src/context/AccountContext.jsx`: carga tickets privados de la sesión; maneja errores y evita reutilizar datos de otra cuenta.
- `frontend/src/context/PurchaseContext.jsx`: conserva el borrador, carrito, butacas reservadas y vencimiento de la reserva.
- `frontend/src/hooks/useSeatReservationHeartbeat.js`: mantiene actualizada la reserva temporal de butacas.

### Páginas

- `CarteleraPage.jsx`: portada principal, filtros y listado de películas con funciones.
- `PeliculaPage.jsx`: ficha de una película, días, formatos, idiomas y horarios.
- `CandyPage.jsx`: compra independiente de productos del candy.
- `ClubPage.jsx`: planes de membresía y cambios de plan.
- `CuentaPage.jsx`: acceso a perfil, tarjetas, membresía y tickets.
- `AboutPage.jsx`: información de Film Zone y del equipo.
- `LegalPage.jsx`: política de privacidad y normas del cine.
- `PlaceholderPage.jsx`: pantalla temporal para secciones aún no desarrolladas.

### Etapas de compra

- `pages/compra/CompraAccesoPage.jsx`: entrada protegida al flujo cuando falta autenticación.
- `pages/compra/CompraButacasPage.jsx`: carga la sala, reserva y libera butacas.
- `pages/compra/CompraCandyPage.jsx`: incorpora productos a una compra de entradas.
- `pages/compra/CompraPagoPage.jsx`: coordina redirecciones y componentes de la etapa de pago.
- `hooks/useCheckout.js`: concentra estado, validaciones, totales y operación final de compra.

### Componentes de cartelera

- `AccordionGallery.jsx`: presentación dinámica de películas destacadas.
- `DateStrip.jsx`: selector horizontal de días.
- `MovieCard.jsx`: portada y funciones agrupadas de cada película.

### Componentes de compra

- `SeatMap.jsx`: representación visual de pantalla y butacas.
- `SeatReservationTimer.jsx`: contador visible de la reserva.
- `CandyCatalog.jsx`: categorías, variantes y cantidades de productos.
- `OrderSummary.jsx`: resumen de entradas, butacas, consumos y total.
- `MobileCheckoutBar.jsx`: resumen fijo y compacto para celular.
- `PaymentMethodSelector.jsx`: tarjetas guardadas y tarjeta nueva.
- `MembershipCheckout.jsx`: cotización y presentación de cambios de membresía.
- `CheckoutSummary.jsx`: resumen previo conectado a los Context.
- `PurchaseSuccess.jsx`: resultado exitoso de entradas, candy o membresía.
- `ExpiryMonthPicker.jsx`: selector de mes y año de vencimiento.
- `common/PaymentCard.jsx`: tarjeta guardada y vista previa compartidas por perfil y pago; sus formatos viven en `utils/payment.js`.
- `PurchaseStepper.jsx`: representación de etapas del flujo.

### Cuenta, tickets y estructura visual

- `PurchaseAuthDrawer.jsx`: coordina la ventana lateral y decide qué etapa de autenticación mostrar.
- `cuenta/auth/LoginForm.jsx`: formulario de inicio de sesión.
- `cuenta/auth/RegisterForm.jsx`: formulario de creación de cuenta y confirmación de contraseña.
- `cuenta/auth/PasswordRecoveryFlow.jsx`: solicitud, correo simulado y cambio de contraseña, compartidos por el panel lateral, la cuenta y el perfil.
- `cuenta/auth/EmailVerification.jsx`: confirmación simulada del correo registrado.
- `PerfilForm.jsx`: edición de datos y cambio simulado de contraseña por correo.
- `MetodosPagoList.jsx`: alta y baja de tarjetas guardadas.
- `MisEntradasList.jsx`: historial de compras y apertura del ticket.
- `MembresiaPanel.jsx`: información del plan actual.
- `TicketDetails.jsx`: QR, código manual, entradas y consumos del ticket.
- `SiteLayout.jsx`: estructura general, navegación principal y lugar donde aparece cada página.
- `HeaderActions.jsx`: agrupa búsqueda, carrito, tema y acceso a la cuenta.
- `ActiveCart.jsx`: muestra y administra la compra que quedó en curso.
- `UserMenu.jsx`: controla el menú de cuenta, membresía y cierre de sesión.
- `ReservationExpiredNotice.jsx`: muestra los avisos de compra conservada o reserva vencida.
- `HeaderMovieSearch.jsx`: buscador desplegable de películas.
- `SiteFooter.jsx`: enlaces institucionales y legales.
- `Modal.jsx`, `ConfirmDialog.jsx` y `Loader.jsx`: piezas comunes para ventanas, confirmaciones y carga.
- `styles/global.css`: punto de entrada de las hojas organizadas en `layout`, `pages` y `components`.
- `utils/format.js`: formato de fechas, dinero, idioma, formato de función, butacas y búsquedas sin tildes.

## 19. Inicio de la aplicación Swing

El punto de entrada es `desktop/inicio/CineDesktopApplication.java`.

1. Configura Nimbus o la apariencia disponible del sistema.
2. Usa `SwingUtilities.invokeLater` para crear la interfaz en el hilo de eventos de Swing.
3. Lee la URL de la API desde `cine.api.url`; por defecto usa `http://localhost:8080`.
4. Crea un `ApiClient`.
5. Abre `LoginFrame`.

`SwingUtilities.invokeLater` es importante porque los componentes Swing deben crearse y modificarse en el Event Dispatch Thread.

## 20. Archivos principales de Swing

### `desktop/inicio/CineDesktopApplication.java`

Es el `main` de la aplicación de escritorio. Solo inicia la apariencia y abre el login; no contiene reglas de negocio.

### `desktop/inicio/LoginFrame.java`

- Muestra usuario y contraseña.
- Usa `SesionApi.iniciarSesion` para enviar `POST /sesion/personal` y consultar luego la sesión, mediante el mismo `ApiClient`.
- Consulta la sesión creada con `GET /sesion/personal`.
- Lee el rol devuelto por el backend.
- Abre `PrincipalFrame`, que selecciona `DuenioPanel` o `EmpleadoPanel`.
- Usa `SwingWorker` para que la petición no congele la ventana.

No hay un selector manual de rol. El backend determina la interfaz según la cuenta utilizada.

### `desktop/comun/ApiClient.java`

Es el equivalente de `frontend/src/api/client.js` para Swing.

- Usa `HttpClient` de Java.
- Expone métodos `get`, `post`, `put` y `delete`.
- Convierte objetos y respuestas con Jackson.
- Conserva la cookie de sesión mediante `CookieManager`.
- Lanza `ApiException` cuando la API responde con error.

### `desktop/comun/PrincipalFrame.java`

Es la ventana principal compartida y coordina la estructura exterior de Swing.

- Construye la ventana, cabecera, barra de estado y menú superior.
- Muestra el usuario y el rol de la sesión.
- Selecciona `DuenioPanel` o `EmpleadoPanel` según el rol informado por el backend.
- Delega la navegación y la recarga al panel seleccionado.
- Controla el cierre de sesión y la salida de la aplicación.

### `desktop/duenio/DuenioPanel.java` y `desktop/empleado/EmpleadoPanel.java`

Representan las dos interfaces disponibles después del login. Cada clase crea los módulos que le corresponden y registra sus pantallas, recargas y cierre. El dueño incorpora administración, salas y butacas, ventas y consumos, personal y actividad. El empleado incorpora salas y butacas, ventas y consumos y avisos.

### `desktop/comun/PanelPersonalBase.java`

Es la base de navegación compartida, no un contenedor de datos del negocio:

- Construye el menú lateral y utiliza `CardLayout` para mostrar una sección.
- Conserva el orden de las secciones y destaca el botón seleccionado.
- Coordina la recarga de los módulos registrados.
- Detiene los módulos cuando se cierra la sesión.

No contiene modelos de tablas, selecciones de butacas o tickets, clientes API ni temporizadores. Ese estado pertenece a los módulos descritos en la sección 35.

### Paneles operativos de Swing

- `desktop/empleado/paneles/SalasPanel.java`: tabla de salas, recarga y cambio de estado.
- `desktop/empleado/paneles/FuncionesPanel.java`: consulta y recarga de funciones para el empleado.
- `desktop/comun/paneles/ButacasPanel.java`: plano, selector de sala y acciones permitidas según el rol.
- `desktop/comun/paneles/TicketsPanel.java`: lectura de QR, selección y operaciones sobre tickets.
- `desktop/comun/paneles/ConsumosPanel.java`: lectura de QR y entrega manual de consumos.

Los paneles comunes también aparecen en la interfaz del dueño. Reciben acciones mediante `Runnable` o `Consumer<Integer>`, por lo que construyen la vista sin conocer los endpoints utilizados. La selección de tickets informa un ID o `null` cuando no hay ninguna fila seleccionada.

### `desktop/duenio/AdministracionPanel.java`

Compone los módulos de los CRUD exclusivos del dueño y coordina su carga conjunta. No contiene sus formularios ni las peticiones de edición.

- Categorías.
- Películas y carga local de imágenes.
- Funciones.
- Salas y editor dinámico de distribución.
- Productos de confitería.

La coordinación de cada uno está en `desktop/duenio/modulos/`: `CategoriasModulo`, `PeliculasModulo`, `FuncionesModulo`, `SalasAdministracionModulo` y `ProductosModulo`. `CrudModulo` comparte tabla, detalle y confirmación de eliminación; `DatosAdministracion` lleva el resultado de la carga conjunta y las opciones de los combos.

Los formularios de las entidades están en `desktop/duenio/dialogos`. `SalaDialog` utiliza `EditorDistribucionSala.java`, ubicado en esa misma carpeta, para crear la pantalla y la matriz editable. Cada posición puede convertirse en butaca o espacio vacío.

### `desktop/duenio/paneles`

- `CrudAdministracionPanel.java`: reutiliza `PanelConTabla` y agrega alta, recarga y doble clic para los CRUD del dueño.
- `CategoriasPanel.java`: gestión visual de categorías.
- `PeliculasPanel.java`: gestión visual de películas.
- `SalasAdminPanel.java`: gestión visual de salas.
- `FuncionesAdminPanel.java`: gestión visual de funciones.
- `ProductosPanel.java`: gestión visual de productos de confitería.
- `EmpleadosPanel.java`: alta, activación, desactivación y recuperación de cuentas del personal.
- `ActividadPanel.java`: consulta de auditorías y referencia visual de cada perfil.

Estos paneles reciben callbacks para ejecutar las operaciones. Los formularios administrativos y los diálogos de estado y cuentas del personal están separados en sus carpetas `dialogos`. Las rutas HTTP están en los clientes de `desktop/comun/api`; los coordinadores ejecutan sus métodos en segundo plano y actualizan la pantalla.

## 21. Módulos del Dueño en Swing

`DuenioPanel` utiliza la navegación y el `CardLayout` compartidos para mostrar una sección por vez.

### Operación

- **Ventas y tickets:** consulta tickets, busca por datos o QR, valida entradas y realiza reembolsos.
- **Consumos:** consulta consumos y valida su entrega manualmente o mediante QR.

### Cartelera

- **Funciones:** crea, edita y elimina funciones.
- **Películas:** administra películas, descripción, categoría y portada.
- **Categorías:** administra categorías y muestra películas relacionadas.

### Instalaciones

- **Salas:** crea, edita y elimina salas; administra su estado y distribución.
- **Butacas:** muestra el plano y los estados de cada asiento.

### Confitería

- **Productos:** administra nombre, categoría, tamaño, precio, stock e imagen.

### Administración

- **Empleados:** crea empleados, activa o desactiva cuentas y restablece contraseñas.
- **Actividad:** consulta acciones registradas del dueño y empleados.

## 22. Módulos del Empleado en Swing

`EmpleadoPanel` muestra únicamente tareas operativas:

- **Control de acceso:** busca el ticket, lee o pega el QR y valida las entradas.
- **Consumos:** entrega productos manualmente o mediante QR.
- **Funciones:** consulta y recarga la programación. No modifica fecha ni horario; la administración de funciones corresponde al dueño.
- **Salas:** consulta funciones y cambia el estado de una sala con descripción y aviso.
- **Butacas:** visualiza el plano, marca una butaca fuera de servicio y vuelve a habilitarla.

El empleado no puede ocupar ni liberar manualmente una butaca vendida. Tampoco accede a empleados, auditoría ni CRUD completo del catálogo.

## 23. Conceptos de Swing que hay que poder explicar

### `JFrame` y `JPanel`

`JFrame` es una ventana y `JPanel` organiza componentes dentro de ella. `LoginFrame` y `PrincipalFrame` son ventanas; `DuenioPanel` y `EmpleadoPanel` componen las vistas de cada rol. `AdministracionPanel` conserva el tipo `JPanel` como padre de diálogos, pero entrega los cinco paneles CRUD directamente al menú del dueño.

### Layout managers

- `BorderLayout`: divide norte, sur, este, oeste y centro.
- `GridBagLayout`: formularios con filas y columnas flexibles.
- `BoxLayout`: navegación vertical.
- `CardLayout`: cambia de módulo sin abrir otra ventana.
- `FlowLayout` y `GridLayout`: grupos simples y grillas.

### `JTable` y `DefaultTableModel`

El modelo contiene columnas y filas. La tabla solamente las representa. Cuando llega JSON de la API, los métodos `cargar...` limpian el modelo y agregan las filas actuales.

### `SwingWorker`

Las peticiones HTTP pueden tardar. `SwingWorker.doInBackground()` las ejecuta fuera del hilo visual y `done()` actualiza la pantalla al finalizar. Así la interfaz no queda congelada.

### Eventos

Los botones registran `ActionListener`. Las tablas usan eventos de selección o doble clic. El temporizador de avisos usa `Timer` para consultar novedades sin bloquear la interfaz.

### `JsonNode`

Swing recibe JSON y lo recorre con Jackson. Por ejemplo, `ticket.path("codigoQR").asText()` obtiene un dato sin crear una clase DTO específica en la interfaz.

## 24. Flujos para estudiar en Swing

### Inicio de sesión

```text
LoginFrame
  -> SesionApi.iniciarSesion(usuario, contrasenia)
  -> ApiClient envía POST /sesion/personal y GET /sesion/personal
  -> SesionController
  -> SesionService
  -> respuesta con usuario y rol
  -> PrincipalFrame selecciona el panel correspondiente
```

### Validación de entrada por QR

```text
Empleado pega o escanea código
  -> VentasModulo consulta el ticket mediante TicketsApi.buscarPorQr
  -> advierte si hay consumos pendientes y solicita confirmación cuando corresponde
  -> PUT /tickets/{id}/validar-entradas
  -> TicketService valida la operación
  -> Swing recarga tickets y consumos
```

Si existen consumos pendientes, la interfaz muestra una advertencia antes de validar el ingreso. Validar entradas no entrega automáticamente los consumos.

### Entrega de consumos

```text
Empleado abre Consumos
  -> busca por QR o selecciona una fila
  -> PUT /tickets/{id}/entregar-consumos
  -> backend actualiza los ítems pendientes
  -> Swing recarga consumos y tickets
```

### Sala o butaca con inconveniente

```text
Empleado selecciona sala o butaca
  -> elige estado o fuera de servicio
  -> agrega descripción y destinatario (obligatoria para sala con Problema particular; opcional para butaca)
  -> API guarda el cambio y crea un aviso
  -> otros empleados reciben el aviso periódico
```

### CRUD del dueño

```text
Dueño abre un módulo
  -> selecciona Crear, Editar o Eliminar
  -> el módulo administrativo del dominio abre su diálogo
  -> el diálogo devuelve un record Datos con lo ingresado
  -> cliente del dominio elige el endpoint
  -> ApiClient envía POST, PUT o DELETE
  -> controller delega en service
  -> repository actualiza MySQL
  -> Swing vuelve a cargar la tabla
```

## 25. React y Swing: comparación rápida

| Tema | React | Swing |
| --- | --- | --- |
| Usuario | Espectador | Dueño y empleado |
| Entrada | `main.jsx` | `CineDesktopApplication.java` |
| Navegación | React Router | `CardLayout` y menú lateral |
| Estado | Hooks y Context | Campos de clase y modelos de tabla |
| HTTP | `api/client.js` con `fetch` | `ApiClient.java` con `HttpClient` |
| Sesión | Cookie y `AuthContext` | Cookie guardada en `CookieManager` |
| Trabajo asíncrono | Promesas y `async/await` | `SwingWorker` |
| Reutilización | Componentes y props | Paneles, diálogos, componentes comunes y callbacks |
| Estilo | CSS responsive | Layout managers y Look and Feel |
| Datos recibidos | Objetos JavaScript | `JsonNode` |

## 26. Cómo iniciar todo en una PC nueva

### Requisitos

- Java 17.
- Maven.
- MySQL Server y MySQL Workbench.
- Node.js 18 o superior.

### Preparar MySQL

1. Solo en una instalación nueva o reinicio deliberado: ejecutar `database/seed_demo.sql`. **Borra la base existente.**
2. Ejecutar `database/completar_funciones_2_semanas.sql`.

### Iniciar el backend en PowerShell

```powershell
cd "C:\ruta\TP_Cine_API"
$env:DB_URL="jdbc:mysql://localhost:3307/tp_cine_api"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="CLAVE_DE_MYSQL"
mvn.cmd spring-boot:run
```

Si MySQL usa el puerto `3306`, hay que reemplazar `3307` en `DB_URL`.

### Iniciar React en otra terminal

```powershell
cd "C:\ruta\TP_Cine_API\frontend"
npm.cmd install
npm.cmd run dev
```

Abrir `http://localhost:5173`.

### Iniciar Swing en otra terminal

```powershell
cd "C:\ruta\TP_Cine_API"
mvn.cmd compile exec:java
```

El backend debe continuar ejecutándose mientras se usan React o Swing. La web del espectador se abre en `5173`; `8080` es la API, no el frontend anterior. El personal opera únicamente en Swing. Para advertencias, solución de errores y prueba cruzada entre las interfaces, seguir [GUIA_EJECUCION.md](../GUIA_EJECUCION.md).

## 27. Reparto sugerido para estudiar en equipo

### Santiago: backend e integración

- `api/client.js` y `ApiClient.java`.
- Flujo controller -> service -> repository.
- Compra, reservas, tickets, sesión y reglas de negocio.
- Explicar por qué ambas interfaces comparten la misma API.

### Antonella: React y experiencia visual

- `main.jsx`, rutas y `SiteLayout`.
- Páginas, componentes, props, Context y hooks.
- Flujo de compra web.
- CSS, responsive y modo oscuro.

### Martín: datos y pruebas

- Entidades, relaciones, enums y repositories.
- Scripts de `database/`.
- Casos normales y casos de error.
- Persistencia de compras, reservas y auditoría.

### Lucas: Swing e integración operativa

- `CineDesktopApplication`, `LoginFrame`, `PrincipalFrame`, `DuenioPanel` y `EmpleadoPanel`.
- `AdministracionPanel` y editor de salas.
- Validación por QR, consumos, salas, butacas y avisos.
- Diferencias de permisos entre Dueño y Empleado.

Todos deberían conocer el recorrido completo, aunque cada uno profundice una sección.

## 28. Guion corto para la exposición

1. Film Zone tiene una interfaz React para espectadores y una interfaz Swing para el personal.
2. Ambas consumen una API REST desarrollada con Spring Boot.
3. El backend separa controllers, services, repositories y entidades.
4. React separa sesión, catálogo público, historial privado y compra mediante cuatro Context.
5. Una compra no se guarda hasta confirmar el pago; antes hay un borrador y reservas temporales.
6. Swing identifica automáticamente el rol al iniciar sesión.
7. El dueño administra el cine y el empleado realiza tareas operativas.
8. Los tickets incorporan QR para validar entradas y entregar consumos.
9. MySQL conserva usuarios, cartelera, salas, ventas, tickets y auditoría.

## 29. Preguntas posibles sobre Swing

### ¿Swing se conecta directamente a MySQL?

No. Swing llama a la API mediante `ApiClient`. El backend valida y accede a MySQL mediante services y repositories.

### ¿Cómo se distingue al dueño del empleado?

`LoginFrame` inicia sesión y consulta el rol. `PrincipalFrame` selecciona `DuenioPanel` o `EmpleadoPanel` sin pedir al usuario que elija su perfil.

### ¿Por qué se usa `SwingWorker`?

Para ejecutar las peticiones HTTP fuera del hilo de interfaz y evitar que la ventana quede congelada.

### ¿Cómo se actualizan las tablas?

Los métodos `cargarSalas` y `cargarFunciones` están en `SalasModulo`; `cargarTickets` y `cargarConsumos`, en `VentasModulo`. Consultan los clientes API, limpian su propio `DefaultTableModel` y agregan las filas recibidas. `PanelPersonalBase.recargar` solo delega a los módulos.

### ¿Por qué hay un `ApiClient` distinto para React y Swing?

Porque usan tecnologías diferentes. React utiliza `fetch` en JavaScript y Swing utiliza `HttpClient` en Java, pero ambos llaman a los mismos endpoints.

### ¿Dónde están los permisos reales?

En el backend. Ocultar un botón mejora la interfaz, pero controllers, interceptores y services deben impedir una acción no autorizada.

## 30. Lista final de repaso

- Poder dibujar el diagrama React/Swing -> API -> service -> repository -> MySQL.
- Saber iniciar backend, React y Swing.
- Explicar la responsabilidad de cada carpeta.
- Recorrer una compra desde la película hasta el ticket.
- Explicar reserva temporal y prevención de doble venta.
- Recorrer una validación QR y una entrega de consumos.
- Explicar el inicio de sesión y la diferencia entre Dueño y Empleado.
- Saber por qué React usa Context y Swing usa modelos de tabla.
- Saber por qué ninguna interfaz aplica por sí sola las reglas definitivas.
- Mostrar dónde se configura el responsive y dónde se construyen los paneles Swing.

## 31. Formularios administrativos de Swing

Los formularios del dueño están separados en `src/main/java/desktop/duenio/dialogos/`:

| Archivo | Responsabilidad |
| --- | --- |
| `CategoriaDialog.java` | Captura el nombre de una categoría. |
| `PeliculaDialog.java` | Captura película, categoría, descripción y portada. |
| `FuncionDialog.java` | Captura fecha, horario, película, sala, formato, idioma y precio. |
| `ProductoDialog.java` | Captura los datos e imagen de un producto del candy. |
| `SalaDialog.java` | Abre y valida el editor de distribución de una sala. |
| `EditorDistribucionSala.java` | Dibuja la pantalla y la matriz dinámica de butacas. |
| `SelectorImagenLocal.java` | Lee imágenes locales y construye su vista previa. |
| `FormularioDialogo.java` | Reúne la estructura visual común de los formularios. |
| `Opcion.java` | Representa una opción con `id` y nombre en los combos. |

Cada diálogo devuelve un `record Datos`. El módulo de su dominio conserva la coordinación: captura el registro seleccionado, abre el diálogo, arma la petición y llama al cliente correspondiente. Ese cliente utiliza `ApiClient`. `AdministracionPanel` coordina únicamente la carga conjunta y el ciclo de vida de los cinco CRUD.

## 32. Diálogos del personal

Los diálogos de uso común para dueño y empleado están en `src/main/java/desktop/comun/dialogos/`:

| Archivo | Responsabilidad |
| --- | --- |
| `EstadoSalaDialog.java` | Captura el estado de la sala, la descripción y el destinatario del aviso. |
| `EstadoButacaDialog.java` | Confirma la butaca a marcar fuera de servicio y captura un comentario opcional y el destinatario. |
| `SelectorDestinatario.java` | Muestra los empleados activos y la opción de avisar a todos. Es un apoyo interno de los dos diálogos. |

Los diálogos exclusivos del dueño están en `src/main/java/desktop/duenio/dialogos/`:

| Archivo | Responsabilidad |
| --- | --- |
| `EmpleadoDialog.java` | Captura nombre, apellido, usuario y contraseña repetida. No permite elegir el rol de dueño. |
| `RestablecerContraseniaDialog.java` | Identifica al empleado seleccionado y captura dos veces la nueva contraseña. |

`FormularioDialogo.contraseniasValidas` reúne la comprobación visual de coincidencia y mínimo de cuatro caracteres. El backend sigue siendo quien valida y autoriza definitivamente cada operación.

Los diálogos devuelven `Optional<Datos>`: al confirmar correctamente hay datos; al cancelar, cerrar la ventana o fallar una comprobación no hay resultado y no se envía ninguna petición. Los módulos operativos y los cinco módulos CRUD administrativos coordinan las llamadas a los clientes de dominio, las recargas y los mensajes.

Flujos para estudiar:

```text
Cambiar estado de sala
  -> coordinador consulta /empleados/activos
  -> EstadoSalaDialog devuelve estado, descripción y destinatario
  -> coordinador envía PUT /salas/{id}/estado y recarga las salas

Marcar butaca fuera de servicio
  -> coordinador identifica la butaca y consulta los empleados activos
  -> EstadoButacaDialog devuelve el comentario y el destinatario
  -> coordinador envía PUT /butacas/{id}/fuera-de-servicio y recarga las butacas

Crear empleado o restablecer contraseña
  -> diálogo exclusivo del dueño solicita y comprueba las dos contraseñas
  -> coordinador envía POST /empleados o PUT /empleados/{id}/contrasenia
```

La descripción es obligatoria para una sala con "Problema particular". El comentario de la butaca es opcional. Si se elige "Todos los empleados", el destinatario del resultado es `null` y el coordinador omite `destinatarioEmpleadoId` en la petición, manteniendo el comportamiento de los avisos generales.

## 33. Componentes comunes de Swing

En `src/main/java/desktop/comun/componentes/`:

- `PanelConTabla.java`: encabezado, ayuda, buscador y tabla con desplazamiento. Lo usan los módulos operativos y los CRUD del dueño.
- `BuscadorTabla.java`: campo "Buscar:" y filtro por todas las columnas, sin distinguir mayúsculas ni tildes. Conserva el orden inicial por fecha y horario, por actividad reciente o por ID según la tabla.
- `VisorDetalle.java`: visor HTML no editable para los cuadros de información. No decide qué acciones puede realizar cada rol.
- `MapaButacasPanel.java`: dibuja la pantalla, filas, espacios vacíos, colores y leyenda del plano. Recibe la selección y notifica clic/doble clic. `SalasModulo` decide qué sala mostrar, conserva la selección y ejecuta las operaciones permitidas; el componente visual no conoce endpoints.

En `src/main/java/desktop/comun/util/`:

- `ConfiguradorTablas.java`: crea modelos no editables y configura selección, orden visual, altura y anchos de columnas. Conserva las configuraciones diferenciadas de las tablas operativas y administrativas. `limpiar` vacía el modelo antes de una recarga.
- `DetalleHtml.java`: genera títulos, campos y secciones de los detalles. Escapa caracteres especiales para que los datos se muestren como texto, no como etiquetas HTML.
- `BotonesSwing.java`: construye botones de acción que ejecutan un `Runnable`. Los paneles siguen decidiendo qué acciones mostrar y qué operación ejecutar.
- `FormatoSwing.java`: centraliza los nombres visibles de estados, formato e idioma y su conversión a códigos de la API. Por ejemplo, muestra `2D` en lugar de `DOS_D` sin cambiar lo guardado en el backend.
- `TextoBusqueda.java`: normaliza el texto para comparar sin distinguir mayúsculas ni tildes. Se utiliza tanto en las tablas como en los combos de búsqueda de películas y salas.

Para estudiar una tabla: empezar en el panel del módulo, seguir a `PanelConTabla` y `BuscadorTabla`, y revisar `ConfiguradorTablas` para sus columnas. Para un detalle: el coordinador arma la descripción con `DetalleHtml` y la muestra en `VisorDetalle`. Ninguno de estos componentes conoce endpoints ni se conecta a MySQL.

## 34. Clientes de la API para Swing

`src/main/java/desktop/comun/api/` agrupa las peticiones por tema:

| Archivo | Responsabilidad |
| --- | --- |
| `CarteleraApi.java` | Consultar, crear, editar y eliminar categorías, películas y funciones; consultar entradas activas y modificar horarios. |
| `SalasApi.java` | Salas, distribución, estado de butacas y avisos de sala. |
| `TicketsApi.java` | Buscar tickets por ID o QR, validar entradas, entregar consumos y reembolsar. También consulta y entrega consumos individuales. |
| `PersonalApi.java` | Listar empleados, crear cuentas, activar, desactivar y restablecer contraseñas. |
| `ProductosApi.java` | Consultar y administrar productos del candy. |
| `AuditoriaApi.java` | Consultar la actividad del sistema. |
| `SesionApi.java` | Iniciar, consultar y cerrar la sesión del personal. |

Estas clases son clientes de la API, no los services del backend. Solo seleccionan el método y la ruta HTTP y delegan en `ApiClient`. No acceden a repositories, no conocen componentes Swing y no deciden permisos ni reglas de negocio.

Todos reciben la misma instancia de `ApiClient` creada al iniciar la aplicación. Así comparten la cookie del usuario, incluso entre el login, los distintos módulos y el cierre de sesión. Los errores y las interrupciones se propagan al coordinador, cuyo `SwingWorker` mantiene la interfaz responsive y muestra los avisos existentes.

Ejemplo para estudiar la entrega de consumos:

```text
ConsumosPanel: botón o lectura del QR
  -> VentasModulo: identifica el ticket y solicita confirmación cuando corresponde
  -> TicketsApi.entregarConsumos(ticketId): define PUT /tickets/{id}/entregar-consumos
  -> ApiClient: envía la petición con la cookie de sesión
  -> controller y service del backend: autorizan y procesan la entrega
  -> VentasModulo: recarga consumos y tickets
```

Para cambiar un endpoint de Swing, buscarlo en estos clientes. Para cambiar un mensaje o la navegación, revisar el panel o coordinador. Para cambiar una regla de negocio, revisar el service del backend. `TicketsApi.buscarPorQr` también centraliza la codificación del código antes de incluirlo en la URL.

## 35. Estado y coordinación por módulo

Los módulos administran los datos temporales de su interfaz: tablas, selección actual, respuestas recibidas y operaciones. Los `paneles` construyen los controles; los `dialogos` capturan datos; los clientes `api` envían peticiones. Ninguna de estas clases reemplaza a los services del backend.

| Archivo dentro de `src/main/java/desktop/` | Estado y responsabilidad |
| --- | --- |
| `comun/modulos/SalasModulo.java` | Tablas de salas y funciones, filtro y selección de butacas, permisos y cambios de estado. Conserva los horarios para los detalles y delega el plano en `MapaButacasPanel`. |
| `comun/modulos/VentasModulo.java` | Tablas de tickets y consumos, caché de tickets, ticket seleccionado y campos QR. Coordina la validación, entrega y reembolso con sus recargas. |
| `empleado/modulos/AvisosModulo.java` | Botón de avisos, consulta periódica, historial y marcas de lectura. Su temporizador se detiene al cerrar sesión. |
| `duenio/modulos/PersonalModulo.java` | Tabla y selección de empleados, alta, activación, desactivación y restablecimiento de contraseña. |
| `duenio/modulos/ActividadModulo.java` | Tabla de actividad, colores por perfil y descripción amigable de las acciones. |
| `comun/modulos/ModuloSwing.java` | Apoyo común para ejecutar tareas con `SwingWorker`, mostrar detalles y avisos y traducir una fila seleccionada al modelo. No conserva datos de ningún dominio. |

`DuenioPanel` y `EmpleadoPanel` crean sus módulos al abrirse. No comparten modelos, selecciones ni cachés entre ventanas: solo reciben el mismo cliente HTTP de la sesión. `PanelPersonalBase` registra recarga y detención sin conocer tablas ni endpoints. `AdministracionPanel` compone los cinco CRUD, que reciben un mismo `DatosAdministracion` dentro de esa ventana.

Tickets y consumos permanecen juntos en `VentasModulo` porque validar, entregar o reembolsar puede requerir actualizar ambas tablas. Validar una entrada no entrega productos: la advertencia por consumos pendientes y la entrega separada se conservan.

Al cerrar sesión, `detener()` marca los módulos como inactivos y detiene el temporizador de avisos. Una petición ya enviada puede terminar en el servidor, pero su respuesta no debe actualizar la interfaz cerrada ni abrir un diálogo tardío. Una sesión nueva crea instancias nuevas con su propio estado.

Los IDs que identifican el destino de una acción se capturan antes de esperar una consulta o abrir un formulario. No se vuelve a buscar el registro usando la posición de una fila que puede cambiar durante una recarga. `FuncionesModulo.formulario` conserva el ID y un `FuncionDialog.Datos` antes de consultar entradas vendidas. `CrudModulo` conserva el registro incluso mientras está abierto el detalle con sus acciones.

En `VentasModulo`, validar manualmente o reembolsar requiere seleccionar un ticket. Leer un QR de Consumos no elige un ticket para esas acciones; al desaparecer la selección de la tabla, también se limpia su ID. El reembolso conserva el ID mostrado en la confirmación aunque la tabla se actualice mientras el cuadro está abierto.

Orden sugerido para estudiar un módulo: abrir el panel del rol, localizar la instancia del módulo y su sección, revisar los campos privados del módulo, seguir el callback del botón y terminar en el cliente API. Para entender la ejecución asíncrona, revisar `ModuloSwing.ejecutar`.

## 36. Recorridos guiados por archivo

Este apartado sirve para practicar sobre el código abierto en IntelliJ, no para memorizar todos los archivos. Usar "Ir a declaración" sobre una llamada y "Buscar usos" para ver quién la invoca.

### A. Encontrar una pantalla React

1. Abrir `frontend/src/routes/AppRoutes.jsx` y buscar la URL que se quiere explicar.
2. Abrir la página importada y distinguir los Context, estado local y componentes que utiliza.
3. Seguir la acción de un botón hasta su handler, hook o Context.
4. Si hay HTTP, terminar en `frontend/src/api/client.js`.
5. Para la apariencia, buscar la clase JSX en `frontend/src/styles/` y revisar después las media queries.

Ejercicio: explicar por qué `/pelicula/3` muestra una película distinta a `/pelicula/4` sin tener dos componentes diferentes.

### B. Seguir una compra sin confundir borrador y persistencia

```text
frontend/src/pages/PeliculaPage.jsx
  -> context/PurchaseContext.jsx: startMoviePurchase
  -> pages/compra/CompraButacasPage.jsx: petición de reserva y toggleSeat
  -> hooks/useSeatReservationHeartbeat.js: consulta y vencimiento
  -> pages/compra/CompraCandyPage.jsx: productos y candyVisitado
  -> pages/compra/CompraPagoPage.jsx: protección y composición
  -> hooks/useCheckout.js: handlePay y petición final
  -> api/client.js: POST /compras
  -> components/compra/PurchaseSuccess.jsx: resultado del servidor
  -> components/tickets/TicketDetails.jsx: datos, QR y PDF
```

Preguntas para contestar mirando esos archivos:

- ¿Dónde se guarda el borrador? En `PurchaseContext` y `sessionStorage`.
- ¿Seleccionar una butaca ya crea una entrada? No; crea una reserva temporal mediante la API.
- ¿El contador mantiene la reserva para siempre? No; consulta su vencimiento, no lo extiende.
- ¿Dónde se confirma la compra? En `useCheckout.handlePay`, mediante la API; el backend realiza la operación persistente.
- ¿El subtotal del carrito es el precio definitivo? No. Es orientativo; el backend valida y calcula la compra, y el resultado contiene el total confirmado.
- ¿El frontend procesa dinero real? El flujo actual registra compras con métodos de pago de prueba; no integra una pasarela bancaria.

Para seguir el otro lado, abrir `src/main/java/controller/ventas/CompraController.java` y `src/main/java/service/ventas/CompraService.java`. La lógica del service no se traslada a React ni a Swing.

### C. Explicar las extracciones de React

| Antes de entrar al detalle | Archivos en los que se reparte ahora |
| --- | --- |
| Estructura del sitio | `SiteLayout` compone; `HeaderActions` agrupa controles; `UserMenu` maneja cuenta; `ActiveCart` maneja carrito; `ReservationExpiredNotice` presenta avisos. Están en `frontend/src/components/layout/`. |
| Pago | `CompraPagoPage` compone; `useCheckout` coordina el pago; `CheckoutSummary` conecta el resumen; `MembershipCheckout` consulta y presenta la cotización; `PurchaseSuccess` muestra el resultado. |
| Autenticación lateral | `PurchaseAuthDrawer` coordina y conserva el formulario compartido; `LoginForm` y `RegisterForm` reciben props; `PasswordRecoveryFlow` conserva las etapas de recuperación y llama a la API; `EmailVerification` presenta la confirmación. |
| Estilos | `global.css` importa; `theme.css` define variables; `base.css` reglas generales; `layout/`, `pages/` y `components/` agrupan reglas; `responsive.css` importa en orden las adaptaciones de `responsive/`. |

La división no significa que todo sea puramente visual: un hook coordina efectos y estado, y un flujo como recuperación puede tener estado local. La regla es poder explicar una responsabilidad concreta por archivo.

### D. Seguir una acción en Swing

Ejemplo de edición de película, con rutas desde `src/main/java/desktop/`:

```text
duenio/DuenioPanel.java: registra la sección Películas
  -> duenio/paneles/PeliculasPanel.java: controles y callback
  -> duenio/modulos/PeliculasModulo.java: registro seleccionado y coordinación
  -> duenio/dialogos/PeliculaDialog.java: Optional<Datos>
  -> duenio/modulos/PeliculasModulo.java: cuerpo de la petición y tarea asíncrona
  -> comun/api/CarteleraApi.java: ruta y método HTTP
  -> comun/ApiClient.java: JSON y cookie de sesión
  -> backend: autorización y guardado
  -> AdministracionPanel: carga conjunta -> cada módulo actualiza su modelo
```

Para una operación de tickets, el coordinador es `comun/modulos/VentasModulo.java`; para salas o butacas es `comun/modulos/SalasModulo.java`. `PanelPersonalBase` solo cambia la vista y delega recarga/detención.

Ejercicio: poner un punto de interrupción en el callback de Validar por QR y seguir la consulta, la advertencia por productos pendientes y la llamada a `TicketsApi.validarEntradas`. Después verificar que los productos sigan pendientes hasta realizar la entrega en Consumos.

### E. Identificar el hilo y el ciclo de vida de Swing

1. `CineDesktopApplication` abre el login en el EDT.
2. `LoginFrame` y los coordinadores ejecutan HTTP en segundo plano con `SwingWorker`.
3. `ModuloSwing.ejecutar` recibe la tarea y el callback de respuesta; actualiza la vista desde `done`, en el EDT.
4. `PrincipalFrame` delega la detención al panel del rol al cerrar sesión o salir.
5. `PanelPersonalBase.detener` detiene cada módulo; `AvisosModulo` detiene el temporizador. Los coordinadores ignoran respuestas posteriores al cierre.

Detener una interfaz no revierte una operación ya enviada al servidor. Tampoco cambiar de sección equivale a cerrar el módulo: `CardLayout` conserva las vistas.

### F. Elegir el archivo correcto para un cambio

| Cambio solicitado | Lugar para empezar |
| --- | --- |
| Orden del menú web o acciones de encabezado | `frontend/src/components/layout/SiteLayout.jsx` y `HeaderActions.jsx`. |
| Qué conserva el carrito al navegar | `frontend/src/context/PurchaseContext.jsx`. |
| Cómo se muestra la compra confirmada | `frontend/src/components/compra/PurchaseSuccess.jsx` y `TicketDetails.jsx`. |
| Campos del login o registro web | `frontend/src/components/cuenta/auth/`; para coordinación, `PurchaseAuthDrawer.jsx`. |
| Ancho, color o tamaño solo en celular | Hoja correspondiente en `frontend/src/styles/` y `responsive.css`. |
| Secciones que ve cada rol de Swing | `src/main/java/desktop/duenio/DuenioPanel.java` y `empleado/EmpleadoPanel.java`. |
| Columna, ancho, búsqueda o etiqueta de una tabla Swing | `desktop/comun/util/ConfiguradorTablas.java`, `TextoBusqueda.java`, `FormatoSwing.java` y `comun/componentes/BuscadorTabla.java`. |
| Endpoint utilizado por Swing | Clase del dominio en `desktop/comun/api/`. |
| Regla de compra, permiso o disponibilidad | Backend en `src/main/java/service/` y configuración de autorización; no los componentes visuales. |

### G. Lista de comprobación para estudiar en equipo

- Seguir una ruta desde el inicio hasta la página o sección sin adivinar archivos.
- Distinguir props, estado local, Context, modelo de tabla y datos persistentes.
- Identificar quién presenta, quién coordina y quién envía HTTP.
- Explicar qué hace el sistema al cancelar un formulario, fallar una petición o vencer una reserva.
- Mostrar por qué ocultar un botón no sustituye una comprobación de permisos en el backend.
- Recorrer una compra React y usar su QR en Swing, separando validación de entradas y entrega de productos.
- Explicar qué se limpia al cerrar sesión y por qué un resultado tardío no debe modificar una ventana cerrada.

Este documento describe el código; no constituye por sí solo una prueba funcional. Las comprobaciones realizadas con MySQL, Dueño, Empleado y navegador, sus correcciones y límites están en [Verificación de interfaces](../VERIFICACION_INTERFACES.md).
