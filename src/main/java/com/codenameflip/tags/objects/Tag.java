package com.codenameflip.tags.objects;

import com.codenameflip.tags.Tags;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class Tag {

    private String identifier;
    private String displayName;
    private boolean exclusive;

    public Tag(String identifier, String displayName, boolean exclusive) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.exclusive = exclusive;

        this.scoreboardTeam = Tags.get().getTagScoreboard().registerNewTeam(identifier + ThreadLocalRandom.current().nextInt(1000));
        scoreboardTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', displayName));
        scoreboardTeam.setSuffix("§r");
    }

    private Team scoreboardTeam;

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Team getScoreboardTeam() {
        return scoreboardTeam;
    }

}
