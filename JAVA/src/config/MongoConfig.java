/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author 2dami
 */
public class MongoConfig {

    private static final MongoClient client;
    private static final MongoDatabase db;

    static {
        client = MongoClients.create("mongodb://localhost:27017");
        db = client.getDatabase("RetoMongoDB");

    }

    public static MongoDatabase getDatabase() {
        return db;
    }
}
