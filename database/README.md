# Base de datos de TP Cine API

Esta carpeta contiene los scripts y recursos utilizados para preparar la base de datos del sistema.

## Archivos disponibles

### `seed_demo.sql`

Es el script principal de instalación. Al ejecutarlo:

- Elimina la base `tp_cine_api` si ya existe.
- Crea nuevamente la base con codificación UTF-8.
- Crea las tablas, claves foráneas e índices del sistema.
- Carga 8 categorías.
- Carga 18 películas con sus descripciones y rutas de portada.
- Carga 4 salas.
- Genera las 266 butacas correspondientes a las salas.
- Carga 19 productos de confitería.
- Carga los planes de membresía del Club Film Zone.

Este script no crea funciones, espectadores, empleados, métodos de pago, tickets ni compras de prueba.

> **Advertencia:** al ejecutar este archivo se elimina toda la información existente en `tp_cine_api`.

### `completar_funciones_1_semana.sql`

Actualiza siete días consecutivos tomando la fecha y hora de MySQL. Si se ejecuta antes de las 12:30 comienza hoy; después comienza mañana, para no crear una jornada que ya empezó. Al terminar muestra el resumen por día y por película.

- Programa 6 películas diferentes cada día, superando el mínimo de 5, con al menos 2 funciones por película.
- Con el catálogo actual produce 16 funciones diarias: cada película tiene 2 o 3, para un total de 112 funciones semanales.
- Rota las películas e intercala géneros a partir del catálogo existente; no requiere IDs consecutivos.
- Utiliza 4 salas disponibles con butacas, con inicios entre las 13:00 y las 23:30. Una función nocturna puede terminar después de medianoche.
- Calcula los bloques según la duración máxima de las películas y deja al menos 20 minutos para limpieza. Películas más largas pueden reducir el cuarto turno, sin bajar de dos funciones por película.
- Incluye 2D, 3D, español y subtituladas. El precio base es $8500, con adicionales por sala premium, 3D y fin de semana.
- Reemplaza solamente funciones de esa semana que no tienen entradas ni reservas y no coinciden con el plan nuevo. No modifica usuarios, tarjetas, compras, entradas, tickets ni otras fechas.
- Conserva las funciones asociadas a cualquier entrada o reserva, incluso si la entrada ya fue reembolsada. Si esos horarios impiden completar el plan o generan superposiciones, revierte toda la actualización y muestra un error.
- Al repetirlo con el mismo catálogo y fechas conserva los IDs de las funciones que ya coinciden con el plan: no duplica horarios. La rotación usa la fecha, no el número de veces que se ejecuta.

El script utiliza una transacción y un procedimiento auxiliar que se elimina al finalizar. Restaura el modo Safe Updates de la sesión. Requiere MySQL 8 y permisos para crear procedimientos. Ejecutarlo completo, preferentemente antes de iniciar ventas o editar horarios en la aplicación. Necesita al menos 6 películas de duración válida (1 a 240 minutos) y 4 salas operativas.

### `descripcionPeliculas.txt`

Contiene las descripciones utilizadas como referencia para las películas cargadas en la seed.

### `portadasPeliculas/`

Contiene las imágenes originales utilizadas como portadas de las películas. Las rutas `/images/...` almacenadas en la base apuntan a los recursos servidos por Spring Boot desde `src/main/resources/static/images/`. En desarrollo, Vite redirige esas peticiones al backend.

## Orden de ejecución en MySQL Workbench

1. Iniciar MySQL Server.
2. Abrir MySQL Workbench y conectarse al servidor.
3. Abrir `seed_demo.sql`.
4. Ejecutar todo el archivo con el botón del rayo.
5. Abrir `completar_funciones_1_semana.sql`.
6. Ejecutar todo el archivo.
7. Actualizar la lista de esquemas y verificar que aparezca `tp_cine_api`.

**Si la base ya tiene datos, no repetir los pasos de `seed_demo.sql`: elimina todas las compras y cuentas.** Para renovar la cartelera ejecutar únicamente `completar_funciones_1_semana.sql` y recargar React/Swing. El archivo anterior de dos semanas fue reemplazado por este.

## Datos de acceso

La cuenta del dueño no se almacena en la base de datos. El backend utiliza por defecto:

- Usuario: `admin`
- Contraseña: `admin`

Estas credenciales pueden reemplazarse mediante las variables de entorno `ADMIN_USERNAME` y `ADMIN_PASSWORD`.

Después de ejecutar la seed, las cuentas de empleados y espectadores deben crearse desde la aplicación o mediante sus endpoints correspondientes.
