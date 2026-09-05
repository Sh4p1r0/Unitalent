package com.unitalent.db;

import com.mongodb.ConnectionString;

public class ResolveMongo {
    public static void main(String[] args) {
        try {
            String srv = "mongodb+srv://usuario_ejemplo:password_ejemplo@cluster0.ejemplo.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
            ConnectionString cs = new ConnectionString(srv);
            System.out.println("RESOLVED HOSTS: " + cs.getHosts());
            System.out.println("RESOLVED REPLICA SET: " + cs.getRequiredReplicaSetName());
            System.out.println("RESOLVED AUTH SOURCE: " + cs.getCredential().getSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
