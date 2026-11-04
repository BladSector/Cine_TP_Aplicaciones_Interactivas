# Verificación de interfaces: etapas 8 y 9

Fecha: 2 de noviembre de 2026.

## Alcance y aislamiento

Se verificó la reorganización de React y Swing mediante compilación, pruebas automatizadas de componentes Swing y pruebas de integración HTTP con Spring Boot y MySQL 8.0.46. Las pruebas crearon un esquema descartable `tp_cine_verificacion_<identificador>` y eliminaron únicamente ese esquema al finalizar. No se ejecutó la seed contra la base original `tp_cine_api`.

Los programas auxiliares y capturas se mantuvieron fuera del repositorio. No se agregaron dependencias al proyecto ni cuentas de prueba a su seed. Este documento registra lo comprobado; no es una suite ejecutable de tests.

## Compilación

Desde la raíz del repositorio:

```powershell
mvn.cmd -o -DskipTests compile
```

Desde `frontend/`:

```powershell
npm.cmd run build
```

Ambas compilaciones finalizaron correctamente. La opción `-o` de Maven utiliza dependencias locales; en una PC nueva debe omitirse para permitir descargarlas. Compilar por sí solo no prueba el comportamiento: las comprobaciones siguientes se ejecutaron por separado.

## Swing

| Área | Comprobación realizada |
| --- | --- |
| Formularios del dueño | Alta/edición, cancelación, imágenes locales, editor dinámico de salas y selección de película/sala en funciones. |
| Formularios del personal | Contraseñas repetidas y mínimo, estado de sala, descripción obligatoria y destinatario de avisos; comentario opcional de butaca. |
| Tablas y búsqueda | Orden y anchos de las 12 configuraciones de tabla; filtro literal sin distinguir tildes o mayúsculas. |
| Clientes API | Rutas y cuerpos de 49 métodos, errores, cookie compartida y codificación del QR. |
| Ciclo de vida | Secciones por rol, estado separado entre instancias, trabajo fuera del EDT y actualización visual en el EDT. |
| Cierre | Detención del temporizador de avisos, recargas bloqueadas después del cierre y respuestas tardías ignoradas. |
| Integración real | Los paneles de Dueño y Empleado cargaron tickets y salas mediante HTTP contra el backend y el esquema temporal. |

Las pruebas aisladas de formularios cubrieron 83 escenarios. Estas pruebas usan respuestas simuladas para comprobar los callbacks y cancelaciones; las operaciones reales se verificaron también por separado contra la API.

## Backend y MySQL utilizados por las interfaces

- La seed cargó 8 categorías, 18 películas, 4 salas, 266 butacas, 19 productos y 3 membresías, sin espectadores, empleados ni tickets iniciales.
- El script de funciones cubrió 14 días y 6 películas por día, sin superposiciones; repetirlo no duplicó funciones.
- Spring Boot inició con `spring.jpa.hibernate.ddl-auto=validate`: el esquema de prueba coincidió con las entidades sin que Hibernate lo modificara.
- Se probaron altas y ediciones de catálogo y sala dinámica, rechazo de una función superpuesta y acceso diferenciado por sesión.
- Se crearon empleados, se restableció una contraseña y se comprobó el bloqueo de acceso de una cuenta desactivada.
- El empleado no pudo listar cuentas, consultar auditoría, modificar horarios ni ocupar/liberar butacas manualmente; sí pudo informar una incidencia y volver a habilitar una butaca fuera de servicio.
- Un cambio de estado de sala generó un aviso para el empleado destinatario.
- Se registró un espectador, se confirmó su correo y se rechazó una tarjeta vencida.
- Una reserva impidió que otro espectador reservara la misma butaca de la misma función.
- Una compra guardó varias entradas y sus productos; el ticket se consultó por el código QR.
- Validar entradas dejó los consumos pendientes; entregarlos fue otra operación y completó el ticket.
- Reembolsar un ticket liberó la butaca y permitió comprarla nuevamente.
- Se procesó también una compra exclusiva del kiosco y se comprobó el registro de actividad.

## React en navegador

Se utilizó Microsoft Edge automatizado con el backend real de prueba y Vite configurado temporalmente hacia ese backend. No se cambió `vite.config.js` del proyecto.

- Apertura lateral de acceso, cambio a registro y recuperación antes del login.
- Inicio de sesión desde el formulario.
- Selección de butaca y creación de reserva en el servidor.
- Paso por el kiosco, incorporación de un producto, método de pago guardado y compra confirmada.
- Consulta del ticket desde Mi cuenta, código QR manual y descarga de un archivo PDF válido.
- Capturas de pago en escritorio de 1440 x 960 y viewport móvil de 390 x 844; comprobación de que el pago no ensanche el documento.
- Recarga del flujo con los datos del catálogo demorados, sin descartar la etapa de compra antes de recibirlos.

## Correcciones realizadas

1. `database/seed_demo.sql`: se cerró con `;` la consulta anterior al alta de membresías y se conservó `destacada`, columna que todavía utiliza la entidad `Membresia`. Se mantuvieron los nombres, precios y beneficios existentes.
2. `frontend/src/components/cuenta/PurchaseAuthDrawer.jsx`: el panel usa `createPortal` hacia `document.body`, como los otros modales. El encabezado con filtro visual ya no limita su alto ni hace que su cabecera tape los formularios.
3. Las páginas de butacas, kiosco y pago esperan el catálogo cuando todavía falta la película o función antes de redirigir. `useCheckout` expone ese estado de carga. La confirmación de una compra no se oculta por la recarga posterior del catálogo.

## Seguimiento de la etapa 9

La revisión se centró en cambios de selección y recargas mientras una operación Swing espera una respuesta HTTP o un diálogo modal. Se corrigieron los siguientes casos, sin modificar permisos ni reglas del backend:

- `desktop/duenio/AdministracionPanel.java`: la edición de categoría conserva su ID antes del formulario; el estado de sala conserva el ID y estado antes de consultar destinatarios; la edición de función conserva el ID y sus datos antes de consultar entradas vendidas. No se obtiene de nuevo el destino a partir de una posición de tabla que pudo cambiar.
- `desktop/comun/modulos/SalasModulo.java`: la incidencia conserva la butaca original antes de consultar empleados. Cambiar de sala o perder la selección mientras llega esa consulta no cambia la butaca del aviso.
- `desktop/comun/paneles/TicketsPanel.java` y `desktop/comun/modulos/VentasModulo.java`: al limpiar la selección se limpia también el ID del ticket. Un QR de Consumos no altera la selección de Tickets. El reembolso captura el ID antes de confirmar y lo mantiene hasta enviar la petición.

Las rutas anteriores parten de `src/main/java/`. Se agregaron, fuera del repositorio, siete pruebas de regresión con consultas demoradas y cambios de tabla/selección. Verifican categoría, sala, función, butaca, reembolso, ausencia de selección y separación entre QR de Consumos y selección de Tickets.

## Correcciones de la revisión del 3 de noviembre de 2026

- React pausa la comprobación de vencimiento mientras procesa el pago y la reactiva si falla. Así, la eliminación normal de reservas después de una compra no oculta su confirmación.
- La selección de butacas ignora respuestas de una pantalla o función anterior. La carga de disponibilidad también descarta consultas superadas por una más reciente.
- `GET /funciones/{id}/butacas-ocupadas` devuelve solo IDs de butacas con entradas activas. El mapa lo consulta junto con las reservas temporales, sin depender de las entradas privadas del espectador. Las entradas canceladas o reembolsadas no bloquean el asiento.
- El panel lateral de autenticación limpia sus campos al cerrarse y las contraseñas al cambiar de formulario.
- Los módulos Swing versionan sus consultas por listado; administración hace lo mismo con su carga conjunta. Una respuesta anterior no reemplaza datos más nuevos ni muestra un error obsoleto.
- El espectador puede acceder a `PUT /tickets/{id}/cancelar-compra`. El service conserva las verificaciones de propiedad, estado y límite de dos horas; la entrega y validación siguen reservadas al personal.

Verificación: compilaciones de React y Java correctas; seis escenarios de regresión en navegador con API simulada; recargas desordenadas sobre `PersonalModulo`; siete regresiones previas de selección y 31 comprobaciones de ciclo de vida Swing. La integración real completó 49 comprobaciones de HTTP/MySQL/JPA y paneles Swing, incluyendo cancelación propia, rechazo de tickets ajenos, plazo de dos horas y cancelación de candy no entregado.

Las pruebas automatizadas se mantuvieron fuera del repositorio. La integración utilizó una base temporal aleatoria y la eliminó al finalizar; no ejecutó la seed sobre `tp_cine_api`.

## Limpieza posterior: componentes compartidos y código sin uso

Se reutilizaron `LoginForm` y `RegisterForm` en la cuenta y el acceso lateral. La recuperación del panel lateral, cuenta y perfil ahora utiliza `PasswordRecoveryFlow`. Las tarjetas del perfil y checkout comparten `PaymentCard`, y sus formatos pertenecen a `utils/payment.js`. Se conservaron las clases CSS, dimensiones de las tarjetas, bloqueo de tarjetas vencidas y contratos HTTP.

El inicio de compra desde cartelera y película se centralizó en `PurchaseContext.startMoviePurchase`. Se eliminaron el componente no utilizado `CheckoutMoviePreview` con sus estilos, utilidades/API sin consumidores y la variante independiente no utilizada de administración Swing. Se conservaron las rutas antiguas de compatibilidad y los endpoints del backend.

Verificación: seis recorridos en navegador con API simulada comprobaron recuperación desde las tres entradas, registro y verificación de cuenta, y tarjetas de perfil/pago a 1440 y 390 px. Se repitieron las seis regresiones de reserva/pago. El análisis de imports no encontró archivos JS/JSX desconectados de `main.jsx` ni imports/variables sin uso, excepto parámetros `_` intencionalmente ignorados.

## Separación de la administración Swing

`AdministracionPanel` pasó de 715 a 63 líneas. Compone cinco coordinadores en `desktop/duenio/modulos/`: categorías, películas, funciones, salas administrativas y productos. `CrudModulo` comparte tabla, detalle, acciones sobre el registro capturado y confirmación de eliminación; reutiliza el ejecutor y las consultas versionadas de `ModuloSwing`. `DatosAdministracion` lleva una misma carga de catálogo a los cinco módulos y genera opciones de combos.

`DuenioPanel` recibe cada panel directamente: ya no construye ni extrae pestañas temporales por su título. Se mantuvieron las columnas, formularios, orden del menú, advertencia por entradas vendidas, imágenes locales y contratos HTTP. La asociación de películas/categorías y funciones/salas en los detalles utiliza IDs, no nombres que podrían repetirse.

Verificación: compilación Java correcta; siete regresiones de selección (26 comprobaciones), 31 de ciclo de vida, 27 escenarios de formularios/CRUD (85 comprobaciones), 56 de personal/operación (121 comprobaciones) y 18 comprobaciones específicas de registro capturado en detalles, relaciones por ID y recargas administrativas fuera de orden. La integración HTTP/MySQL/JPA y paneles de ambos roles volvió a completar 49 comprobaciones sobre una base temporal; al finalizar se verificó que no quedaran bases de verificación.

Las pruebas siguen fuera del repositorio. No se modificaron las reglas del backend ni la base `tp_cine_api` en esta reorganización.

## Extracción del mapa de butacas Swing

El dibujo del plano se movió desde `SalasModulo` a `comun/componentes/MapaButacasPanel`. El componente dibuja pantalla, filas, huecos, botones y leyenda; recibe los datos y el ID seleccionado, y notifica selección/detalle por callbacks. No realiza HTTP, no modifica estados y no decide permisos. `SalasModulo` conserva datos, filtrado por sala, selección y operaciones de ambos roles. Al quedar sin sala se limpia la selección y el resumen.

Se mantuvieron tamaños, etiquetas, colores y acciones. Verificación: compilación correcta; 17 comprobaciones del mapa (incluido dibujo real en imagen, colores y callbacks), siete regresiones de selección con 26 comprobaciones, 31 de ciclo de vida y 56 escenarios de personal/operación con 121 comprobaciones. Las pruebas utilizaron datos simulados, sin MySQL ni llamadas a la API real.

## Organización final para estudio y presentación

Se realizaron cinco etapas sin crear commits ni hacer push:

1. **Estilos:** `responsive.css` pasó a ser un índice de diez hojas en `styles/responsive/`. Se comparó la estructura CSS y se conservaron las 260 reglas, condiciones y orden de cascada. Se normalizó la indentación sin cambiar declaraciones.
2. **Dependencias:** se dejó de seguir en Git el `node_modules/` de la raíz, conservando sus archivos locales. Se agregaron exclusiones y se quitaron los manifiestos npm duplicados de la raíz. Las dependencias de React permanecen en `frontend/package.json` y su lockfile.
3. **Ventanas:** `useDialog` comparte foco, Tab/Escape, restauración del foco y bloqueo de desplazamiento. Solo la ventana superior captura el teclado. Los formularios y confirmaciones bloquean el cierre mientras procesan; el panel lateral usa la misma coordinación.
4. **Estado público/privado:** `CatalogContext` conserva solo catálogo público. `AccountContext` carga los tickets de la sesión con error/reintento, cancelación de consultas y descarte de respuestas tardías. El historial se oculta al cambiar o cerrar cuenta. La recarga del perfil tampoco repone una sesión que ya se cerró. Se eliminó la petición React no utilizada de entradas privadas; el endpoint del backend se conserva.
5. **Documentación y comprobación:** se agregó `README.md` y `GUIA_PRESENTACION.md`, y se actualizaron las guías React/Swing/CSS y los nombres Candy que todavía figuraban como Kiosco. Se quitó la afirmación de datos cifrados del pago y se indicó su carácter de demostración.

Verificaciones realizadas en esta etapa:

- `npm.cmd run build` y compilación Maven correctos. En esa etapa Vite todavía mostraba una advertencia de tamaño del paquete JavaScript; se resolvió posteriormente con la carga diferida descrita más abajo, sin aumentar su límite.
- Seis regresiones React de reserva/pago y seis recorridos de recuperación, registro, verificación y tarjetas compartidas.
- Pruebas aisladas de catálogo sin peticiones privadas anónimas, cambio de cuenta con respuestas tardías, fallo/reintento de historial y cierre de sesión durante una consulta de perfil.
- La repetición del recorrido perfil-pago detectó una carrera en la restauración de sesión: una petición obsoleta podía finalizar la carga antes de recibir el usuario vigente y desviar el pago a acceso/butacas. Se corrigió la limpieza del efecto de arranque en `AuthContext`. Dos casos deterministas (primera respuesta exitosa o fallida y segunda demorada) y diez recorridos repetidos entre escritorio/celular comprobaron la corrección, sin instrumentar el código de la aplicación.
- Pruebas de teclado, ventanas anidadas, bloqueo del cierre ocupado, devolución del foco y limpieza del desplazamiento al desmontar.
- 48 combinaciones de ruta/ancho/tema: cartelera, película, butacas, Candy, pago y tarjetas de cuenta; anchos de 1440, 900, 640 y 390 px en claro/oscuro. Sin errores de navegador ni desbordamiento horizontal del documento. Se guardaron y revisaron capturas fuera del repositorio.
- Swing: siete regresiones de selección (26 comprobaciones), 31 de ciclo de vida, 27 escenarios de formularios (85 comprobaciones), 56 de personal/operación (121), 205 de componentes/tablas, 354 comparaciones de formato, 49 métodos API (169 comprobaciones), 18 de CRUD administrativo, 17 del mapa y descarte de una recarga antigua de empleados.
- Análisis de imports: no quedaron archivos JS/JSX sin conexión con el arranque ni bindings sin uso, salvo parámetros `_` intencionalmente ignorados. `git diff --check` no informó errores de espacios; mostró avisos de conversión LF/CRLF de Windows.

Estas pruebas utilizaron APIs simuladas o servidores locales aislados; no modificaron `tp_cine_api`. La integración real HTTP/MySQL indicada en las secciones anteriores no se repitió para esta etapa de organización del frontend. Los arneses automatizados continuaron fuera del repositorio.

## Ajustes visuales de películas y fondo cinematográfico

Las funciones del detalle de película ahora usan recuadros compactos de igual tamaño, con texto adaptable y borde naranja. Al seleccionar una se destaca todo el marco sin modificar sus dimensiones; `aria-pressed` informa la selección. Las reglas se concentran en `pages/compra.css` y sus ajustes móviles en `responsive/mobile-pelicula.css`. El detalle agrega 24 px de espacio inferior antes del footer.

Se incorporó `frontend/src/assets/cinema-background.png`, conservando la imagen proporcionada sin modificaciones. `base.css` la encuadra sin repetirla detrás del contenido y `theme.css` define filtros e intensidad por tema. En celular el encuadre favorece la cinta derecha. No se conserva el patrón SVG anterior ni sus variables sin uso.

Verificación de esta revisión:

- Build de React correcto; la imagen se incluye en `dist/assets/`. En esta revisión visual todavía persistía la advertencia del paquete JavaScript mayor a 500 kB, resuelta en la etapa siguiente.
- Dieciséis combinaciones de ancho/tema para dimensiones, bordes, texto y selección de funciones, más tres casos de nombres de sala largos. En 320 px se validaron los recuadros; el encabezado sin sesión tiene un desborde previo a estos cambios y no se considera corregido por esta revisión.
- Seis combinaciones de ancho/tema para carga de la imagen, encuadre, filtros, diferencia visible en píxeles, clics y superposición correcta de modales. Se revisaron capturas de escritorio y celular.
- Separación antes del footer comprobada en 1440 y 390 px, sin cambiar las otras páginas.
- Análisis de imports sin archivos JS/JSX desconectados ni nuevos bindings sin uso; solo quedan los parámetros `_` intencionalmente ignorados. Enlaces locales de nueve documentos revisados sin rutas rotas.

Los recorridos utilizan datos simulados y no modifican MySQL. No se cambiaron reglas de compra, endpoints ni clases Swing en estos ajustes visuales.

## Carga diferida y build sin advertencia de tamaño

`AppRoutes` mantiene la cartelera, redirecciones simples y páginas informativas pequeñas en la carga inicial. Nueve pantallas secundarias se declaran mediante `React.lazy` e `import()`. Vite genera automáticamente paquetes por pantalla y componentes compartidos; no se agregaron librerías ni se modificó `chunkSizeWarningLimit` para ocultar el aviso.

`SiteLayout` conserva encabezado/footer y rodea `Outlet` con `Suspense` y `RouteErrorBoundary`. El primero muestra el indicador de carga del módulo. El segundo captura fallos del contenido y permite recargar manualmente, sin reintentar operaciones de compra. Su instancia cambia por ruta para no mantener el error al abrir otra sección.

El JavaScript inicial pasó de aproximadamente 505,6 a 402,9 kB, cerca de un 20 % menos antes de gzip. El build no muestra la advertencia y todos los paquetes JavaScript están por debajo de 500 kB. Es una mejora de descarga inicial, no una reducción garantizada de la suma de todos los módulos o imágenes; el fondo PNG mantiene su tamaño original.

Verificaciones: build de producción correcto; inspección de peticiones que confirma que pago/cuenta/club/película no se descargan al abrir la cartelera; carga demorada con indicador y encabezado conservado; acceso directo y recarga de rutas, alias de Candy y etapas de compra. Se simularon fallo de un paquete y recuperación mediante recarga manual. También se repitieron las regresiones de reserva/pago y los recorridos de autenticación/tarjetas. Se comprobaron compras de Candy y membresía en el build con un único POST y confirmación.

Los tests de producción utilizaron `dist/` servido en un puerto local temporal y APIs simuladas; el servidor se cerró al finalizar. El análisis de imports contempla también `import()` para no confundir pantallas diferidas con archivos sin uso. No se modificaron MySQL, endpoints ni Swing.

## Programación semanal de funciones

Se reemplazó `database/completar_funciones_2_semanas.sql` por `database/completar_funciones_1_semana.sql`. La seed base sigue separada y sin usuarios/compras de prueba; sus instrucciones y las guías ahora indican ejecutar el script semanal después de instalarla. Para actualizar una base existente se utiliza únicamente el script de funciones, no la seed que elimina la base.

El plan rota seis películas por día e intercala géneros. Distribuye dos o tres funciones por película entre cuatro salas operativas, con inicios entre las 13:00 y las 23:30 y bloques que contemplan duración y 20 minutos de limpieza. Antes de las 12:30 comienza hoy; después comienza mañana. En el catálogo actual resulta en 16 funciones diarias, 112 en siete días.

El script conserva IDs de funciones que ya coinciden con el plan. Solo reemplaza funciones libres de esa semana; cualquier entrada o reserva protege la función original. Verifica un mínimo de dos funciones para cada película del plan y ausencia de superposiciones, incluso considerando el cruce de medianoche. Si no puede cumplirlo, revierte la transacción. El procedimiento auxiliar se elimina al finalizar y se restaura Safe Updates.

Pruebas realizadas en un esquema MySQL aislado: instalación desde la seed, siete días/seis películas/16 funciones por día, dos o tres funciones por película, rotación de las 18 películas, horarios futuros y separación mínima de 20 minutos; repetición conservando IDs y precios; protección de entradas/tickets/reservas; reemplazo de una función libre antigua; rechazo por sala no disponible y rollback ante un horario protegido incompatible. Se comprobaron también el inicio desde hoy al ejecutar por la mañana, la reducción de turnos para una película de cuatro horas y el rechazo de una jornada de hoy ya iniciada. El esquema temporal se eliminó al terminar.

Aplicación local del 3 de noviembre de 2026: se creó antes un respaldo completo fuera del repositorio y se programó del 4 al 10 de noviembre. Se conservaron las 284 funciones históricas, las 23 entradas, los 20 tickets y las reservas existentes. La API `GET /funciones` devolvió las 112 funciones nuevas distribuidas correctamente. No se borraron cuentas ni se ejecutó la seed destructiva sobre la base real.

## Límites

No se probó un lector físico de QR, correo SMTP real ni cobro bancario real. El código se introdujo como texto, el correo se mantuvo simulado y las tarjetas fueron datos de prueba. El viewport móvil no sustituye una prueba en un teléfono físico. Tampoco se realizó una prueba de carga o de concurrencia masiva.

La limpieza de interfaces no constituye una auditoría de seguridad de producción. Antes de publicar deben revisarse los flujos de verificación/recuperación, las credenciales predeterminadas, revocación de sesiones y tratamiento de datos de tarjetas. No utilizar cuentas ni tarjetas reales para la demostración.

Para repetir manualmente los recorridos con una base preparada para pruebas, seguir [la guía de ejecución](GUIA_EJECUCION.md). No ejecutar la seed sobre una base que se quiera conservar.
