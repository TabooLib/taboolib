package io.izzel.taboolib.cronus.bridge.database;

import com.google.common.collect.Maps;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Map;

public class BridgeDatabase {

   private final String client;
   private final String database;
   private final MongoClient mongoClient;
   private final MongoDatabase mongoDatabase;
   private final Map<String, BridgeCollection> collectionMap = Maps.newConcurrentMap();

   public BridgeDatabase(String client, String database) {
      this.client = client;
      this.database = database;
      this.mongoClient = MongoClients.create(new ConnectionString(this.client));
      this.mongoDatabase = this.mongoClient.getDatabase(this.database);
   }

   public BridgeCollection get(String collection) {
      return this.collectionMap.computeIfAbsent(collection, i -> new BridgeCollection(this, collection));
   }

   public BridgeCollection get(String collection, IndexType indexType) {
      return this.collectionMap.computeIfAbsent(collection, i -> new BridgeCollection(this, collection, indexType));
   }

   public void release(String collection) {
      this.collectionMap.remove(collection);
   }

   public void releaseId(String collection, String id) {
      BridgeCollection bridgeCollection = this.collectionMap.get(collection);
      if (bridgeCollection != null) {
         bridgeCollection.getDataMap().remove(id);
      }
   }

   public void close() {
      this.mongoClient.close();
   }

   public String getClient() {
      return this.client;
   }

   public String getDatabase() {
      return this.database;
   }

   public MongoClient getMongoClient() {
      return this.mongoClient;
   }

   public MongoDatabase getMongoDatabase() {
      return this.mongoDatabase;
   }

   public Map<String, BridgeCollection> getCollectionMap() {
      return collectionMap;
   }
}
