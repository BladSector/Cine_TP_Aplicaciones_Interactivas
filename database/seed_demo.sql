-- Script de instalación y datos de prueba para MySQL 8.
-- Carga una cartelera de 14 días consecutivos desde la fecha de ejecución.
-- ATENCIÓN: elimina por completo la base tp_cine_api antes de recrearla.
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
    capacidad INT NOT NULL,
    estado VARCHAR(40) NOT NULL DEFAULT 'DISPONIBLE',
    detalle_estado VARCHAR(500) NULL
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
    contrasenia VARCHAR(255) NOT NULL,
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion_email VARCHAR(255) NULL,
    token_recuperacion_contrasenia VARCHAR(255) NULL
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
    metodo_pago_id INT NULL,
    metodo_pago_resumen VARCHAR(120),
    codigo_qr VARCHAR(120) NOT NULL,
    CONSTRAINT uq_ticket_codigo_qr UNIQUE (codigo_qr),
    CONSTRAINT fk_ticket_espectador FOREIGN KEY (espectador_id) REFERENCES espectador(id),
    CONSTRAINT fk_ticket_metodo_pago FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago(id)
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

CREATE TABLE reserva_butaca (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    funcion_id INT NOT NULL,
    butaca_id INT NOT NULL,
    espectador_id INT NOT NULL,
    vence_en DATETIME(6) NOT NULL,
    CONSTRAINT uq_reserva_funcion_butaca UNIQUE (funcion_id, butaca_id),
    CONSTRAINT fk_reserva_funcion FOREIGN KEY (funcion_id) REFERENCES funcion(id),
    CONSTRAINT fk_reserva_butaca FOREIGN KEY (butaca_id) REFERENCES butaca(id),
    CONSTRAINT fk_reserva_espectador FOREIGN KEY (espectador_id) REFERENCES espectador(id),
    INDEX idx_reserva_vencimiento (vence_en)
);

CREATE TABLE producto_confiteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    precio DOUBLE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tamano VARCHAR(30) NOT NULL,
    imagen_url LONGTEXT NULL
);

CREATE TABLE item_consumo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    ticket_id INT NULL,
    cantidad INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_item_consumo_producto FOREIGN KEY (producto_id) REFERENCES producto_confiteria(id),
    CONSTRAINT fk_item_consumo_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id)
);

CREATE TABLE empleado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    contrasenia VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'EMPLEADO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_empleado_usuario UNIQUE (usuario)
);

CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id INT NULL,
    usuario VARCHAR(100) NOT NULL,
    tipo_actor VARCHAR(30) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(60) NULL,
    entidad_id INT NULL,
    detalle VARCHAR(500) NULL,
    resultado VARCHAR(20) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_auditoria_empleado
        FOREIGN KEY (empleado_id) REFERENCES empleado(id) ON DELETE SET NULL,
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_usuario (usuario),
    INDEX idx_auditoria_accion (accion)
);

CREATE TABLE aviso_sala (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sala_id INT NOT NULL,
    estado VARCHAR(40) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    creado_por_empleado_id INT NULL,
    creado_por_usuario VARCHAR(100) NOT NULL,
    destinatario_empleado_id INT NOT NULL,
    destinatario_usuario VARCHAR(100) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_aviso_sala
        FOREIGN KEY (sala_id) REFERENCES sala(id),
    CONSTRAINT fk_aviso_creador
        FOREIGN KEY (creado_por_empleado_id) REFERENCES empleado(id) ON DELETE SET NULL,
    CONSTRAINT fk_aviso_destinatario
        FOREIGN KEY (destinatario_empleado_id) REFERENCES empleado(id),
    INDEX idx_aviso_destinatario_leido (destinatario_empleado_id, leido),
    INDEX idx_aviso_fecha (fecha_hora)
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
(1, 'El Padrino', 175, 'El anciano patriarca de una dinastía del crimen organizado en la ciudad de Nueva York de la posguerra transfiere el control de su imperio clandestino a su reacio hijo menor.', '/images/peliculas/el_padrino.jpg', 2),
(2, 'La naranja mecánica', 136, 'En el futuro, un sádico líder de una pandilla es encarcelado y se ofrece como voluntario para un experimento de aversión a la conducta, pero el proceso no sale como estaba planeado.', '/images/peliculas/naranja_mecanica.jpg', 5),
(3, 'El bueno, el malo y el feo', 161, 'Durante la Guerra de Secesión, tres cazarrecompensas se lanzan a la búsqueda de un tesoro que ninguno de ellos puede encontrar sin la ayuda de los otros dos.', '/images/peliculas/the_good_the_bad_the_ugly.jpg', 8),
(4, 'Matrix', 136, 'Un hacker informático descubre la impactante verdad: la vida tal como la conoce es una simulación virtual creada por una inteligencia artificial, y se une a la rebelión para liberar a la humanidad.', '/images/peliculas/the_matrix.jpg', 5),
(5, 'Pulp Fiction', 154, 'Las vidas de dos sicarios de la mafia, un boxeador, la esposa de un gángster y un par de bandidos se entrelazan en cuatro historias de violencia y redención.', '/images/peliculas/pulp_fiction.jpg', 7),
(6, 'Jurassic Park', 127, 'Un paleontólogo pragmático que visita un parque temático secreto recibe el encargo de proteger a dos niños después de que un sabotaje provoca un corte de energía y los dinosaurios clonados escapan de sus recintos.', '/images/peliculas/jurasic_park.jpg', 8),
(7, 'The Truman Show', 103, 'Un vendedor de seguros descubre poco a poco que toda su vida es en realidad un programa de telerrealidad y que todos los que lo rodean son actores.', '/images/peliculas/the_truman_show.jpg', 4),
(8, 'Shrek Tercero', 93, 'Cuando su suegro, el rey Harold, enferma, Shrek es designado como el heredero al trono de Muy Muy Lejano, pero él prefiere buscar al primo de Fiona, Artie, para que reine en su lugar.', '/images/peliculas/sherk3.jpg', 6),
(9, 'Volver al futuro', 116, 'Un adolescente de 17 años es enviado accidentalmente treinta años al pasado en un DeLorean que viaja en el tiempo, inventado por su excéntrico amigo científico.', '/images/peliculas/back_to_the_future.jpg', 5),
(10, 'La cosa', 109, 'Un equipo de investigación estadounidense en la Antártida es perseguido por un organismo extraterrestre que cambia de forma y asume la apariencia exacta de sus víctimas.', '/images/peliculas/The_thing.jpg', 3),
(11, 'Rocky IV', 91, 'Después de que el letal boxeador soviético Ivan Drago mata a su amigo Apollo Creed en una pelea de exhibición, Rocky Balboa decide viajar a la Unión Soviética para enfrentarse a él en el ring y vengar su muerte.', '/images/peliculas/rocky4.jpg', 1),
(12, 'El club de la pelea', 139, 'Un oficinista insomne deprimido y un fabricante de jabón desentendido de la sociedad forman un club de lucha clandestino que evoluciona hacia algo mucho más grande y peligroso.', '/images/peliculas/club_fight.jpg', 2),
(13, 'Gladiador', 155, 'Un ex general romano es traicionado y se propone vengarse del emperador corrupto que asesinó a su familia y lo envió a la esclavitud como gladiador.', '/images/peliculas/gladiator.jpg', 1),
(14, 'El caballero oscuro', 152, 'Cuando la amenaza conocida como el Guasón causa estragos y caos en Ciudad Gótica, Batman debe aceptar una de las mayores pruebas psicológicas y físicas de su capacidad para luchar contra la injusticia.', '/images/peliculas/el_caballero_oscruo.jpg', 1),
(15, 'El pianista', 150, 'Un músico judío polaco lucha por sobrevivir a la destrucción del gueto de Varsovia durante la ocupación nazi en la Segunda Guerra Mundial.', '/images/peliculas/the_pianist.jpg', 2),
(16, 'Gran Torino', 116, 'Un viudo y amargado veterano de la Guerra de Corea se propone reformar a su vecino, un adolescente hmong que, presionado por una pandilla, intentó robar su preciado auto Ford Gran Torino de 1972.', '/images/peliculas/gran_torino.jpg', 2),
(17, 'Piratas del Caribe: La maldición de la Perla Negra', 143, 'El herrero Will Turner se une al excéntrico pirata Capitán Jack Sparrow para salvar a su amada, la hija del gobernador, de los antiguos aliados piratas de Jack, que ahora son no muertos.', '/images/peliculas/piratas_del_caribe.jpg', 8),
(18, 'El secreto de sus ojos', 129, 'Un oficial de un juzgado de instrucción retirado escribe una novela con la esperanza de encontrar cierre para un caso de violación y homicidio no resuelto del pasado y para su amor no correspondido por su superior.', '/images/peliculas/el_secreto_de_sus_ojos.jpg', 7);

INSERT INTO sala (id, nombre, capacidad, estado) VALUES
(1, 'Sala Premium', 60, 'DISPONIBLE'),
(2, 'Sala Central', 96, 'DISPONIBLE'),
(3, 'Sala 3D', 70, 'DISPONIBLE'),
(4, 'Sala Familiar', 40, 'DISPONIBLE');

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
INSERT INTO seed_dias (n) VALUES
(0), (1), (2), (3), (4), (5), (6),
(7), (8), (9), (10), (11), (12), (13);

CREATE TEMPORARY TABLE seed_cartelera (
    dia_modulo INT,
    pelicula_id INT,
    orden_pelicula INT,
    precio_base DOUBLE
);

INSERT INTO seed_cartelera (dia_modulo, pelicula_id, orden_pelicula, precio_base) VALUES
(0, 1, 1, 8200), (0, 2, 2, 7800), (0, 3, 3, 8400),
(0, 4, 4, 8100), (0, 5, 5, 8300), (0, 6, 6, 8000),
(1, 4, 1, 8100), (1, 5, 2, 8300), (1, 6, 3, 8000),
(1, 7, 4, 7400), (1, 8, 5, 7200), (1, 9, 6, 7600),
(2, 7, 1, 7400), (2, 8, 2, 7200), (2, 9, 3, 7600),
(2, 10, 4, 7900), (2, 11, 5, 7300), (2, 12, 6, 8100),
(3, 10, 1, 7900), (3, 11, 2, 7300), (3, 12, 3, 8100),
(3, 13, 4, 8500), (3, 14, 5, 8900), (3, 15, 6, 8000),
(4, 13, 1, 8500), (4, 14, 2, 8900), (4, 15, 3, 8000),
(4, 16, 4, 7500), (4, 17, 5, 8400), (4, 18, 6, 8200),
(5, 16, 1, 7500), (5, 17, 2, 8400), (5, 18, 3, 8200),
(5, 1, 4, 8200), (5, 2, 5, 7800), (5, 3, 6, 8400);

CREATE TEMPORARY TABLE seed_variantes (
    orden_pelicula INT,
    sala_id INT,
    horario TIME,
    formato VARCHAR(30),
    idioma VARCHAR(30),
    recargo DOUBLE
);

INSERT INTO seed_variantes (orden_pelicula, sala_id, horario, formato, idioma, recargo) VALUES
(1, 1, '13:00:00', 'DOS_D', 'ESPANIOL', 0),
(1, 2, '19:45:00', 'DOS_D', 'SUBTITULADA', 300),
(1, 3, '16:45:00', 'TRES_D', 'ESPANIOL', 1200),
(1, 4, '13:45:00', 'TRES_D', 'SUBTITULADA', 1500),
(2, 1, '16:15:00', 'DOS_D', 'ESPANIOL', 0),
(2, 2, '13:15:00', 'DOS_D', 'SUBTITULADA', 300),
(2, 3, '20:00:00', 'TRES_D', 'ESPANIOL', 1200),
(2, 4, '17:00:00', 'TRES_D', 'SUBTITULADA', 1500),
(3, 1, '19:30:00', 'DOS_D', 'ESPANIOL', 0),
(3, 2, '16:30:00', 'DOS_D', 'SUBTITULADA', 300),
(3, 3, '13:30:00', 'TRES_D', 'ESPANIOL', 1200),
(3, 4, '20:15:00', 'TRES_D', 'SUBTITULADA', 1500),
(4, 1, '23:20:00', 'DOS_D', 'ESPANIOL', 0),
(4, 2, '23:20:00', 'DOS_D', 'SUBTITULADA', 300),
(5, 3, '23:20:00', 'TRES_D', 'ESPANIOL', 1200),
(6, 4, '23:20:00', 'TRES_D', 'SUBTITULADA', 1500);

INSERT INTO funcion (fecha, horario, pelicula_id, sala_id, formato, idioma, precio_entrada)
SELECT DATE_ADD(CURDATE(), INTERVAL d.n DAY),
       v.horario,
       c.pelicula_id,
       v.sala_id,
       v.formato,
       v.idioma,
       c.precio_base + v.recargo
FROM seed_dias d
JOIN seed_cartelera c ON c.dia_modulo = MOD(d.n, 6)
JOIN seed_variantes v ON v.orden_pelicula = c.orden_pelicula
ORDER BY d.n, v.sala_id, v.horario;

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

UPDATE producto_confiteria
SET imagen_url = CASE tipo
    WHEN 'POCHOCLOS' THEN '/images/consumibles/pochoclos.png'
    WHEN 'BEBIDA' THEN '/images/consumibles/bebida.png'
    WHEN 'DULCE' THEN '/images/consumibles/dulce.png'
    WHEN 'COMBO' THEN '/images/consumibles/combo.png'
    ELSE NULL
END;

-- Cuenta y medio de pago técnicos para probar tickets desde las interfaces.
INSERT INTO espectador (
    id, nombre, apellido, email, contrasenia, email_verificado,
    token_verificacion_email, token_recuperacion_contrasenia
) VALUES (
    1, 'Cliente', 'Demostración', 'cliente.demo@cine.test',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    TRUE, NULL, NULL
);

INSERT INTO metodo_pago (
    id, numero, fecha_vencimiento, nombre, apellido, cvv, activa, espectador_id
) VALUES (
    1, '4500123412345678', '2028-12', 'Cliente', 'Demostración', '123', TRUE, 1
);

INSERT INTO ticket (
    id, espectador_id, metodo_pago_id, metodo_pago_resumen, codigo_qr
) VALUES
(1, 1, 1, 'Tarjeta terminada en 5678', 'TCK-DEMO-PENDIENTE'),
(2, 1, 1, 'Tarjeta terminada en 5678', 'TCK-DEMO-CONSUMOS'),
(3, 1, 1, 'Tarjeta terminada en 5678', 'TCK-DEMO-PROCESADO'),
(4, 1, 1, 'Tarjeta terminada en 5678', 'TCK-DEMO-REEMBOLSADO');

-- Ticket 1: dos entradas pagadas y consumos pendientes.
INSERT INTO entrada (precio, espectador_id, ticket_id, funcion_id, butaca_id, horario, estado)
SELECT f.precio_entrada, 1, 1, f.id, b.id, TIMESTAMP(f.fecha, f.horario), 'PAGADA'
FROM funcion f
JOIN butaca b ON b.sala_id = f.sala_id
WHERE f.id = 17 AND b.fila = 'A' AND b.numero IN (1, 2);

-- Ticket 2: entrada validada y consumo pendiente de entrega.
INSERT INTO entrada (precio, espectador_id, ticket_id, funcion_id, butaca_id, horario, estado)
SELECT f.precio_entrada, 1, 2, f.id, b.id, TIMESTAMP(f.fecha, f.horario), 'ESCANEADA'
FROM funcion f
JOIN butaca b ON b.sala_id = f.sala_id
WHERE f.id = 18 AND b.fila = 'A' AND b.numero = 3;

-- Ticket 3: entradas y consumos ya procesados.
INSERT INTO entrada (precio, espectador_id, ticket_id, funcion_id, butaca_id, horario, estado)
SELECT f.precio_entrada, 1, 3, f.id, b.id, TIMESTAMP(f.fecha, f.horario), 'ESCANEADA'
FROM funcion f
JOIN butaca b ON b.sala_id = f.sala_id
WHERE f.id = 21 AND b.fila = 'A' AND b.numero IN (1, 2);

-- Ticket 4: compra reembolsada.
INSERT INTO entrada (precio, espectador_id, ticket_id, funcion_id, butaca_id, horario, estado)
SELECT f.precio_entrada, 1, 4, f.id, b.id, TIMESTAMP(f.fecha, f.horario), 'REEMBOLSADA'
FROM funcion f
JOIN butaca b ON b.sala_id = f.sala_id
WHERE f.id = 22 AND b.fila = 'A' AND b.numero = 3;

INSERT INTO item_consumo (producto_id, ticket_id, cantidad, estado) VALUES
(15, 1, 1, 'PENDIENTE'),
(8, 1, 2, 'PENDIENTE'),
(3, 2, 1, 'PENDIENTE'),
(16, 3, 1, 'ENTREGADO'),
(11, 4, 2, 'CANCELADO');

DROP TEMPORARY TABLE IF EXISTS seed_funciones;
DROP TEMPORARY TABLE IF EXISTS seed_dias;
DROP TEMPORARY TABLE IF EXISTS seed_numeros;
DROP TEMPORARY TABLE IF EXISTS seed_filas;
