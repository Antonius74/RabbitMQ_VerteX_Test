package com.nexi.rabbitmq.vertex.test.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class MongoDBTest {
    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB database = mongoClient.getDB("myMongoDb");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("type", "test");
        DBCollection collection = database.getCollection("NexiCollection");
        DBCursor cursor = collection.find(searchQuery);
        System.out.println(collection.getCollection("NexiCollection"));
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }
    }
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    public DB getConnection() {
        //mongoClient.listDatabases().iterator().forEachRemaining(a-> System.out.println(a.get("name")));
        return mongoClient.getDB("myMongoDb");
    }

    public void MongoTestWrite(String k, String v) {

        DB database = getConnection();

        //database.createCollection("NexiCollection", null);

        //database.getCollectionNames().forEach(System.out::println);

        DBCollection collection = database.getCollection("NexiCollection");
        BasicDBObject document = new BasicDBObject();
        document.put(k, v);
        //document.put("type", "test");

        //System.out.println(document.toJson() + " ack: " +
                collection.insert(document).wasAcknowledged();

        /*// update data
        BasicDBObject query = new BasicDBObject();
        query.put("name", "Shubham");
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("name", "John");
        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument);
        collection.update(query, updateObject);

        // read data
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", "John");
        DBCursor cursor = collection.find(searchQuery);
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }

        // delete data
        BasicDBObject deleteQuery = new BasicDBObject();
        deleteQuery.put("name", "John");
        collection.remove(deleteQuery);*/
    }
}



