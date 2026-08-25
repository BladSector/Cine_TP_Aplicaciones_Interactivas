CREATE DATABASE IF NOT EXISTS tp_cine_api;
USE tp_cine_api;

CREATE TABLE IF NOT EXISTS categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS sala (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT NOT NULL
);

CREATE TABLE IF NOT EXISTS producto_confiteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DOUBLE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tamano VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS pelicula (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    duracion INT NOT NULL,
    descripcion LONGTEXT,
    portada_url LONGTEXT,
    categoria_id INT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE IF NOT EXISTS butaca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fila VARCHAR(10) NOT NULL,
    numero INT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    bloqueo_hasta DATETIME,
    sala_id INT NOT NULL,
    FOREIGN KEY (sala_id) REFERENCES sala(id)
);

CREATE TABLE IF NOT EXISTS funcion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    horario TIME NOT NULL,
    formato VARCHAR(30) NOT NULL,
    idioma VARCHAR(30) NOT NULL,
    precio_entrada DOUBLE NOT NULL,
    pelicula_id INT NOT NULL,
    sala_id INT NOT NULL,
    FOREIGN KEY (pelicula_id) REFERENCES pelicula(id),
    FOREIGN KEY (sala_id) REFERENCES sala(id)
);

CREATE TABLE IF NOT EXISTS espectador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    contrasenia VARCHAR(100) NOT NULL,
    email_verificado BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS metodo_pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(16) NOT NULL,
    fecha_vencimiento VARCHAR(7) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    cvv VARCHAR(3) NOT NULL,
    espectador_id INT,
    FOREIGN KEY (espectador_id) REFERENCES espectador(id)
);

CREATE TABLE IF NOT EXISTS ticket (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_qr VARCHAR(80) NOT NULL,
    espectador_id INT NOT NULL,
    metodo_pago_id INT,
    FOREIGN KEY (espectador_id) REFERENCES espectador(id),
    FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago(id)
);

CREATE TABLE IF NOT EXISTS entrada (
    id INT AUTO_INCREMENT PRIMARY KEY,
    precio DOUBLE NOT NULL,
    horario DATETIME NOT NULL,
    estado VARCHAR(30) NOT NULL,
    espectador_id INT NOT NULL,
    ticket_id INT,
    funcion_id INT NOT NULL,
    butaca_id INT NOT NULL,
    FOREIGN KEY (espectador_id) REFERENCES espectador(id),
    FOREIGN KEY (ticket_id) REFERENCES ticket(id),
    FOREIGN KEY (funcion_id) REFERENCES funcion(id),
    FOREIGN KEY (butaca_id) REFERENCES butaca(id)
);

CREATE TABLE IF NOT EXISTS item_consumo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    producto_id INT NOT NULL,
    ticket_id INT,
    FOREIGN KEY (producto_id) REFERENCES producto_confiteria(id),
    FOREIGN KEY (ticket_id) REFERENCES ticket(id)
);

INSERT INTO categoria (nombre)
SELECT 'Accion' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Accion');
INSERT INTO categoria (nombre)
SELECT 'Aventura' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Aventura');
INSERT INTO categoria (nombre)
SELECT 'Animacion' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Animacion');
INSERT INTO categoria (nombre)
SELECT 'Drama' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Drama');

SET @accion_id = (SELECT id FROM categoria WHERE nombre = 'Accion' LIMIT 1);
SET @aventura_id = (SELECT id FROM categoria WHERE nombre = 'Aventura' LIMIT 1);
SET @animacion_id = (SELECT id FROM categoria WHERE nombre = 'Animacion' LIMIT 1);
SET @drama_id = (SELECT id FROM categoria WHERE nombre = 'Drama' LIMIT 1);

INSERT INTO pelicula (titulo, duracion, descripcion, portada_url, categoria_id)
SELECT 'Spider-man: Un Dia Nuevo', 144,
       'Peter Parker intenta reconstruir su vida mientras una nueva amenaza pone en riesgo la ciudad.',
       'https://placehold.co/320x480/c1121f/ffffff?text=Spider-man',
       @aventura_id
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo = 'Spider-man: Un Dia Nuevo');

INSERT INTO pelicula (titulo, duracion, descripcion, portada_url, categoria_id)
SELECT 'Mision Eclipse', 128,
       'Un equipo especial debe detener un ataque tecnologico durante un eclipse global.',
       'https://placehold.co/320x480/0f766e/ffffff?text=Mision+Eclipse',
       @accion_id
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo = 'Mision Eclipse');

INSERT INTO pelicula (titulo, duracion, descripcion, portada_url, categoria_id)
SELECT 'La Ciudad de Papel', 102,
       'Una joven artista descubre una ciudad oculta dentro de los dibujos de su abuelo.',
       'https://placehold.co/320x480/2563eb/ffffff?text=Ciudad+de+Papel',
       @animacion_id
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo = 'La Ciudad de Papel');

INSERT INTO pelicula (titulo, duracion, descripcion, portada_url, categoria_id)
SELECT 'Ultima Funcion', 116,
       'El cierre de un viejo cine une a sus empleados en una noche de recuerdos y decisiones.',
       'https://placehold.co/320x480/7c2d12/ffffff?text=Ultima+Funcion',
       @drama_id
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo = 'Ultima Funcion');

INSERT INTO sala (nombre, capacidad)
SELECT 'Sala 1', 38 WHERE NOT EXISTS (SELECT 1 FROM sala WHERE nombre = 'Sala 1');
INSERT INTO sala (nombre, capacidad)
SELECT 'Sala Premium', 30 WHERE NOT EXISTS (SELECT 1 FROM sala WHERE nombre = 'Sala Premium');

SET @sala1_id = (SELECT id FROM sala WHERE nombre = 'Sala 1' LIMIT 1);
SET @premium_id = (SELECT id FROM sala WHERE nombre = 'Sala Premium' LIMIT 1);

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT fila, numero, 'DISPONIBLE', NULL, @sala1_id
FROM (
    SELECT 'A' fila, 2 numero UNION ALL SELECT 'A', 3 UNION ALL SELECT 'A', 4 UNION ALL SELECT 'A', 5 UNION ALL SELECT 'A', 6 UNION ALL SELECT 'A', 7 UNION ALL
    SELECT 'B', 1 UNION ALL SELECT 'B', 2 UNION ALL SELECT 'B', 3 UNION ALL SELECT 'B', 4 UNION ALL SELECT 'B', 5 UNION ALL SELECT 'B', 6 UNION ALL SELECT 'B', 7 UNION ALL SELECT 'B', 8 UNION ALL
    SELECT 'C', 1 UNION ALL SELECT 'C', 2 UNION ALL SELECT 'C', 3 UNION ALL SELECT 'C', 4 UNION ALL SELECT 'C', 5 UNION ALL SELECT 'C', 6 UNION ALL SELECT 'C', 7 UNION ALL SELECT 'C', 8 UNION ALL
    SELECT 'D', 1 UNION ALL SELECT 'D', 2 UNION ALL SELECT 'D', 3 UNION ALL SELECT 'D', 4 UNION ALL SELECT 'D', 5 UNION ALL SELECT 'D', 6 UNION ALL SELECT 'D', 7 UNION ALL SELECT 'D', 8 UNION ALL
    SELECT 'E', 2 UNION ALL SELECT 'E', 3 UNION ALL SELECT 'E', 4 UNION ALL SELECT 'E', 5 UNION ALL SELECT 'E', 6 UNION ALL SELECT 'E', 7 UNION ALL
    SELECT 'F', 3 UNION ALL SELECT 'F', 4
) nuevas
WHERE NOT EXISTS (
    SELECT 1 FROM butaca b
    WHERE b.sala_id = @sala1_id AND b.fila = nuevas.fila AND b.numero = nuevas.numero
);

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT fila, numero, 'DISPONIBLE', NULL, @premium_id
FROM (
    SELECT 'A' fila, 2 numero UNION ALL SELECT 'A', 3 UNION ALL SELECT 'A', 4 UNION ALL SELECT 'A', 5 UNION ALL
    SELECT 'B', 1 UNION ALL SELECT 'B', 2 UNION ALL SELECT 'B', 3 UNION ALL SELECT 'B', 4 UNION ALL SELECT 'B', 5 UNION ALL SELECT 'B', 6 UNION ALL
    SELECT 'C', 1 UNION ALL SELECT 'C', 2 UNION ALL SELECT 'C', 3 UNION ALL SELECT 'C', 4 UNION ALL SELECT 'C', 5 UNION ALL SELECT 'C', 6 UNION ALL
    SELECT 'D', 1 UNION ALL SELECT 'D', 2 UNION ALL SELECT 'D', 3 UNION ALL SELECT 'D', 4 UNION ALL SELECT 'D', 5 UNION ALL SELECT 'D', 6 UNION ALL
    SELECT 'E', 2 UNION ALL SELECT 'E', 3 UNION ALL SELECT 'E', 4 UNION ALL SELECT 'E', 5 UNION ALL
    SELECT 'F', 3 UNION ALL SELECT 'F', 4 UNION ALL SELECT 'F', 5 UNION ALL SELECT 'F', 6
) nuevas
WHERE NOT EXISTS (
    SELECT 1 FROM butaca b
    WHERE b.sala_id = @premium_id AND b.fila = nuevas.fila AND b.numero = nuevas.numero
);

SET @spiderman_id = (SELECT id FROM pelicula WHERE titulo = 'Spider-man: Un Dia Nuevo' LIMIT 1);
SET @mision_id = (SELECT id FROM pelicula WHERE titulo = 'Mision Eclipse' LIMIT 1);
SET @ciudad_id = (SELECT id FROM pelicula WHERE titulo = 'La Ciudad de Papel' LIMIT 1);
SET @ultima_id = (SELECT id FROM pelicula WHERE titulo = 'Ultima Funcion' LIMIT 1);

INSERT INTO funcion (fecha, horario, formato, idioma, precio_entrada, pelicula_id, sala_id)
SELECT '2026-08-26', '18:30:00', 'DOS_D', 'ESPANIOL', 7700, @spiderman_id, @sala1_id
WHERE NOT EXISTS (SELECT 1 FROM funcion WHERE pelicula_id = @spiderman_id AND sala_id = @sala1_id AND fecha = '2026-08-26' AND horario = '18:30:00');

INSERT INTO funcion (fecha, horario, formato, idioma, precio_entrada, pelicula_id, sala_id)
SELECT '2026-08-26', '21:20:00', 'TRES_D', 'SUBTITULADA', 9500, @spiderman_id, @premium_id
WHERE NOT EXISTS (SELECT 1 FROM funcion WHERE pelicula_id = @spiderman_id AND sala_id = @premium_id AND fecha = '2026-08-26' AND horario = '21:20:00');

INSERT INTO funcion (fecha, horario, formato, idioma, precio_entrada, pelicula_id, sala_id)
SELECT '2026-08-27', '20:00:00', 'DOS_D', 'SUBTITULADA', 7200, @mision_id, @sala1_id
WHERE NOT EXISTS (SELECT 1 FROM funcion WHERE pelicula_id = @mision_id AND sala_id = @sala1_id AND fecha = '2026-08-27' AND horario = '20:00:00');

INSERT INTO funcion (fecha, horario, formato, idioma, precio_entrada, pelicula_id, sala_id)
SELECT '2026-08-28', '17:15:00', 'DOS_D', 'ESPANIOL', 6800, @ciudad_id, @sala1_id
WHERE NOT EXISTS (SELECT 1 FROM funcion WHERE pelicula_id = @ciudad_id AND sala_id = @sala1_id AND fecha = '2026-08-28' AND horario = '17:15:00');

INSERT INTO funcion (fecha, horario, formato, idioma, precio_entrada, pelicula_id, sala_id)
SELECT '2026-08-29', '22:10:00', 'DOS_D', 'ESPANIOL', 7000, @ultima_id, @premium_id
WHERE NOT EXISTS (SELECT 1 FROM funcion WHERE pelicula_id = @ultima_id AND sala_id = @premium_id AND fecha = '2026-08-29' AND horario = '22:10:00');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Pochoclos salados medianos', 3200, 'POCHOCLOS', 'MEDIANO'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Pochoclos salados medianos');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Pochoclos dulces grandes', 4200, 'POCHOCLOS', 'GRANDE'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Pochoclos dulces grandes');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Gaseosa grande', 2600, 'BEBIDA', 'GRANDE'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Gaseosa grande');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Agua mineral', 1800, 'BEBIDA', 'UNICO'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Agua mineral');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Chocolate', 2100, 'DULCE', 'UNICO'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Chocolate');

INSERT INTO producto_confiteria (nombre, precio, tipo, tamano)
SELECT 'Combo pareja', 8500, 'COMBO', 'UNICO'
WHERE NOT EXISTS (SELECT 1 FROM producto_confiteria WHERE nombre = 'Combo pareja');

SELECT 'Datos de simulacion cargados' AS resultado;
