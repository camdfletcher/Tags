package com.codenameflip.tags.listeners;

import com.codenameflip.tags.Tags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getOnlinePlayers().forEach(online -> {
            online.setScoreboard(Tags.get().getTagScoreboard());
        });

        // Check and see if the player is cached already
        if (Tags.get().getTagHolder(player.getUniqueId()) == null) {

            // Load the TagHolder data for the player
            Tags.get().getDataStrategy().loadTagData(player.getUniqueId());
        }
    }

}
