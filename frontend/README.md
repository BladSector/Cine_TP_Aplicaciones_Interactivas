# Film Zone: código del frontend React

Interfaz web para espectadores. Permite consultar películas y funciones, comprar entradas y productos del Candy, gestionar la cuenta y contratar membresías. Dueño y empleados utilizan la aplicación Swing, no un administrador web.

Este README explica cada carpeta y sus archivos: dónde se usan y qué responsabilidad modificar. Las rutas de las tablas parten de la raíz del repositorio, no de la ubicación de este README.

## Índice

- [Inicio y configuración](#inicio-y-configuración)
- [Rutas, comunicación y estado](#rutas-comunicación-y-estado)
- [Páginas](#páginas)
- [Componentes](#componentes)
- [Estilos y recursos](#estilos-y-recursos)
- [Flujo y organización](#flujo-y-organización)
- [Ejecución y estudio](#ejecución-y-estudio)

## Inicio y configuración

### Carpeta `frontend/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `index.html` | Documento HTML inicial. Contiene el elemento donde se monta React. | Título de la pestaña, metadatos y estructura HTML inicial, no pantallas individuales. |
| `package.json` | Dependencias y comandos npm. | Librerías y scripts como `dev`, `build` o `preview`. |
| `package-lock.json` | Versiones concretas instaladas por npm. | Se actualiza con npm al cambiar dependencias; no se edita a mano como código de pantalla. |
| `vite.config.js` | Servidor de desarrollo, plugin React y proxy hacia Spring Boot. | Puerto de desarrollo y destino de las rutas de API/imágenes. No es la lista de páginas. |
| `README.md` | Referencia rápida de React. | Instrucciones de inicio y mapa resumido. |
| `GUIA_ESTUDIO_FRONTEND.md` | Guía conceptual de ambas interfaces. | Explicaciones de hooks, estado, comunicación y preguntas de exposición. |

### Carpeta `frontend/src/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `main.jsx` | Arranque de toda la web. | Montaje de React, tema inicial, Router y proveedores globales. |
| `App.jsx` | Composición principal después del arranque. | Integración de rutas y comportamiento común de desplazamiento. |

`frontend/node_modules/` contiene dependencias instaladas. `frontend/dist/` contiene el resultado del build. Ninguna es el lugar para editar el código fuente: los cambios se hacen en `src/` y después se vuelve a compilar.

## Rutas, comunicación y estado

### Carpeta `frontend/src/routes/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `AppRoutes.jsx` | Relación entre URL y pantalla. | Agregar una página, cambiar una ruta o una redirección. Conserva redirecciones de `/candy` a `/candy`. |

Una ruta como `/pelicula/3` es una URL de React. `/peliculas` es un endpoint de la API. No cumplen la misma función.

### Carpeta `frontend/src/api/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `client.js` | Comunicación HTTP de la web con Spring Boot. | Rutas, métodos, cuerpos de peticiones, cookies y tratamiento común de respuestas/errores. |

No conviene repartir llamadas `fetch` independientes en los componentes: este archivo centraliza la configuración y la sesión mediante `credentials: 'include'`.

### Carpeta `frontend/src/context/`

Los Context conservan información compartida por varias pantallas.

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `AuthContext.jsx` | Sesión del espectador. | Restauración del usuario, login, registro, verificación, actualización de datos y logout. El ID local ayuda a restaurar datos; la sesión real depende del backend y su cookie. |
| `CatalogContext.jsx` | Datos del cine usados por varias páginas. | Carga/recarga de categorías, películas, funciones, salas, butacas, productos, entradas y tickets, según permisos. |
| `PurchaseContext.jsx` | Compra en curso y carrito. | Borrador, función elegida, butacas seleccionadas, cantidades de productos, continuidad, cancelación y limpieza al vencer la reserva. Persiste el borrador en `sessionStorage`. |

### Carpeta `frontend/src/hooks/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `useCheckout.js` | Etapa final de pago de entradas, candy o membresía. | Estado del pago, comprobaciones visuales, tarjeta elegida, totales, armado de la petición y procesamiento de su resultado. No sustituye el cálculo definitivo del backend. |
| `useSeatReservationHeartbeat.js` | Seguimiento de las reservas durante la compra. | Consulta periódica, errores y cálculo del tiempo restante. Consulta cada cinco segundos y actualiza el contador cada segundo; no renueva el vencimiento. |

Los diez minutos de reserva se definen en el backend, en `service/ventas/ReservaButacaService.java`, no en el componente que dibuja el contador.

### Carpeta `frontend/src/utils/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `format.js` | Presentación y comparaciones compartidas. | Formato de dinero, fechas, idiomas, 2D/3D, etiquetas de butacas, normalización de búsquedas y comprobaciones auxiliares de fechas. |

## Páginas

Una página coordina una pantalla asociada a una ruta; delega partes visuales en componentes.

### Carpeta `frontend/src/pages/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CarteleraPage.jsx` | `/`: página principal. | Composición de cartelera, filtros, fecha y listado de películas. La tarjeta individual está en `MovieCard`. |
| `PeliculaPage.jsx` | `/pelicula/:id`. | Ficha, día, función seleccionada y continuación hacia butacas o autenticación. |
| `CandyPage.jsx` | `/candy`. | Compra de productos sin entradas y paso al pago. El catálogo visual se reutiliza desde `CandyCatalog`. |
| `ClubPage.jsx` | `/club`. | Presentación y elección de planes de membresía. Las condiciones definitivas y la cotización vienen del backend. |
| `CuentaPage.jsx` | `/cuenta` y sus secciones por fragmento `#perfil`, `#pagos`, `#entradas` o `#membresia`. | Composición de la cuenta y sus flujos de acceso, recuperación y verificación. No confundir con el panel lateral `PurchaseAuthDrawer`. |
| `AboutPage.jsx` | `/nosotros`. | Presentación de la empresa y datos del equipo. |
| `LegalPage.jsx` | `/privacidad` y `/normas`. | Contenido de políticas y normas según el tipo de página. |
| `PlaceholderPage.jsx` | Secciones temporales como Próximamente y Experiencias. | Texto y presentación de esas pantallas informativas. |

### Carpeta `frontend/src/pages/compra/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CompraAccesoPage.jsx` | `/compra/:movieId/acceso`. | Redirección a la película con la indicación de abrir el panel de autenticación. No es un formulario de login independiente. |
| `CompraButacasPage.jsx` | Selección de asientos. | Carga de la sala, disponibilidad, reserva/liberación al hacer clic y continuación. El dibujo está en `SeatMap`. |
| `CompraCandyPage.jsx` | Productos dentro de una compra de entradas. | Visita de la etapa, cantidades, resumen y continuación al pago. Los productos son opcionales, visitar el paso no. |
| `CompraPagoPage.jsx` | Pago de entradas, candy y membresías. | Protección del paso y composición de formulario, resumen y resultado. La operación está en `useCheckout`. |
| `CompraExitosaPage.jsx` | Contenedor de confirmación usado por `PurchaseSuccess`. | Animación de pochoclos con `react-confetti`, dimensiones de ventana y distribución del contenido recibido como `children`. Aunque está en `pages/compra`, no tiene una ruta propia. |
| `CompraExitosaPage.css` | Solo ese contenedor de confirmación. | Canvas de animación sin bloquear clics y marco adaptable del comprobante. |

Butacas, candy y pago esperan el catálogo cuando faltan datos antes de redirigir: así una recarga no confunde datos todavía no recibidos con una compra inválida.

## Componentes

### Carpeta `frontend/src/components/layout/`

Elementos compartidos entre páginas.

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `SiteLayout.jsx` | Estructura común del sitio. | Marca, navegación, menú móvil, `Outlet`, avisos y footer. No concentra toda la lógica de sesión o carrito. |
| `HeaderActions.jsx` | Acciones del encabezado. | Composición de lupa, carrito, tema y usuario; apertura del acceso lateral. |
| `HeaderMovieSearch.jsx` | Lupa y resultados de películas. | Apertura, escritura, resultados con imágenes y navegación desde una búsqueda. |
| `ActiveCart.jsx` | Compra conservada fuera del flujo. | Visualización del carrito, subtotal estimado, continuación y confirmación de cancelación. El borrador vive en `PurchaseContext`. |
| `UserMenu.jsx` | Icono y menú de cuenta. | Enlaces a perfil, tarjetas, tickets, membresía y confirmación de cierre de sesión. |
| `ReservationExpiredNotice.jsx` | Avisos de reserva/compra. | Mensajes cuando se conserva la compra al salir o cuando vence la reserva. |
| `SiteFooter.jsx` | Pie de todas las páginas. | Marca, enlaces legales y Sobre nosotros; botón Contacto que abre un modal con datos ficticios. |

### Carpeta `frontend/src/components/cartelera/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `AccordionGallery.jsx` | Presentación dinámica de películas destacadas. | Selección, interacción, avance automático y navegación de la galería. |
| `AccordionGallery.css` | Solo esa galería. | Distribución, portadas, transiciones y sus ajustes visuales. |
| `DateStrip.jsx` | Selector horizontal de fechas. | Días mostrados, selección y navegación entre fechas. |
| `MovieCard.jsx` | Una película de la cartelera. | Portada clickeable, información compacta y despliegue de funciones agrupadas. |

### Carpeta `frontend/src/components/compra/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `SeatMap.jsx` | Plano de la sala. | Pantalla, filas, nombres y botones de butacas; apariencia de selección/disponibilidad. Recibe callbacks, no decide por sí solo la reserva en MySQL. |
| `SeatReservationTimer.jsx` | Contador de la compra. | Presentación del tiempo recibido y estado de advertencia. |
| `PurchaseStepper.jsx` | Pasos del flujo. | Etiquetas, enlaces y estado visual de las etapas. |
| `CandyCatalog.jsx` | Catálogo de productos, con o sin entradas. | Pestañas, imágenes, grupos por tamaño, variantes dulces/saladas, descripciones y cantidades. |
| `OrderSummary.jsx` | Resumen reutilizable. | Película, función, butacas, productos, totales y acciones de edición/eliminación con confirmación. |
| `CheckoutSummary.jsx` | Resumen conectado al pago. | Obtención de datos de los Context y enlaces/callbacks que pasa a `OrderSummary`. |
| `MobileCheckoutBar.jsx` | Barra inferior en celular. | Total compacto, botón de continuar y despliegue de detalles. |
| `PaymentMethodSelector.jsx` | Método de pago en checkout. | Tarjetas guardadas, nueva tarjeta, vista previa y opción de guardarla. |
| `ExpiryMonthPicker.jsx` | Vencimiento de tarjeta. | Selector de mes/año y navegación del calendario. |
| `CheckoutMoviePreview.jsx` | Vista breve de película donde se incorpora este componente. | Portada y datos breves. Su existencia no implica que aparezca en todas las variantes del pago. |
| `MembershipCheckout.jsx` | Pago o cambio de membresía. | Componente de presentación y hook exportado `useMembershipCheckout`: consulta plan/cotización y muestra el mensaje recibido. No fija las reglas del cambio en el servidor. |
| `PurchaseSuccess.jsx` | Compra o membresía confirmada. | Mensajes, resultado y composición del ticket. El detalle del comprobante está en `TicketDetails`. |

### Carpeta `frontend/src/components/cuenta/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `PurchaseAuthDrawer.jsx` | Acceso lateral desde encabezado o compra. | Coordinación de login, registro, recuperación, verificación y continuación. Usa un portal al `body` para no quedar limitado por el encabezado. |
| `PerfilForm.jsx` | Datos personales del espectador. | Edición de perfil y flujos de confirmación/cambio de contraseña por correo simulado. |
| `MetodosPagoList.jsx` | Tarjetas de Mi cuenta. | Listado, alta, presentación y baja de métodos de pago. |
| `MisEntradasList.jsx` | Historial de compras. | Listado de tickets, identificación de compras del candy y apertura del detalle. |
| `MembresiaPanel.jsx` | Membresía en Mi cuenta. | Información y acciones relacionadas con el plan del espectador. |

### Carpeta `frontend/src/components/cuenta/auth/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `LoginForm.jsx` | Formulario del acceso lateral. | Campos de correo/contraseña, mensajes y enlace de recuperación. Recibe el envío por props. |
| `RegisterForm.jsx` | Registro del acceso lateral. | Datos de cuenta y repetición de contraseña. La coordinación está en el drawer. |
| `PasswordRecoveryFlow.jsx` | Recuperación antes del login en el acceso lateral. | Etapas de solicitud, correo simulado y contraseña nueva; realiza las peticiones de recuperación. |
| `EmailVerification.jsx` | Confirmación de correo en el acceso lateral. | Presentación del correo simulado y botón que ejecuta el callback de verificación. |

Estos archivos separan el flujo lateral. `CuentaPage` conserva también sus propios flujos de acceso: modificar un formulario del drawer no cambia automáticamente el formulario de esa página.

### Carpeta `frontend/src/components/tickets/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `TicketDetails.jsx` | Confirmación y detalle de compra desde el historial. | Datos de entradas/consumos, QR, código manual, descarga PDF y solicitud de cancelación cuando el backend la permite. |

El QR visible se representa en React. El PDF se solicita al backend; su diseño se modifica en `src/main/java/service/comunicacion/TicketPdfService.java`, no en el CSS del ticket web.

### Carpeta `frontend/src/components/common/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `Modal.jsx` | Ventanas emergentes compartidas. | Portal, estructura, cierre por fondo/tecla Escape y espacio de acciones. |
| `ConfirmDialog.jsx` | Confirmaciones de operaciones. | Mensaje, botones y estado de procesamiento. La acción concreta llega por callback. |
| `Loader.jsx` | Pantalla/indicador de carga reutilizado. | Marcado y presentación de la carga. |
| `Loader.css` | Solo el loader. | Apariencia y animación. |
| `ScrollToTop.jsx` | Desplazamiento al navegar. | Cuándo corresponde subir la página según el cambio de ruta. No es el único lugar del proyecto que puede desplazarla. |

## Estilos y recursos

### Carpeta `frontend/src/styles/`

| Archivo o ruta | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `global.css` | Entrada de estilos importada por `main.jsx`. | Orden de los `@import`. No agregar aquí todas las reglas de las pantallas. |
| `theme.css` | Temas claro/oscuro y variables compartidas. | Colores, superficies, fuentes y valores generales del tema. |
| `base.css` | Base común del documento. | Reset, texto, enlaces, formularios y clases generales. |
| `responsive.css` | Ajustes por ancho de pantalla. | Distribución y tamaños para tablet/celular. Incluye cortes de 900, 640 y 390 px; también puede haber ajustes locales en otras hojas. |
| `layout/header.css` | Encabezado y controles relacionados. | Navegación, lupa, menú de usuario, carrito y posiciones del layout. |
| `layout/footer.css` | Footer. | Distribución, márgenes y espaciado del pie. |
| `pages/cartelera.css` | Cartelera. | Filtros, películas, fechas y funciones desplegables. |
| `pages/compra.css` | Película y compra. | Ficha, horarios, plano, resumen, pago y confirmación. |
| `pages/cuenta.css` | Mi cuenta. | Distribución de perfil, formularios y secciones de cuenta. |
| `pages/club.css` | Membresías. | Tarjetas de planes, beneficios y estados visuales. |
| `pages/about.css` | Sobre nosotros. | Presentación de empresa y equipo. |
| `pages/legal.css` | Privacidad y normas. | Lectura y distribución del contenido legal. |
| `components/cards.css` | Tarjetas bancarias y productos. | Dimensiones, selección y controles de tarjetas reutilizadas. |
| `components/modals.css` | Modales y acceso lateral. | Fondo, panel, cabecera, botones y animaciones. |
| `components/tickets.css` | Historial y detalle de tickets. | QR, filas de datos, estados y acciones del comprobante. |
| `README.md` | Mapa de CSS. | Documentación de la organización de estilos. |

**Cómo encontrar un estilo:** buscar la clase del JSX, localizarla en estas hojas y revisar después las media queries. No agregar una regla duplicada al final de cualquier CSS sin identificar antes quién la aplica.

### Recursos compartidos con el backend

`src/main/resources/static/images/peliculas/` contiene portadas y `src/main/resources/static/images/consumibles/` contiene imágenes de productos. Spring Boot las sirve bajo `/images/...`; Vite redirige esas peticiones durante el desarrollo.

`database/portadasPeliculas/` conserva originales y `database/descripcionPeliculas.txt` es material de referencia. Editar un archivo de referencia no actualiza por sí solo los registros ya cargados en MySQL.

## Flujo y organización

```text
index.html -> main.jsx -> App.jsx -> AppRoutes.jsx -> SiteLayout.jsx
  -> página -> componentes -> Context/hook -> api/client.js
  -> API Spring Boot -> respuesta -> estado -> renderizado
```

- Una página coordina una pantalla; un componente presenta una parte reutilizable.
- Los Context comparten sesión, catálogo y compra. Los hooks agrupan coordinación, efectos y estado de un caso de uso.
- `PurchaseContext` conserva `draft` en `sessionStorage`: seleccionar entradas no crea un ticket pagado. Las reservas temporales, cuentas y tarjetas sí pueden guardarse antes.
- `useCheckout.handlePay` confirma la compra; `PurchaseSuccess` muestra el resultado y utiliza `CompraExitosaPage` para la animación y `TicketDetails` para el comprobante.
- La interfaz ayuda a validar datos, pero los permisos, precios y disponibilidad definitivos pertenecen al backend.
- React no accede directamente a MySQL ni debe contener credenciales secretas de base de datos o correo.

### Rutas de compra

| URL | Pantalla |
| --- | --- |
| `/` | Cartelera. |
| `/pelicula/:id` | Película y elección de función. |
| `/compra/:movieId/acceso` | Redirección a película con autenticación lateral. |
| `/compra/:movieId/butacas` | Selección y reserva. |
| `/compra/:movieId/candy` | Productos opcionales dentro de la compra. |
| `/compra/:movieId/pago` | Pago y resultado. |
| `/candy` y `/candy/pago` | Compra sin entradas. |
| `/club` y `/club/pago` | Membresías. |
| `/cuenta` | Perfil, tarjetas, tickets y membresía. |
| `/nosotros`, `/privacidad`, `/normas` | Información institucional/legal. |
| `/proximamente`, `/experiencias` | Pantallas informativas temporales. |

Los enlaces antiguos `/kiosco`, `/kiosco/pago` y `/compra/:movieId/kiosco` redirigen a Candy. `PurchaseContext` también adapta borradores anteriores que usaban `purchaseType: 'kiosco'` o `kioscoVisitado`. Estas referencias antiguas son compatibilidad, no nombres pendientes de reemplazar.

## Ejecución y estudio

Con Node.js, npm y el backend iniciado en `http://localhost:8080`, ejecutar desde `frontend/`:

```powershell
npm.cmd install
npm.cmd run dev
```

Abrir `http://localhost:5173` o el puerto indicado por Vite. Para generar `frontend/dist/`:

```powershell
npm.cmd run build
```

En macOS/Linux se usa `npm` en lugar de `npm.cmd`. Vite redirige la API y las imágenes durante desarrollo; un despliegue requiere configurar también ese acceso al backend.

Orden de lectura: inicio -> rutas -> Context/API -> una página -> componentes -> hook de compra -> CSS. Para cada archivo, identificar qué recibe, qué conserva y a quién delega.

- [README de Swing](../src/main/java/desktop/README.md).
- [Guía conceptual de estudio](GUIA_ESTUDIO_FRONTEND.md).
- [Detalle de estilos](src/styles/README.md).
- [Inicio del proyecto completo](../GUIA_EJECUCION.md).
- [Verificaciones y límites de las pruebas](../VERIFICACION_INTERFACES.md).
