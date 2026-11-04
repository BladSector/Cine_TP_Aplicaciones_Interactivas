# Film Zone

Sistema académico de gestión de un cine. React es la web del espectador; Swing es la aplicación del
dueño y los empleados. Las dos interfaces consumen la misma API Spring Boot, que aplica las reglas y
utiliza MySQL.

## Documentación del código

Hay dos guías, sin recorridos repetidos ni una guía de ejecución aparte:

**[Backend](GUIA_BACKEND.md)**

Configuración, seguridad, controllers, services, repositories, entidades, enums, recursos y scripts
SQL. Archivos y métodos para seguir cada operación.

**[Frontend: React y Swing](GUIA_FRONTEND.md)**

Páginas, componentes, estado, hooks, CSS y cliente HTTP de React; ventanas, módulos, paneles,
diálogos y comunicación de Swing.

Las guías incluyen el flujo de compra, reservas, pago, ticket/QR y operación del personal, además de
un mapa de dónde modificar cada cosa. Los métodos principales se explican por responsabilidad; no se
repiten getters ni código obvio.

## Ubicación rápida

- `frontend/src/`: código de React.
- `src/main/java/desktop/`: código de Swing.
- `src/main/java/controller/`, `service/`, `repository/`, `modelo/` y `com/tp/cine/`: backend.
- `src/main/resources/`: configuración e imágenes públicas.
- `database/`: seed, programación semanal y material de películas.

`target/`, `frontend/dist/` y `frontend/node_modules/` son archivos generados, no código fuente
para editar.

**Atención:** `database/seed_demo.sql` elimina los datos anteriores. Para renovar solo funciones
existe `database/completar_funciones_1_semana.sql`; su comportamiento se explica en la guía del
backend. El cobro y los correos de confirmación/recuperación de la demostración son simulados: usar
datos ficticios.
