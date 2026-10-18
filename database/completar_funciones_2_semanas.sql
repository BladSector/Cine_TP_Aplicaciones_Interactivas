USE tp_cine_api;

-- Completa los próximos 14 días sin borrar funciones, compras ni usuarios.
CREATE TEMPORARY TABLE completar_dias (n INT PRIMARY KEY);
INSERT INTO completar_dias (n) VALUES
(0), (1), (2), (3), (4), (5), (6),
(7), (8), (9), (10), (11), (12), (13);

CREATE TEMPORARY TABLE completar_cartelera (
    dia_modulo INT,
    pelicula_id INT,
    orden_pelicula INT,
    precio_base DOUBLE
);

INSERT INTO completar_cartelera (dia_modulo, pelicula_id, orden_pelicula, precio_base) VALUES
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

CREATE TEMPORARY TABLE completar_variantes (
    orden_pelicula INT,
    sala_id INT,
    horario TIME,
    formato VARCHAR(30),
    idioma VARCHAR(30),
    recargo DOUBLE
);

INSERT INTO completar_variantes (orden_pelicula, sala_id, horario, formato, idioma, recargo) VALUES
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
FROM completar_dias d
-- La base activa comenzó su rotación un día antes de este complemento.
JOIN completar_cartelera c ON c.dia_modulo = MOD(d.n + 1, 6)
JOIN completar_variantes v ON v.orden_pelicula = c.orden_pelicula
JOIN pelicula nueva_pelicula ON nueva_pelicula.id = c.pelicula_id
WHERE NOT EXISTS (
    SELECT 1
    FROM funcion existente
    JOIN pelicula pelicula_existente ON pelicula_existente.id = existente.pelicula_id
    WHERE existente.fecha = DATE_ADD(CURDATE(), INTERVAL d.n DAY)
      AND existente.sala_id = v.sala_id
      AND TIMESTAMP(existente.fecha, v.horario)
            < DATE_ADD(TIMESTAMP(existente.fecha, existente.horario), INTERVAL pelicula_existente.duracion MINUTE)
      AND TIMESTAMP(existente.fecha, existente.horario)
            < DATE_ADD(TIMESTAMP(existente.fecha, v.horario), INTERVAL nueva_pelicula.duracion MINUTE)
)
ORDER BY d.n, v.sala_id, v.horario;

DROP TEMPORARY TABLE completar_variantes;
DROP TEMPORARY TABLE completar_cartelera;
DROP TEMPORARY TABLE completar_dias;

SELECT fecha, COUNT(*) AS funciones
FROM funcion
WHERE fecha BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 13 DAY)
GROUP BY fecha
ORDER BY fecha;
