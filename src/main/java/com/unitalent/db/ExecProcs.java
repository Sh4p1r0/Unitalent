package com.unitalent.db;

import java.sql.Connection;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExecProcs {
    public static void main(String[] args) {
        try {
            String sql = new String(Files.readAllBytes(Paths.get("BASE DE DATOS - PostgreSQL - MongoDB/procedimientos.sql")));
            try (Connection conn = PostgresConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Procedimientos y funciones creados exitosamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
