package com.unitalent.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.InputStream;
import java.util.Properties;

/**
 * Maneja la conexion a la base de datos NoSQL MongoDB.
 * Las credenciales reales se cargan desde src/main/resources/db.properties (ignorado por Git).
 */
public class MongoConnection {

    private static String uri = "mongodb://localhost:27017";
    private static String dbName = "unitalent";
    private static MongoClient client;

    static {
        try (InputStream is = MongoConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                uri = props.getProperty("mongo.uri", uri);
                dbName = props.getProperty("mongo.db", dbName);
            }
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo cargar db.properties en MongoConnection: " + e.getMessage());
        }
    }

    private static MongoClient getClient() {
        if (client == null) {
            client = MongoClients.create(uri);
        }
        return client;
    }

    public static MongoDatabase getDatabase() {
        return getClient().getDatabase(dbName);
    }
}
