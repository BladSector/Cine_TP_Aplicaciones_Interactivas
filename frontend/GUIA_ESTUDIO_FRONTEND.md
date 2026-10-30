# Guía de estudio de las interfaces de Film Zone

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
4. `src/components/layout/SiteLayout.jsx`: estructura compartida entre páginas.
5. `src/api/client.js`: comunicación con el backend.
6. `src/context/AuthContext.jsx`: sesión del espectador.
7. `src/context/CatalogContext.jsx`: catálogo global.
8. `src/context/PurchaseContext.jsx`: compra en curso.
9. Páginas de cartelera y película.
10. Flujo de compra: acceso, butacas, kiosco y pago.
11. Cuenta, tickets y métodos de pago.
12. Componentes reutilizables, utilidades y CSS.

No conviene comenzar por `global.css`: primero hay que entender qué componente genera cada elemento.

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
- `CandyPage.jsx`: compra exclusiva de productos del kiosco.
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
- `PurchaseContext`: borrador de compra, reservas y carrito.

### `src/hooks`

- `useSeatReservationHeartbeat.js`: consulta periódicamente si las reservas siguen vigentes y calcula el tiempo restante.

### `src/utils`

- `format.js`: fechas, dinero, formatos, idiomas, nombres de butacas y normalización de texto.

### `src/styles`

- `global.css`: tema, layout, componentes y reglas responsive.
- Algunos componentes complejos conservan un CSS propio, como `AccordionGallery` y `Loader`.

## 4. Inicio de la aplicación

`main.jsx` busca el elemento `#root` de `index.html` y monta React con `createRoot`.

Los proveedores se anidan de esta manera:

```jsx
<BrowserRouter>
  <AuthProvider>
    <CatalogProvider>
      <PurchaseProvider>
        <App />
      </PurchaseProvider>
    </CatalogProvider>
  </AuthProvider>
</BrowserRouter>
```

- `BrowserRouter` habilita la navegación por URL.
- `AuthProvider` administra la sesión.
- `CatalogProvider` carga datos del cine.
- `PurchaseProvider` conserva el estado de la compra.
- `App` muestra las rutas.

## 5. Enrutamiento

`AppRoutes.jsx` usa React Router:

- `Route`: relaciona una URL con un componente.
- `:id` o `:movieId`: parámetro dinámico de la URL.
- `Navigate`: redirección controlada.
- `SiteLayout`: ruta contenedora que mantiene encabezado y footer.
- `Outlet`: lugar donde se renderiza la página hija actual.

Ejemplo:

```text
/pelicula/3
```

Abre `PeliculaPage` y `useParams()` obtiene el ID `3`.

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

### Compra

`PurchaseContext.jsx` contiene un `draft` con:

```js
{
  purchaseType,
  movieId,
  functionId,
  selectedSeats,
  consumptions,
  candyVisited,
  returnPath,
  membershipId,
}
```

El borrador se guarda en `sessionStorage`. Sobrevive a una recarga de la pestaña, pero no representa una compra confirmada en la base de datos.

La compra real se crea solamente en la etapa de pago, cuando se ejecuta `api.comprar`, `api.comprarCandy` o `api.comprarMembresia`.

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

La primera butaca inicia una reserva de 10 minutos. `useSeatReservationHeartbeat` consulta las reservas cada 5 segundos y actualiza el contador cada segundo. Si vence, `PurchaseContext` limpia las butacas y los consumos de esa compra.

### Paso 4: kiosco

`CompraCandyPage.jsx` usa `CandyCatalog` para modificar las cantidades guardadas en `draft.consumptions`.

Los productos son opcionales, pero la etapa debe visitarse antes del pago. Eso se controla con `candyVisited`.

### Paso 5: pago

`CompraPagoPage.jsx` coordina la operación final:

- Valida que la función no haya comenzado.
- Valida correo, términos y método de pago.
- Calcula entradas y productos.
- Construye el cuerpo de la petición.
- Ejecuta la compra.
- Limpia el borrador únicamente después de recibir una respuesta correcta.
- Recarga catálogo y espectador.
- Muestra el ticket resultante.

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

Permite consumir sesión, catálogo o compra mediante `useAuth`, `useCatalog` y `usePurchase`.

### `useRef`

Conserva una referencia sin provocar renderizados. Se usa para enfocar inputs, detectar clics fuera y recordar una ruta previa.

## 11. Props, estado y datos derivados

- **Props:** información que un componente padre entrega a un hijo.
- **Estado:** información que puede cambiar y provoca un nuevo renderizado.
- **Dato derivado:** valor calculado a partir de props o estado; no debe duplicarse innecesariamente en otro `useState`.

Ejemplo: el total del kiosco se calcula a partir de productos y cantidades. No hace falta guardarlo como estado independiente.

## 12. Formularios y validación

Los inputs son controlados: su `value` proviene del estado y `onChange` actualiza ese estado.

Existen dos niveles de validación:

- HTML/React: `required`, `type="email"`, `minLength`, formato y coincidencia de contraseñas.
- Backend: reglas definitivas como correo existente, tarjeta vencida, función pasada o butaca ocupada.

El frontend ayuda al usuario, pero no reemplaza las reglas del backend.

## 13. CSS y responsive

`global.css` utiliza:

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
3. Compra: `PeliculaPage` -> `PurchaseContext` -> butacas -> kiosco -> pago -> ticket.
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

- `database/seed_demo.sql`: crea la base, tablas, películas, salas, butacas y productos.
- `database/completar_funciones_2_semanas.sql`: agrega funciones para dos semanas sin borrar compras ni usuarios.
- `database/descripcionPeliculas.txt`: textos usados para las películas.
- `database/portadasPeliculas/`: archivos originales de las portadas.

## 18. Estructura de React archivo por archivo

### Inicio y navegación

- `frontend/src/main.jsx`: monta React, define el tema inicial y registra los tres Context globales.
- `frontend/src/App.jsx`: agrega el comportamiento de desplazamiento y las rutas.
- `frontend/src/routes/AppRoutes.jsx`: relaciona cada URL con su página.
- `frontend/src/components/common/ScrollToTop.jsx`: sube la página al cambiar de ruta cuando corresponde.

### Comunicación y estado

- `frontend/src/api/client.js`: único punto de comunicación HTTP con Spring Boot.
- `frontend/src/context/AuthContext.jsx`: restaura la sesión, inicia y cierra sesión, registra al espectador y verifica el correo.
- `frontend/src/context/CatalogContext.jsx`: carga películas, funciones, salas, butacas, entradas y productos.
- `frontend/src/context/PurchaseContext.jsx`: conserva el borrador, carrito, butacas reservadas y vencimiento de la reserva.
- `frontend/src/hooks/useSeatReservationHeartbeat.js`: mantiene actualizada la reserva temporal de butacas.

### Páginas

- `CarteleraPage.jsx`: portada principal, filtros y listado de películas con funciones.
- `PeliculaPage.jsx`: ficha de una película, días, formatos, idiomas y horarios.
- `CandyPage.jsx`: compra independiente de productos del kiosco.
- `ClubPage.jsx`: planes de membresía y cambios de plan.
- `CuentaPage.jsx`: acceso a perfil, tarjetas, membresía y tickets.
- `AboutPage.jsx`: información de Film Zone y del equipo.
- `LegalPage.jsx`: política de privacidad y normas del cine.
- `PlaceholderPage.jsx`: pantalla temporal para secciones aún no desarrolladas.

### Etapas de compra

- `pages/compra/CompraAccesoPage.jsx`: entrada protegida al flujo cuando falta autenticación.
- `pages/compra/CompraButacasPage.jsx`: carga la sala, reserva y libera butacas.
- `pages/compra/CompraCandyPage.jsx`: incorpora productos a una compra de entradas.
- `pages/compra/CompraPagoPage.jsx`: valida datos, realiza la compra y muestra la confirmación.

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
- `ExpiryMonthPicker.jsx`: selector de mes y año de vencimiento.
- `CheckoutMoviePreview.jsx`: portada e información breve durante el pago.
- `PurchaseStepper.jsx`: representación de etapas del flujo.

### Cuenta, tickets y estructura visual

- `PurchaseAuthDrawer.jsx`: ventana lateral de login, registro, recuperación y verificación.
- `PerfilForm.jsx`: edición de datos y cambio simulado de contraseña por correo.
- `MetodosPagoList.jsx`: alta y baja de tarjetas guardadas.
- `MisEntradasList.jsx`: historial de compras y apertura del ticket.
- `MembresiaPanel.jsx`: información del plan actual.
- `TicketDetails.jsx`: QR, código manual, entradas y consumos del ticket.
- `SiteLayout.jsx`: encabezado, navegación, carrito, sesión y contenido común.
- `HeaderMovieSearch.jsx`: buscador desplegable de películas.
- `SiteFooter.jsx`: enlaces institucionales y legales.
- `Modal.jsx`, `ConfirmDialog.jsx` y `Loader.jsx`: piezas comunes para ventanas, confirmaciones y carga.
- `styles/global.css`: estilo global, modo oscuro y responsive.
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
- Envía `POST /sesion/personal` mediante `ApiClient`.
- Consulta la sesión creada con `GET /sesion/personal`.
- Lee el rol devuelto por el backend.
- Abre `PrincipalFrame` como Dueño o Empleado.
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

Es la ventana principal compartida. Su contenido cambia según el rol.

- Construye la navegación del Dueño y del Empleado.
- Crea tablas y sus `DefaultTableModel`.
- Carga salas, funciones, butacas, tickets, consumos, empleados y auditorías.
- Filtra y ordena tablas con `TableRowSorter`.
- Muestra el plano de butacas.
- Procesa validación de entradas, entrega de consumos y reembolsos.
- Gestiona avisos de salas y butacas fuera de servicio.
- Ejecuta llamadas HTTP en segundo plano.
- Controla cierre de sesión y salida de la aplicación.

### `desktop/duenio/AdministracionPanel.java`

Agrupa los CRUD exclusivos del dueño.

- Categorías.
- Películas y carga local de imágenes.
- Funciones.
- Salas y editor dinámico de distribución.
- Productos de confitería.

`EditorDistribucionSala`, clase interna de este archivo, crea la pantalla y la matriz editable. Cada posición puede convertirse en butaca o espacio vacío.

## 21. Módulos del Dueño en Swing

`PrincipalFrame.crearVistaDuenio()` usa un menú lateral y `CardLayout` para mostrar una sección por vez.

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

`PrincipalFrame.crearVistaEmpleado()` muestra únicamente tareas operativas:

- **Control de acceso:** busca el ticket, lee o pega el QR y valida las entradas.
- **Consumos:** entrega productos manualmente o mediante QR.
- **Funciones:** consulta la programación y, cuando está permitido, modifica fecha u horario con advertencia.
- **Salas:** consulta funciones y cambia el estado de una sala con descripción y aviso.
- **Butacas:** visualiza el plano, marca una butaca fuera de servicio y vuelve a habilitarla.

El empleado no puede ocupar ni liberar manualmente una butaca vendida. Tampoco accede a empleados, auditoría ni CRUD completo del catálogo.

## 23. Conceptos de Swing que hay que poder explicar

### `JFrame` y `JPanel`

`JFrame` es una ventana. `JPanel` organiza componentes dentro de ella. `LoginFrame` y `PrincipalFrame` son ventanas; `AdministracionPanel` es un panel reutilizado dentro de la ventana principal.

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
  -> ApiClient.post("/sesion/personal")
  -> SesionController
  -> SesionService
  -> respuesta con usuario y rol
  -> PrincipalFrame correspondiente
```

### Validación de entrada por QR

```text
Empleado pega o escanea código
  -> PrincipalFrame busca /tickets/qr/{codigo}
  -> muestra película, función, sala y estado
  -> empleado confirma
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
  -> agrega comentario opcional y destinatario
  -> API guarda el cambio y crea un aviso
  -> otros empleados reciben el aviso periódico
```

### CRUD del dueño

```text
Dueño abre un módulo
  -> selecciona Crear, Editar o Eliminar
  -> AdministracionPanel arma el formulario
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
| Reutilización | Componentes y props | Métodos constructores de paneles |
| Estilo | CSS responsive | Layout managers y Look and Feel |
| Datos recibidos | Objetos JavaScript | `JsonNode` |

## 26. Cómo iniciar todo en una PC nueva

### Requisitos

- Java 17.
- Maven.
- MySQL Server y MySQL Workbench.
- Node.js 18 o superior.

### Preparar MySQL

1. Ejecutar `database/seed_demo.sql`.
2. Ejecutar `database/completar_funciones_2_semanas.sql`.

### Iniciar el backend en PowerShell

```powershell
cd "C:\ruta\TP_Cine_API"
$env:DB_URL="jdbc:mysql://localhost:3307/tp_cine_api"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="CineAPI"
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
mvn.cmd exec:java
```

El backend debe continuar ejecutándose mientras se usan React o Swing.

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

- `CineDesktopApplication`, `LoginFrame` y `PrincipalFrame`.
- `AdministracionPanel` y editor de salas.
- Validación por QR, consumos, salas, butacas y avisos.
- Diferencias de permisos entre Dueño y Empleado.

Todos deberían conocer el recorrido completo, aunque cada uno profundice una sección.

## 28. Guion corto para la exposición

1. Film Zone tiene una interfaz React para espectadores y una interfaz Swing para el personal.
2. Ambas consumen una API REST desarrollada con Spring Boot.
3. El backend separa controllers, services, repositories y entidades.
4. React administra sesión, catálogo y compra mediante Context.
5. Una compra no se guarda hasta confirmar el pago; antes hay un borrador y reservas temporales.
6. Swing identifica automáticamente el rol al iniciar sesión.
7. El dueño administra el cine y el empleado realiza tareas operativas.
8. Los tickets incorporan QR para validar entradas y entregar consumos.
9. MySQL conserva usuarios, cartelera, salas, ventas, tickets y auditoría.

## 29. Preguntas posibles sobre Swing

### ¿Swing se conecta directamente a MySQL?

No. Swing llama a la API mediante `ApiClient`. El backend valida y accede a MySQL mediante services y repositories.

### ¿Cómo se distingue al dueño del empleado?

`LoginFrame` inicia sesión y consulta el rol. `PrincipalFrame` construye el menú correspondiente sin pedir al usuario que elija su perfil.

### ¿Por qué se usa `SwingWorker`?

Para ejecutar las peticiones HTTP fuera del hilo de interfaz y evitar que la ventana quede congelada.

### ¿Cómo se actualizan las tablas?

Los métodos `cargarSalas`, `cargarFunciones`, `cargarTickets` y similares consultan la API, limpian el `DefaultTableModel` y agregan las filas recibidas.

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
