package com.unitalent.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Maneja la conexión a la base de datos NoSQL MongoDB.
 * Ajusta la URI si tu MongoDB no corre en localhost:27017.
 */
public class MongoConnection {

    private static final String URI = "mongodb://shapiamaleonardo_db_user:p3HrW1v1CHFnHHR9@ac-uczunlc-shard-00-00.no3bvre.mongodb.net:27017,ac-uczunlc-shard-00-01.no3bvre.mongodb.net:27017,ac-uczunlc-shard-00-02.no3bvre.mongodb.net:27017/?ssl=true&replicaSet=atlas-33e489-shard-0&authSource=admin&retryWrites=true&w=majority";
    private static final String DB_NAME = "unitalent";

    private static MongoClient client;

    private static MongoClient getClient() {
        if (client == null) {
            client = MongoClients.create(URI);
        }
        return client;
    }

    public static MongoDatabase getDatabase() {
        return getClient().getDatabase(DB_NAME);
    }
}
