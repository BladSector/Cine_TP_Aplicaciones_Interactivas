# Backend: mapa del código

Esta guía explica dónde está cada parte de la API y cómo seguir una operación. La interfaz React y
el cliente Swing se explican en [GUIA_FRONTEND.md](GUIA_FRONTEND.md). Las rutas de archivos parten
de la raíz del repositorio.

## Índice

- [Estructura y responsabilidades](#estructura-y-responsabilidades)
- [Configuración y seguridad](#configuración-y-seguridad)
- [Controllers: entrada HTTP](#controllers-entrada-http)
- [Services: operaciones y reglas](#services-operaciones-y-reglas)
- [Repositories: acceso a datos](#repositories-acceso-a-datos)
- [Modelo: entidades y relaciones](#modelo-entidades-y-relaciones)
- [Enums y conversor](#enums-y-conversor)
- [Recorridos para estudiar](#recorridos-para-estudiar)
- [Base de datos y recursos](#base-de-datos-y-recursos)

## Estructura y responsabilidades

```text
React o Swing
  -> petición HTTP + cookie de sesión
  -> interceptores: autorización y registro de actividad
  -> controller: recibe datos y prepara la respuesta
  -> service: valida y coordina la operación
  -> repository: consulta o guarda entidades
  -> MySQL
```

**`com/tp/cine/`**

Arranque de Spring Boot, configuración e interceptores.

**`controller/`**

URLs, métodos HTTP, datos de entrada/salida y comprobaciones de sesión/propiedad.

**`service/`**

Reglas del negocio, cálculos, transacciones y coordinación entre entidades.

**`repository/`**

Consultas y persistencia con Spring Data JPA.

**`modelo/entidades/`**

Objetos persistentes y sus asociaciones. También tienen métodos para cambiar sus propios
datos/estados.

**`modelo/enums/`**

Valores permitidos de estados, formatos, roles y permisos.

**`modelo/conversores/`**

Conversión de un tipo Java al valor que se guarda en la base.

**`desktop/`**

Cliente Swing, no backend. Se documenta en la guía del frontend.

`cartelera`, `instalaciones`, `usuarios`, `ventas` y `administracion` agrupan por tema dentro de
las capas. Por ejemplo, una función tiene su controller, service y repository en las respectivas
subcarpetas `cartelera/`.

### Cómo leer los nombres y anotaciones

- `@RestController` y `@RequestMapping` exponen una URL; `@GetMapping`, `@PostMapping`,
  `@PutMapping` y `@DeleteMapping` identifican la operación HTTP.
- Los `record...Request` y `...Response` dentro de los controllers transportan datos. No son
  entidades ni crean tablas. Los métodos `desde`/`crearResponse` preparan la representación que
  recibe el cliente.
- `@Service` identifica una clase de operaciones. Sus dependencias llegan por constructor,
  administradas por Spring.
- `@Transactional` agrupa cambios de base de datos: ante un fallo que revierte la transacción, no
  queda guardada solo una parte de esa operación. No equivale a una transacción bancaria.
- `@Entity`, `@Id`, `@ManyToOne`, `@OneToMany` y `@JoinColumn` describen tablas, identificadores
  y relaciones JPA.
- En los CRUD, `listar`, `buscarPorId`, `guardar`, `actualizar` y `eliminar` significan consultar
  todos, consultar uno, crear, modificar y borrar. Las fichas siguientes destacan lo específico de
  cada archivo, sin repetir ese significado.
- Los getters entregan atributos; los constructores crean objetos. No se enumeran uno por uno. Los
  métodos `validar...` privados apoyan la operación pública del service.

## Configuración y seguridad

**[pom.xml](pom.xml)**

Dependencias Java, Java 17, Spring Boot, persistencia, correo, PDF/QR y punto de entrada del cliente
Swing. No define las pantallas web.

**[application.properties](src/main/resources/application.properties)**

URL/usuario/contraseña de MySQL mediante `DB_*`, configuración JPA, credenciales del dueño y
opciones `MAIL_*`. `ddl-auto=update` adapta el esquema; no completa la cartelera.

**[TpCineApiApplication.java](src/main/java/com/tp/cine/TpCineApiApplication.java)**

`main`: inicia Spring Boot y el descubrimiento de componentes del proyecto.

**[PasswordConfig.java](src/main/java/com/tp/cine/config/PasswordConfig.java)**

`passwordEncoder`: proporciona el codificador de contraseñas.

**[WebConfig.java](src/main/java/com/tp/cine/config/WebConfig.java)**

`addInterceptors`: registra los controles que se ejecutan alrededor de los endpoints.

**[AdminWriteInterceptor.java](src/main/java/com/tp/cine/seguridad/AdminWriteInterceptor.java)**

`preHandle` y `permisoRequerido`: identifican el permiso exigido por la ruta/operación y comprueban
la sesión antes del controller.

**[AuditoriaInterceptor.java](src/main/java/com/tp/cine/seguridad/AuditoriaInterceptor.java)**

`preHandle` conserva el actor y `afterCompletion` registra las operaciones auditables; los métodos
`resolver...` identifican acción y entidad.

Los permisos no dependen de ocultar botones. El interceptor, `SesionService` y las comprobaciones de
cada controller protegen la API. La cookie identifica la sesión; enviar un `espectadorId` en JSON no
autentica a ese espectador.

## Controllers: entrada HTTP

Todos están en `src/main/java/controller/`. Para localizar la URL de una acción, combinar el
`@RequestMapping` de la clase con el mapping de su método.

### `cartelera/`

**[CategoriaController.java](src/main/java/controller/cartelera/CategoriaController.java)**

`/categorias`: CRUD de géneros/categorías.

**[PeliculaController.java](src/main/java/controller/cartelera/PeliculaController.java)**

`/peliculas`: CRUD de películas; convierte categoría y portada en datos de respuesta.

**[FuncionController.java](src/main/java/controller/cartelera/FuncionController.java)**

`/funciones`: CRUD; `actualizarHorario`, `contarEntradasActivas` y `listarButacasOcupadas`. La
última consulta devuelve IDs ocupados para una función, no datos privados de compradores.

**[PrecioController.java](src/main/java/controller/cartelera/PrecioController.java)**

`/precios`: `calcularPrecioEntrada` y `calcularTotal` delegan la cotización en `PrecioService`.

### `instalaciones/`

**[SalaController.java](src/main/java/controller/instalaciones/SalaController.java)**

`/salas`: CRUD y creación/edición con matriz mediante `guardarConMatriz`/`actualizarConMatriz`;
`cambiarEstado` coordina el cambio y su aviso/actividad.

**[ButacaController.java](src/main/java/controller/instalaciones/ButacaController.java)**

`/butacas`: CRUD y operaciones de estado. `marcarFueraDeServicio`/`habilitar` se usan para
mantenimiento; `bloquear`, `ocupar` y `liberar` son operaciones distintas de la reserva por
función.

**[AvisoSalaController.java](src/main/java/controller/instalaciones/AvisoSalaController.java)**

`/avisos-sala`: `listarHistorial`, `listarPendientes`, `marcarLeido`; usa la identidad del
empleado de la sesión.

### `usuarios/`

**[SesionController.java](src/main/java/controller/usuarios/SesionController.java)**

`/sesion`: `iniciarPersonal` distingue dueño/empleado; `obtenerPersonalActual` consulta la sesión y
`cerrar` la invalida. Conserva accesos `iniciarAdmin`/`iniciarEmpleado`.

**[EspectadorController.java](src/main/java/controller/usuarios/EspectadorController.java)**

`/espectadores`: registro, `login`, edición, `solicitarRecuperacion`/`recuperarContrasenia` y
verificación de correo por ID o token. `validarPropietarioOAdmin` protege datos de la cuenta.

**[MetodoDePagoController.java](src/main/java/controller/usuarios/MetodoDePagoController.java)**

`/metodos-pago`: listado/alta/edición/baja. `obtenerTitular` adapta el nombre recibido y comprueba
el propietario o administrador.

**[EmpleadoController.java](src/main/java/controller/usuarios/EmpleadoController.java)**

`/empleados`: alta, edición, consulta, `activar`, `desactivar` y `restablecerContrasenia`. No
ofrece crear otro dueño.

### `ventas/`

**[CompraController.java](src/main/java/controller/ventas/CompraController.java)**

`/compras`: `comprar` recibe entradas y productos; `comprarCandy` solo productos;
`comprarMembresia` un plan. Comprueba la sesión del comprador y devuelve el resultado del service.

**[ReservaButacaController.java](src/main/java/controller/ventas/ReservaButacaController.java)**

`/reservas-butacas`: `reservar`, `listar` por función y `liberar`. La respuesta indica butaca,
vencimiento y si la reserva es propia.

**[TicketController.java](src/main/java/controller/ventas/TicketController.java)**

`/tickets`: consulta por ID/QR, `descargarPdf`, `validarEntradas`, `entregarConsumos`,
`reembolsar` y `cancelarCompra`. `crearResponse` reúne estado calculado, nombres y condiciones de
cancelación. También conserva operaciones separadas de creación/asociación y `procesarIngreso`.

**[EntradaController.java](src/main/java/controller/ventas/EntradaController.java)**

`/entradas`: consulta y operaciones individuales `vender`, `pagar`, `escanear`, `reembolsar`,
`cancelar` y eliminación. No es el coordinador de la compra web completa.

**[ItemConsumoController.java](src/main/java/controller/ventas/ItemConsumoController.java)**

`/items-consumo`: CRUD de ítems y `entregar` un consumo individual.

**[ProductoConfiteriaController.java](src/main/java/controller/ventas/ProductoConfiteriaController.java)**

`/productos-confiteria`: CRUD del catálogo de productos.

**[MembresiaController.java](src/main/java/controller/ventas/MembresiaController.java)**

`/membresias`: `listar` planes, `actual` del espectador, `cotizar` un cambio y `darDeBaja`. La
contratación entra por `CompraController`.

### `administracion/` y `comun/`

**[AuditoriaController.java](src/main/java/controller/administracion/AuditoriaController.java)**

`/auditorias`: `listar` actividad para el dueño.

**[ApiExceptionHandler.java](src/main/java/controller/comun/ApiExceptionHandler.java)**

Maneja errores de estado HTTP, JSON inválido e integridad de datos. `crearError` prepara la
respuesta común de error.

## Services: operaciones y reglas

Todos están en `src/main/java/service/`. Aquí se empieza a leer cuando la pregunta es por qué una
operación se permite, se rechaza o calcula un importe.

### `cartelera/`

**[CategoriaService.java](src/main/java/service/cartelera/CategoriaService.java)**

CRUD; `validarNombre` evita categorías sin nombre.

**[PeliculaService.java](src/main/java/service/cartelera/PeliculaService.java)**

CRUD; `validarDatos` comprueba los datos y `buscarCategoria` resuelve una categoría existente antes
de guardar.

**[FuncionService.java](src/main/java/service/cartelera/FuncionService.java)**

`guardar`/`actualizar`/`actualizarHorario` validan fecha, película, sala y precio.
`validarDisponibilidadSala` compara intervalos de duración de las funciones de esa sala en la fecha
consultada.

**[PrecioService.java](src/main/java/service/cartelera/PrecioService.java)**

`calcularPrecioUnitario` aplica recargo 3D del 20% y descuento del 10% a cliente frecuente;
`esClienteFrecuente` cuenta al menos cinco entradas pagadas/escaneadas. `calcularTotal` aplica 2x1
los miércoles.

### `instalaciones/`

**[SalaService.java](src/main/java/service/instalaciones/SalaService.java)**

`guardarConMatriz`, `actualizarConMatriz` y `actualizarDistribucion` crean/adaptan butacas y
espacios; validan nombre/posiciones y referencias existentes. `cambiarEstado` modifica la situación
operativa.

**[ButacaService.java](src/main/java/service/instalaciones/ButacaService.java)**

CRUD y estado físico de la butaca; `marcarFueraDeServicio`, `habilitarFueraDeServicio`, `bloquear`
, `ocupar`, `liberar`. No confundir este estado con una entrada vendida para una función concreta.

**[AvisoSalaService.java](src/main/java/service/instalaciones/AvisoSalaService.java)**

`notificarCambio` y `notificarButacaFueraDeServicio` generan avisos para destinatarios.
`listarPendientes`, `listarHistorial` y `marcarLeido` conservan el historial en vez de borrarlo.

### `usuarios/`

**[AdminService.java](src/main/java/service/usuarios/AdminService.java)**

`credencialesValidas` compara las credenciales configuradas; `iniciarSesion` establece la sesión del
dueño. No requiere crear un empleado con rol de dueño.

**[EmpleadoService.java](src/main/java/service/usuarios/EmpleadoService.java)**

`guardar`, `actualizar`, `autenticar`, `activar`, `desactivar` y `restablecerContrasenia`.
Comprueba usuario, datos y contraseña; la nueva tiene un mínimo de cuatro caracteres.

**[EspectadorService.java](src/main/java/service/usuarios/EspectadorService.java)**

`guardar` registra; `autenticar` comprueba acceso; `actualizar` modifica perfil y solicita nueva
verificación al cambiar correo. `solicitarRecuperacionContrasenia`, `recuperarContrasenia` y
`verificarMail` implementan esos flujos.

**[MetodoDePagoService.java](src/main/java/service/usuarios/MetodoDePagoService.java)**

`validarDatos` comprueba tarjeta/titular; `validarParaCompra` revisa dueño, actividad y vencimiento.
`eliminar` desactiva con `activa=false`; `crearResumen` prepara el texto del comprobante.

**[SesionService.java](src/main/java/service/usuarios/SesionService.java)**

`iniciarEspectador`/`iniciarAdministrador`/`iniciarEmpleado` establecen atributos de `HttpSession`
; `validar...` exige identidad/rol, `validarPermiso` autoriza y `cerrar` invalida la sesión.

**[PermisoService.java](src/main/java/service/usuarios/PermisoService.java)**

`tienePermiso` relaciona rol del personal con acciones permitidas.

### `ventas/`

**[CompraService.java](src/main/java/service/ventas/CompraService.java)**

`comprar` coordina compra atómica de varias entradas y productos; `comprarConsumibles` compra solo
Candy. Busca espectador verificado, valida tarjeta, bloquea butacas, calcula precio y crea
ticket/entradas/ítems. `desactivarMetodoTemporal` aplica la opción de no guardar tarjeta.

**[ReservaButacaService.java](src/main/java/service/ventas/ReservaButacaService.java)**

`reservar` retiene una butaca por función durante diez minutos; `listarActivas`, `liberar`,
`validarDisponibilidadParaCompra` y `liberarTrasCompra` gestionan vigencia y compra.

**[EntradaService.java](src/main/java/service/ventas/EntradaService.java)**

`validarVenta` comprueba función no pasada, sala/butaca y disponibilidad. `crearEntradaPagada` se
usa en la compra; `escanear`, `reembolsar` y `cancelar` cambian estados. También conserva
venta/pago individual.

**[ItemConsumoService.java](src/main/java/service/ventas/ItemConsumoService.java)**

CRUD de productos asociados al ticket; `validarCantidad`, `validarPendiente`, `entregar`,
`cancelar` y `calcularSubtotal`.

**[TicketService.java](src/main/java/service/ventas/TicketService.java)**

`crearTicket` genera `TCK-...`; consulta, asociación y PDF. `validarEntradas` y `entregarConsumos`
son operaciones separadas; `procesarIngreso` conserva la operación combinada. `calcularEstado`
deriva el estado, `evaluarCancelacion` calcula el permiso y `cancelarPorEspectador`/`reembolsar`
cancelan contenido.

**[ProductoConfiteriaService.java](src/main/java/service/ventas/ProductoConfiteriaService.java)**

CRUD del catálogo; `validarDatos` comprueba nombre, precio, tipo y tamaño, y normaliza campos
opcionales.

**[MembresiaService.java](src/main/java/service/ventas/MembresiaService.java)**

`buscarVigente`, `cotizar`, `crearCotizacion`, `comprar` y `darDeBaja` gestionan planes.
`aplicarCambiosProgramados` ejecuta los cambios pendientes; `Cotizacion` y `TipoOperacion` describen
el resultado, no son tablas.

**[MembresiaInicializador.java](src/main/java/service/ventas/MembresiaInicializador.java)**

`run` comprueba/completa los planes iniciales al arrancar. No crea cuentas de prueba.

### `administracion/` y `comunicacion/`

**[AuditoriaService.java](src/main/java/service/administracion/AuditoriaService.java)**

`obtenerActor`, `registrar` y `listar` conservan quién realizó una acción y su detalle.

**[MigracionRolEmpleado.java](src/main/java/service/administracion/MigracionRolEmpleado.java)**

`run` adapta roles antiguos del personal al modelo simplificado. Es compatibilidad de datos, no una
pantalla.

**[EmailService.java](src/main/java/service/comunicacion/EmailService.java)**

`enviarConfirmacionCuenta`, `enviarRecuperacionContrasenia`, `enviarTicket` y
`enviarConfirmacionMembresia`; `enviar`/`enviarConAdjunto` centralizan correo y adjuntos según
configuración.

**[TicketPdfService.java](src/main/java/service/comunicacion/TicketPdfService.java)**

`generar` dibuja el comprobante; `generarQr` produce la imagen del código. Aquí cambia el PDF, no en
el CSS de React.

**[TicketCompradoEvent.java](src/main/java/service/comunicacion/TicketCompradoEvent.java)**

`record` que transporta el ID de una compra confirmada hacia su listener.

**[TicketEmailListener.java](src/main/java/service/comunicacion/TicketEmailListener.java)**

`enviarTicket` atiende el evento después del commit, obtiene el ticket y solicita correo/PDF. El
envío no anticipa una compra que todavía pueda revertirse.

**[MembresiaAdquiridaEvent.java](src/main/java/service/comunicacion/MembresiaAdquiridaEvent.java)**

`record` con el ID de la suscripción adquirida.

**[MembresiaEmailListener.java](src/main/java/service/comunicacion/MembresiaEmailListener.java)**

`enviarConfirmacion` procesa el evento de membresía y solicita su correo.

## Repositories: acceso a datos

Todos están en `src/main/java/repository/` y extienden `JpaRepository`. Heredan `findAll`,
`findById`, `save`, `delete` y otras operaciones: no hay que escribir una implementación manual de
cada interfaz. Los nombres `findBy...` expresan filtros; algunas consultas específicas usan `@Query`
y bloqueos.

**[cartelera/CategoriaRepository.java](src/main/java/repository/cartelera/CategoriaRepository.java)**

CRUD heredado de categorías.

**[cartelera/PeliculaRepository.java](src/main/java/repository/cartelera/PeliculaRepository.java)**

CRUD heredado de películas.

**[cartelera/FuncionRepository.java](src/main/java/repository/cartelera/FuncionRepository.java)**

`findBySalaIdAndFecha`: programación de una sala para comprobar superposición.

**[instalaciones/SalaRepository.java](src/main/java/repository/instalaciones/SalaRepository.java)**

CRUD heredado de salas.

**[instalaciones/ButacaRepository.java](src/main/java/repository/instalaciones/ButacaRepository.java)**

`findBySalaId`; `buscarPorIdsConBloqueo`/`buscarPorIdConBloqueo` para operaciones concurrentes
sobre butacas.

**[instalaciones/AvisoSalaRepository.java](src/main/java/repository/instalaciones/AvisoSalaRepository.java)**

Avisos de un destinatario, pendientes o historial, ordenados por fecha.

**[usuarios/EspectadorRepository.java](src/main/java/repository/usuarios/EspectadorRepository.java)**

`findByEmailIgnoreCase`, `existsByEmailIgnoreCase` y búsqueda por tokens de
verificación/recuperación.

**[usuarios/EmpleadoRepository.java](src/main/java/repository/usuarios/EmpleadoRepository.java)**

Búsqueda/duplicados por usuario sin distinguir mayúsculas y empleados activos ordenados.

**[usuarios/MetodoDePagoRepository.java](src/main/java/repository/usuarios/MetodoDePagoRepository.java)**

`findByEspectadorIdAndNumero`: tarjeta del espectador y control de duplicados.

**[ventas/EntradaRepository.java](src/main/java/repository/ventas/EntradaRepository.java)**

Entradas por espectador, conteos por estado/función, `buscarButacasOcupadas` y
`existeEntradaActivaParaButaca`.

**[ventas/TicketRepository.java](src/main/java/repository/ventas/TicketRepository.java)**

`findByEspectadorId` y `findByCodigoQR`: historial propio o identificación del comprobante.

**[ventas/ItemConsumoRepository.java](src/main/java/repository/ventas/ItemConsumoRepository.java)**

CRUD heredado de ítems.

**[ventas/ProductoConfiteriaRepository.java](src/main/java/repository/ventas/ProductoConfiteriaRepository.java)**

CRUD heredado de productos.

**[ventas/ReservaButacaRepository.java](src/main/java/repository/ventas/ReservaButacaRepository.java)**

Consulta por función/butaca y `deleteByVenceEnLessThanEqual` para reservas vencidas.

**[ventas/MembresiaRepository.java](src/main/java/repository/ventas/MembresiaRepository.java)**

`findAllByOrderByPrecioMensualAsc`: planes ordenados por precio.

**[ventas/SuscripcionRepository.java](src/main/java/repository/ventas/SuscripcionRepository.java)**

Suscripción activa, `bloquearActivas` y consulta de suscripciones vencidas con cambio programado.

**[administracion/AuditoriaRepository.java](src/main/java/repository/administracion/AuditoriaRepository.java)**

`findAllByOrderByFechaHoraDesc`: actividad más reciente primero.

## Modelo: entidades y relaciones

`Butaca.espacioSillaDeRuedas` identifica un espacio sin asiento físico para una persona en silla de
ruedas. Es independiente del estado de disponibilidad. Su columna booleana tiene valor inicial
`false` para conservar las butacas existentes; Hibernate la agrega al actualizar el esquema.
Las ubicaciones de `/salas/con-matriz` y `/salas/{id}/distribucion` admiten ese campo. Si se omite
en una edición antigua, se conserva el valor existente. `SalaService` impide cambiar el tipo con
entradas futuras o reservas vigentes, bajo el bloqueo de butacas compartido con las compras.

Todos los archivos siguientes están en `src/main/java/modelo/entidades/`. Un atributo asociado con
`@JoinColumn` contiene la clave foránea. Una colección con `mappedBy` es el lado inverso de esa
relación, no otra columna con todos los IDs.

**[Categoria.java](src/main/java/modelo/entidades/Categoria.java)**

Género/categoría; colección de películas. `actualizarNombre` y `agregarPelicula`.

**[Pelicula.java](src/main/java/modelo/entidades/Pelicula.java)**

Título, duración, descripción y portada; FK `categoria_id`. `actualizarDatos`.

**[Funcion.java](src/main/java/modelo/entidades/Funcion.java)**

Película programada en fecha/horario, formato, idioma y precio; FK `pelicula_id` y `sala_id`.
`actualizarDatos`, `agregarEntrada`.

**[Sala.java](src/main/java/modelo/entidades/Sala.java)**

Nombre, capacidad/distribución y estado; colección de butacas. `actualizarDatos`, `agregarButaca`,
`actualizarEstado`.

**[Butaca.java](src/main/java/modelo/entidades/Butaca.java)**

Posición física, fila/número y estado; FK `sala_id`. `bloquear`, `ocupar`, `liberarButaca`,
`marcarFueraDeServicio` cambian sus propios datos.

**[Espectador.java](src/main/java/modelo/entidades/Espectador.java)**

Cuenta del cliente, correo verificado y tokens; colecciones de entradas y métodos de pago.
`actualizarDatos`, `verificarMail`, `requerirNuevaVerificacionDeEmail`.

**[Empleado.java](src/main/java/modelo/entidades/Empleado.java)**

Cuenta del personal, rol y actividad. `actualizarDatos`, `cambiarContrasenia`, `activar`,
`desactivar`.

**[MetodoDePago.java](src/main/java/modelo/entidades/MetodoDePago.java)**

Tarjeta del cliente; FK `espectador_id`, vencimiento y bandera `activa`. `actualizarDatos`,
`asignarEspectador`, `desactivar`.

**[Ticket.java](src/main/java/modelo/entidades/Ticket.java)**

Agrupa la compra y su QR; FK `espectador_id` y `metodo_pago_id`; guarda además un resumen textual
del pago. `agregarEntrada`/`agregarItem` mantienen las asociaciones.

**[Entrada.java](src/main/java/modelo/entidades/Entrada.java)**

Una butaca vendida para una función; precio y estado. FK `espectador_id`, `ticket_id`,
`funcion_id`, `butaca_id`. `pagar`, `escanear`, `reembolsarEntrada`, `cancelar`.

**[ProductoConfiteria.java](src/main/java/modelo/entidades/ProductoConfiteria.java)**

Producto del Candy con nombre, descripción, precio, tipo, tamaño e imagen. `actualizarDatos`. No
confundir con una unidad comprada.

**[ItemConsumo.java](src/main/java/modelo/entidades/ItemConsumo.java)**

Producto y cantidad comprados, con estado de entrega; FK `producto_id` y `ticket_id`.
`actualizarDatos`, `asignarTicket`, `actualizarEstado`.

**[ReservaButaca.java](src/main/java/modelo/entidades/ReservaButaca.java)**

Retención temporal; FK `funcion_id`, `butaca_id`, `espectador_id`. `renovar` y `estaVencida`.
Todavía no es una entrada pagada.

**[Membresia.java](src/main/java/modelo/entidades/Membresia.java)**

Plan ofrecido, precio, descripción y beneficios. `listarBeneficios` adapta sus beneficios para
mostrarlos.

**[Suscripcion.java](src/main/java/modelo/entidades/Suscripcion.java)**

Plan contratado, importe y vigencia; FK a espectador, membresía y método de pago. Guarda
opcionalmente plan/método programados. `desactivar`, `programarCambio`, `tieneCambioProgramado`.

**[AvisoSala.java](src/main/java/modelo/entidades/AvisoSala.java)**

Incidencia, estado, fecha y lectura; FK `sala_id`. Los IDs/nombres del emisor y destinatario son
campos escalares, no asociaciones JPA con `Empleado`. `marcarLeido`.

**[Auditoria.java](src/main/java/modelo/entidades/Auditoria.java)**

Registro histórico del actor, acción, entidad, detalle y fecha. `empleadoId` es un dato escalar de
referencia, no una relación JPA.

**Diferencia central:** la sala tiene butacas físicas; la función utiliza esa sala; la entrada
vincula función y butaca. Una venta no debe ocupar esa misma butaca en todas las funciones. El
service consulta entradas y reservas de la función elegida.

El estado público del ticket se calcula en `TicketService.calcularEstado`, no se mantiene como otro
campo que deba actualizarse junto con cada entrada o consumo. Algunos importes de consumos se
calculan usando el precio del producto relacionado: no todo el detalle es una copia histórica
inmutable.

## Enums y conversor

**[enums/EstadoButaca.java](src/main/java/modelo/enums/EstadoButaca.java)**

Estado físico/operativo de una butaca. No reemplaza la disponibilidad por función.

**[enums/EstadoSala.java](src/main/java/modelo/enums/EstadoSala.java)**

Estados operativos de sala, incluidos limpieza y problema particular.

**[enums/EstadoEntrada.java](src/main/java/modelo/enums/EstadoEntrada.java)**

Ciclo de la entrada individual: pago, escaneo, cancelación/reembolso.

**[enums/EstadoConsumo.java](src/main/java/modelo/enums/EstadoConsumo.java)**

Pendiente, entregado o cancelado.

**[enums/EstadoTicket.java](src/main/java/modelo/enums/EstadoTicket.java)**

Resultado global: `PENDIENTE_DE_INGRESO`, `CONSUMOS_PENDIENTES`, `PROCESADO` o `REEMBOLSADO`.

**[enums/FormatoFuncion.java](src/main/java/modelo/enums/FormatoFuncion.java)**

Códigos de formato 2D/3D.

**[enums/IdiomaFuncion.java](src/main/java/modelo/enums/IdiomaFuncion.java)**

Idioma/versión de la función. La interfaz traduce los códigos a etiquetas legibles.

**[enums/TipoProductoConfiteria.java](src/main/java/modelo/enums/TipoProductoConfiteria.java)**

Categorías de productos del Candy.

**[enums/TamanoProductoConfiteria.java](src/main/java/modelo/enums/TamanoProductoConfiteria.java)**

Tamaños disponibles de producto.

**[enums/RolEmpleado.java](src/main/java/modelo/enums/RolEmpleado.java)**

Roles del personal usados por sesión y permisos.

**[enums/Permiso.java](src/main/java/modelo/enums/Permiso.java)**

Acciones autorizables; se consultan desde `PermisoService`.

**[conversores/YearMonthAttributeConverter.java](src/main/java/modelo/conversores/YearMonthAttributeConverter.java)**

`convertToDatabaseColumn`/`convertToEntityAttribute` convierten el vencimiento `YearMonth` de una
tarjeta a su representación en la base y viceversa.

## Recorridos para estudiar

### 1. De elegir butaca a confirmar la compra

1. `ReservaButacaController.reservar` obtiene al espectador de la sesión y llama a
   `ReservaButacaService.reservar`. Se guarda una reserva de diez minutos, no un ticket. La
   consulta periódica de React no renueva ese vencimiento.
2. Al pagar, `CompraController.comprar` recibe función, butacas, productos y método de pago.
   Comprueba que el comprador corresponda a la sesión.
3. `CompraService.comprar`, dentro de una transacción, exige un espectador verificado y una tarjeta
   válida. Ordena los IDs y usa `ButacaRepository.buscarPorIdsConBloqueo` para coordinar compras
   concurrentes.
4. `ReservaButacaService.validarDisponibilidadParaCompra` y `EntradaService.validarVenta` comprueban
   disponibilidad y condiciones de venta. `PrecioService.calcularTotal` calcula el importe
   definitivo de entradas: el cliente no impone el precio enviándolo desde React.
5. `TicketService.crearTicket` genera el QR; `EntradaService.crearEntradaPagada` crea las entradas y
   `agregarItem` incorpora productos. Se liberan las reservas y, si el cliente no guarda la tarjeta,
   se desactiva.
6. Tras confirmar la transacción, `TicketEmailListener` puede enviar el comprobante.
   `TicketController.crearResponse` devuelve detalles, estado y opciones de cancelación. React
   presenta ese resultado.

En este flujo el carrito es solo un borrador de interfaz hasta pagar. Antes sí pueden existir
cuenta, tarjeta y reservas. Los endpoints individuales de entradas/tickets también existen, pero no
son el flujo completo coordinado por `/compras`.

### 2. QR, entrada y retiro de productos

`GET /tickets/qr/{codigoQR}` identifica la compra. Swing usa después
`PUT /tickets/{id}/validar-entradas` o `PUT /tickets/{id}/entregar-consumos`, según la sección.
`TicketService` impide repetir una operación ya completada y calcula el nuevo estado al responder.
Validar acceso no entrega automáticamente los productos en este flujo; Swing muestra una advertencia
si todavía hay consumos pendientes. El endpoint combinado `procesar-ingreso` sigue existiendo, pero
no es el usado por esos dos botones separados.

### 3. Cancelación, tarjeta y membresía

- `TicketController.cancelarCompra` llama a `TicketService.cancelarPorEspectador`. Comprueba dueño
  del ticket y `evaluarCancelacion`: para entradas, la hora actual debe ser anterior al inicio de
  la primera función menos dos horas; en el límite exacto ya no permite cancelar. Para Candy solo,
  permite mientras los productos no se hayan entregado. Tampoco permite cancelar contenido ya
  utilizado. Reembolsa las entradas y cancela consumos pendientes; las butacas dejan de contarse
  como vendidas para esa función.
- `TicketService.reembolsar` es la operación del personal autorizado, con controles de estado
  propios. El proyecto no devuelve dinero a un banco: el pago y el reintegro son simulados.
- `MetodoDePagoService.eliminar` no borra físicamente una tarjeta referenciada: la desactiva. El
  ticket conserva su asociación y el resumen textual, aunque esa tarjeta deje de ofrecerse para
  nuevas compras.
- `MembresiaService.crearCotizacion` diferencia alta, mejora y cambio a un plan menor. La mejora
  hasta siete días desde el inicio cobra la diferencia y conserva vencimiento; después cobra el
  precio completo y activa un nuevo mes inmediatamente. El cambio a un plan menor se programa al
  terminar el actual. React muestra la cotización del servidor.

### 4. Editar cartelera y cambiar el estado de sala

Para crear/editar una función: `FuncionController` -> `FuncionService.guardar`/`actualizar` ->
`buscarPelicula`, `buscarSala`, `validarDatos` y `validarDisponibilidadSala` ->
`FuncionRepository`. La validación del service compara duración y horarios de las funciones de la
misma sala en la fecha consultada. El SQL de programación semanal agrega su propio margen de veinte
minutos: no son la misma implementación.

Para una incidencia: `SalaController.cambiarEstado` -> `SalaService.cambiarEstado` y
`AvisoSalaService.notificarCambio`; los avisos se consultan por destinatario y se marcan leídos sin
borrarse. La auditoría conserva además el actor y el detalle de la operación. En butacas, la
operación equivalente pasa por `ButacaController`/`ButacaService` y el aviso correspondiente.

## Base de datos y recursos

**[database/seed_demo.sql](database/seed_demo.sql)**

Reconstruye la base y carga categorías, películas, salas/butacas, productos y planes. **Es
destructivo: elimina datos anteriores.** No crea usuarios de prueba, tarjetas, tickets ni funciones.

**[database/completar_funciones_1_semana.sql](database/completar_funciones_1_semana.sql)**

Programa siete días con seis películas diarias y varias funciones por película, según duración y
salas disponibles. Parte hoy o mañana según la hora; conserva funciones referenciadas por
entradas/reservas, puede reemplazar otras de esa semana y revierte si no puede cumplir el plan sin
conflictos.

**[database/descripcionPeliculas.txt](database/descripcionPeliculas.txt)**

Material de referencia de las películas; editarlo no actualiza MySQL automáticamente.

**[database/portadasPeliculas/](database/portadasPeliculas/)**

Originales de las portadas. No es la URL pública de las imágenes.

**[src/main/resources/static/images/](src/main/resources/static/images/)**

Imágenes públicas de películas y productos, servidas como `/images/...`. Son recursos de la API, no
un frontend web antiguo.

**`target/`**

Salida de Maven: clases y paquete compilados. No editar ni estudiar los `.class` como si fueran
fuentes.

**Límite de la demostración:** usar cuentas y tarjetas ficticias. El pago es simulado;
verificar/recuperar correo en una ventana no prueba acceso real al buzón. SMTP requiere
configuración. Antes de publicar el sistema deben revisarse tokens, sesiones, permisos, credenciales
predeterminadas y tratamiento de datos sensibles de tarjetas.

### Orden de lectura recomendado

Empezar por `CompraController.comprar`, seguir `CompraService.comprar` y abrir solo los
services/repositories que invoca. Después recorrer un QR en `TicketController`/`TicketService`.
Finalmente estudiar un CRUD pequeño (`Categoria`) y compararlo con `Funcion`, que agrega reglas de
horario. Así se explica el proyecto por operaciones y no como una lista de clases aisladas.
