# Organización de Swing

La interfaz de escritorio es un cliente de la API. No se conecta directamente a MySQL ni contiene las reglas definitivas del negocio.

Las rutas de este documento son relativas a `src/main/java/desktop/`. El paquete se llama `duenio` por convención de código; en pantalla se muestra **Dueño**.

## Índice

- [Responsabilidad de cada carpeta](#responsabilidad-de-cada-carpeta)
- [Mapa de archivos](#mapa-de-archivos)
- [Dónde está cada estado](#dónde-está-cada-estado)
- [Recorridos para estudiar](#recorridos-para-estudiar)
- [Navegación y cierre](#navegación-y-cierre)
- [Dónde realizar un cambio](#dónde-realizar-un-cambio)

## Responsabilidad de cada carpeta

| Carpeta | Qué contiene |
| --- | --- |
| `inicio/` | Inicio de la aplicación y login. |
| `comun/` | Cliente HTTP, ventana principal y navegación compartida. |
| `comun/api/` | Peticiones HTTP agrupadas por dominio. |
| `comun/modulos/` | Estado y coordinación de salas, butacas, tickets y consumos; apoyo asíncrono común. |
| `comun/paneles/` | Controles visuales utilizados por ambos roles. |
| `comun/dialogos/` | Formularios compartidos de estado y destinatarios. |
| `comun/componentes/` | Tabla con buscador y visor de detalles. |
| `comun/util/` | Configuración de tablas, HTML, botones, etiquetas y normalización de búsquedas. |
| `duenio/` | Composición de la interfaz del dueño y coordinación del CRUD administrativo. |
| `duenio/modulos/` | Estado y operaciones de empleados y auditoría. |
| `duenio/paneles/` y `duenio/dialogos/` | Vistas y formularios exclusivos del dueño. |
| `empleado/` | Composición de la interfaz del empleado. |
| `empleado/paneles/` y `empleado/modulos/` | Consultas operativas y gestión de avisos. |

Para seguir una acción: panel del rol -> panel visual y su callback -> módulo o `AdministracionPanel` -> cliente de `comun/api` -> `ApiClient` -> backend.

Los paneles organizan controles; los módulos conservan tablas, selección y respuestas de la API; los diálogos devuelven datos. Los helpers de `util` no deciden permisos. `FormatoSwing` cambia las etiquetas mostradas, no los códigos del negocio; `TextoBusqueda` cambia cómo se compara una búsqueda, no los datos almacenados.

## Mapa de archivos

```text
desktop/
  inicio/
    CineDesktopApplication.java       main, apariencia y apertura en el EDT
    LoginFrame.java                   Usuario, contraseña y acceso a la sesión
  comun/
    ApiClient.java                    HttpClient, JSON, cookie y errores HTTP
    PrincipalFrame.java               Ventana, menú, rol, cierre y salida
    PanelPersonalBase.java            Navegación CardLayout y ciclo de vida
    api/
      SesionApi.java                  Inicio, consulta y cierre de sesión
      CarteleraApi.java               Categorías, películas y funciones
      SalasApi.java                   Salas, butacas y avisos
      TicketsApi.java                 Tickets, validación, entrega y reembolso
      ProductosApi.java               Productos del kiosco
      PersonalApi.java                Cuentas de empleados
      AuditoriaApi.java               Actividad del sistema
    modulos/
      ModuloSwing.java                SwingWorker, selección y detalles comunes
      SalasModulo.java                Coordina salas, funciones y plano de butacas
      VentasModulo.java               Coordina tickets y entrega de consumos
    paneles/
      ButacasPanel.java               Selector de sala, plano y botones
      TicketsPanel.java               Campo QR, tabla y acciones de entradas
      ConsumosPanel.java              Campo QR, tabla y acciones de entrega
    dialogos/
      EstadoSalaDialog.java           Estado, descripción y destinatario
      EstadoButacaDialog.java         Comentario y destinatario de una incidencia
      SelectorDestinatario.java       Empleado activo o aviso general
    componentes/
      PanelConTabla.java              Título, buscador, ayuda y tabla
      BuscadorTabla.java              Filtro literal y orden inicial
      VisorDetalle.java               Presentación HTML de solo lectura
    util/
      ConfiguradorTablas.java         Modelos, columnas y anchos
      DetalleHtml.java                Títulos, campos y escape de texto
      BotonesSwing.java               Botones con callbacks
      FormatoSwing.java               Etiquetas y conversiones de códigos
      TextoBusqueda.java              Comparaciones sin tildes ni mayúsculas
  duenio/
    DuenioPanel.java                  Composición y secciones del rol
    AdministracionPanel.java          Coordinación de los cinco CRUD de catálogo
    modulos/
      PersonalModulo.java             Cuentas, estado y contraseñas de empleados
      ActividadModulo.java            Tabla y detalles de la auditoría
    paneles/
      CrudAdministracionPanel.java    Base visual de los CRUD
      CategoriasPanel.java            Vista de categorías
      PeliculasPanel.java             Vista de películas
      FuncionesAdminPanel.java        Vista administrativa de funciones
      SalasAdminPanel.java            Vista administrativa de salas
      ProductosPanel.java             Vista de productos
      EmpleadosPanel.java             Vista de cuentas del personal
      ActividadPanel.java             Vista de actividad y referencia de perfiles
    dialogos/
      FormularioDialogo.java          Estructura y comprobaciones visuales comunes
      CategoriaDialog.java            Nombre de categoría
      PeliculaDialog.java             Datos, descripción, categoría y portada
      FuncionDialog.java              Fecha, horario, sala, película y precio
      SalaDialog.java                 Nombre y confirmación de distribución
      EditorDistribucionSala.java     Pantalla, filas, butacas y espacios vacíos
      ProductoDialog.java             Datos, stock e imagen del producto
      SelectorImagenLocal.java        Lectura local, tamaño y vista previa
      EmpleadoDialog.java             Alta de empleado y contraseña repetida
      RestablecerContraseniaDialog.java Nueva contraseña repetida
      Opcion.java                     record de ID y etiqueta para combos
  empleado/
    EmpleadoPanel.java                Composición y secciones del rol
    modulos/
      AvisosModulo.java               Consulta periódica, historial y lectura
    paneles/
      SalasPanel.java                 Consulta de salas y cambio de estado
      FuncionesPanel.java             Consulta y recarga de funciones
```

`ModuloSwing` no es un `JPanel`: es una base de coordinación. `AdministracionPanel` sí hereda de `JPanel`, pero además coordina los CRUD del dueño. El nombre no implica que ambos tengan la misma responsabilidad.

## Dónde está cada estado

| Clase | Qué conserva |
| --- | --- |
| `ApiClient` | Cliente HTTP y cookies de la sesión, compartidos por los clientes de dominio. |
| `PanelPersonalBase` | Sección visible, botones y callbacks de recarga/detención. No conserva datos del catálogo ni ventas. |
| `AdministracionPanel` | Modelos y respuestas de categorías, películas, funciones, salas y productos. |
| `SalasModulo` | Salas, funciones consultables, selector de sala, butacas actuales y asiento seleccionado. |
| `VentasModulo` | Tickets y consumos, caché de tickets, selección y códigos QR ingresados. |
| `PersonalModulo` | Tabla y selección de empleados. |
| `ActividadModulo` | Tabla de auditoría y presentación por perfil. |
| `AvisosModulo` | Botón de avisos, temporizador y control local de lectura/consulta. |

Cada sesión nueva crea sus módulos; no hay tablas o selecciones globales compartidas entre ventanas. Compartir `ApiClient` conserva la sesión HTTP, no comparte los modelos visuales.

Una acción conserva el ID original antes de esperar una consulta o un diálogo, porque una recarga puede cambiar el orden de las filas. Las validaciones manuales y los reembolsos requieren seleccionar un ticket: el QR de Consumos no cambia ese destino, y quitar la selección limpia el ID mediante un `Consumer<Integer>` que admite `null`.

## Recorridos para estudiar

### Inicio y selección de rol

```text
CineDesktopApplication.main
  -> SwingUtilities.invokeLater
  -> LoginFrame
  -> SesionApi.iniciarSesion (POST y posterior GET de la sesión)
  -> ApiClient
  -> PrincipalFrame
  -> DuenioPanel o EmpleadoPanel según el rol recibido
```

### Editar una película

```text
DuenioPanel: sección Películas
  -> PeliculasPanel / CrudAdministracionPanel: selección y acción
  -> AdministracionPanel: consulta datos y abre PeliculaDialog
  -> Optional<Datos>: resultado del formulario, sin petición HTTP
  -> AdministracionPanel: arma el cuerpo y ejecuta la tarea
  -> CarteleraApi -> ApiClient -> controller/service del backend
  -> respuesta: recarga del catálogo y modelo de tabla
```

Cancelar el diálogo o cerrar su ventana devuelve un resultado vacío y no guarda. Los diálogos comprueban lo necesario para orientar al usuario; el service del backend aplica las reglas definitivas.

### Validar entradas sin entregar consumos

```text
TicketsPanel: QR o selección manual
  -> VentasModulo.leerQrAcceso o validarEntradasManualmente
  -> TicketsApi: buscar ticket
  -> VentasModulo.confirmarValidacionEntradas: advertencia si hay consumos pendientes
  -> TicketsApi.validarEntradas -> PUT /tickets/{id}/validar-entradas
  -> VentasModulo: recarga tickets y consumos
```

La entrega se hace en `ConsumosPanel` -> `VentasModulo` -> `TicketsApi.entregarConsumos`. Es otra operación: `PUT /tickets/{id}/entregar-consumos`. La acción de una fila individual utiliza `entregarConsumo`.

### Cambiar estado de una sala y avisar

```text
SalasPanel: Cambiar estado
  -> SalasModulo: selección y consulta de empleados activos
  -> EstadoSalaDialog: estado, descripción y destinatario
  -> SalasApi.cambiarEstado -> backend guarda y genera el aviso
  -> AvisosModulo: consulta periódica del empleado e historial
```

La descripción es obligatoria para "Problema particular". La incidencia de una butaca usa `EstadoButacaDialog` con comentario opcional. Los empleados pueden dejarla fuera de servicio y volver a habilitarla, pero no ocupar ni liberar manualmente una butaca vendida. En Funciones solo consultan la programación: no tienen edición de fecha u horario.

## Navegación y cierre

1. `PrincipalFrame` crea el panel correspondiente al rol.
2. El panel del rol crea vistas y módulos, registra secciones y callbacks y solicita la primera recarga.
3. `PanelPersonalBase.mostrarSeccion` cambia el `CardLayout`: navegar no destruye ni recrea el módulo.
4. `ModuloSwing.ejecutar` realiza HTTP en `doInBackground`; `done` actualiza la vista en el EDT o muestra el error.
5. Al cerrar sesión correctamente, `PrincipalFrame` llama a `detener`, cierra la ventana y abre el login. Al salir de la aplicación también se detienen los módulos.

`AvisosModulo` detiene su temporizador al cerrar. Un módulo detenido ignora respuestas tardías y no inicia nuevas recargas; esto no cancela ni revierte una petición que ya haya llegado al servidor. `AdministracionPanel` también controla si sigue activo.

## Dónde realizar un cambio

| Necesidad | Abrir primero |
| --- | --- |
| Cambiar menú o selección por rol | `PrincipalFrame`, `DuenioPanel`, `EmpleadoPanel` y `PanelPersonalBase`. |
| Cambiar controles de una sección | Su clase en `paneles/`; seguir el callback para la operación. |
| Cambiar campos de un formulario | Su clase en `dialogos/`. |
| Cambiar qué se recarga o qué selección se utiliza | El módulo correspondiente o `AdministracionPanel`. |
| Cambiar URL o método HTTP | El cliente de `comun/api/`; `ApiClient` solo transporta. |
| Ajustar columnas, búsqueda o etiquetas | `ConfiguradorTablas`, `BuscadorTabla`, `TextoBusqueda` o `FormatoSwing`. |
| Cambiar permisos, disponibilidad o condiciones de reembolso | El backend en `controller/`, `service/` y su configuración de acceso, fuera de `desktop/`. |

Orden de lectura: inicio -> ventana y rol -> una sección -> su módulo -> diálogo o cliente API -> backend. Para cada archivo, identificar qué recibe, qué estado conserva y a quién delega; no hace falta estudiar todos los diálogos de una vez.

Ver también la [guía de estudio de ambas interfaces](../../../../frontend/GUIA_ESTUDIO_FRONTEND.md) y la [guía de ejecución](../../../../GUIA_EJECUCION.md).
