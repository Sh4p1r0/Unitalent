package com.unitalent.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Maneja la conexión a la base de datos relacional PostgreSQL.
 * Ajusta HOST, PORT, DB_NAME, USER y PASSWORD según tu instalación local.
 */
public class PostgresConnection {

    private static final String HOST = "aws-0-sa-east-1.pooler.supabase.com";
    private static final String PORT = "5432";
    private static final String DB_NAME = "postgres";
    private static final String USER = "postgres.nsydfsjznxqvowbixouq";
    private static final String PASSWORD = "H910j-@PEEEE";

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME + "?sslmode=require";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de PostgreSQL", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
