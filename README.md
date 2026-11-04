# Film Zone

Proyecto académico de gestión de un cine. Los espectadores utilizan React; el dueño y los empleados utilizan Swing. Ambas interfaces se comunican por HTTP con la misma API Spring Boot, que aplica las reglas y guarda datos en MySQL.

## Por dónde empezar

| Necesito… | Documento |
| --- | --- |
| Iniciar el proyecto en otra PC | [Guía de ejecución](GUIA_EJECUCION.md) |
| Preparar la exposición y seguir una operación por archivos | [Guía de presentación](GUIA_PRESENTACION.md) |
| Saber qué hace cada archivo React | [README de React](frontend/README.md) |
| Saber qué hace cada clase Swing | [README de Swing](src/main/java/desktop/README.md) |
| Encontrar un estilo o ajuste de celular | [Mapa de estilos](frontend/src/styles/README.md) |
| Estudiar conceptos y preguntas | [Guía de estudio](frontend/GUIA_ESTUDIO_FRONTEND.md) |
| Consultar recorridos conjuntos de ambas interfaces | [Guía web y Swing](GUIA_FRONT_WEB_Y_SWING.md) |
| Revisar lo comprobado y los límites | [Verificación de interfaces](VERIFICACION_INTERFACES.md) |

## Organización

```text
frontend/                       React: páginas, componentes, estado y estilos
src/main/java/desktop/          Swing: ventanas, módulos, paneles, diálogos y HTTP
src/main/java/controller/       Endpoints y conversión de peticiones/respuestas
src/main/java/service/          Reglas de negocio y coordinación de operaciones
src/main/java/repository/       Acceso a datos mediante Spring Data JPA
src/main/java/modelo/           Entidades persistentes y enums del dominio
src/main/java/com/tp/cine/      Arranque, configuración y seguridad de la API
src/main/resources/            Configuración del backend e imágenes públicas
database/                      Seed, funciones de una semana y material de películas
pom.xml                        Dependencias y ejecución Java
```

`target/`, `frontend/dist/` y `node_modules/` son resultados de compilación o instalación, no código para estudiar ni versionar. Las dependencias JavaScript se declaran solo en `frontend/package.json`; en la raíz se ejecuta Maven.

## Recorrido principal

```text
React: película -> función -> reserva de butacas -> Candy opcional -> pago
API: verifica sesión, disponibilidad y precios -> guarda compra -> devuelve ticket
React: muestra ticket, PDF y QR
Swing: consulta el QR -> entrega consumos o valida entradas -> recarga el estado
```

En el flujo web normal, el carrito es un borrador en `sessionStorage`, no un ticket pagado. Las reservas sí se guardan temporalmente. La compra de entradas/productos se confirma mediante `CompraService` al finalizar el pago simulado.

## Desarrollo local

Requisitos: JDK 17, Maven, MySQL Server, Node.js y npm. Seguir [GUIA_EJECUCION.md](GUIA_EJECUCION.md) para configurar las variables de conexión, preparar datos e iniciar cada proceso.

- API: `http://localhost:8080`.
- React: `http://localhost:5173`.
- Swing: `mvn.cmd compile exec:java` desde la raíz, con la API iniciada.
- React: `npm.cmd install` y `npm.cmd run dev` desde `frontend/`.

React descarga las pantallas secundarias cuando se visitan, en lugar de incluirlas todas en el JavaScript inicial. La cartelera y la estructura compartida se cargan al inicio. El detalle está en [Carga por ruta](frontend/README.md#carga-por-ruta); no cambia la ejecución ni las reglas del backend.

**La seed elimina los datos existentes.** No ejecutarla para resolver un problema de interfaz ni para actualizar únicamente las fechas de funciones.

Para renovar solo la cartelera, ejecutar [completar_funciones_1_semana.sql](database/completar_funciones_1_semana.sql): programa siete días con seis películas diarias y varias funciones, conservando las que tengan entradas o reservas. La preparación desde cero y las precauciones están en [database/README.md](database/README.md).

## Alcance de la demostración

El cobro es una simulación: no existe una pasarela bancaria. La recuperación y confirmación de correo permiten una demostración dentro de la aplicación; no prueban la propiedad real del correo. El envío SMTP de tickets requiere configuración adicional. Para simular un lector se pega el código `TCK-...` en Swing.

Usar únicamente cuentas y tarjetas ficticias. No publicar esta configuración como un sistema de producción: antes deben revisarse tokens de verificación/recuperación, sesiones y revocación, credenciales predeterminadas, protección de endpoints y tratamiento de datos de tarjetas. Cambiar la estructura del frontend no resuelve por sí mismo esos puntos.
