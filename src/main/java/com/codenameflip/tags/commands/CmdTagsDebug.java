package com.codenameflip.tags.commands;

import com.codenameflip.tags.Tags;
import com.simplexitymc.command.api.Command;
import com.simplexitymc.command.api.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class CmdTagsDebug extends Command {

    public CmdTagsDebug(CommandHandler handler, String permission, String... executors) {
        super(handler, permission, executors);

        addChild(new CmdDebugDataDump(this, "dumpdata", "datadump"));
    }

    @Override
    public void execute(Player player, String... strings) {
        if (strings.length < 1) {
            Tags.get().getTagSet().forEach(tag -> {
                debugMessage(player, "&e&l" + tag.getIdentifier());
            });

            Tags.get().getTagHolderSet().forEach(tagHolder -> {
                debugMessage(player, "&b" + tagHolder.getUuid().toString());
                debugMessage(player, "  &3" + tagHolder.getFormattedTags());
                debugMessage(player, "  &3&o" + tagHolder.getSelectedTag());
            });

            debugMessage(player, "Data Strategy &a" + Tags.get().getDataStrategy().getIdentifier().toUpperCase());
            debugMessage(player, "Strategy Status " + (Tags.get().getDataStrategy().isAlive() ? "&2&lALIVE" : "&c&lDEAD"));
        } else
            attemptChildCommand(player, strings);
    }

    private void debugMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDebug> &f" + message));
    }

}
