# Guía de ejecución de Film Zone

El sistema tiene tres procesos: backend Spring Boot (`8080`), web React para espectadores (`5173`) e interfaz Swing para dueño y empleados. React y Swing consumen la misma API; ninguno se conecta directamente a MySQL.

## Requisitos

- JDK 17 y Maven disponibles en la terminal (`java -version` y `mvn.cmd -version`).
- MySQL Server 8 iniciado; Workbench para ejecutar los scripts.
- Node.js 18 o posterior y npm para React (`node -v` y `npm.cmd -v`).
- Puertos disponibles para backend y frontend.

En los ejemplos se utiliza PowerShell. Reemplazar `C:\ruta\TP_Cine_API` por la carpeta real del repositorio, preferentemente sin tildes en la ruta. En macOS/Linux se usa `mvn` y `npm` en lugar de `mvn.cmd` y `npm.cmd`, y `export DB_PASSWORD='...'` para las variables.

## 1. Preparar la base de datos

Solo para una instalación nueva o un reinicio deliberado de los datos:

1. Conectarse al servidor desde Workbench.
2. Abrir y ejecutar completo `database/seed_demo.sql`.
3. Abrir y ejecutar completo `database/completar_funciones_2_semanas.sql`.

**Atención: `seed_demo.sql` elimina la base `tp_cine_api` existente y todos sus datos.** Crea las tablas y carga catálogo, salas, butacas, productos y membresías. No crea espectadores, empleados, compras ni tickets de prueba.

El segundo script agrega funciones para 14 días desde la fecha de MySQL y evita superposiciones. Para renovar únicamente la cartelera, ejecutar solo ese segundo script. No es necesario borrar la base al descargar cambios del código.

## 2. Iniciar el backend

En una terminal:

```powershell
cd "C:\ruta\TP_Cine_API"
$env:DB_URL="jdbc:mysql://localhost:3307/tp_cine_api"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="CLAVE_DE_MYSQL"

mvn.cmd spring-boot:run
```

Si MySQL usa `3306`, cambiar el puerto en `DB_URL`. Las variables se definen en esta misma terminal, antes de iniciar Maven. Esperar el mensaje de arranque de Spring Boot y dejar la terminal abierta.

`http://localhost:8080` es la API y sirve imágenes; ya no contiene la web anterior ni un panel `/admin`. Para comprobar la API se puede consultar `http://localhost:8080/peliculas`.

## 3. Iniciar React

En otra terminal:

```powershell
cd "C:\ruta\TP_Cine_API\frontend"
npm.cmd install
npm.cmd run dev
```

Abrir `http://localhost:5173`, o la dirección que muestre Vite si eligió otro puerto. `npm.cmd install` es necesario al preparar la PC y cuando cambian las dependencias; no hace falta repetirlo en cada inicio.

Las dependencias JavaScript se administran únicamente en `frontend/package.json`. La raíz usa Maven; no ejecutar `npm install` allí. `node_modules/` y `dist/` no se versionan.

El proxy de `frontend/vite.config.js` envía las peticiones al backend. El frontend no necesita conocer la contraseña de MySQL. El envío y la confirmación de correo se simulan en la aplicación con la configuración de desarrollo.

## 4. Iniciar Swing

Con el backend todavía abierto, en una tercera terminal:

```powershell
cd "C:\ruta\TP_Cine_API"
mvn.cmd compile exec:java
```

Se utiliza `compile` para disponer de las clases después de descargar o modificar el código. Maven ejecuta `desktop.inicio.CineDesktopApplication`, configurada en `pom.xml`.

Credenciales predeterminadas del dueño:

```text
Usuario: admin
Contraseña: admin
```

El backend permite reemplazarlas con `ADMIN_USERNAME` y `ADMIN_PASSWORD`. No hay que crear esa cuenta en MySQL. Desde Dueño se crean los empleados; el login decide qué interfaz abrir según la cuenta, sin selector manual de rol.

Cerrar sesión vuelve al login; Salir o cerrar la ventana principal termina Swing, después de la confirmación. Esto no detiene el backend ni React.

## 5. Prueba corta entre las dos interfaces

1. En React, crear una cuenta de espectador y confirmar el correo en la ventana simulada.
2. Elegir una función futura, seleccionar butacas, pasar por el Candy y pagar con una tarjeta de prueba no vencida.
3. Copiar el código completo `TCK-...` del ticket o de Mi cuenta.
4. En Swing, iniciar sesión y abrir Consumos para validar la entrega por QR, si se compraron productos.
5. Abrir Ventas y tickets (Dueño) o Control de acceso (Empleado), pegar el mismo código y validar las entradas.

Validar entradas **no entrega los productos**. Si hay consumos pendientes aparece una advertencia antes de confirmar el ingreso. También existen acciones manuales para operar cuando falla el lector. Un lector puede simularse pegando el código y presionando Enter.

La seed actual no carga códigos `TCK-DEMO-*`: los códigos para esta prueba se obtienen realizando una compra.

## 6. Problemas frecuentes

| Síntoma | Qué revisar |
| --- | --- |
| Maven no encuentra el proyecto o el plugin | Ejecutar desde la carpeta que contiene `pom.xml`, no desde `C:\Windows\system32`. |
| npm no encuentra `package.json` | Cambiar a la subcarpeta `frontend/`. |
| PowerShell bloquea `npm.ps1` | Usar `npm.cmd`; no hace falta desactivar la política de seguridad. |
| No se reconoce `vite` | Ejecutar primero `npm.cmd install` dentro de `frontend/`. |
| No encuentra la clase principal de Java | Comprobar la ruta y el JDK; ejecutar `mvn.cmd clean compile`. Si compila, volver a iniciar. |
| Error de conexión con MySQL | Revisar servidor iniciado, puerto, esquema, usuario y contraseña. |
| React o Swing informa un error de servidor | Revisar primero la terminal del backend; iniciar Vite por sí solo no inicia Spring Boot. |
| No hay funciones futuras | Ejecutar únicamente `database/completar_funciones_2_semanas.sql` y recargar la interfaz. |
| Aparece "No pudimos abrir esta página" | Volver a cargar y revisar la conexión. Si se publica la web, debe servirse el build completo de `frontend/dist/`, incluidos sus archivos `assets/`; no hace falta borrar MySQL. |

Para estudiar la organización: [React](frontend/README.md), [Swing](src/main/java/desktop/README.md), [estilos](frontend/src/styles/README.md) y [guía completa](frontend/GUIA_ESTUDIO_FRONTEND.md).

