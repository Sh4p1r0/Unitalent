package com.unitalent.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Maneja la conexion a la base de datos relacional PostgreSQL.
 * Las credenciales reales se cargan desde src/main/resources/db.properties
 * (ignorado por Git).
 */
public class PostgresConnection {

    private static String host = "localhost";
    private static String port = "5432";
    private static String dbName = "postgres";
    private static String user = "postgres";
    private static String password = "password_ejemplo";
    private static boolean ssl = false;

    static {
        try (InputStream is = PostgresConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                host = props.getProperty("postgres.host", host);
                port = props.getProperty("postgres.port", port);
                dbName = props.getProperty("postgres.db", dbName);
                user = props.getProperty("postgres.user", user);
                password = props.getProperty("postgres.password", password);
                ssl = Boolean.parseBoolean(props.getProperty("postgres.ssl", "false"));
            }
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo cargar db.properties, usando valores por defecto: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de PostgreSQL", e);
        }
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + (ssl ? "?sslmode=require" : "");
        return DriverManager.getConnection(url, user, password);
    }
}