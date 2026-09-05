package com.unitalent.db;

import org.bson.Document;

public class TestMongoConnection {
    public static void main(String[] args) {
        try {
            Document ping = new Document("ping", 1);
            Document result = MongoConnection.getDatabase().runCommand(ping);
            System.out.println("PING RESULT: " + result.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
