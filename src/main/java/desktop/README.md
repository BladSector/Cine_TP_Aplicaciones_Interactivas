# Film Zone: código de la interfaz Swing

Cliente Java de escritorio para el dueño y los empleados. Utiliza la misma API Spring Boot que React. No se conecta directamente a MySQL ni reemplaza las reglas o permisos del backend.

Este README explica cada carpeta y archivo de `src/main/java/desktop/`, su aplicación y qué modificar allí. Las rutas de los encabezados parten de la raíz del repositorio. Este `README.md` es documentación, no una clase ejecutable.

Para preparar una explicación completa, seguir [la guía de presentación](../../../../GUIA_PRESENTACION.md): conecta los archivos Swing con la compra web y los endpoints que procesan el QR.

## Índice

- [Inicio y estructura](#inicio-y-estructura)
- [Comunicación con la API](#comunicación-con-la-api)
- [Módulos compartidos y vistas](#módulos-compartidos-y-vistas)
- [Componentes, diálogos y utilidades](#componentes-diálogos-y-utilidades)
- [Interfaz del dueño](#interfaz-del-dueño)
- [Interfaz del empleado](#interfaz-del-empleado)
- [Cómo se conectan las carpetas](#cómo-se-conectan-las-carpetas)
- [Ejecución y estudio](#ejecución-y-estudio)

## Inicio y estructura

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

## Comunicación con la API

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

## Módulos compartidos y vistas

### Carpeta `src/main/java/desktop/comun/modulos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ModuloSwing.java` | Base de los coordinadores operativos. | Ejecución con `SwingWorker`, avisos, detalles, selección de IDs y control de módulo activo. `consultar` versiona cada listado para ignorar respuestas antiguas; `ejecutar` se usa para operaciones. No es un panel visual. |
| `SalasModulo.java` | Salas, consulta de funciones y butacas. | Carga de datos, filtro por sala, selección, permisos y cambios de estado/incidencias. Delega el dibujo en `MapaButacasPanel`. |
| `VentasModulo.java` | Tickets y Consumos de ambos roles. | Consultas, selección, lectura de QR, advertencias, validación, entrega y reembolso con sus recargas. |

Un módulo conserva estado de pantalla y coordina acciones. No es una entidad persistente. `SwingWorker.doInBackground` realiza HTTP y `done` actualiza la interfaz en el EDT, evitando bloquear la ventana.

### Carpeta `src/main/java/desktop/comun/paneles/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `ButacasPanel.java` | Vista del plano. | Selector, ubicación de controles y botones según el rol; recibe el plano y callbacks del módulo. |
| `TicketsPanel.java` | Ventas y tickets / Control de acceso. | Campo QR, tabla, botones manuales, reembolso y notificación de selección. Informa `null` cuando se limpia la selección. |
| `ConsumosPanel.java` | Entrega de productos. | Campo QR, tabla y controles de entrega/recarga. |

**Vista y coordinador no son lo mismo:** para cambiar el texto o posición de un botón se revisa el panel; para cambiar qué hace se sigue su callback hasta el módulo.

## Componentes, diálogos y utilidades

### Carpeta `src/main/java/desktop/comun/componentes/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `PanelConTabla.java` | Secciones con tablas. | Título, ayuda, buscador y contenedor con desplazamiento. |
| `BuscadorTabla.java` | Búsqueda en tablas. | Campo y filtro literal por columnas, integración con el orden inicial. |
| `VisorDetalle.java` | Cuadros de información. | Presentación HTML de solo lectura; no es un navegador ni el frontend web. |
| `MapaButacasPanel.java` | Plano de butacas para ambos roles. | Pantalla del cine, filas, espacios vacíos, botones, colores y leyenda. Recibe butacas/selección y emite callbacks de clic/doble clic; no consulta la API ni modifica estados. |

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

## Interfaz del dueño

### Carpeta `src/main/java/desktop/duenio/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `DuenioPanel.java` | Composición del rol Dueño. | Grupos, orden de secciones, creación de módulos y registro de su ciclo de vida. |
| `AdministracionPanel.java` | Composición de los cinco CRUD del dueño. | Creación de módulos, carga conjunta de datos y cierre. Expone los paneles directamente al menú; no contiene formularios ni cuerpos HTTP de edición. |

La carpeta usa `duenio` como nombre de paquete; el texto visible es Dueño.

### Carpeta `src/main/java/desktop/duenio/modulos/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `PersonalModulo.java` | Empleados. | Carga de cuentas, creación, activación/desactivación y recuperación de contraseñas. |
| `ActividadModulo.java` | Actividad del sistema. | Carga de auditoría, presentación por actor y detalles amigables de acciones. |
| `CrudModulo.java` | Base de los cinco CRUD. | Modelo/tabla, detalle con acciones, confirmación de eliminación y recarga. Hereda las tareas asíncronas de `ModuloSwing`; conserva el registro mostrado aunque se recargue la tabla. |
| `DatosAdministracion.java` | Resultado de la carga conjunta. | Categorías, películas, salas, funciones y productos recibidos de la API; genera las opciones de los combos. No es una entidad de base de datos. |
| `CategoriasModulo.java` | Gestión de categorías. | Filas, detalle de películas asociadas, formulario y peticiones de alta/edición/baja. |
| `PeliculasModulo.java` | Gestión de películas. | Detalle y funciones asociadas, categoría, portada y peticiones del formulario. |
| `FuncionesModulo.java` | Gestión de funciones. | Datos de función, comprobación y advertencia por entradas vendidas, formulario y peticiones. |
| `SalasAdministracionModulo.java` | Salas del dueño. | Alta, distribución, estado, destinatarios de avisos y funciones programadas. No confundir con `comun/modulos/SalasModulo`, que coordina las vistas operativas y el mapa de butacas. |
| `ProductosModulo.java` | Productos de confitería. | Tabla, detalle, imagen y peticiones de alta/edición/baja. |

### Carpeta `src/main/java/desktop/duenio/paneles/`

| Archivo | Dónde se aplica | Qué modificar ahí |
| --- | --- | --- |
| `CrudAdministracionPanel.java` | Estructura visual común de los CRUD. | Tabla, alta, recarga y apertura del detalle por doble clic. |
| `CategoriasPanel.java` | Categorías. | Configuración visual y callbacks de esa sección. |
| `PeliculasPanel.java` | Películas. | Configuración visual y callbacks de esa sección. |
| `FuncionesAdminPanel.java` | Funciones del dueño. | Configuración visual y callbacks del CRUD de funciones. |
| `SalasAdminPanel.java` | Salas del dueño. | Configuración visual y callbacks de administración de salas. |
| `ProductosPanel.java` | Productos. | Configuración visual y callbacks del catálogo del Candy. |
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

## Interfaz del empleado

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

## Cómo se conectan las carpetas

```text
inicio/CineDesktopApplication
  -> inicio/LoginFrame -> comun/api/SesionApi -> comun/ApiClient
  -> comun/PrincipalFrame
  -> duenio/DuenioPanel o empleado/EmpleadoPanel
  -> panel visual -> módulo del dominio -> diálogo o cliente API
  -> backend -> respuesta -> actualización del modelo visual
```

### Ejemplo: editar una película

`PeliculasPanel` ofrece la acción. `PeliculasModulo` conserva el registro, prepara y abre `PeliculaDialog`. El formulario devuelve `Optional<Datos>`; al cancelar no guarda. El módulo construye la petición y llama a `CarteleraApi`, que delega el transporte en `ApiClient`. Tras la respuesta, `AdministracionPanel` recarga el catálogo compartido y cada módulo actualiza su modelo.

### Ejemplo: QR y entrega

`TicketsPanel` recibe el QR o una selección manual. `VentasModulo` consulta el ticket y advierte si hay consumos pendientes antes de validar entradas. La entrega de productos se hace por separado desde `ConsumosPanel`. Leer un QR en Consumos no cambia el ticket seleccionado para reembolsar.

### Estado y cierre

- Los paneles del rol crean sus módulos; cada instancia conserva sus propias tablas y selecciones.
- `PanelPersonalBase` utiliza `CardLayout`: cambiar de sección no destruye el módulo.
- `ModuloSwing` y `CrudModulo` no son vistas. Los paneles construyen controles; los módulos coordinan acciones. `AdministracionPanel` conserva su tipo `JPanel` como componente padre de los diálogos, pero el menú muestra los cinco paneles devueltos por sus getters, sin crear pestañas temporales.
- Los IDs se capturan antes de consultas y confirmaciones: una recarga no debe redirigir una acción a otra fila.
- La selección de Tickets utiliza `Consumer<Integer>`: si se limpia la fila, informa `null`. Validar manualmente o reembolsar requiere una selección.
- `detener()` impide recargas y actualizaciones tardías; `AvisosModulo` detiene su temporizador. No revierte una petición ya procesada por el servidor.
- Cerrar sesión vuelve al login; Salir cierra la aplicación. Los permisos efectivos siempre se deciden en el backend.

## Ejecución y estudio

Con Java 17, Maven y el backend iniciado, ejecutar desde la carpeta que contiene `pom.xml`:

```powershell
mvn.cmd compile exec:java
```

En macOS/Linux se utiliza `mvn`. El punto de entrada está configurado como `desktop.inicio.CineDesktopApplication`. La API predeterminada es `http://localhost:8080`; `cine.api.url` permite configurar otra dirección.

Orden de lectura: inicio -> ventana y rol -> una sección -> su módulo -> diálogo/cliente API. Después revisar tablas, utilidades y cierre. Para cambiar texto o disposición, empezar por el panel; para cambiar la operación, seguir su callback.

- [README de React](../../../../frontend/README.md).
- [Guía conceptual de estudio](../../../../frontend/GUIA_ESTUDIO_FRONTEND.md).
- [Guía de ejecución](../../../../GUIA_EJECUCION.md).
- [Verificaciones y límites de las pruebas](../../../../VERIFICACION_INTERFACES.md).
