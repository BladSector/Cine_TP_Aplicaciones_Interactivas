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

## Límites

No se probó un lector físico de QR, correo SMTP real ni cobro bancario real. El código se introdujo como texto, el correo se mantuvo simulado y las tarjetas fueron datos de prueba. El viewport móvil no sustituye una prueba en un teléfono físico. Tampoco se realizó una prueba de carga o de concurrencia masiva.

Para repetir manualmente los recorridos con una base preparada para pruebas, seguir [la guía de ejecución](GUIA_EJECUCION.md). No ejecutar la seed sobre una base que se quiera conservar.
