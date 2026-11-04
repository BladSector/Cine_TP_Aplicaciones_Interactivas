# Frontend: React y Swing

Mapa único de las dos interfaces: carpetas, archivos y funciones principales. Las reglas de negocio
y la base de datos se explican en [GUIA_BACKEND.md](GUIA_BACKEND.md). Todas las rutas parten de la
raíz del repositorio.

React es la web del espectador. Swing es el cliente de escritorio del dueño y del empleado. Ambos
envían peticiones HTTP a Spring Boot; ninguno accede directamente a MySQL. Un archivo Java dentro de
`desktop/` pertenece a la interfaz, no a los services del backend.

## Índice

- [React: estructura y estado](#react-estructura-y-estado)
- [React: páginas](#react-páginas)
- [React: componentes](#react-componentes)
- [React: estilos y recursos](#react-estilos-y-recursos)
- [Swing: estructura y comunicación](#swing-estructura-y-comunicación)
- [Swing: módulos y vistas compartidas](#swing-módulos-y-vistas-compartidas)
- [Swing: dueño y empleado](#swing-dueño-y-empleado)
- [Recorridos para estudiar](#recorridos-para-estudiar)
- [Dónde modificar cada cosa](#dónde-modificar-cada-cosa)

## React: estructura y estado

```text
frontend/
  index.html, package.json, package-lock.json, vite.config.js
  src/
    main.jsx, App.jsx       Arranque y composición
    routes/                URL -> página
    pages/                 Pantallas completas
    components/            Partes visuales reutilizables
    context/               Estado compartido
    hooks/                 Lógica reutilizable con estado/efectos
    api/                   Peticiones al backend
    utils/                 Formatos y funciones auxiliares
    styles/                CSS por tema, sección y tamaño de pantalla
    assets/                Recursos importados por React
```

Un componente es una función que devuelve JSX. Su nombre normalmente coincide con el archivo. Las
fichas destacan los auxiliares y callbacks importantes, sin enumerar cada setter de `useState` ni
cada manejador trivial de un campo.

`useState` conserva estado local; `useEffect` sincroniza peticiones, eventos y temporizadores y
permite limpiarlos al salir; `useContext` consume estado compartido. `useMemo` conserva un cálculo
derivado y `useCallback` una referencia de función según sus dependencias. No guardan datos en MySQL
por sí mismos.

### Entrada y configuración

**[frontend/index.html](frontend/index.html)**

HTML inicial y nodo donde se monta React; título/metadatos, no una pantalla de compra.

**[frontend/package.json](frontend/package.json)**

Dependencias y scripts de Vite.

**[frontend/package-lock.json](frontend/package-lock.json)**

Versiones exactas resueltas por npm; se actualiza con las dependencias, no se escribe como un
componente.

**[frontend/vite.config.js](frontend/vite.config.js)**

Plugin React, servidor y proxy de API/imágenes. No define las rutas de navegación de la web.

**[frontend/src/main.jsx](frontend/src/main.jsx)**

Monta React, aplica el tema inicial e instala Router y proveedores de Context. Importa los estilos
globales.

**[frontend/src/App.jsx](frontend/src/App.jsx)**

Compone el comportamiento común de navegación/desplazamiento y las rutas.

**[frontend/src/routes/AppRoutes.jsx](frontend/src/routes/AppRoutes.jsx)**

Relaciona URL y página; usa `lazy` para nueve pantallas secundarias. `LegacyCandyRedirect` conserva
enlaces antiguos de kiosco.

`/pelicula/3` es una ruta de React; `/peliculas` es un endpoint de Spring. `SiteLayout` contiene el
encabezado/footer común y un `Outlet` donde se dibuja la página. `Suspense` muestra carga durante el
`import()` y `RouteErrorBoundary` ofrece recargar si falla el contenido de esa ruta. La cartelera se
carga al inicio.

### `frontend/src/api/`, `context/`, `hooks/` y `utils/`

**[api/client.js](frontend/src/api/client.js)**

`request`, `download` y `errorMessage` centralizan HTTP, cookies (`credentials: 'include'`),
descargas y errores. `api` expone operaciones como `loginEspectador`, `getFunciones`,
`reservarButaca`, `comprar`, `comprarCandy`, `cancelarCompra` y `getTicketPdf`. Aquí se cambia
el endpoint/cuerpo de una petición.

**[context/AuthContext.jsx](frontend/src/context/AuthContext.jsx)**

`AuthProvider` conserva sesión/perfil; `bootstrap`, `refreshSpectator`, `loginSpectator`,
`registerSpectator`, `verifySpectatorEmail` y `logout`. `useAuth` consume ese estado. El ID local
ayuda a restaurar datos; no sustituye la autenticación por cookie del backend.

**[context/CatalogContext.jsx](frontend/src/context/CatalogContext.jsx)**

`CatalogProvider`, `reload` y `useCatalog`: catálogo público de categorías, películas, funciones,
salas, butacas y productos. Descarta respuestas de recargas anteriores. No carga tickets privados.

**[context/AccountContext.jsx](frontend/src/context/AccountContext.jsx)**

`AccountProvider`, `reloadTickets` y `useAccount`: historial privado, carga, error y reintento.
Vacía datos al cerrar/cambiar sesión e ignora respuestas antiguas.

**[context/PurchaseContext.jsx](frontend/src/context/PurchaseContext.jsx)**

`PurchaseProvider`/`usePurchase`: borrador `draft` en `sessionStorage`. `startMoviePurchase`,
`iniciarCompraCandy` y `startMembership` inician el tipo de compra; `toggleSeat`,
`actualizarConsumo`, `limpiarConsumos`, `marcarCandyVisitado` lo modifican. `cancelActivePurchase`
libera reservas y limpia; `expireSeatReservation` quita selecciones vencidas; `reset` limpia el
borrador.

**[hooks/useCheckout.js](frontend/src/hooks/useCheckout.js)**

`useCheckout` reúne estado/validaciones del pago. `ensurePaymentMethod` obtiene/crea la tarjeta y
`handlePay` arma la compra, solicita la API y procesa éxito/error. El precio definitivo lo calcula
el backend.

**[hooks/useSeatReservationHeartbeat.js](frontend/src/hooks/useSeatReservationHeartbeat.js)**

`checkReservations` consulta cada cinco segundos y actualiza el contador cada segundo. Detecta
vencimiento/pérdida de reserva; consultar no extiende los diez minutos.

**[hooks/useDialog.js](frontend/src/hooks/useDialog.js)**

Manejo común de modales: foco inicial y retorno, Tab/Escape, diálogo superior y bloqueo del scroll.
`requestClose` respeta `closeDisabled` mientras hay una operación.

**[utils/format.js](frontend/src/utils/format.js)**

`money`, funciones `formatDate...`/`formatDay...`, `shortTime`, `todayValue`, `addDays`,
`formatFunction`, `formatLanguage`, `labelProductType`, `paymentIsExpired`, `functionHasStarted`
, `seatLabel` y `normalizeText`: formatos y comparaciones, sin peticiones HTTP.

**[utils/payment.js](frontend/src/utils/payment.js)**

`EMPTY_CARD`, `formatCardNumber`, `formatExpiry` y `cardPreviewNumber`: datos vacíos y formato
compartido de tarjetas. No procesa cobros.

`Auth` responde quién está conectado; `Catalog`, qué ofrece el cine; `Account`, qué compró esa
persona; `Purchase`, qué está preparando ahora. No poner las cuatro responsabilidades en un único
componente.

## React: páginas

### `frontend/src/pages/`

**[CarteleraPage.jsx](frontend/src/pages/CarteleraPage.jsx)**

`/`: filtros, fecha y películas. `handleSelectFunction`/`beginPurchase` abren compra o acceso;
`replaceActivePurchase` confirma reemplazar otra compra.

**[PeliculaPage.jsx](frontend/src/pages/PeliculaPage.jsx)**

`/pelicula/:id`: ficha y selección de fecha/función. `continueToSeats`/`beginSelectedPurchase`
continúan o abren autenticación; `replaceActivePurchase` resuelve una compra previa.

**[CandyPage.jsx](frontend/src/pages/CandyPage.jsx)**

`/candy`: compra solo de productos. `continueToPayment` continúa con los productos seleccionados.
Reutiliza `CandyCatalog`.

**[ClubPage.jsx](frontend/src/pages/ClubPage.jsx)**

`/club`: planes y acciones. `handleMembresia`, `goToPayment`, `adquirir` y `confirmarBaja`
coordinan elección/contratación/baja; muestra condiciones recibidas del servidor.

**[CuentaPage.jsx](frontend/src/pages/CuentaPage.jsx)**

`/cuenta` y fragmentos `#perfil`, `#pagos`, `#entradas`, `#membresia`. `changeMode`,
`handleAuth`, `handleVerify` y `completeRecovery` coordinan los formularios de acceso y las
secciones.

**[AboutPage.jsx](frontend/src/pages/AboutPage.jsx)**

`/nosotros`: información de la empresa y equipo.

**[LegalPage.jsx](frontend/src/pages/LegalPage.jsx)**

`/privacidad` y `/normas`: contenido según el tipo de página.

**[PlaceholderPage.jsx](frontend/src/pages/PlaceholderPage.jsx)**

Páginas informativas temporales de Próximamente/Experiencias; existen como rutas aunque no estén en
el menú principal.

### `frontend/src/pages/compra/`

**[CompraAccesoPage.jsx](frontend/src/pages/compra/CompraAccesoPage.jsx)**

Redirige a la película indicando que debe abrirse el acceso lateral; no contiene otro formulario de
login.

**[CompraButacasPage.jsx](frontend/src/pages/compra/CompraButacasPage.jsx)**

`loadReservations` consulta disponibilidad; `isUnavailable` decide qué se puede seleccionar
visualmente; `handleToggleSeat` reserva/libera en la API antes de actualizar el borrador. El plano
lo dibuja `SeatMap`.

**[CompraCandyPage.jsx](frontend/src/pages/compra/CompraCandyPage.jsx)**

Selección opcional de productos dentro de una compra de entradas. `continueToPayment` registra la
visita al paso y continúa: se visita Candy aunque no se agreguen productos.

**[CompraPagoPage.jsx](frontend/src/pages/compra/CompraPagoPage.jsx)**

Compone formulario, resumen y resultado. Usa `useCheckout` y las variantes `candyOnly`
/`membershipOnly`; no concentra toda la lógica del pago.

**[CompraExitosaPage.jsx](frontend/src/pages/compra/CompraExitosaPage.jsx)**

Contenedor visual de confirmación con `children`, dimensiones y confeti; `drawPopcorn` dibuja la
animación. Lo usa `PurchaseSuccess`, no tiene ruta propia.

**[CompraExitosaPage.css](frontend/src/pages/compra/CompraExitosaPage.css)**

Marco/animación de esa confirmación; el canvas no debe bloquear clics.

Los pasos esperan la carga del catálogo antes de decidir que faltan datos y redirigir. Recargar la
página no debe confundir una petición todavía pendiente con una compra inexistente.

## React: componentes

### `frontend/src/components/layout/`

**[SiteLayout.jsx](frontend/src/components/layout/SiteLayout.jsx)**

Estructura persistente, navegación, menú móvil, `Outlet`, carga/error de ruta, avisos y footer.

**[HeaderActions.jsx](frontend/src/components/layout/HeaderActions.jsx)**

Compone lupa, carrito, tema y usuario; coordina apertura del acceso lateral.

**[HeaderMovieSearch.jsx](frontend/src/components/layout/HeaderMovieSearch.jsx)**

Lupa, input y resultados con portadas. `closeSearch` y `openMovie` cierran/navegan; maneja clic
exterior.

**[ActiveCart.jsx](frontend/src/components/layout/ActiveCart.jsx)**

Carrito al salir del flujo, precio estimado y tiempo. `continuePurchase` retoma; `cancelPurchase`
cancela tras confirmación. Los datos viven en `PurchaseContext`.

**[UserMenu.jsx](frontend/src/components/layout/UserMenu.jsx)**

Cuenta y enlaces; `toggleMenu`, `handleMembership`, `handleLogout`. El cierre de sesión requiere
confirmación.

**[ReservationExpiredNotice.jsx](frontend/src/components/layout/ReservationExpiredNotice.jsx)**

Avisos de compra conservada y reserva vencida; no decide la duración de la reserva.

**[SiteFooter.jsx](frontend/src/components/layout/SiteFooter.jsx)**

Marca, enlaces legales/Sobre nosotros y modal Contacto con datos ficticios.

### `frontend/src/components/cartelera/`

**[AccordionGallery.jsx](frontend/src/components/cartelera/AccordionGallery.jsx)**

Galería dinámica. `measure`/`applyLayout` ajustan distribución; `updateMode`, `handleEnter`,
`handleClick` y `handleKeyDown` coordinan interacción y modo automático.

**[AccordionGallery.css](frontend/src/components/cartelera/AccordionGallery.css)**

Aspecto y transiciones exclusivos de la galería.

**[DateStrip.jsx](frontend/src/components/cartelera/DateStrip.jsx)**

Selector horizontal de días. `scrollDates` usa flechas; `startDrag`/`moveDrag`/`endDrag` permiten
arrastrar; `updateEdges` ajusta extremos.

**[MovieCard.jsx](frontend/src/components/cartelera/MovieCard.jsx)**

Portada clickeable, información y despliegue de funciones de una película. `compactLanguage` abrevia
la etiqueta de idioma.

### `frontend/src/components/compra/`

**[SeatMap.jsx](frontend/src/components/compra/SeatMap.jsx)**

Dibuja pantalla, filas, espacios y butacas con etiquetas/colores. Recibe datos y callback de
selección; no reserva directamente en MySQL.

**[SeatReservationTimer.jsx](frontend/src/components/compra/SeatReservationTimer.jsx)**

Presenta el tiempo restante y la advertencia visual.

**[PurchaseStepper.jsx](frontend/src/components/compra/PurchaseStepper.jsx)**

Pasos, enlaces y estado visual del recorrido.

**[CandyCatalog.jsx](frontend/src/components/compra/CandyCatalog.jsx)**

Productos, fotos, pestañas y cantidades; `buildCatalogItems` agrupa opciones, `popcornFlavor`
distingue sabores y `productDescription` prepara descripciones.

**[OrderSummary.jsx](frontend/src/components/compra/OrderSummary.jsx)**

Resumen reutilizado: película, función, butacas, productos y total; botones de edición/eliminación
con confirmación. Recibe las acciones por props.

**[CheckoutSummary.jsx](frontend/src/components/compra/CheckoutSummary.jsx)**

Conecta los Context y enlaces del pago con `OrderSummary`.

**[MobileCheckoutBar.jsx](frontend/src/components/compra/MobileCheckoutBar.jsx)**

Barra inferior del celular: importe, continuar y detalles desplegables.

**[PaymentMethodSelector.jsx](frontend/src/components/compra/PaymentMethodSelector.jsx)**

Tarjetas guardadas/nueva tarjeta y opción de guardar. `updateCard` modifica campos locales; utiliza
la presentación común `PaymentCard`.

**[ExpiryMonthPicker.jsx](frontend/src/components/compra/ExpiryMonthPicker.jsx)**

Calendario mes/año para vencimiento; `yearFrom` obtiene el año y los manejadores de clic
exterior/Escape cierran el selector.

**[MembershipCheckout.jsx](frontend/src/components/compra/MembershipCheckout.jsx)**

Presentación del plan y `useMembershipCheckout`: obtiene membresía/cotización. Muestra lo que se
cobrará; las reglas están en `MembresiaService`.

**[PurchaseSuccess.jsx](frontend/src/components/compra/PurchaseSuccess.jsx)**

Presenta éxito de compra/membresía y compone el comprobante con `CompraExitosaPage` y
`TicketDetails`.

### `frontend/src/components/cuenta/` y `auth/`

**[PurchaseAuthDrawer.jsx](frontend/src/components/cuenta/PurchaseAuthDrawer.jsx)**

Panel lateral en un portal al `body`; `changeMode`, `handleLogin`, `handleRegister`,
`handleVerify`, `openRecovery` y `completeRecovery` coordinan acceso y continuación.

**[PerfilForm.jsx](frontend/src/components/cuenta/PerfilForm.jsx)**

`handleProfile` actualiza datos; `confirmNewEmail` confirma el nuevo correo. Reutiliza recuperación
para cambiar contraseña por correo simulado.

**[MetodosPagoList.jsx](frontend/src/components/cuenta/MetodosPagoList.jsx)**

Listado/formulario de tarjetas; `handleAddCard`, `handleDeleteCard`, `updateCard` y
`closeCardForm`.

**[MisEntradasList.jsx](frontend/src/components/cuenta/MisEntradasList.jsx)**

Historial de tickets, compras de Candy y apertura del detalle. Lee los tickets privados de
`AccountContext`.

**[MembresiaPanel.jsx](frontend/src/components/cuenta/MembresiaPanel.jsx)**

Plan actual y acciones; `handleMembresia` y `confirmarBaja`.

**[auth/LoginForm.jsx](frontend/src/components/cuenta/auth/LoginForm.jsx)**

Correo/contraseña, error y enlace de recuperación. Recibe por props qué ejecutar al enviar.

**[auth/RegisterForm.jsx](frontend/src/components/cuenta/auth/RegisterForm.jsx)**

Datos personales y doble contraseña; `groupNameFields` adapta la disposición de nombre/apellido.

**[auth/PasswordRecoveryFlow.jsx](frontend/src/components/cuenta/auth/PasswordRecoveryFlow.jsx)**

`requestRecovery` solicita recuperación; `finishRecovery` envía token/contraseña nueva. Incluye
correo simulado; `fixedEmail` evita pedir de nuevo el correo en perfil.

**[auth/EmailVerification.jsx](frontend/src/components/cuenta/auth/EmailVerification.jsx)**

Muestra el correo simulado y ejecuta el callback de confirmación.

Login y registro se comparten entre `CuentaPage` y `PurchaseAuthDrawer`; recuperación también se
reutiliza en `PerfilForm`. La pantalla decide cuándo abrir/cerrar y qué hacer después, sin copiar
los campos en cada lugar.

### `frontend/src/components/tickets/` y `common/`

**[tickets/TicketDetails.jsx](frontend/src/components/tickets/TicketDetails.jsx)**

Entradas, consumos, QR y código manual. `groupEntradas` agrupa la presentación, `downloadPdf`
solicita el archivo, `cancelPurchase` solicita cancelación y `cancellationDeadline` presenta el
límite recibido.

**[common/Modal.jsx](frontend/src/components/common/Modal.jsx)**

Portal y estructura de ventana; delega foco/teclado en `useDialog`.

**[common/ConfirmDialog.jsx](frontend/src/components/common/ConfirmDialog.jsx)**

Confirmación y botones/estado de procesamiento. La operación concreta llega por callback.

**[common/PaymentCard.jsx](frontend/src/components/common/PaymentCard.jsx)**

Apariencia común de la tarjeta en perfil y pago; admite controles recibidos como `children`, sin
llamadas HTTP.

**[common/Loader.jsx](frontend/src/components/common/Loader.jsx)**

Indicador de carga compartido.

**[common/Loader.css](frontend/src/components/common/Loader.css)**

Animación/aspecto del indicador.

**[common/ScrollToTop.jsx](frontend/src/components/common/ScrollToTop.jsx)**

Desplazamiento al cambiar de ruta. No es el único código del proyecto que puede cambiar el scroll.

**[common/RouteErrorBoundary.jsx](frontend/src/components/common/RouteErrorBoundary.jsx)**

Componente de clase: `getDerivedStateFromError` activa el estado de error y `render` ofrece recarga
manual. No reemplaza los errores de peticiones de `client.js`.

## React: estilos y recursos

Buscar primero la clase en el JSX y después su regla CSS. Revisar finalmente las media queries;
agregar otra regla al final de cualquier hoja puede esconder el origen del problema.

### `frontend/src/styles/`

**[global.css](frontend/src/styles/global.css)**

Entrada de estilos y orden de `@import`; no debe concentrar todas las reglas.

**[theme.css](frontend/src/styles/theme.css)**

Variables compartidas y temas claro/oscuro; filtros e intensidad del fondo.

**[base.css](frontend/src/styles/base.css)**

Base del documento, tipografía, formularios y elementos comunes; `body::before` coloca el fondo sin
interceptar clics.

**[layout/header.css](frontend/src/styles/layout/header.css)**

Navegación, lupa, acciones, menú de usuario y carrito.

**[layout/footer.css](frontend/src/styles/layout/footer.css)**

Distribución, márgenes y espaciado del pie.

**[pages/cartelera.css](frontend/src/styles/pages/cartelera.css)**

Filtros, tira de días, películas y funciones desplegadas en cartelera.

**[pages/compra.css](frontend/src/styles/pages/compra.css)**

Ficha de película, opciones de función, plano, resumen y pago. Aquí están tamaños iguales y marco
naranja completo de la función seleccionada.

**[pages/cuenta.css](frontend/src/styles/pages/cuenta.css)**

Secciones/formularios de Mi cuenta.

**[pages/club.css](frontend/src/styles/pages/club.css)**

Planes de membresía y beneficios.

**[pages/about.css](frontend/src/styles/pages/about.css)**

Empresa/equipo en Sobre nosotros.

**[pages/legal.css](frontend/src/styles/pages/legal.css)**

Lectura de políticas y normas.

**[components/cards.css](frontend/src/styles/components/cards.css)**

Tarjetas bancarias/productos y estados de selección.

**[components/modals.css](frontend/src/styles/components/modals.css)**

Modales, fondo, acceso lateral y botones.

**[components/tickets.css](frontend/src/styles/components/tickets.css)**

Historial, comprobante, QR y estados.

**[responsive.css](frontend/src/styles/responsive.css)**

Índice de adaptaciones: importa los diez archivos siguientes en orden.

**[responsive/tablet.css](frontend/src/styles/responsive/tablet.css)**

Ajustes generales hasta 900 px.

**[responsive/mobile-header.css](frontend/src/styles/responsive/mobile-header.css)**

Encabezado/acciones del celular, hasta 640 px.

**[responsive/mobile-cartelera.css](frontend/src/styles/responsive/mobile-cartelera.css)**

Cartelera y filtros del celular.

**[responsive/mobile-pelicula.css](frontend/src/styles/responsive/mobile-pelicula.css)**

Ficha, días y funciones del celular.

**[responsive/mobile-compra.css](frontend/src/styles/responsive/mobile-compra.css)**

Plano y pasos de compra del celular.

**[responsive/mobile-club.css](frontend/src/styles/responsive/mobile-club.css)**

Distribución de membresías del celular.

**[responsive/mobile-candy.css](frontend/src/styles/responsive/mobile-candy.css)**

Catálogo de Candy del celular.

**[responsive/mobile-summary.css](frontend/src/styles/responsive/mobile-summary.css)**

Resumen y barra inferior del celular.

**[responsive/mobile-overlays-footer.css](frontend/src/styles/responsive/mobile-overlays-footer.css)**

Modales, QR y footer del celular.

**[responsive/small-phone.css](frontend/src/styles/responsive/small-phone.css)**

Ajustes adicionales hasta 390 px.

Las hojas `mobile-*` se aplican hasta 640 px; los tres CSS locales de galería, carga y confirmación
están junto a sus componentes y ya aparecen en las fichas anteriores. Conservar el orden de imports
para no cambiar accidentalmente la cascada.

[frontend/src/assets/cinema-background.png](frontend/src/assets/cinema-background.png) es el fondo
importado por Vite. Su encuadre vive en `base.css` y su adaptación al tema en `theme.css`. Las
portadas y fotos de productos servidas por `/images/...` están en
`src/main/resources/static/images/`, en el backend; no dentro del estado de React.

`frontend/node_modules/` contiene dependencias y `frontend/dist/` el build generado. Se
estudia/edita `src/`, no esos directorios. Las variables públicas de Vite y el código del navegador
no son un lugar para guardar contraseñas de MySQL o secretos SMTP.

## Swing: estructura y comunicación

Todas las rutas de esta parte comienzan en `src/main/java/desktop/`.

```text
inicio/                 Entrada y login
comun/                  Ventana, navegación y HTTP
  api/                  Endpoints por tema
  modulos/              Coordinación de acciones y estado de pantalla
  paneles/              Vistas reutilizadas entre roles
  componentes/          Tabla, buscador, plano y visor
  dialogos/             Formularios compartidos de incidencias
  util/                 Formatos, tablas, HTML y botones
duenio/                 Composición del dueño
  modulos/, paneles/, dialogos/
empleado/               Composición del empleado
  modulos/, paneles/
```

### Inicio, ventana y API

**[inicio/CineDesktopApplication.java](src/main/java/desktop/inicio/CineDesktopApplication.java)**

`main` y `configurarApariencia`: inician Swing en el hilo de eventos y crean el login. La dirección
de API se toma de `cine.api.url`.

**[inicio/LoginFrame.java](src/main/java/desktop/inicio/LoginFrame.java)**

`construirVista`, `iniciarSesion` y `mostrarError`: formulario de acceso y apertura de la ventana
del rol recibido.

**[comun/PrincipalFrame.java](src/main/java/desktop/comun/PrincipalFrame.java)**

`crearPanelPersonal`, cabecera/menú, `cerrarSesion` y `salirAplicacion`. Decide entre panel del
dueño/empleado, no contiene todos sus formularios.

**[comun/PanelPersonalBase.java](src/main/java/desktop/comun/PanelPersonalBase.java)**

`agregarGrupo`, `agregarSeccion`, `mostrarSeccion`, `registrarModulo`, `recargar`, `detener`.
Menú y `CardLayout` conservan las secciones y su ciclo de vida.

**[comun/ApiClient.java](src/main/java/desktop/comun/ApiClient.java)**

`get`, `post`, `put`, `delete` delegan en `enviar`; configura `HttpClient`, cookies, JSON,
tiempo de espera y errores.

**[comun/api/SesionApi.java](src/main/java/desktop/comun/api/SesionApi.java)**

`iniciarSesion`, `consultarSesion`, `cerrarSesion`: acceso del personal.

**[comun/api/CarteleraApi.java](src/main/java/desktop/comun/api/CarteleraApi.java)**

Consulta/CRUD de categorías, películas y funciones; `consultarEntradasActivas`/`actualizarHorario`.
Tener el método no autoriza a cualquier rol a usarlo.

**[comun/api/SalasApi.java](src/main/java/desktop/comun/api/SalasApi.java)**

Consulta/distribución/estado de salas, butacas y avisos; `cambiarEstado`,
`marcarButacaFueraDeServicio`, `habilitarButaca`, consultas y lectura de avisos.

**[comun/api/TicketsApi.java](src/main/java/desktop/comun/api/TicketsApi.java)**

`listarTickets`, `buscarPorId`, `buscarPorQr`, `validarEntradas`, `entregarConsumos`,
`reembolsarTicket`, `listarConsumos`, `entregarConsumo`.

**[comun/api/ProductosApi.java](src/main/java/desktop/comun/api/ProductosApi.java)**

`listarProductos`, `crearProducto`, `actualizarProducto`, `eliminarProducto`.

**[comun/api/PersonalApi.java](src/main/java/desktop/comun/api/PersonalApi.java)**

Listar/crear empleados, activar/desactivar y `restablecerContrasenia`.

**[comun/api/AuditoriaApi.java](src/main/java/desktop/comun/api/AuditoriaApi.java)**

`listarActividad` consulta la auditoría del sistema.

Las clases `...Api` eligen URL, método y datos; `ApiClient` realiza el transporte. No son
repositories ni services del backend.

## Swing: módulos y vistas compartidas

### `comun/modulos/` y `comun/paneles/`

**[modulos/ModuloSwing.java](src/main/java/desktop/comun/modulos/ModuloSwing.java)**

Base de coordinadores. `consultar` realiza lecturas versionadas; `ejecutar` operaciones;
`mostrarDetalle`/`mostrarDetalleConAccion` presentan información; `detener` evita actualizaciones
tardías.

**[modulos/SalasModulo.java](src/main/java/desktop/comun/modulos/SalasModulo.java)**

`cargarSalas`, `cargarFunciones`, `cargarButacas` y `actualizarMapaButacas` coordinan vistas
operativas. `mostrarFormularioEstadoSala`, `mostrarFormularioButacaFueraServicio` y
`habilitarButacaFueraServicio` gestionan incidencias/estado según rol.

**[modulos/VentasModulo.java](src/main/java/desktop/comun/modulos/VentasModulo.java)**

`cargarTickets`/`cargarConsumos`, `leerQrAcceso`, `validarEntradasManualmente`,
`entregarConsumosPorQr`, `entregarConsumosManualmente`, `reembolsarTicket`. Confirma acciones y
advierte si quedan productos pendientes al ingresar.

**[paneles/ButacasPanel.java](src/main/java/desktop/comun/paneles/ButacasPanel.java)**

Selector de sala, plano, controles y botones según rol; informa acciones al módulo por callbacks.

**[paneles/TicketsPanel.java](src/main/java/desktop/comun/paneles/TicketsPanel.java)**

QR, buscador, tabla y botones de entradas/reembolso. Informa selección y también `null` cuando se
limpia.

**[paneles/ConsumosPanel.java](src/main/java/desktop/comun/paneles/ConsumosPanel.java)**

Campo QR, tabla y botones de entrega/recarga de productos.

El panel construye controles; el módulo decide qué consultar y qué acción ejecutar. `ModuloSwing`
usa `SwingWorker`: `doInBackground` hace HTTP y `done` actualiza Swing en el EDT. No hacer una
petición lenta directamente dentro del evento de un botón.

### `comun/componentes/`, `dialogos/` y `util/`

**[componentes/PanelConTabla.java](src/main/java/desktop/comun/componentes/PanelConTabla.java)**

Contenedor de título, ayuda, buscador y tabla con desplazamiento.

**[componentes/BuscadorTabla.java](src/main/java/desktop/comun/componentes/BuscadorTabla.java)**

`conectarFiltro` y `configurarOrdenInicial`: búsqueda literal/orden de filas.

**[componentes/VisorDetalle.java](src/main/java/desktop/comun/componentes/VisorDetalle.java)**

Visor HTML de solo lectura para cuadros de información. No es el frontend web.

**[componentes/MapaButacasPanel.java](src/main/java/desktop/comun/componentes/MapaButacasPanel.java)**

`mostrar`/`mostrarSinSala` dibujan pantalla, filas, butacas, huecos y leyenda; `colorEstado` elige
colores. Emite clics sin hacer HTTP ni guardar estados.

**[dialogos/EstadoSalaDialog.java](src/main/java/desktop/comun/dialogos/EstadoSalaDialog.java)**

`mostrar`: estado, descripción y destinatario; exige descripción para Problema particular.

**[dialogos/EstadoButacaDialog.java](src/main/java/desktop/comun/dialogos/EstadoButacaDialog.java)**

`mostrar`: incidencia de butaca con comentario opcional y destinatario.

**[dialogos/SelectorDestinatario.java](src/main/java/desktop/comun/dialogos/SelectorDestinatario.java)**

Control compartido para seleccionar empleado activo o aviso a todos.

**[util/ConfiguradorTablas.java](src/main/java/desktop/comun/util/ConfiguradorTablas.java)**

`tabla`, `tablaAdministracion`, `modelo`, `ordenarColumnas` y `configurarAnchos`: modelos no
editables, selección y dimensiones. `limpiar` vacía filas.

**[util/DetalleHtml.java](src/main/java/desktop/comun/util/DetalleHtml.java)**

`inicioHtml`, `campoHtml`, `tituloHtml`, `seccionHtml`, `itemHtml` y `escaparHtml`: detalle
legible y datos escapados.

**[util/FormatoSwing.java](src/main/java/desktop/comun/util/FormatoSwing.java)**

`nombreFormato`/`nombreIdioma`/`nombreEstado...` muestran etiquetas; `codigoFormato`
/`codigoIdioma`/`codigoEstadoSala` convierten hacia la API.

**[util/TextoBusqueda.java](src/main/java/desktop/comun/util/TextoBusqueda.java)**

`normalizarBusqueda` ignora mayúsculas y tildes para comparar; no modifica los datos guardados.

**[util/BotonesSwing.java](src/main/java/desktop/comun/util/BotonesSwing.java)**

`boton` construye un botón común que ejecuta el `Runnable` recibido.

## Swing: dueño y empleado

### `duenio/` y `duenio/modulos/`

**[DuenioPanel.java](src/main/java/desktop/duenio/DuenioPanel.java)**

Crea módulos y grupos/secciones del menú del dueño, y registra recarga/detención.

**[AdministracionPanel.java](src/main/java/desktop/duenio/AdministracionPanel.java)**

Compone cinco CRUD y carga sus datos compartidos; `recargar` y `detener`. Expone paneles de
categorías/películas/salas/funciones/productos sin otro nivel de pestañas.

**[modulos/CrudModulo.java](src/main/java/desktop/duenio/modulos/CrudModulo.java)**

Base de los cinco CRUD: `nuevo`, `mostrarDetalle`, `guardar`, `confirmarEliminacion`,
`aplicarDatos`, `recargar`. Delega datos/formulario/descripcion en las subclases.

**[modulos/DatosAdministracion.java](src/main/java/desktop/duenio/modulos/DatosAdministracion.java)**

Datos de la carga conjunta; `vacios`, `opcionesCategorias`, `opcionesPeliculas`, `opcionesSalas`
preparan listas/combos. No es una entidad de MySQL.

**[modulos/CategoriasModulo.java](src/main/java/desktop/duenio/modulos/CategoriasModulo.java)**

Implementa `registros`, `agregarFila`, `formulario`, `eliminarRegistro` y `descripcion` de
categorías y sus películas asociadas.

**[modulos/PeliculasModulo.java](src/main/java/desktop/duenio/modulos/PeliculasModulo.java)**

Los mismos puntos del CRUD para películas, portada y funciones/salas asociadas.

**[modulos/FuncionesModulo.java](src/main/java/desktop/duenio/modulos/FuncionesModulo.java)**

CRUD de programación; `mostrarFormulario` consulta entradas activas y advierte antes de editar.

**[modulos/SalasAdministracionModulo.java](src/main/java/desktop/duenio/modulos/SalasAdministracionModulo.java)**

CRUD de sala/distribución y `cambiarEstado`; detalle de funciones programadas. No es el coordinador
del mapa operativo `SalasModulo`.

**[modulos/ProductosModulo.java](src/main/java/desktop/duenio/modulos/ProductosModulo.java)**

CRUD de productos, descripción e imagen.

**[modulos/PersonalModulo.java](src/main/java/desktop/duenio/modulos/PersonalModulo.java)**

`cargarEmpleados`, `mostrarFormularioEmpleado`, `cambiarEstadoEmpleado`,
`mostrarRestablecimientoContrasenia`.

**[modulos/ActividadModulo.java](src/main/java/desktop/duenio/modulos/ActividadModulo.java)**

`cargarAuditorias`, `nombrePerfilAuditoria`, `nombreAccionAuditoria`, `detalleActividad`: tabla
amigable y diferenciación de dueño/empleado.

### `duenio/paneles/`

Estos archivos contienen la disposición visual y callbacks, no las reglas de negocio.

**[CrudAdministracionPanel.java](src/main/java/desktop/duenio/paneles/CrudAdministracionPanel.java)**

Tabla común, nuevo, recarga y apertura de detalle por doble clic.

**[CategoriasPanel.java](src/main/java/desktop/duenio/paneles/CategoriasPanel.java)**

Categorías.

**[PeliculasPanel.java](src/main/java/desktop/duenio/paneles/PeliculasPanel.java)**

Películas.

**[FuncionesAdminPanel.java](src/main/java/desktop/duenio/paneles/FuncionesAdminPanel.java)**

Funciones del dueño.

**[SalasAdminPanel.java](src/main/java/desktop/duenio/paneles/SalasAdminPanel.java)**

Salas del dueño.

**[ProductosPanel.java](src/main/java/desktop/duenio/paneles/ProductosPanel.java)**

Productos del Candy.

**[EmpleadosPanel.java](src/main/java/desktop/duenio/paneles/EmpleadosPanel.java)**

Alta, contraseña, activación/desactivación y recarga del personal.

**[ActividadPanel.java](src/main/java/desktop/duenio/paneles/ActividadPanel.java)**

Tabla de auditoría y referencia visual de perfiles.

### `duenio/dialogos/`

Los formularios devuelven `Optional<Datos>`: si se cancela, el módulo no envía la modificación. Los
`record Datos` agrupan campos de interfaz; no son nuevas entidades JPA.

**[CategoriaDialog.java](src/main/java/desktop/duenio/dialogos/CategoriaDialog.java)**

`mostrar`: nombre de categoría.

**[PeliculaDialog.java](src/main/java/desktop/duenio/dialogos/PeliculaDialog.java)**

`mostrar`: título, duración, descripción, categoría y portada local.

**[FuncionDialog.java](src/main/java/desktop/duenio/dialogos/FuncionDialog.java)**

`mostrar`: fecha, horario, búsqueda de película/sala, formato, idioma y precio.

**[SalaDialog.java](src/main/java/desktop/duenio/dialogos/SalaDialog.java)**

`mostrarNueva`, `mostrarEdicion`, `mostrar`: nombre, editor de distribución y confirmación.

**[EditorDistribucionSala.java](src/main/java/desktop/duenio/dialogos/EditorDistribucionSala.java)**

`nuevo`/`desde` preparan el plano; `cambiarDimensiones` y `dibujarMapa` ajustan filas/posiciones,
`pintarButaca` representa cada lugar editable.

**[ProductoDialog.java](src/main/java/desktop/duenio/dialogos/ProductoDialog.java)**

`mostrar`: nombre, descripción, precio, tipo, tamaño e imagen; el formulario actual no tiene stock.

**[EmpleadoDialog.java](src/main/java/desktop/duenio/dialogos/EmpleadoDialog.java)**

`mostrar`: nueva cuenta de empleado y contraseña repetida; no ofrece otro dueño.

**[RestablecerContraseniaDialog.java](src/main/java/desktop/duenio/dialogos/RestablecerContraseniaDialog.java)**

`mostrar`: contraseña nueva repetida para el empleado elegido.

**[FormularioDialogo.java](src/main/java/desktop/duenio/dialogos/FormularioDialogo.java)**

`crear`, `confirmar`, `aviso`, `contraseniasValidas`, `conectarBusqueda`, `seleccionarPorId`
/`seleccionarPorNombre`: apoyo visual y de selección para formularios.

**[SelectorImagenLocal.java](src/main/java/desktop/duenio/dialogos/SelectorImagenLocal.java)**

`seleccionar`, `actualizarVistaPrevia`, `mimePorExtension`: selección local, tamaño, codificación
y previsualización de imágenes.

**[Opcion.java](src/main/java/desktop/duenio/dialogos/Opcion.java)**

`record` con ID y etiqueta; `toString` permite mostrar el nombre en el combo conservando su
identificador.

### `empleado/`

**[EmpleadoPanel.java](src/main/java/desktop/empleado/EmpleadoPanel.java)**

Compone Control de acceso, Consumos, Funciones, Salas y Butacas; registra módulos y `botonAvisos`.

**[modulos/AvisosModulo.java](src/main/java/desktop/empleado/modulos/AvisosModulo.java)**

`iniciarMonitoreoAvisos`, `consultarAvisos`, `mostrarHistorialAvisos` y `mostrarAvisos`.
`detener`/`detenerMonitoreoAvisos` paran el temporizador al salir.

**[paneles/FuncionesPanel.java](src/main/java/desktop/empleado/paneles/FuncionesPanel.java)**

Tabla consultable y recarga; no ofrece editar fecha/horario al empleado.

**[paneles/SalasPanel.java](src/main/java/desktop/empleado/paneles/SalasPanel.java)**

Consulta de salas y botón para cambiar estado, además del flujo por detalle.

El empleado puede validar entradas, entregar productos y gestionar incidencias de salas/butacas. No
debe marcar una venta como ocupación física ni liberar una butaca vendida: para mantenimiento usa
fuera de servicio/disponible. Los permisos efectivos se comprueban otra vez en el backend.

## Recorridos para estudiar

### Compra web de entradas y productos

1. `main.jsx` instala proveedores; `AppRoutes` elige `CarteleraPage` o `PeliculaPage` dentro de
   `SiteLayout`.
2. Al elegir función, la página abre `PurchaseAuthDrawer` si hace falta sesión o llama a
   `PurchaseContext.startMoviePurchase` y navega a butacas.
3. `CompraButacasPage.handleToggleSeat` solicita la reserva con `api.reservarButaca`. Si responde
   bien, actualiza `draft.selectedSeats`; `SeatMap` solo dibuja ese estado. Se combinan IDs
   vendidos de `/funciones/{id}/butacas-ocupadas` con reservas activas.
4. `CompraCandyPage` reutiliza `CandyCatalog`. `actualizarConsumo` conserva cantidades y
   `marcarCandyVisitado` permite continuar con o sin productos. No se crea un ticket todavía.
5. `CompraPagoPage` usa `useCheckout`, `PaymentMethodSelector` y `CheckoutSummary`. `handlePay`
   solicita `/compras`; la API valida y guarda. `paymentInProgress` pausa el control visual de
   vencimiento mientras responde el pago, sin cambiar las reglas del servidor.
6. Ante éxito se limpia el borrador y `PurchaseSuccess` muestra `TicketDetails`;
   `AccountContext.reloadTickets` renueva el historial. Ante error se conserva información para
   corregir/reintentar.

Si se sale del flujo, `ActiveCart` permite continuar o cancelar. El heartbeat no renueva reservas
indefinidamente: al vencer, el contexto limpia las selecciones y muestra el aviso. `sessionStorage`
conserva un borrador de esa pestaña, no una compra pagada ni una autorización.

### Acceso y cuenta

`HeaderActions` abre `PurchaseAuthDrawer`; la página de cuenta también usa `LoginForm`
/`RegisterForm`. Sus acciones llaman a `AuthContext` y después a `api/client.js`. La recuperación
se concentra en `PasswordRecoveryFlow`; la verificación muestra un correo simulado. Una nueva
petición o una cuenta distinta no debe recibir el resultado tardío de la anterior: por eso los
Context controlan la vigencia de las cargas y limpian datos privados al salir.

### QR y operación de Swing

`LoginFrame.iniciarSesion` -> `SesionApi` -> `ApiClient` -> respuesta del backend ->
`PrincipalFrame` -> `DuenioPanel` o `EmpleadoPanel`. Al seleccionar una sección,
`PanelPersonalBase` muestra su panel sin crear otro módulo cada vez.

En Tickets: `TicketsPanel` captura QR o selección manual -> `VentasModulo.leerQrAcceso`
/`validarEntradasManualmente` -> `TicketsApi` -> API. El módulo advierte sobre consumos pendientes
antes de validar acceso. En Consumos, el campo QR utiliza `entregarConsumosPorQr`; entregar
productos no reemplaza la validación de entradas. El detalle se consulta en ambos casos y la tabla
se recarga tras la respuesta.

Los IDs se capturan antes de abrir confirmaciones o hacer peticiones: si la tabla cambia mientras
tanto, la acción no debe aplicarse a otra fila. `detener` evita actualizaciones tardías al cerrar
una sección/sesión, pero no revierte una escritura que el servidor ya procesó.

### Editar película o informar incidencia desde Swing

`PeliculasPanel` -> `PeliculasModulo.formulario` -> `PeliculaDialog.mostrar` -> `Optional<Datos>` ->
`CarteleraApi` -> `ApiClient`. Tras guardar, `AdministracionPanel.recargar` actualiza el conjunto
compartido y cada CRUD aplica sus datos. Cancelar el diálogo no hace una petición de modificación.

En una sala: el botón/detalle del panel llama a `SalasModulo` (o `SalasAdministracionModulo` para
dueño), abre `EstadoSalaDialog` y envía la petición con `SalasApi`. El backend cambia estado y
genera avisos. `AvisosModulo` consulta pendientes e historial; marcar leído no elimina la
incidencia.

## Dónde modificar cada cosa

**URL de una página / URL de una petición**

`routes/AppRoutes.jsx`/ `api/client.js` en React; clases `comun/api/` en Swing.

**Encabezado, carrito o footer web**

Componentes `layout/` y CSS `styles/layout/`.

**Tamaño de funciones o ajustes del celular**

CSS de la página y luego su hoja `styles/responsive/`; no el service Java.

**Flujo de pago / resumen / éxito**

`useCheckout.js`/ `OrderSummary.jsx` y `CheckoutSummary.jsx`/ `PurchaseSuccess.jsx`.

**Apariencia de tarjeta o QR en pantalla**

`PaymentCard.jsx`/ `TicketDetails.jsx` y sus CSS. El PDF cambia en
`service/comunicacion/TicketPdfService.java`.

**Duración de reservas, precio o autorización**

Services e interceptores del backend. El contador/botón del frontend no establece la regla.

**Texto/posición de un control Swing**

El `...Panel` o `...Dialog` correspondiente.

**Acción ejecutada por ese control**

Su callback en `...Modulo`, luego la clase `...Api` y el endpoint/service.

**Columnas, búsqueda o etiquetas Swing**

`ConfiguradorTablas`, `BuscadorTabla`/`TextoBusqueda`, `FormatoSwing`.

**Dibujo de sala operativa / editor de distribución**

`MapaButacasPanel`/ `EditorDistribucionSala`.

**Orden de estudio:** entrada -> rutas/ventana y rol -> una página/panel -> su hook/context o módulo
-> cliente HTTP -> controller/service correspondiente. Explicar un clic completo primero; revisar
después CSS, componentes compartidos y utilidades. No es necesario memorizar todos los getters,
imports ni métodos auxiliares de dibujo.
