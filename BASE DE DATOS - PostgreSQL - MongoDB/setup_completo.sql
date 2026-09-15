-- =============================================================================
-- UNITALENT: SCRIPT COMPLETO DE CONFIGURACIÓN POSTGRESQL
-- Incluye: Tablas, Funciones, Triggers y Datos de Prueba Iniciales
-- Contraseña de todos los usuarios de prueba: 123456
-- =============================================================================

-- 1. LIMPIEZA DE TABLAS PREVIAS
DROP TABLE IF EXISTS postulaciones CASCADE;
DROP TABLE IF EXISTS ofertas CASCADE;
DROP TABLE IF EXISTS empresas CASCADE;
DROP TABLE IF EXISTS estudiantes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- 2. CREACIÓN DE TABLAS
CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,   -- hash SHA-256 + salt Base64
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ESTUDIANTE','EMPRESA','ADMIN')),
    fecha_registro TIMESTAMP DEFAULT NOW()
);

CREATE TABLE estudiantes (
    id_estudiante SERIAL PRIMARY KEY,
    id_usuario INT UNIQUE NOT NULL,
    carrera VARCHAR(100),
    ciclo INT,
    cv VARCHAR(255),
    telefono VARCHAR(20),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE empresas (
    id_empresa SERIAL PRIMARY KEY,
    id_usuario INT UNIQUE NOT NULL,
    nombre_empresa VARCHAR(100) NOT NULL,
    rubro VARCHAR(100),
    descripcion TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE ofertas (
    id_oferta SERIAL PRIMARY KEY,
    id_empresa INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    carrera VARCHAR(100),
    modalidad VARCHAR(50) CHECK (modalidad IN ('Presencial','Remoto','Hibrido')),
    estado VARCHAR(20) DEFAULT 'Activa' CHECK (estado IN ('Activa','Cerrada')),
    fecha_publicacion DATE DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa) ON DELETE CASCADE
);

CREATE TABLE postulaciones (
    id_postulacion SERIAL PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_oferta INT NOT NULL,
    fecha_postulacion DATE DEFAULT CURRENT_DATE,
    estado VARCHAR(20) DEFAULT 'Enviada' CHECK (estado IN ('Enviada','En Proceso','Aceptada','Rechazada')),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_oferta) REFERENCES ofertas(id_oferta) ON DELETE CASCADE,
    UNIQUE (id_estudiante, id_oferta)
);

-- 3. FUNCIONES Y PROCEDIMIENTOS ALMACENADOS (PL/pgSQL)
CREATE OR REPLACE FUNCTION fn_total_postulaciones(p_id_oferta INT)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_total INT;
BEGIN
    SELECT COUNT(*) INTO v_total
    FROM postulaciones
    WHERE id_oferta = p_id_oferta;

    RETURN v_total;
END;
$$;

CREATE OR REPLACE FUNCTION fn_nivel_empleabilidad(p_id_estudiante INT)
RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE
    v_aceptadas INT;
BEGIN
    SELECT COUNT(*) INTO v_aceptadas
    FROM postulaciones
    WHERE id_estudiante = p_id_estudiante AND estado = 'Aceptada';

    IF v_aceptadas > 0 THEN
        RETURN 'Empleable';
    ELSE
        RETURN 'En búsqueda';
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_registrar_postulacion(
    p_id_estudiante INT,
    p_id_oferta INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_oferta VARCHAR;
    v_existe INT;
BEGIN
    SELECT estado INTO v_estado_oferta FROM ofertas WHERE id_oferta = p_id_oferta;

    IF v_estado_oferta IS NULL THEN
        RAISE EXCEPTION 'La oferta % no existe', p_id_oferta;
    END IF;

    IF v_estado_oferta <> 'Activa' THEN
        RAISE EXCEPTION 'No se puede postular: la oferta % no está activa', p_id_oferta;
    END IF;

    SELECT COUNT(*) INTO v_existe
    FROM postulaciones
    WHERE id_estudiante = p_id_estudiante AND id_oferta = p_id_oferta;

    IF v_existe > 0 THEN
        RAISE EXCEPTION 'El estudiante % ya postuló a la oferta %', p_id_estudiante, p_id_oferta;
    END IF;

    INSERT INTO postulaciones (id_estudiante, id_oferta, estado)
    VALUES (p_id_estudiante, p_id_oferta, 'Enviada');
END;
$$;

-- 4. TRIGGER DE VALIDACIÓN ACTIVA
CREATE OR REPLACE FUNCTION trg_fn_validar_oferta_activa()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
BEGIN
    SELECT estado INTO v_estado FROM ofertas WHERE id_oferta = NEW.id_oferta;

    IF v_estado <> 'Activa' THEN
        RAISE EXCEPTION 'No se permite postular a una oferta cerrada (id_oferta=%)', NEW.id_oferta;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_validar_oferta_activa ON postulaciones;

CREATE TRIGGER trg_validar_oferta_activa
BEFORE INSERT ON postulaciones
FOR EACH ROW
EXECUTE FUNCTION trg_fn_validar_oferta_activa();

-- 5. DATOS DE PRUEBA (Hash compatible con PasswordUtil para clave: 123456)
INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES 
('Admin Principal', 'admin@unitalent.com', 'Clj3UQCj3YSFbNuI2Hckzg==$0Js6mwYkA+ybMNRtHI4F2bjek3InzezBSKwnNzliIKM=', 'ADMIN'),
('Juan Perez', 'estudiante@unitalent.com', 'Clj3UQCj3YSFbNuI2Hckzg==$0Js6mwYkA+ybMNRtHI4F2bjek3InzezBSKwnNzliIKM=', 'ESTUDIANTE'),
('TechCorp S.A.C.', 'empresa@techcorp.com', 'Clj3UQCj3YSFbNuI2Hckzg==$0Js6mwYkA+ybMNRtHI4F2bjek3InzezBSKwnNzliIKM=', 'EMPRESA');

INSERT INTO estudiantes (id_usuario, carrera, ciclo, telefono) VALUES 
((SELECT id_usuario FROM usuarios WHERE correo='estudiante@unitalent.com'), 'Ingeniería de Sistemas', 8, '987654321');

INSERT INTO empresas (id_usuario, nombre_empresa, rubro, descripcion) VALUES 
((SELECT id_usuario FROM usuarios WHERE correo='empresa@techcorp.com'), 'TechCorp S.A.C.', 'Tecnología', 'Empresa líder en desarrollo de software y consultoría tecnológica');

-- Ofertas de ejemplo
INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) 
SELECT id_empresa, 'Desarrollador Backend Java Junior', 'Buscamos estudiante o egresado con conocimientos en Java, SQL y APIs REST.', 'Remoto', 'Ingeniería de Sistemas', 'Activa' 
FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='empresa@techcorp.com';

INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) 
SELECT id_empresa, 'Practicante de Soporte e Infraestructura', 'Apoyo en redes locales y mantenimiento de servidores.', 'Presencial', 'Ingeniería de Sistemas', 'Activa' 
FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='empresa@techcorp.com';

INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) 
SELECT id_empresa, 'Analista de Datos Jr', 'Creación de dashboards y reportes con PostgreSQL y Python.', 'Hibrido', 'Ingeniería de Sistemas', 'Activa' 
FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='empresa@techcorp.com';
