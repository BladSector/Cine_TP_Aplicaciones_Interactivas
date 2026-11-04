# Guía de presentación: seguir el código de Film Zone

Esta guía es el recorrido corto. Los README de [React](frontend/README.md) y [Swing](src/main/java/desktop/README.md) contienen el inventario de archivos. Las rutas de esta guía parten de la raíz del repositorio.

## 1. Idea que conviene explicar primero

Film Zone separa presentación, coordinación de pantalla, comunicación y negocio. React y Swing presentan los mismos datos con interfaces distintas. La API decide si una operación está permitida y persiste sus cambios; ninguno de los dos clientes accede directamente a MySQL.

```text
Acción del usuario -> pantalla -> coordinador -> cliente HTTP
  -> controller -> service -> repository -> MySQL
  -> respuesta -> estado de pantalla -> nueva representación
```

No todos los archivos tienen que pasar por todas las etapas: un componente que dibuja una tarjeta o un mapa solo recibe datos y callbacks. Una búsqueda local en una tabla tampoco necesita escribir en la base.

## 2. Orden para estudiar

1. Leer el arranque de React y Swing para ubicar las pantallas.
2. Seguir una compra web completa, sin empezar por todos los componentes a la vez.
3. Seguir el QR de esa compra hasta el módulo Swing que lo procesa.
4. Recorrer un alta o edición del dueño.
5. Repasar estado, asincronía, errores y responsabilidades.
6. Buscar el estilo de un elemento y distinguirlo de su comportamiento.

Para cada archivo responder cuatro preguntas: **qué recibe, qué conserva, qué muestra y a quién delega**.

## 3. Arranque web y estado

Abrir `frontend/src/main.jsx`: monta React, configura el Router y anida los proveedores. `App.jsx` compone las rutas y el comportamiento común. `routes/AppRoutes.jsx` relaciona URL y página. `components/layout/SiteLayout.jsx` presenta el encabezado, la navegación, el contenido y el footer.

| Archivo en `frontend/src/context/` | Responsabilidad | Qué no debe hacer |
| --- | --- | --- |
| `AuthContext.jsx` | Cuenta activa, login, registro, verificación y logout. | Confiar en un ID local como autorización del servidor. |
| `CatalogContext.jsx` | Categorías, películas, funciones, salas, butacas y productos públicos. | Cargar tickets privados de todos los usuarios. |
| `AccountContext.jsx` | Tickets de la cuenta activa, carga, error y reintento. | Mantener visible el historial anterior al cambiar de cuenta. |
| `PurchaseContext.jsx` | Borrador, cantidades, selección, carrito y vencimiento. | Presentar un borrador como compra ya pagada. |

`localStorage` conserva el ID de la cuenta y la preferencia de tema. `sessionStorage` conserva el borrador de compra de esa pestaña. La sesión efectiva se comprueba en el backend mediante la cookie; ambos almacenes del navegador pueden manipularse y no son fuentes de autorización.

## 4. Compra web, archivo por archivo

1. **Cartelera:** `pages/CarteleraPage.jsx` consume el catálogo, filtra funciones y utiliza `components/cartelera/MovieCard.jsx`. `DateStrip.jsx` permite elegir el día. No crean entradas.
2. **Película y función:** `pages/PeliculaPage.jsx` muestra la ficha y selección. Si falta cuenta/verificación abre `components/cuenta/PurchaseAuthDrawer.jsx`. `PurchaseContext.startMoviePurchase` inicia el borrador.
3. **Butacas:** `pages/compra/CompraButacasPage.jsx` obtiene disponibilidad y coordina reserva/liberación. `components/compra/SeatMap.jsx` dibuja la sala y notifica clics; no escribe directamente en MySQL.
4. **Reserva temporal:** `hooks/useSeatReservationHeartbeat.js` consulta las reservas y calcula el contador. No renueva su plazo. Los diez minutos se definen en `src/main/java/service/ventas/ReservaButacaService.java`.
5. **Productos:** `pages/compra/CompraCandyPage.jsx` reutiliza `components/compra/CandyCatalog.jsx`. Cambia `draft.consumos`; visitar la etapa es obligatorio, agregar productos no.
6. **Pago:** `pages/compra/CompraPagoPage.jsx` compone formulario y resumen. `hooks/useCheckout.js`, mediante `handlePay`, valida la interacción, obtiene el método de pago y arma la petición. `components/compra/CheckoutSummary.jsx` y `OrderSummary.jsx` presentan el resumen.
7. **HTTP:** `api/client.js` define `comprar`, `comprarCandy` y `comprarMembresia`, incluye cookies y convierte respuestas HTTP en datos o errores. El pago no se implementa en el CSS ni en la tarjeta visual.
8. **Resultado:** `components/compra/PurchaseSuccess.jsx` compone la confirmación y `components/tickets/TicketDetails.jsx` muestra entradas, consumos, QR y descarga PDF. El historial consulta `AccountContext`, no el catálogo público.

**Por qué no se crea una compra al seleccionar:** hasta pagar se modifica un borrador y, para butacas, se crean reservas temporales. En el flujo normal, la creación del ticket y las entradas pagadas se solicita al confirmar. Si el usuario abandona, no se debe mostrar una compra pagada por el simple hecho de tener un carrito.

## 5. Límite entre frontend y backend

`controller/ventas/CompraController.java` recibe `POST /compras`, comprueba la sesión mediante los servicios correspondientes y delega en `service/ventas/CompraService.java`. Su método `comprar` está marcado con `@Transactional`: agrupa el guardado de ticket, entradas y productos. No debe quedar una parte confirmada si falla otra.

El service comprueba espectador verificado, método de pago, función, butacas y disponibilidad, utiliza `PrecioService`, crea los registros y libera las reservas. Los repositories obtienen/guardan las entidades. Los bloqueos y validaciones del servidor, no los botones deshabilitados en React, impiden que dos clientes vendan la misma butaca.

El evento `TicketCompradoEvent` se publica desde la compra. `service/comunicacion/TicketEmailListener.java` procesa el envío después del commit; `TicketPdfService.java` genera el PDF. Un error de correo no debe confundirse con que el navegador dibuje un QR.

## 6. Swing: del inicio a una operación

```text
desktop/inicio/CineDesktopApplication.java
  -> LoginFrame.java
  -> desktop/comun/PrincipalFrame.java
  -> desktop/duenio/DuenioPanel.java o desktop/empleado/EmpleadoPanel.java
  -> panel -> modulo -> cliente de dominio -> ApiClient -> API
```

`PrincipalFrame` gestiona ventana, menú superior, navegación de rol y cierre. Los paneles de rol componen secciones; no concentran todas las reglas. `PanelPersonalBase` comparte el menú lateral y `CardLayout` para cambiar de sección.

### Ejemplo: validar un ticket

`desktop/comun/paneles/TicketsPanel.java` recibe el QR o una selección manual. Su callback llega a `desktop/comun/modulos/VentasModulo.java`, que consulta el ticket y muestra la advertencia si todavía hay consumos pendientes. `desktop/comun/api/TicketsApi.java` selecciona el endpoint y `desktop/comun/ApiClient.java` envía HTTP con las cookies de la sesión.

La API recibe `PUT /tickets/{id}/validar-entradas` en `controller/ventas/TicketController.java` y delega la operación en `service/ventas/TicketService.java`. La entrega de productos utiliza `ConsumosPanel`, el mismo coordinador de ventas y `PUT /tickets/{id}/entregar-consumos`. Validar entradas no equivale a entregar consumos.

### Ejemplo: editar una película

`desktop/duenio/DuenioPanel.java` incluye el CRUD de películas compuesto por `AdministracionPanel`. `desktop/duenio/modulos/PeliculasModulo.java` coordina tabla, detalle y operación. `desktop/duenio/dialogos/PeliculaDialog.java` devuelve `Optional<Datos>`: cancelar devuelve vacío y no envía una modificación. `CarteleraApi` realiza la petición y se recarga el catálogo administrativo.

`CrudModulo` comparte la coordinación repetida de los cinco CRUD; `DatosAdministracion` transporta un mismo catálogo cargado para sus opciones. No son entidades JPA ni tablas nuevas.

## 7. Asincronía y errores que conviene poder explicar

| Situación | Dónde se resuelve |
| --- | --- |
| Consulta lenta sin congelar Swing | `desktop/comun/modulos/ModuloSwing.java`: `SwingWorker` hace HTTP fuera del EDT y actualiza la vista en `done`. |
| Una recarga anterior termina después de otra nueva | Consultas versionadas en `ModuloSwing`, `CatalogContext` y `AccountContext`; se ignora el resultado viejo. |
| Cambia la selección mientras hay un diálogo abierto | El módulo Swing conserva el ID del registro original, no vuelve a tomar la fila actual al confirmar. |
| Cambia o se cierra la cuenta durante la carga de tickets | `AccountContext` oculta datos anteriores, cancela la consulta y descarta respuestas tardías. |
| Se superponen restauraciones de sesión durante el arranque | `AuthContext` descarta resultados anteriores; la limpieza del efecto evita que una consulta abandonada quite la carga o provoque una redirección sin usuario. |
| Termina la reserva mientras el pago está respondiendo | `PurchaseContext.paymentInProgress` pausa la comprobación visual; el backend mantiene la validación definitiva. Si el pago falla se reactiva el seguimiento. |
| Escape, Tab o clic fuera de una ventana | `hooks/useDialog.js`; solo captura el diálogo superior y no permite cerrar cuando está ocupado. |
| Falla la carga del historial | `AccountContext` conserva el error; `MisEntradasList` lo muestra con reintento. No informa falsamente que no existen compras. |

## 8. Apariencia: dónde cambiar cada cosa

- React: `frontend/src/styles/global.css` importa las hojas. `theme.css` define colores/tema; `base.css` la base; `layout/`, `pages/` y `components/` agrupan estilos por responsabilidad.
- Adaptación de pantalla: `styles/responsive.css` importa diez hojas de `styles/responsive/` en un orden fijo. Revisar [el mapa de estilos](frontend/src/styles/README.md), no agregar otra regla al azar al final.
- Tarjetas bancarias: `components/common/PaymentCard.jsx` es presentación reutilizada; `utils/payment.js` da formato. Ninguno ejecuta un cobro bancario.
- Swing: `desktop/comun/componentes/MapaButacasPanel.java` dibuja pantalla, huecos, butacas y colores. `SalasModulo` conserva la selección, permisos y operaciones.
- Formularios del dueño: `desktop/duenio/dialogos/`. Anchos y columnas: `desktop/comun/util/ConfiguradorTablas.java`.

## 9. Demostración y límites

Realizar una compra ficticia, copiar su `TCK-...`, entregar productos en Swing y luego validar la entrada. Consultar el historial web para observar el estado. Para cancelar, el servidor verifica propietario, estado y plazo permitido; no basta con que exista un botón.

Presentar honestamente las simulaciones: no hay pasarela bancaria, el correo de recuperación/verificación dentro de la interfaz no demuestra acceso al buzón real y pegar un QR no es probar un lector físico. No usar tarjetas reales ni publicar esta configuración como producción. La documentación de una función no equivale a haber resuelto sus requisitos de seguridad.

## 10. Preguntas breves para practicar

1. ¿Qué diferencia hay entre una página, un componente y un Context?
2. ¿Dónde vive el carrito y por qué no es un ticket pagado?
3. ¿Qué parte dibuja las butacas y qué parte impide la doble venta?
4. ¿Por qué el precio definitivo y los permisos no se deciden en React?
5. ¿Qué diferencia hay entre panel, módulo, diálogo y cliente HTTP en Swing?
6. ¿Por qué se usa `SwingWorker` y qué es el EDT?
7. ¿Qué ocurre al cerrar sesión con una consulta todavía pendiente?
8. ¿Qué operación valida entradas y cuál entrega productos?
9. ¿Qué archivo cambiarías para modificar un color, una URL o una regla de cancelación?
10. ¿Qué funciones son simuladas y qué faltaría antes de publicar el sistema?
