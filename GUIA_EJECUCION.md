# Guía de ejecución

## Requisitos

- Java 17 o posterior.
- Maven.
- MySQL Server 8.
- MySQL Workbench.
- Puerto 8080 disponible.

## 1. Preparar la base de datos

1. Iniciar el servidor MySQL.
2. Abrir MySQL Workbench.
3. Abrir `database/seed_demo.sql`.
4. Ejecutar el script completo con el botón del rayo.

El script crea la base `tp_cine_api` y carga películas, salas, butacas,
funciones para 14 días, productos de confitería y tickets de prueba.

## 2. Iniciar el backend

Abrir PowerShell en la carpeta que contiene `pom.xml` y ejecutar:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/tp_cine_api"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="CLAVE_DE_MYSQL"

mvn spring-boot:run
```

Si MySQL utiliza el puerto 3307, reemplazar `3306` por `3307`. La terminal
debe permanecer abierta mientras se utiliza la aplicación.

## 3. Iniciar la interfaz Swing

Abrir otra terminal en la misma carpeta y ejecutar:

```powershell
mvn exec:java
```

Credenciales del dueño:

```text
Usuario: admin
Contraseña: admin
```

Desde la sesión del dueño se puede crear un empleado para probar su interfaz.

## 4. Tickets de demostración

Los siguientes valores simulan el contenido leído desde un código QR. Se
pueden escribir o pegar en los campos de control de acceso y consumos de Swing:

- `TCK-DEMO-PENDIENTE`: entradas pagadas sin validar y consumos pendientes.
- `TCK-DEMO-CONSUMOS`: entradas validadas con consumos pendientes de entrega.
- `TCK-DEMO-PROCESADO`: entradas validadas y consumos ya entregados.
- `TCK-DEMO-REEMBOLSADO`: entradas reembolsadas y consumos cancelados.

Por ejemplo, al procesar `TCK-DEMO-PENDIENTE`, el sistema valida las entradas y
marca sus consumos como entregados. Los tickets creados mediante una compra real
generan un código único con el formato `TCK-<UUID>`.

## 5. Interfaz web

Con el backend iniciado:

- Cliente: `http://localhost:8080`
- Administración web: `http://localhost:8080/admin`

Los comandos de Maven deben ejecutarse desde la carpeta donde se encuentra
`pom.xml`.

