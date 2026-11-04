USE tp_cine_api;

-- Ejecutar completo en MySQL 8 / Workbench, antes de operar ventas o editar horarios.
-- Antes de las 12:30 programa desde hoy; mas tarde comienza manana.
-- No ejecutar seed_demo.sql para renovar solamente la cartelera.
SET @fecha_inicio_programacion = IF(
    CURTIME() < '12:30:00', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY)
);

DELIMITER $$
DROP PROCEDURE IF EXISTS completar_cartelera_semana$$
CREATE PROCEDURE completar_cartelera_semana(IN p_fecha_inicio DATE)
BEGIN
    DECLARE v_fecha_fin DATE;
    DECLARE v_peliculas INT;
    DECLARE v_salas INT;
    DECLARE v_intervalo_minutos INT;
    DECLARE v_incumplimientos INT;
    DECLARE v_safe_updates INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET SQL_SAFE_UPDATES = v_safe_updates;
        DROP TEMPORARY TABLE IF EXISTS agenda_plan, agenda_peliculas,
            agenda_salas, agenda_dias, agenda_turnos;
        RESIGNAL;
    END;

    SET v_safe_updates = @@SQL_SAFE_UPDATES;
    SET v_fecha_fin = DATE_ADD(p_fecha_inicio, INTERVAL 6 DAY);
    IF p_fecha_inicio IS NULL OR p_fecha_inicio < CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La semana debe comenzar hoy o en una fecha futura.';
    END IF;
    IF p_fecha_inicio = CURDATE() AND CURTIME() >= '13:00:00' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La jornada de hoy ya comenzo. Programar desde manana.';
    END IF;

    START TRANSACTION;

    CREATE TEMPORARY TABLE agenda_peliculas (
        orden INT PRIMARY KEY,
        id INT NOT NULL,
        duracion INT NOT NULL
    );
    -- Intercala generos; no depende de que los IDs sean consecutivos.
    INSERT INTO agenda_peliculas (orden, id, duracion)
    SELECT ROW_NUMBER() OVER (ORDER BY p.orden_genero, p.categoria_id, p.id), p.id, p.duracion
    FROM (
        SELECT id, duracion, categoria_id,
               ROW_NUMBER() OVER (PARTITION BY categoria_id ORDER BY id) AS orden_genero
        FROM pelicula
        WHERE duracion BETWEEN 1 AND 240
    ) p;

    SELECT COUNT(*), CEILING((MAX(duracion) + 20) / 15) * 15
    INTO v_peliculas, v_intervalo_minutos
    FROM agenda_peliculas;
    IF v_peliculas < 6 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se necesitan al menos 6 peliculas con duracion entre 1 y 240 minutos.';
    END IF;

    CREATE TEMPORARY TABLE agenda_salas (orden INT PRIMARY KEY, id INT NOT NULL);
    INSERT INTO agenda_salas (orden, id)
    SELECT ROW_NUMBER() OVER (ORDER BY s.id), s.id
    FROM sala s
    WHERE s.estado = 'DISPONIBLE'
      AND EXISTS (SELECT 1 FROM butaca b WHERE b.sala_id = s.id AND b.estado = 'DISPONIBLE')
    ORDER BY s.id
    LIMIT 4;
    SELECT COUNT(*) INTO v_salas FROM agenda_salas;
    IF v_salas < 4 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se necesitan 4 salas disponibles con butacas para programar la semana.';
    END IF;

    CREATE TEMPORARY TABLE agenda_dias (n INT PRIMARY KEY);
    INSERT INTO agenda_dias (n) VALUES (0), (1), (2), (3), (4), (5), (6);

    CREATE TEMPORARY TABLE agenda_turnos (
        sala_orden INT,
        turno INT,
        pelicula_orden INT,
        PRIMARY KEY (sala_orden, turno)
    );
    -- Los primeros tres turnos dan dos funciones a cada una de las seis peliculas.
    -- El cuarto agrega otra opcion cuando entra antes de las 23:30.
    INSERT INTO agenda_turnos (sala_orden, turno, pelicula_orden) VALUES
    (1, 1, 1), (1, 2, 3), (1, 3, 5), (1, 4, 2),
    (2, 1, 2), (2, 2, 4), (2, 3, 6), (2, 4, 1),
    (3, 1, 5), (3, 2, 1), (3, 3, 3), (3, 4, 6),
    (4, 1, 6), (4, 2, 2), (4, 3, 4), (4, 4, 5);

    CREATE TEMPORARY TABLE agenda_plan (
        fecha DATE,
        horario TIME,
        pelicula_id INT,
        sala_id INT,
        formato VARCHAR(30),
        idioma VARCHAR(30),
        precio DOUBLE,
        inicio DATETIME,
        fin DATETIME,
        PRIMARY KEY (fecha, sala_id, horario)
    );
    INSERT INTO agenda_plan (fecha, horario, pelicula_id, sala_id, formato, idioma, precio, inicio, fin)
    SELECT p.fecha, TIME(p.inicio), p.pelicula_id, p.sala_id, p.formato, p.idioma,
           p.precio, p.inicio, DATE_ADD(p.inicio, INTERVAL p.duracion MINUTE)
    FROM (
        SELECT DATE_ADD(p_fecha_inicio, INTERVAL d.n DAY) AS fecha,
               DATE_ADD(
                   TIMESTAMP(DATE_ADD(p_fecha_inicio, INTERVAL d.n DAY), '13:00:00'),
                   INTERVAL ((s.orden - 1) * 15 + (t.turno - 1) * v_intervalo_minutos) MINUTE
               ) AS inicio,
               pelicula.id AS pelicula_id, pelicula.duracion, s.id AS sala_id,
               IF(s.orden = 3, 'TRES_D', 'DOS_D') AS formato,
               IF(s.orden IN (1, 4), 'SUBTITULADA', 'ESPANIOL') AS idioma,
               8500 + IF(s.orden = 1, 500, 0) + IF(s.orden = 3, 1200, 0)
                   + IF(DAYOFWEEK(DATE_ADD(p_fecha_inicio, INTERVAL d.n DAY)) IN (1, 7), 500, 0) AS precio
        FROM agenda_dias d
        CROSS JOIN agenda_turnos t
        JOIN agenda_salas s ON s.orden = t.sala_orden
        JOIN agenda_peliculas pelicula ON pelicula.orden = 1 + MOD(
            TO_DAYS(DATE_ADD(p_fecha_inicio, INTERVAL d.n DAY)) * 6 + t.pelicula_orden - 1,
            v_peliculas
        )
    ) p
    WHERE p.inicio <= TIMESTAMP(p.fecha, '23:30:00');

    -- Solo reemplaza horarios libres que no pertenecen al nuevo plan.
    -- Una entrada (incluso reembolsada) o reserva conserva la funcion original.
    SET SQL_SAFE_UPDATES = 0;
    DELETE f FROM funcion f
    LEFT JOIN agenda_plan p ON p.fecha = f.fecha AND p.horario = f.horario
        AND p.pelicula_id = f.pelicula_id AND p.sala_id = f.sala_id
        AND p.formato = f.formato AND p.idioma = f.idioma
    WHERE f.fecha BETWEEN p_fecha_inicio AND v_fecha_fin
      AND p.pelicula_id IS NULL
      AND NOT EXISTS (SELECT 1 FROM entrada e WHERE e.funcion_id = f.id)
      AND NOT EXISTS (SELECT 1 FROM reserva_butaca r WHERE r.funcion_id = f.id);
    SET SQL_SAFE_UPDATES = v_safe_updates;

    -- El intervalo usa la duracion maxima mas 20 minutos, por lo que los nuevos
    -- horarios tampoco se superponen entre si. Se revisan tambien otros dias.
    INSERT INTO funcion (fecha, horario, pelicula_id, sala_id, formato, idioma, precio_entrada)
    SELECT p.fecha, p.horario, p.pelicula_id, p.sala_id, p.formato, p.idioma, p.precio
    FROM agenda_plan p
    WHERE NOT EXISTS (
        SELECT 1 FROM funcion f
        JOIN pelicula pelicula_existente ON pelicula_existente.id = f.pelicula_id
        WHERE f.sala_id = p.sala_id
          AND p.inicio < DATE_ADD(TIMESTAMP(f.fecha, f.horario), INTERVAL (pelicula_existente.duracion + 20) MINUTE)
          AND TIMESTAMP(f.fecha, f.horario) < DATE_ADD(p.fin, INTERVAL 20 MINUTE)
    )
    ORDER BY p.fecha, p.sala_id, p.horario;

    SELECT COUNT(*) INTO v_incumplimientos
    FROM (
        SELECT p.fecha, p.pelicula_id
        FROM agenda_plan p
        LEFT JOIN funcion f ON f.fecha = p.fecha AND f.pelicula_id = p.pelicula_id
        GROUP BY p.fecha, p.pelicula_id
        HAVING COUNT(DISTINCT f.id) < 2
    ) faltantes;
    IF v_incumplimientos > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No entran dos funciones por pelicula por horarios protegidos. No se aplicaron cambios.';
    END IF;

    SELECT COUNT(*) INTO v_incumplimientos
    FROM funcion a
    JOIN pelicula pa ON pa.id = a.pelicula_id
    JOIN funcion b ON b.sala_id = a.sala_id AND b.id > a.id
    JOIN pelicula pb ON pb.id = b.pelicula_id
    WHERE (a.fecha BETWEEN p_fecha_inicio AND v_fecha_fin OR b.fecha BETWEEN p_fecha_inicio AND v_fecha_fin)
      AND TIMESTAMP(a.fecha, a.horario) < DATE_ADD(TIMESTAMP(b.fecha, b.horario), INTERVAL pb.duracion MINUTE)
      AND TIMESTAMP(b.fecha, b.horario) < DATE_ADD(TIMESTAMP(a.fecha, a.horario), INTERVAL pa.duracion MINUTE);
    IF v_incumplimientos > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hay funciones superpuestas en la semana. No se aplicaron cambios.';
    END IF;

    COMMIT;

    SELECT fecha, COUNT(DISTINCT pelicula_id) AS peliculas, COUNT(*) AS funciones,
           COUNT(DISTINCT sala_id) AS salas, MIN(horario) AS primera, MAX(horario) AS ultima
    FROM funcion
    WHERE fecha BETWEEN p_fecha_inicio AND v_fecha_fin
    GROUP BY fecha ORDER BY fecha;

    SELECT f.fecha, p.titulo AS pelicula, COUNT(*) AS funciones
    FROM funcion f JOIN pelicula p ON p.id = f.pelicula_id
    WHERE f.fecha BETWEEN p_fecha_inicio AND v_fecha_fin
    GROUP BY f.fecha, p.id, p.titulo ORDER BY f.fecha, p.titulo;

    DROP TEMPORARY TABLE agenda_plan, agenda_peliculas, agenda_salas, agenda_dias, agenda_turnos;
END$$

CALL completar_cartelera_semana(@fecha_inicio_programacion)$$
DROP PROCEDURE completar_cartelera_semana$$
DELIMITER ;
