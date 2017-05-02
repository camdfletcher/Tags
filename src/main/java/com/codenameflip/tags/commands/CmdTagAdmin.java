package com.codenameflip.tags.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author codenameflip
 * @since 5/1/17
 */

public class CmdTagAdmin implements CommandExecutor {

    // This class is done using Bukkit's command system to allow console to execute commands

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("tags.cmd.admin")) {
            if (args.length < 1) {

            } else {

            }
        }

        return false;
    }

    private void awardTag(Player player, String tag) {

    }

    private void removeTag(Player player, String tag) {

    }

}
