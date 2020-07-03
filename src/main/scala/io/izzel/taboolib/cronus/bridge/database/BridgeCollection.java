package io.izzel.taboolib.cronus.bridge.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import io.izzel.taboolib.module.config.TConfigMigrate;
import io.izzel.taboolib.util.KV;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public final class BridgeCollection {

    private final BridgeDatabase database;
    private final String collection;
    private final MongoCollection<Document> mongoCollection;
    private final Map<String, Data> dataMap = Maps.newConcurrentMap();
    private final Gson gson = new Gson();
    private final IndexType indexType;

    public BridgeCollection(BridgeDatabase database, String collection) {
        this(database, collection, IndexType.NONE);
    }

    public BridgeCollection(BridgeDatabase database, String collection, IndexType indexType) {
        this.database = database;
        this.collection = collection;
        this.mongoCollection = this.database.getMongoDatabase().getCollection(this.collection);
        if (!hasIndex()) {
            this.mongoCollection.createIndex(Indexes.ascending("id"));
        }
        this.indexType = indexType;
    }

    public void release(String id) {
        this.dataMap.remove(id);
    }

    public void update(String id) {
        if (dataMap.containsKey(id)) {
            update(id, dataMap.get(id));
        }
    }

    public void update(String id, Data data) {
        Map<String, Object> current = TConfigMigrate.toMap(data.getData());
        if (!data.isChecked() && mongoCollection.countDocuments(Filters.eq("id", id)) == 0) {
            mongoCollection.insertOne(new Document().append("data", Document.parse(gson.toJson(current))).append("id", id));
        } else {
            List<KV<String, Object>> contrast = TConfigMigrate.contrast(current, data.getUpdate());
            if (contrast.size() > 0) {
                mongoCollection.updateOne(Filters.eq("id", id), Updates.combine(toBson(contrast)));
                data.setChecked(true);
                data.getUpdate().clear();
                data.getUpdate().putAll(current);
            }
        }
    }

    public FileConfiguration get(String id) {
        return get(id, true);
    }

    public FileConfiguration get(String id, boolean cache) {
        if (cache && dataMap.containsKey(id)) {
            return dataMap.get(id).getData();
        }
        Document find = mongoCollection.find(Filters.eq("id", id)).first();
        Data data;
        if (find != null) {
            data = new Data(id, find.get("data", Document.class).entrySet()).setChecked(true);
        } else {
            data = new Data(id);
        }
        dataMap.put(id, data);
        return data.getData();
    }

    private boolean hasIndex() {
        Iterable<Document> indexes = this.mongoCollection.listIndexes();
        for (Document document : indexes) {
            if (document.get("key", Document.class).containsKey("id")) {
                return true;
            }
        }
        return false;
    }

    private List<Bson> toBson(List<KV<String, Object>> difference) {
        List<Bson> list = Lists.newArrayList();
        for (KV<String, Object> pair : difference) {
            if (pair.getValue() == null) {
                list.add(Updates.unset("data." + pair.getKey()));
            } else {
                list.add(Updates.set("data." + pair.getKey(), pair.getValue()));
            }
        }
        return list;
    }

    public BridgeDatabase getDatabase() {
        return this.database;
    }

    public String getCollection() {
        return this.collection;
    }

    public MongoCollection<Document> getMongoCollection() {
        return this.mongoCollection;
    }

    public Map<String, Data> getDataMap() {
        return dataMap;
    }

    public IndexType getIndexType() {
        return indexType;
    }
}
