

DROP TABLE IF EXISTS postulaciones CASCADE;
DROP TABLE IF EXISTS ofertas CASCADE;
DROP TABLE IF EXISTS empresas CASCADE;
DROP TABLE IF EXISTS estudiantes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,   -- hash SHA-256 + salt
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

