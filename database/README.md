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
- Carga 18 productos de confitería.

Este script no crea funciones, espectadores, empleados, métodos de pago, tickets ni compras de prueba.

> **Advertencia:** al ejecutar este archivo se elimina toda la información existente en `tp_cine_api`.

### `completar_funciones_2_semanas.sql`

Completa la cartelera desde la fecha actual hasta los siguientes 13 días, cubriendo un total de 14 días.

- Programa 6 películas diferentes por día.
- Distribuye las funciones entre las 4 salas.
- Incluye funciones 2D, 3D, en español y subtituladas.
- Evita insertar una función cuando se superpone con otra existente en la misma sala.
- No elimina espectadores, empleados, compras, entradas, tickets ni funciones existentes.
- Puede volver a ejecutarse más adelante para completar una nueva ventana de dos semanas.

### `descripcionPeliculas.txt`

Contiene las descripciones utilizadas como referencia para las películas cargadas en la seed.

### `portadasPeliculas/`

Contiene las imágenes originales utilizadas como portadas de las películas. Las rutas almacenadas en la base apuntan a los recursos publicados por el frontend.

## Orden de ejecución en MySQL Workbench

1. Iniciar MySQL Server.
2. Abrir MySQL Workbench y conectarse al servidor.
3. Abrir `seed_demo.sql`.
4. Ejecutar todo el archivo con el botón del rayo.
5. Abrir `completar_funciones_2_semanas.sql`.
6. Ejecutar todo el archivo.
7. Actualizar la lista de esquemas y verificar que aparezca `tp_cine_api`.

Para renovar solamente las fechas de la cartelera no es necesario volver a ejecutar la seed. Se debe ejecutar únicamente `completar_funciones_2_semanas.sql`.

## Datos de acceso

La cuenta del dueño no se almacena en la base de datos. El backend utiliza por defecto:

- Usuario: `admin`
- Contraseña: `admin`

Estas credenciales pueden reemplazarse mediante las variables de entorno `ADMIN_USERNAME` y `ADMIN_PASSWORD`.

Después de ejecutar la seed, las cuentas de empleados y espectadores deben crearse desde la aplicación o mediante sus endpoints correspondientes.
