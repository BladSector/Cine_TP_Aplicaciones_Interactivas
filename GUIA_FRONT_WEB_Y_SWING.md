# Guía de carpetas y archivos del frontend: web y Swing

## Índice

1. [Panorama del sistema](#1-panorama-del-sistema)
2. [Web: inicio y configuración](#2-web-inicio-y-configuración)
3. [Web: rutas, comunicación y estado](#3-web-rutas-comunicación-y-estado)
4. [Web: páginas](#4-web-páginas)
5. [Web: componentes](#5-web-componentes)
6. [Web: estilos y recursos](#6-web-estilos-y-recursos)
7. [Swing: inicio y estructura](#7-swing-inicio-y-estructura)
8. [Swing: comunicación con la API](#8-swing-comunicación-con-la-api)
9. [Swing: módulos compartidos y vistas](#9-swing-módulos-compartidos-y-vistas)
10. [Swing: componentes, diálogos y utilidades](#10-swing-componentes-diálogos-y-utilidades)
11. [Swing: interfaz del dueño](#11-swing-interfaz-del-dueño)
12. [Swing: interfaz del empleado](#12-swing-interfaz-del-empleado)
13. [Recorridos por archivos](#13-recorridos-por-archivos)
14. [Dónde modificar cada cosa](#14-dónde-modificar-cada-cosa)
15. [Cómo estudiar y documentos de apoyo](#15-cómo-estudiar-y-documentos-de-apoyo)

## 1. Panorama del sistema

Film Zone tiene dos frontends que utilizan el mismo backend:

```text
Espectador -> React en el navegador -> API Spring Boot -> MySQL
Dueño / Empleado -> Swing en escritorio -> API Spring Boot -> MySQL
```

- La web está en `frontend/` y durante el desarrollo se abre en `http://localhost:5173`.
- Swing está en `src/main/java/desktop/`.
- El backend está en las otras carpetas de `src/main/java/` y escucha en `http://localhost:8080`.
- Dueño y empleados operan por Swing, no mediante un administrador web.

**Qué significa modificar un archivo:** cambiar JSX, CSS o un panel Java cambia cómo se presenta o coordina la interfaz. Eso no modifica por sí mismo los registros de MySQL. Una acción del usuario puede enviar HTTP; el backend decide si se permite y guarda los datos.

**Límite importante:** reglas como evitar superposición de funciones, impedir una doble venta o autorizar un reembolso pertenecen al backend. El frontend muestra opciones, orienta al usuario y presenta errores, pero no reemplaza esas reglas.

Las rutas de archivo de esta guía parten de la raíz del repositorio. En cada tabla, el archivo se encuentra dentro de la carpeta indicada en el encabezado.

## 2. Web: inicio y configuración

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

## 3. Web: rutas, comunicación y estado

### Carpeta `frontend/src/routes/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `AppRoutes.jsx` | Relación entre URL y pantalla. | Agregar una página, cambiar una ruta o una redirección. Conserva redirecciones de `/candy` a `/kiosco`. |

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
| `useCheckout.js` | Etapa final de pago de entradas, kiosco o membresía. | Estado del pago, comprobaciones visuales, tarjeta elegida, totales, armado de la petición y procesamiento de su resultado. No sustituye el cálculo definitivo del backend. |
| `useSeatReservationHeartbeat.js` | Seguimiento de las reservas durante la compra. | Consulta periódica, errores y cálculo del tiempo restante. Consulta cada cinco segundos y actualiza el contador cada segundo; no renueva el vencimiento. |

Los diez minutos de reserva se definen en el backend, en `service/ventas/ReservaButacaService.java`, no en el componente que dibuja el contador.

### Carpeta `frontend/src/utils/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `format.js` | Presentación y comparaciones compartidas. | Formato de dinero, fechas, idiomas, 2D/3D, etiquetas de butacas, normalización de búsquedas y comprobaciones auxiliares de fechas. |

## 4. Web: páginas

Una página coordina una pantalla asociada a una ruta; delega partes visuales en componentes.

### Carpeta `frontend/src/pages/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CarteleraPage.jsx` | `/`: página principal. | Composición de cartelera, filtros, fecha y listado de películas. La tarjeta individual está en `MovieCard`. |
| `PeliculaPage.jsx` | `/pelicula/:id`. | Ficha, día, función seleccionada y continuación hacia butacas o autenticación. |
| `KioscoPage.jsx` | `/kiosco`. | Compra de productos sin entradas y paso al pago. El catálogo visual se reutiliza desde `KioscoCatalog`. |
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
| `CompraKioscoPage.jsx` | Productos dentro de una compra de entradas. | Visita de la etapa, cantidades, resumen y continuación al pago. Los productos son opcionales, visitar el paso no. |
| `CompraPagoPage.jsx` | Pago de entradas, kiosco y membresías. | Protección del paso y composición de formulario, resumen y resultado. La operación está en `useCheckout`. |

Butacas, kiosco y pago esperan el catálogo cuando faltan datos antes de redirigir: así una recarga no confunde datos todavía no recibidos con una compra inválida.

## 5. Web: componentes

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
| `SiteFooter.jsx` | Pie de todas las páginas. | Marca, textos y enlaces institucionales/legales. Algunos textos son informativos, no enlaces a pantallas implementadas. |

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
| `KioscoCatalog.jsx` | Catálogo de productos, con o sin entradas. | Pestañas, imágenes, grupos por tamaño, variantes dulces/saladas, descripciones y cantidades. |
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
| `MisEntradasList.jsx` | Historial de compras. | Listado de tickets, identificación de compras del kiosco y apertura del detalle. |
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

## 6. Web: estilos y recursos

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

## 7. Swing: inicio y estructura

### Carpeta `src/main/java/desktop/inicio/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CineDesktopApplication.java` | Inicio de Swing. | `main`, apariencia y creación del login en el hilo de eventos. Lee `cine.api.url`, por defecto `http://localhost:8080`. |
| `LoginFrame.java` | Ventana de acceso del personal. | Campos, botón de ingreso, errores y apertura de la ventana principal según la respuesta de sesión. |

### Carpeta `src/main/java/desktop/comun/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ApiClient.java` | Transporte HTTP de Swing. | `HttpClient`, cookies, JSON, tiempos de espera y errores. No contiene el catálogo de pantallas ni reglas de compra. |
| `PrincipalFrame.java` | Ventana principal. | Cabecera, menú superior, barra de estado, selección del panel por rol, cierre de sesión y salida. |
| `PanelPersonalBase.java` | Navegación compartida. | Menú lateral, `CardLayout`, sección activa y registro de recargas/detenciones. No almacena tablas del negocio. |

`pom.xml`, en la raíz, configura las dependencias y el punto de entrada de Swing para `mvn.cmd compile exec:java`. `target/` contiene clases compiladas; se modifica el `.java` fuente, no los `.class`.

## 8. Swing: comunicación con la API

### Carpeta `src/main/java/desktop/comun/api/`

Cada clase selecciona rutas y métodos HTTP y usa el mismo `ApiClient` de la sesión.

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `SesionApi.java` | Login y cierre. | Iniciar, consultar y cerrar sesión del personal. |
| `CarteleraApi.java` | Categorías, películas y funciones. | Endpoints de consulta/CRUD, comprobación de entradas activas y modificación de horario. La existencia de un método no habilita ese permiso al empleado. |
| `SalasApi.java` | Salas, butacas y avisos. | Rutas para distribución, estados, incidencias y consulta/lectura de avisos. |
| `TicketsApi.java` | Tickets y consumos. | Búsqueda por ID/QR, validación, entrega y reembolso. Centraliza la codificación del QR en la URL. |
| `ProductosApi.java` | Productos de confitería. | Endpoints de consulta, creación, edición y eliminación. |
| `PersonalApi.java` | Cuentas del personal. | Listado, alta, activación, desactivación y restablecimiento de contraseña. |
| `AuditoriaApi.java` | Actividad del sistema. | Consulta del registro de acciones. |

Estas clases **no son los services del backend**. No acceden a repositories ni autorizan operaciones; si cambia una regla de negocio se revisa `service/`, fuera de `desktop/`.

## 9. Swing: módulos compartidos y vistas

### Carpeta `src/main/java/desktop/comun/modulos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ModuloSwing.java` | Base de los coordinadores operativos. | Ejecución con `SwingWorker`, avisos, detalles, selección de IDs y control de módulo activo. No es un panel visual. |
| `SalasModulo.java` | Salas, consulta de funciones y butacas. | Carga de datos, selección, dibujo del plano y coordinación de cambios de estado/incidencias. |
| `VentasModulo.java` | Tickets y Consumos de ambos roles. | Consultas, selección, lectura de QR, advertencias, validación, entrega y reembolso con sus recargas. |

Un módulo conserva estado de pantalla y coordina acciones. No es una entidad persistente. `SwingWorker.doInBackground` realiza HTTP y `done` actualiza la interfaz en el EDT, evitando bloquear la ventana.

### Carpeta `src/main/java/desktop/comun/paneles/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ButacasPanel.java` | Vista del plano. | Selector, ubicación de controles y botones según el rol; recibe el plano y callbacks del módulo. |
| `TicketsPanel.java` | Ventas y tickets / Control de acceso. | Campo QR, tabla, botones manuales, reembolso y notificación de selección. Informa `null` cuando se limpia la selección. |
| `ConsumosPanel.java` | Entrega de productos. | Campo QR, tabla y controles de entrega/recarga. |

**Vista y coordinador no son lo mismo:** para cambiar el texto o posición de un botón se revisa el panel; para cambiar qué hace se sigue su callback hasta el módulo.

## 10. Swing: componentes, diálogos y utilidades

### Carpeta `src/main/java/desktop/comun/componentes/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `PanelConTabla.java` | Secciones con tablas. | Título, ayuda, buscador y contenedor con desplazamiento. |
| `BuscadorTabla.java` | Búsqueda en tablas. | Campo y filtro literal por columnas, integración con el orden inicial. |
| `VisorDetalle.java` | Cuadros de información. | Presentación HTML de solo lectura; no es un navegador ni el frontend web. |

### Carpeta `src/main/java/desktop/comun/dialogos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `EstadoSalaDialog.java` | Cambio de estado de sala. | Estado, descripción y destinatario. Exige descripción para Problema particular. |
| `EstadoButacaDialog.java` | Butaca fuera de servicio. | Confirmación de identidad, comentario opcional y destinatario. |
| `SelectorDestinatario.java` | Los dos diálogos anteriores. | Selección de empleado activo o aviso a todos. |

### Carpeta `src/main/java/desktop/comun/util/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ConfiguradorTablas.java` | Tablas administrativas y operativas. | Modelos no editables, selección, orden y anchos de columnas. |
| `DetalleHtml.java` | Información de registros. | Construcción de títulos/campos/secciones y escape de datos para mostrarlos como texto. |
| `BotonesSwing.java` | Creación común de botones. | Configuración compartida de botones que ejecutan un `Runnable`. |
| `FormatoSwing.java` | Etiquetas y códigos. | Mostrar 2D/3D, español y estados legibles; conversiones a códigos que espera la API. |
| `TextoBusqueda.java` | Comparaciones en tablas y combos. | Normalización sin mayúsculas ni tildes, sin alterar los datos guardados. |

## 11. Swing: interfaz del dueño

### Carpeta `src/main/java/desktop/duenio/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `DuenioPanel.java` | Composición del rol Dueño. | Grupos, orden de secciones, creación de módulos y registro de su ciclo de vida. |
| `AdministracionPanel.java` | CRUD de categorías, películas, funciones, salas y productos. | Selección, apertura de formularios, armado de cuerpos HTTP y recarga de modelos. Delega rutas y formularios a sus clases específicas. |

La carpeta usa `duenio` como nombre de paquete; el texto visible es Dueño.

### Carpeta `src/main/java/desktop/duenio/modulos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `PersonalModulo.java` | Empleados. | Carga de cuentas, creación, activación/desactivación y recuperación de contraseñas. |
| `ActividadModulo.java` | Actividad del sistema. | Carga de auditoría, presentación por actor y detalles amigables de acciones. |

### Carpeta `src/main/java/desktop/duenio/paneles/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CrudAdministracionPanel.java` | Estructura visual común de los CRUD. | Tabla, alta, recarga y apertura del detalle por doble clic. |
| `CategoriasPanel.java` | Categorías. | Configuración visual y callbacks de esa sección. |
| `PeliculasPanel.java` | Películas. | Configuración visual y callbacks de esa sección. |
| `FuncionesAdminPanel.java` | Funciones del dueño. | Configuración visual y callbacks del CRUD de funciones. |
| `SalasAdminPanel.java` | Salas del dueño. | Configuración visual y callbacks de administración de salas. |
| `ProductosPanel.java` | Productos. | Configuración visual y callbacks del catálogo del kiosco. |
| `EmpleadosPanel.java` | Personal. | Controles de alta, contraseña, activación/desactivación y recarga. |
| `ActividadPanel.java` | Auditoría. | Distribución de tabla y referencia visual de actores. |

### Carpeta `src/main/java/desktop/duenio/dialogos/`

Los formularios devuelven datos mediante `Optional<Datos>`; cancelar no envía una modificación. Los `record Datos` agrupan campos del formulario, no son tablas nuevas de MySQL.

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CategoriaDialog.java` | Alta/edición de categoría. | Campo Nombre y captura del resultado. |
| `PeliculaDialog.java` | Alta/edición de película. | Título, duración, descripción, categoría y portada local. |
| `FuncionDialog.java` | Alta/edición de función. | Fecha, horario, búsqueda de película/sala, combos, formato, idioma y precio. |
| `SalaDialog.java` | Alta/edición de distribución. | Nombre, apertura del editor, confirmación y resultado. |
| `EditorDistribucionSala.java` | Plano editable de una sala. | Pantalla, filas, posiciones de butacas y espacios vacíos. No confundir con el plano operativo de `SalasModulo`. |
| `ProductoDialog.java` | Alta/edición de producto. | Nombre, descripción, precio, tipo, tamaño e imagen. El formulario actual no tiene un campo de stock. |
| `EmpleadoDialog.java` | Alta de empleado. | Datos de cuenta y contraseña repetida. No ofrece crear otro dueño. |
| `RestablecerContraseniaDialog.java` | Recuperación de cuenta por el dueño. | Identificación del empleado y contraseña nueva repetida. |
| `FormularioDialogo.java` | Apoyo compartido de formularios. | Disposición de campos, confirmación, comprobaciones visuales y búsqueda/selección de opciones. |
| `SelectorImagenLocal.java` | Películas y productos. | Selección del archivo, comprobación de tamaño, codificación y vista previa. |
| `Opcion.java` | Combos de selección. | `record` con ID y etiqueta para mostrar un nombre y conservar su identificador. No es una entidad JPA. |

## 12. Swing: interfaz del empleado

### Carpeta `src/main/java/desktop/empleado/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `EmpleadoPanel.java` | Composición del rol Empleado. | Secciones Control de acceso, Consumos, Funciones, Salas y Butacas; registro de módulos y botón de avisos. |

### Carpeta `src/main/java/desktop/empleado/modulos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `AvisosModulo.java` | Avisos del empleado. | Botón, consulta periódica, historial y lectura. Detiene su temporizador al cerrar la sesión. |

### Carpeta `src/main/java/desktop/empleado/paneles/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `FuncionesPanel.java` | Programación consultable. | Tabla y recarga. No ofrece edición de fecha u horario al empleado. |
| `SalasPanel.java` | Consulta de salas y operación. | Tabla, recarga y botón de cambio de estado. |

Los nombres de carpeta reflejan la organización del código, no una autorización automática. Algunas vistas se reutilizan desde módulos compartidos; los permisos efectivos se comprueban en el backend.

## 13. Recorridos por archivos

### Comprar una entrada en la web

```text
CarteleraPage -> MovieCard -> PeliculaPage
  -> PurchaseAuthDrawer, si falta acceso/verificación
  -> PurchaseContext: película, función y borrador
  -> CompraButacasPage -> SeatMap -> api/client: reservar
  -> CompraKioscoPage -> KioscoCatalog: cantidades
  -> CompraPagoPage -> useCheckout -> api/client: comprar
  -> backend: valida y guarda
  -> PurchaseSuccess -> TicketDetails: resultado y QR
```

El borrador no crea un ticket pagado. Cuenta, tarjeta y reservas sí pueden persistirse antes; entradas y compra se confirman al finalizar el pago.

### Validar el QR en Swing

```text
EmpleadoPanel o DuenioPanel
  -> TicketsPanel: código o selección manual
  -> VentasModulo: consulta y advertencia si hay consumos pendientes
  -> TicketsApi -> ApiClient -> backend
  -> recarga de tickets y consumos
```

Validar el acceso no entrega productos. La entrega se hace en `ConsumosPanel`, también mediante `VentasModulo` y `TicketsApi`. El QR de Consumos no cambia el ticket seleccionado para reembolsar.

### Editar una película desde el dueño

```text
DuenioPanel -> PeliculasPanel
  -> AdministracionPanel -> PeliculaDialog
  -> Optional<Datos>
  -> AdministracionPanel -> CarteleraApi -> ApiClient
  -> backend -> recarga del catálogo administrativo
```

Las operaciones conservan el ID original antes de esperar una consulta o un diálogo. Una recarga puede cambiar las filas, pero no debe cambiar el registro que se está modificando.

## 14. Dónde modificar cada cosa

| Quiero cambiar… | Revisar primero |
| --- | --- |
| Menú web, orden de enlaces | `SiteLayout.jsx`. |
| Lupa, tema, carrito o cuenta en el encabezado | `HeaderActions.jsx` y su componente específico. |
| Colores del modo oscuro | `styles/theme.css`. |
| Tamaños o distribución solo en celular | Hoja del componente/página y `styles/responsive.css`. |
| Película de cartelera y sus horarios desplegables | `MovieCard.jsx` y `pages/cartelera.css`. |
| Cantidades y variantes de pochoclos | `KioscoCatalog.jsx`; si faltan productos, revisar también los datos del backend. |
| QR en pantalla | `TicketDetails.jsx` y `components/tickets.css`. |
| QR o estructura del PDF | `service/comunicacion/TicketPdfService.java`, en el backend. |
| Recuperación de contraseña lateral | `cuenta/auth/PasswordRecoveryFlow.jsx`; para la página de cuenta, revisar también `CuentaPage.jsx`. |
| Qué conserva una compra al navegar | `PurchaseContext.jsx`. |
| Orden del menú del dueño/empleado | `DuenioPanel.java` o `EmpleadoPanel.java`. |
| Ventana, cabecera o salida de Swing | `PrincipalFrame.java`. |
| Columnas y anchos de tablas Swing | `ConfiguradorTablas.java`. |
| Buscador sin tildes en Swing | `BuscadorTabla.java`, `TextoBusqueda.java` y los combos de `FormularioDialogo.java`. |
| Campos de un formulario Swing | Su clase en `dialogos/`. |
| Qué hace un botón Swing | Callback en el panel y método del módulo/`AdministracionPanel`. |
| URL utilizada por una operación Swing | Cliente correspondiente en `desktop/comun/api/`. |
| Reglas, permisos o duración de reserva | Services y autorización del backend, no los paneles visuales. |

## 15. Cómo estudiar y documentos de apoyo

Para cada archivo responder: **qué recibe**, **qué estado conserva**, **dónde se muestra** y **a quién delega**. No hace falta memorizar todos los métodos antes de seguir una acción completa.

Orden recomendado:

1. Web: `main.jsx` -> `App.jsx` -> `AppRoutes.jsx` -> `SiteLayout.jsx`.
2. Leer los tres Context y `api/client.js` para identificar datos y sesión.
3. Seguir una compra completa; después cuenta, tickets y membresías.
4. Localizar clases JSX y sus hojas CSS para entender la presentación.
5. Swing: `CineDesktopApplication` -> `LoginFrame` -> `PrincipalFrame` -> panel del rol.
6. Seguir un botón: panel -> módulo -> diálogo o cliente API -> backend -> recarga.
7. Revisar `SwingWorker`, captura de IDs, `detener()` y limpieza de temporizadores.

Documentos complementarios:

- [Guía conceptual y preguntas de estudio](frontend/GUIA_ESTUDIO_FRONTEND.md).
- [Mapa específico de Swing](src/main/java/desktop/README.md).
- [Mapa específico de estilos](frontend/src/styles/README.md).
- [Guía para iniciar el proyecto](GUIA_EJECUCION.md).
- [Pruebas realizadas y sus límites](VERIFICACION_INTERFACES.md).

Esta guía es un mapa del código actual. No modifica las pantallas, la base ni los permisos.
