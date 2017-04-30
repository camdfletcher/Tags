package com.codenameflip.tags.data;

import com.codenameflip.tags.Tags;
import com.codenameflip.tags.objects.Tag;
import com.codenameflip.tags.objects.TagHolder;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.UUID;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class MongoDataStrategy extends DataStrategy {

    private FileConfiguration config;

    public MongoDataStrategy(FileConfiguration config) {
        super("MongoDB");

        this.config = config;

        System.out.println("Storage> New data strategy recognized (" + getIdentifier() + ")...");
    }

    private String host = getConfig().getString("connection-details.host");
    private String port = getConfig().getString("connection-details.port");
    private String user = getConfig().getString("connection-details.user");
    private String pass = getConfig().getString("connection-details.pass");
    private String database = getConfig().getString("connection-details.database");

    private MongoClient connection;

    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void connect() {
        try {
            MongoCredential authCredentials = MongoCredential.createCredential(user, database, pass.toCharArray());
            this.connection = new MongoClient(new ServerAddress(host, Integer.parseInt(port)), Collections.singletonList(authCredentials));

            System.out.println("Mongo> Connected to host successfully.");
        } catch (Exception e) {
            System.out.println("Mongo> Error encountered when trying to establish a connection to the database.");
        }
    }

    @Override
    public void disconnect() {
        if (isAlive()) {
            connection.close();

            System.out.println("Mongo> Disconnected from host successfully");
        }
    }

    @Override
    public boolean isAlive() {
        try {
            connection.getAddress();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void loadTags() {
        // Iterate through all documents in the collection and load them (as all of them should be tag objects)
        try (MongoCursor<Document> cursor = getCollection("tags").find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();

                Tag tag = new Tag(document.getString("identifier"), document.getString("displayName"), document.getBoolean("exclusive"));
                Tags.get().registerTag(tag);
            }
        }
    }

    @Override
    public void saveTag(Tag tag) {
        // Insert the updated document in the collection
        Document tagDocument = new Document("identifier", tag.getIdentifier());
        tagDocument.append("identifier", tag.getIdentifier()); // TODO: Make sure this doesn't duplicate
        tagDocument.append("displayName", tag.getDisplayName());
        tagDocument.append("exclusive", tag.isExclusive());

        getCollection("tags").insertOne(tagDocument);
    }

    @Override
    public void deleteTag(Tag tag) {
        getCollection("tags").findOneAndDelete(Filters.eq("identifier", tag.getIdentifier()));
    }

    @Override
    public void loadTagData(UUID uuid) {
        Document targetDocument = getCollection("tagData").find(new Document("uuid", uuid.toString())).first();

        // If the player does not exist then generate new data and save it
        if (targetDocument == null) {
            TagHolder newTagHolder = new TagHolder(uuid);

            Tags.get().registerTagHolder(newTagHolder);
            saveTagData(newTagHolder);
        } else {
            TagHolder tagHolder = new TagHolder(uuid);

            // Loop through the split version of the tags string
            String[] ownedTags = targetDocument.getString("tags").split(":");

            for (String ownedTag : ownedTags) {
                Tag tag = Tags.get().getTag(ownedTag);

                if (tag != null)
                    tagHolder.getTags().add(tag);
            }

            // Select the player's tag
            Tag selectedTag = Tags.get().getTag(targetDocument.getString("selected"));

            if (selectedTag != null)
                tagHolder.selectTag(selectedTag);
            else
                tagHolder.setSelectedTag(null);
        }
    }

    @Override
    public void saveTagData(TagHolder tagHolder) {
        // Insert the updated document in the collection
        Document tagHolderDocument = new Document("uuid", tagHolder.getUuid());
        tagHolderDocument.append("tags", tagHolder.getFormattedTags());
        tagHolderDocument.append("selected", tagHolder.getSelectedTag().getIdentifier());

        getCollection("tagData").insertOne(tagHolderDocument);
    }

    @Override
    public void deleteTagData(UUID uuid) {
        getCollection("tagData").findOneAndDelete(Filters.eq("uuid", uuid.toString()));

    }

    @Override
    public void deleteTagData(TagHolder tagHolder) {
        deleteTagData(tagHolder.getUuid());
    }

    private MongoCollection<Document> getCollection(String name) {
        return connection.getDatabase(database).getCollection(name);
    }

}
