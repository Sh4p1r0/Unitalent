package com.unitalent.db;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

public class ExecMongo {
    public static void main(String[] args) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            
            System.out.println("Creando indices en MongoDB...");
            
            // Historial busquedas
            db.getCollection("historial_busquedas").createIndex(new Document("id_estudiante", 1));
            
            // Preferencias
            IndexOptions uniqueOpt = new IndexOptions().unique(true);
            db.getCollection("preferencias").createIndex(new Document("id_estudiante", 1), uniqueOpt);
            
            System.out.println("Indices de MongoDB creados exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
