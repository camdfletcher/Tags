package com.codenameflip.tags.data;

import com.codenameflip.tags.objects.Tag;
import com.codenameflip.tags.objects.TagHolder;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public abstract class DataStrategy {

    private String identifier;

    public DataStrategy(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract void connect();
    public abstract void disconnect();
    public abstract boolean isAlive();

    public abstract void loadTags();
    public abstract void saveTag(Tag tag);
    public abstract void deleteTag(Tag tag);

    public abstract void loadTagData(UUID uuid);
    public abstract void saveTagData(TagHolder tagHolder);
    public abstract void updateTagData(TagHolder tagHolder);
    public abstract void deleteTagData(UUID uuid);
    public abstract void deleteTagData(TagHolder tagHolder);

    public abstract void dumpData(Player player);

}
