DROP DATABASE IF EXISTS tp_cine_api;
CREATE DATABASE tp_cine_api CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tp_cine_api;

CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE pelicula (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    duracion INT NOT NULL,
    descripcion LONGTEXT,
    portada_url LONGTEXT,
    categoria_id INT NOT NULL,
    CONSTRAINT fk_pelicula_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE sala (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT NOT NULL
);

CREATE TABLE butaca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fila VARCHAR(10) NOT NULL,
    numero INT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    bloqueo_hasta DATETIME NULL,
    sala_id INT NOT NULL,
    CONSTRAINT fk_butaca_sala FOREIGN KEY (sala_id) REFERENCES sala(id)
);

CREATE TABLE funcion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    horario TIME NOT NULL,
    pelicula_id INT NOT NULL,
    sala_id INT NOT NULL,
    formato VARCHAR(30) NOT NULL,
    idioma VARCHAR(30) NOT NULL,
    precio_entrada DOUBLE NOT NULL,
    CONSTRAINT fk_funcion_pelicula FOREIGN KEY (pelicula_id) REFERENCES pelicula(id),
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id) REFERENCES sala(id)
);

CREATE TABLE espectador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    contrasenia VARCHAR(100) NOT NULL,
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE metodo_pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(16) NOT NULL,
    fecha_vencimiento VARCHAR(7) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    cvv VARCHAR(3) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    espectador_id INT NULL,
    CONSTRAINT fk_metodo_pago_espectador FOREIGN KEY (espectador_id) REFERENCES espectador(id)
);

CREATE TABLE ticket (
    id INT AUTO_INCREMENT PRIMARY KEY,
    espectador_id INT NOT NULL,
    metodo_pago_resumen VARCHAR(120),
    codigo_qr VARCHAR(120) NOT NULL,
    CONSTRAINT fk_ticket_espectador FOREIGN KEY (espectador_id) REFERENCES espectador(id)
);

CREATE TABLE entrada (
    id INT AUTO_INCREMENT PRIMARY KEY,
    precio DOUBLE NOT NULL,
    espectador_id INT NOT NULL,
    ticket_id INT NULL,
    funcion_id INT NOT NULL,
    butaca_id INT NOT NULL,
    horario DATETIME NOT NULL,
    estado VARCHAR(40) NOT NULL,
    CONSTRAINT fk_entrada_espectador FOREIGN KEY (espectador_id) REFERENCES espectador(id),
    CONSTRAINT fk_entrada_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id),
    CONSTRAINT fk_entrada_funcion FOREIGN KEY (funcion_id) REFERENCES funcion(id),
    CONSTRAINT fk_entrada_butaca FOREIGN KEY (butaca_id) REFERENCES butaca(id)
);

CREATE TABLE producto_confiteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    precio DOUBLE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tamano VARCHAR(30) NOT NULL
);

CREATE TABLE item_consumo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    ticket_id INT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT fk_item_consumo_producto FOREIGN KEY (producto_id) REFERENCES producto_confiteria(id),
    CONSTRAINT fk_item_consumo_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id)
);

INSERT INTO categoria (id, nombre) VALUES
(1, 'Acción'),
(2, 'Drama'),
(3, 'Terror'),
(4, 'Comedia'),
(5, 'Ciencia ficción'),
(6, 'Animación'),
(7, 'Suspenso'),
(8, 'Aventura');

INSERT INTO pelicula (id, titulo, duracion, descripcion, portada_url, categoria_id) VALUES
(1, 'Código Rojo', 112, 'Una agente retirada vuelve a la ciudad para frenar un ataque que amenaza con paralizar todo el sistema de transporte.', 'https://picsum.photos/seed/codigo-rojo/420/620', 1),
(2, 'La Última Carta', 104, 'Dos hermanos encuentran cartas familiares que revelan una historia oculta y los obliga a reconstruir su vínculo.', 'https://picsum.photos/seed/la-ultima-carta/420/620', 2),
(3, 'Casa del Lago Negro', 98, 'Un grupo de amigos pasa un fin de semana en una cabaña aislada donde cada ruido parece venir de otra época.', 'https://picsum.photos/seed/lago-negro/420/620', 3),
(4, 'Cita a las Ocho', 95, 'Una comedia romántica sobre citas cruzadas, confusiones y una reserva imposible de cancelar.', 'https://picsum.photos/seed/cita-ocho/420/620', 4),
(5, 'Órbita 9', 118, 'La tripulación de una estación espacial descubre una señal que podría cambiar el destino de la Tierra.', 'https://picsum.photos/seed/orbita-nueve/420/620', 5),
(6, 'Luna y el Bosque Encantado', 101, 'Una joven inventora atraviesa un bosque mágico para devolverle la luz a su pueblo.', 'https://picsum.photos/seed/luna-bosque/420/620', 6),
(7, 'El Testigo Silencioso', 108, 'Un fotógrafo presencia un crimen desde su ventana y comienza una investigación que lo pone en peligro.', 'https://picsum.photos/seed/testigo-silencioso/420/620', 7),
(8, 'Rumbo al Norte', 92, 'Tres amigos viajan por la ruta patagónica buscando un mapa perdido y una aventura que los cambie para siempre.', 'https://picsum.photos/seed/rumbo-norte/420/620', 8);

INSERT INTO sala (id, nombre, capacidad) VALUES
(1, 'Sala Premium', 60),
(2, 'Sala Central', 96),
(3, 'Sala 3D', 70),
(4, 'Sala Familiar', 40);

CREATE TEMPORARY TABLE seed_filas (
    fila VARCHAR(2),
    orden INT
);

INSERT INTO seed_filas (fila, orden) VALUES
('A', 1), ('B', 2), ('C', 3), ('D', 4), ('E', 5), ('F', 6),
('G', 7), ('H', 8), ('I', 9), ('J', 10), ('K', 11), ('L', 12);

CREATE TEMPORARY TABLE seed_numeros (numero INT);

INSERT INTO seed_numeros (numero) VALUES
(1), (2), (3), (4), (5), (6), (7), (8), (9), (10), (11), (12);

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT f.fila, n.numero, 'DISPONIBLE', NULL, 1
FROM seed_filas f
JOIN seed_numeros n
WHERE f.orden <= 6 AND n.numero <= 10;

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT f.fila, n.numero, 'DISPONIBLE', NULL, 2
FROM seed_filas f
JOIN seed_numeros n
WHERE f.orden <= 8 AND n.numero <= 12;

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT f.fila, n.numero, 'DISPONIBLE', NULL, 3
FROM seed_filas f
JOIN seed_numeros n
WHERE f.orden <= 7 AND n.numero <= 10;

INSERT INTO butaca (fila, numero, estado, bloqueo_hasta, sala_id)
SELECT f.fila, n.numero, 'DISPONIBLE', NULL, 4
FROM seed_filas f
JOIN seed_numeros n
WHERE f.orden <= 5 AND n.numero <= 8;

CREATE TEMPORARY TABLE seed_dias (n INT);
INSERT INTO seed_dias (n) VALUES (0), (1), (2), (3), (4), (5), (6);

CREATE TEMPORARY TABLE seed_funciones (
    pelicula_id INT,
    sala_id INT,
    horario TIME,
    formato VARCHAR(30),
    idioma VARCHAR(30),
    precio_entrada DOUBLE
);

INSERT INTO seed_funciones (pelicula_id, sala_id, horario, formato, idioma, precio_entrada) VALUES
(1, 1, '13:00:00', 'DOS_D', 'ESPANIOL', 7800),
(2, 1, '15:20:00', 'DOS_D', 'SUBTITULADA', 7600),
(3, 1, '17:35:00', 'DOS_D', 'SUBTITULADA', 8200),
(4, 1, '20:00:00', 'DOS_D', 'ESPANIOL', 7400),
(5, 2, '13:15:00', 'DOS_D', 'SUBTITULADA', 8400),
(6, 2, '15:45:00', 'DOS_D', 'ESPANIOL', 7200),
(7, 2, '18:00:00', 'DOS_D', 'SUBTITULADA', 8000),
(8, 2, '20:30:00', 'DOS_D', 'ESPANIOL', 7300),
(1, 3, '14:15:00', 'TRES_D', 'SUBTITULADA', 9400),
(3, 3, '16:45:00', 'TRES_D', 'ESPANIOL', 9600),
(5, 3, '19:00:00', 'TRES_D', 'SUBTITULADA', 9800),
(7, 3, '21:35:00', 'TRES_D', 'SUBTITULADA', 9300),
(2, 4, '13:45:00', 'DOS_D', 'ESPANIOL', 6900),
(4, 4, '16:10:00', 'DOS_D', 'ESPANIOL', 6900),
(6, 4, '18:20:00', 'TRES_D', 'ESPANIOL', 8500),
(8, 4, '20:45:00', 'DOS_D', 'SUBTITULADA', 7100);

INSERT INTO funcion (fecha, horario, pelicula_id, sala_id, formato, idioma, precio_entrada)
SELECT DATE_ADD(CURDATE(), INTERVAL d.n DAY),
       sf.horario,
       sf.pelicula_id,
       sf.sala_id,
       sf.formato,
       sf.idioma,
       sf.precio_entrada
FROM seed_dias d
CROSS JOIN seed_funciones sf
ORDER BY d.n, sf.sala_id, sf.horario;

INSERT INTO producto_confiteria (id, nombre, precio, tipo, tamano) VALUES
(1, 'Pochoclos salados chicos', 2600, 'POCHOCLOS', 'CHICO'),
(2, 'Pochoclos salados medianos', 3400, 'POCHOCLOS', 'MEDIANO'),
(3, 'Pochoclos salados grandes', 4300, 'POCHOCLOS', 'GRANDE'),
(4, 'Pochoclos dulces medianos', 3600, 'POCHOCLOS', 'MEDIANO'),
(5, 'Pochoclos mixtos grandes', 4600, 'POCHOCLOS', 'GRANDE'),
(6, 'Gaseosa chica', 1900, 'BEBIDA', 'CHICO'),
(7, 'Gaseosa mediana', 2500, 'BEBIDA', 'MEDIANO'),
(8, 'Gaseosa grande', 3100, 'BEBIDA', 'GRANDE'),
(9, 'Agua mineral', 1700, 'BEBIDA', 'UNICO'),
(10, 'Café americano', 1800, 'BEBIDA', 'UNICO'),
(11, 'Chocolate confitado', 2100, 'DULCE', 'UNICO'),
(12, 'Gomitas surtidas', 1900, 'DULCE', 'UNICO'),
(13, 'Alfajor de chocolate', 1600, 'DULCE', 'UNICO'),
(14, 'Tableta de chocolate', 2300, 'DULCE', 'UNICO'),
(15, 'Combo clásico', 6200, 'COMBO', 'UNICO'),
(16, 'Combo pareja', 9800, 'COMBO', 'UNICO'),
(17, 'Combo familiar', 14500, 'COMBO', 'UNICO'),
(18, 'Combo infantil', 5600, 'COMBO', 'UNICO');

DROP TEMPORARY TABLE IF EXISTS seed_funciones;
DROP TEMPORARY TABLE IF EXISTS seed_dias;
DROP TEMPORARY TABLE IF EXISTS seed_numeros;
DROP TEMPORARY TABLE IF EXISTS seed_filas;
