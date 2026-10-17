-- Actualiza una instalacion existente de tp_cine_api sin borrar sus datos.
-- Ejecutar el archivo completo desde MySQL Workbench con MySQL 8.

CREATE DATABASE IF NOT EXISTS tp_cine_api
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE tp_cine_api;

-- Tablas incorporadas para usuarios internos, auditoria y avisos de sala.
CREATE TABLE IF NOT EXISTS empleado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    contrasenia VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'EMPLEADO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_empleado_usuario UNIQUE (usuario)
);

CREATE TABLE IF NOT EXISTS auditoria (
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
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_usuario (usuario),
    INDEX idx_auditoria_accion (accion)
);

CREATE TABLE IF NOT EXISTS aviso_sala (
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
    INDEX idx_aviso_destinatario_leido (destinatario_empleado_id, leido),
    INDEX idx_aviso_fecha (fecha_hora)
);

-- Utilidades para que la migracion pueda ejecutarse mas de una vez.
DROP PROCEDURE IF EXISTS agregar_columna_si_falta;
DROP PROCEDURE IF EXISTS quitar_columna_si_existe;
DROP PROCEDURE IF EXISTS agregar_indice_si_falta;
DROP PROCEDURE IF EXISTS agregar_fk_si_falta;

DELIMITER $$

CREATE PROCEDURE agregar_columna_si_falta(
    IN tabla_nombre VARCHAR(64),
    IN columna_nombre VARCHAR(64),
    IN definicion TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = tabla_nombre
          AND column_name = columna_nombre
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tabla_nombre, '` ADD COLUMN ', definicion);
        PREPARE sentencia FROM @sql;
        EXECUTE sentencia;
        DEALLOCATE PREPARE sentencia;
    END IF;
END$$

CREATE PROCEDURE quitar_columna_si_existe(
    IN tabla_nombre VARCHAR(64),
    IN columna_nombre VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = tabla_nombre
          AND column_name = columna_nombre
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tabla_nombre, '` DROP COLUMN `', columna_nombre, '`');
        PREPARE sentencia FROM @sql;
        EXECUTE sentencia;
        DEALLOCATE PREPARE sentencia;
    END IF;
END$$

CREATE PROCEDURE agregar_indice_si_falta(
    IN tabla_nombre VARCHAR(64),
    IN columna_nombre VARCHAR(64),
    IN indice_nombre VARCHAR(64),
    IN definicion TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = tabla_nombre
          AND column_name = columna_nombre
          AND non_unique = 0
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tabla_nombre, '` ADD ', definicion);
        PREPARE sentencia FROM @sql;
        EXECUTE sentencia;
        DEALLOCATE PREPARE sentencia;
    END IF;
END$$

CREATE PROCEDURE agregar_fk_si_falta(
    IN tabla_nombre VARCHAR(64),
    IN columna_nombre VARCHAR(64),
    IN restriccion_nombre VARCHAR(64),
    IN definicion TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        JOIN information_schema.key_column_usage
          ON key_column_usage.constraint_schema = table_constraints.constraint_schema
         AND key_column_usage.table_name = table_constraints.table_name
         AND key_column_usage.constraint_name = table_constraints.constraint_name
        WHERE table_constraints.constraint_schema = DATABASE()
          AND table_constraints.table_name = tabla_nombre
          AND table_constraints.constraint_type = 'FOREIGN KEY'
          AND key_column_usage.column_name = columna_nombre
          AND key_column_usage.referenced_table_name IS NOT NULL
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tabla_nombre, '` ADD CONSTRAINT `',
                          restriccion_nombre, '` ', definicion);
        PREPARE sentencia FROM @sql;
        EXECUTE sentencia;
        DEALLOCATE PREPARE sentencia;
    END IF;
END$$

DELIMITER ;

-- Columnas agregadas durante la evolucion del modelo.
CALL agregar_columna_si_falta('pelicula', 'descripcion', '`descripcion` LONGTEXT NULL');
CALL agregar_columna_si_falta('pelicula', 'portada_url', '`portada_url` LONGTEXT NULL');

CALL agregar_columna_si_falta('sala', 'estado', "`estado` VARCHAR(40) NOT NULL DEFAULT 'DISPONIBLE'");
CALL agregar_columna_si_falta('sala', 'detalle_estado', '`detalle_estado` VARCHAR(500) NULL');

CALL agregar_columna_si_falta('butaca', 'estado', "`estado` VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'");
CALL agregar_columna_si_falta('butaca', 'bloqueo_hasta', '`bloqueo_hasta` DATETIME NULL');

CALL agregar_columna_si_falta('funcion', 'formato', "`formato` VARCHAR(30) NOT NULL DEFAULT 'DOS_D'");
CALL agregar_columna_si_falta('funcion', 'idioma', "`idioma` VARCHAR(30) NOT NULL DEFAULT 'ESPANIOL'");
CALL agregar_columna_si_falta('funcion', 'precio_entrada', '`precio_entrada` DOUBLE NOT NULL DEFAULT 1');

CALL agregar_columna_si_falta('espectador', 'email_verificado', '`email_verificado` BOOLEAN NOT NULL DEFAULT FALSE');
CALL agregar_columna_si_falta('espectador', 'token_verificacion_email', '`token_verificacion_email` VARCHAR(255) NULL');
CALL agregar_columna_si_falta('espectador', 'token_recuperacion_contrasenia', '`token_recuperacion_contrasenia` VARCHAR(255) NULL');

CALL agregar_columna_si_falta('metodo_pago', 'activa', '`activa` BOOLEAN NOT NULL DEFAULT TRUE');
CALL agregar_columna_si_falta('metodo_pago', 'espectador_id', '`espectador_id` INT NULL');

CALL agregar_columna_si_falta('ticket', 'metodo_pago_id', '`metodo_pago_id` INT NULL');
CALL agregar_columna_si_falta('ticket', 'metodo_pago_resumen', '`metodo_pago_resumen` VARCHAR(255) NULL');
CALL agregar_columna_si_falta('ticket', 'codigo_qr', '`codigo_qr` VARCHAR(255) NULL');

CALL agregar_columna_si_falta('entrada', 'ticket_id', '`ticket_id` INT NULL');
CALL agregar_columna_si_falta('entrada', 'estado', "`estado` VARCHAR(40) NOT NULL DEFAULT 'GENERADA'");

CALL agregar_columna_si_falta('item_consumo', 'ticket_id', '`ticket_id` INT NULL');
CALL agregar_columna_si_falta('item_consumo', 'estado', "`estado` VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE'");

-- Relaciones actuales entre las entidades.
CALL agregar_fk_si_falta(
    'metodo_pago', 'espectador_id', 'fk_metodo_pago_espectador',
    'FOREIGN KEY (`espectador_id`) REFERENCES `espectador` (`id`)'
);
CALL agregar_fk_si_falta(
    'ticket', 'metodo_pago_id', 'fk_ticket_metodo_pago',
    'FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`id`)'
);
CALL agregar_fk_si_falta(
    'entrada', 'ticket_id', 'fk_entrada_ticket',
    'FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)'
);
CALL agregar_fk_si_falta(
    'item_consumo', 'ticket_id', 'fk_item_consumo_ticket',
    'FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)'
);
CALL agregar_fk_si_falta(
    'aviso_sala', 'sala_id', 'fk_aviso_sala',
    'FOREIGN KEY (`sala_id`) REFERENCES `sala` (`id`)'
);

-- El QR identifica de manera unica a cada ticket.
CALL agregar_indice_si_falta(
    'ticket', 'codigo_qr', 'uq_ticket_codigo_qr',
    'UNIQUE INDEX `uq_ticket_codigo_qr` (`codigo_qr`)'
);

-- Columnas antiguas que ya no existen en las entidades Java actuales.
-- El estado del ticket ahora se calcula a partir de entradas y consumos.
CALL quitar_columna_si_existe('ticket', 'estado');
CALL quitar_columna_si_existe('metodo_pago', 'nombre_titular');
CALL quitar_columna_si_existe('producto_confiteria', 'imagen_url');

DROP PROCEDURE agregar_columna_si_falta;
DROP PROCEDURE quitar_columna_si_existe;
DROP PROCEDURE agregar_indice_si_falta;
DROP PROCEDURE agregar_fk_si_falta;

SELECT 'Esquema tp_cine_api actualizado correctamente.' AS resultado;
