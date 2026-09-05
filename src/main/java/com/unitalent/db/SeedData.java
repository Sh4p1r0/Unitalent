package com.unitalent.db;

import java.sql.Connection;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.Arrays;

public class SeedData {
    public static String hash(String plainPassword) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(plainPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void main(String[] args) {
        String pw = hash("123456");
        try (Connection con = PostgresConnection.getConnection();
             Statement st = con.createStatement()) {

            // Empresas
            st.execute("INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES ('Global Tech', 'contacto@globaltech.com', '"+pw+"', 'EMPRESA')");
            st.execute("INSERT INTO empresas (id_usuario, nombre_empresa, rubro) SELECT id_usuario, 'Global Tech SAC', 'Desarrollo de Software' FROM usuarios WHERE correo='contacto@globaltech.com'");
            
            st.execute("INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES ('Marketing Pro', 'hr@marketingpro.com', '"+pw+"', 'EMPRESA')");
            st.execute("INSERT INTO empresas (id_usuario, nombre_empresa, rubro) SELECT id_usuario, 'Marketing Pro Agencia', 'Marketing y Publicidad' FROM usuarios WHERE correo='hr@marketingpro.com'");

            // Estudiantes
            st.execute("INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES ('Carlos Ramirez', 'carlos@uni.com', '"+pw+"', 'ESTUDIANTE')");
            st.execute("INSERT INTO estudiantes (id_usuario, carrera, ciclo) SELECT id_usuario, 'Ingeniería de Sistemas', 8 FROM usuarios WHERE correo='carlos@uni.com'");

            st.execute("INSERT INTO usuarios (nombre, correo, contrasena, rol) VALUES ('Ana Torres', 'ana@uni.com', '"+pw+"', 'ESTUDIANTE')");
            st.execute("INSERT INTO estudiantes (id_usuario, carrera, ciclo) SELECT id_usuario, 'Marketing', 6 FROM usuarios WHERE correo='ana@uni.com'");

            // Ofertas
            st.execute("INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) SELECT id_empresa, 'Desarrollador Backend Java', 'Experiencia con Spring Boot y SQL', 'Remoto', 'Ingeniería de Sistemas', 'Activa' FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='contacto@globaltech.com'");
            st.execute("INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) SELECT id_empresa, 'Soporte TI / Redes', 'Mantenimiento de equipos', 'Presencial', 'Ingeniería de Sistemas', 'Activa' FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='contacto@globaltech.com'");
            
            st.execute("INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) SELECT id_empresa, 'Analista de Marketing Digital', 'Gestión de RRSS y Ads', 'Hibrido', 'Marketing', 'Activa' FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='hr@marketingpro.com'");
            st.execute("INSERT INTO ofertas (id_empresa, titulo, descripcion, modalidad, carrera, estado) SELECT id_empresa, 'Community Manager', 'Creación de contenido', 'Remoto', 'Marketing', 'Activa' FROM empresas JOIN usuarios ON empresas.id_usuario = usuarios.id_usuario WHERE correo='hr@marketingpro.com'");

            System.out.println("SQL Inserted successfully.");
        } catch (Exception e) { e.printStackTrace(); }

        try {
            // Setup MongoDB Prefs for the students manually
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> prefs = db.getCollection("preferencias");

            // We need the SQL id_estudiante. For simplicity, just insert logic in SeedData if possible, but hardcoding won't match serial id.
            // Wait, we can fetch the id_estudiante directly in Postgres and map to MongoDB. Let's do that.
            try (Connection con = PostgresConnection.getConnection();
                 Statement st = con.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT e.id_estudiante, u.correo FROM estudiantes e JOIN usuarios u ON e.id_usuario = u.id_usuario")) {
                
                while(rs.next()) {
                    int id = rs.getInt("id_estudiante");
                    String correo = rs.getString("correo");
                    
                    if (correo.equals("carlos@uni.com")) {
                        Document doc = new Document("id_estudiante", id)
                                        .append("areas", Arrays.asList("Ingeniería de Sistemas", "Desarrollo de Software"))
                                        .append("modalidad", "Remoto");
                        prefs.replaceOne(com.mongodb.client.model.Filters.eq("id_estudiante", id), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
                    }
                    if (correo.equals("ana@uni.com")) {
                        Document doc = new Document("id_estudiante", id)
                                        .append("areas", Arrays.asList("Marketing", "Comunicaciones"))
                                        .append("modalidad", "Hibrido");
                        prefs.replaceOne(com.mongodb.client.model.Filters.eq("id_estudiante", id), doc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
                    }
                }
            }
            System.out.println("MongoDB Prefs Inserted successfully.");
        } catch(Exception e) { e.printStackTrace(); }
        System.exit(0);
    }
}
