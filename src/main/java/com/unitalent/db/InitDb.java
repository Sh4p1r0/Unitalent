package com.unitalent.db;

import com.unitalent.util.PasswordUtil;
import java.sql.Connection;
import java.sql.Statement;

public class InitDb {
    public static void main(String[] args) {
        System.out.println("Iniciando creacion de tablas en Supabase...");
        
        String sql = "DROP TABLE IF EXISTS postulaciones CASCADE;\n" +
            "DROP TABLE IF EXISTS ofertas CASCADE;\n" +
            "DROP TABLE IF EXISTS empresas CASCADE;\n" +
            "DROP TABLE IF EXISTS estudiantes CASCADE;\n" +
            "DROP TABLE IF EXISTS usuarios CASCADE;\n" +
            "\n" +
            "CREATE TABLE usuarios (\n" +
            "    id_usuario SERIAL PRIMARY KEY,\n" +
            "    nombre VARCHAR(100) NOT NULL,\n" +
            "    correo VARCHAR(100) UNIQUE NOT NULL,\n" +
            "    contrasena VARCHAR(255) NOT NULL,\n" +
            "    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ESTUDIANTE','EMPRESA','ADMIN')),\n" +
            "    fecha_registro TIMESTAMP DEFAULT NOW()\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE estudiantes (\n" +
            "    id_estudiante SERIAL PRIMARY KEY,\n" +
            "    id_usuario INT UNIQUE NOT NULL,\n" +
            "    carrera VARCHAR(100),\n" +
            "    ciclo INT,\n" +
            "    cv VARCHAR(255),\n" +
            "    telefono VARCHAR(20),\n" +
            "    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE empresas (\n" +
            "    id_empresa SERIAL PRIMARY KEY,\n" +
            "    id_usuario INT UNIQUE NOT NULL,\n" +
            "    nombre_empresa VARCHAR(100) NOT NULL,\n" +
            "    rubro VARCHAR(100),\n" +
            "    descripcion TEXT,\n" +
            "    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE ofertas (\n" +
            "    id_oferta SERIAL PRIMARY KEY,\n" +
            "    id_empresa INT NOT NULL,\n" +
            "    titulo VARCHAR(150) NOT NULL,\n" +
            "    descripcion TEXT,\n" +
            "    carrera VARCHAR(100),\n" +
            "    modalidad VARCHAR(50) CHECK (modalidad IN ('Presencial','Remoto','Hibrido')),\n" +
            "    estado VARCHAR(20) DEFAULT 'Activa' CHECK (estado IN ('Activa','Cerrada')),\n" +
            "    fecha_publicacion DATE DEFAULT CURRENT_DATE,\n" +
            "    FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa) ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE postulaciones (\n" +
            "    id_postulacion SERIAL PRIMARY KEY,\n" +
            "    id_estudiante INT NOT NULL,\n" +
            "    id_oferta INT NOT NULL,\n" +
            "    fecha_postulacion DATE DEFAULT CURRENT_DATE,\n" +
            "    estado VARCHAR(20) DEFAULT 'Enviada' CHECK (estado IN ('Enviada','En Proceso','Aceptada','Rechazada')),\n" +
            "    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,\n" +
            "    FOREIGN KEY (id_oferta) REFERENCES ofertas(id_oferta) ON DELETE CASCADE,\n" +
            "    UNIQUE (id_estudiante, id_oferta)\n" +
            ");\n";
            
        String p1 = PasswordUtil.hash("123456");
        String p2 = PasswordUtil.hash("123456");
        String p3 = PasswordUtil.hash("123456");
        
        String inserts = "INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES \n" +
            "('Admin Principal', 'admin@unitalent.com', '" + p1 + "', 'ADMIN'),\n" +
            "('Juan Perez', 'estudiante@unitalent.com', '" + p2 + "', 'ESTUDIANTE'),\n" +
            "('TechCorp', 'empresa@techcorp.com', '" + p3 + "', 'EMPRESA');\n" +
            "INSERT INTO estudiantes (id_usuario, carrera, ciclo) VALUES ((SELECT id_usuario FROM usuarios WHERE correo='estudiante@unitalent.com'), 'Ingeniería de Sistemas', 8);\n" +
            "INSERT INTO empresas (id_usuario, nombre_empresa, rubro, descripcion) VALUES ((SELECT id_usuario FROM usuarios WHERE correo='empresa@techcorp.com'), 'TechCorp S.A.C.', 'Tecnología', 'Desarrollo de Software');\n";
            
        try (Connection conn = PostgresConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("Tablas creadas exitosamente!");
            
            stmt.execute(inserts);
            System.out.println("Usuarios de prueba insertados exitosamente!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
