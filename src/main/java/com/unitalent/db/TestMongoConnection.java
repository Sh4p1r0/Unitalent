package com.unitalent.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

public class TestMongoConnection {
    public static void main(String[] args) {
        String uri = "mongodb://shapiamaleonardo_db_user:p3HrW1v1CHFnHHR9@ac-uczunlc-shard-00-00.no3bvre.mongodb.net:27017,ac-uczunlc-shard-00-01.no3bvre.mongodb.net:27017,ac-uczunlc-shard-00-02.no3bvre.mongodb.net:27017/?ssl=true&replicaSet=atlas-33e489-shard-0&authSource=admin&retryWrites=true&w=majority";
        try (MongoClient client = MongoClients.create(uri)) {
            Document ping = new Document("ping", 1);
            Document result = client.getDatabase("admin").runCommand(ping);
            System.out.println("PING RESULT: " + result.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
