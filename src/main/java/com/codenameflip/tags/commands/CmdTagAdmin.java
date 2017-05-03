package com.codenameflip.tags.commands;

import com.codenameflip.tags.Tags;
import com.codenameflip.tags.objects.Tag;
import com.codenameflip.tags.objects.TagHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Stream;

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
                Stream.of(
                        "" + ChatColor.GOLD + ChatColor.BOLD + "Tags Commands " + ChatColor.GRAY + "(Specify a sub command)",
                        ChatColor.RED + "/tagAdmin " + ChatColor.BOLD + "award " + ChatColor.GRAY + "(player|uuid) (tag)",
                        ChatColor.RED + "/tagAdmin " + ChatColor.BOLD + "remove " + ChatColor.GRAY + "(player|uuid) (tag)"
                ).forEach(sender::sendMessage);
            } else {
                if (args[0].equalsIgnoreCase("award") && args.length >= 3) {
                    String target = args[1];
                    Tag targetTag = Tags.get().getTag(args[2]);

                    if (targetTag == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid tag name specified.");

                        return false;
                    }

                    try {
                        UUID uuid = UUID.fromString(target);

                        awardTag(sender, uuid.toString(), targetTag.getIdentifier());
                    } catch (IllegalArgumentException e) {
                        // If the target is not a UUID; check if it's a player

                        Player playerTarget = Bukkit.getPlayerExact(target);

                        if (playerTarget == null) {
                            sender.sendMessage(ChatColor.RED + "Invalid player name specified.");

                            return false;
                        }

                        awardTag(sender, playerTarget.getUniqueId().toString(), targetTag.getIdentifier());
                    }
                } else if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
                    String target = args[1];
                    Tag targetTag = Tags.get().getTag(args[2]);

                    if (targetTag == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid tag name specified.");

                        return false;
                    }

                    try {
                        UUID uuid = UUID.fromString(target);

                        removeTag(sender, uuid.toString(), targetTag.getIdentifier());
                    } catch (IllegalArgumentException e) {
                        // If the target is not a UUID; check if it's a player

                        Player playerTarget = Bukkit.getPlayerExact(target);

                        if (playerTarget == null) {
                            sender.sendMessage(ChatColor.RED + "Invalid player name specified.");

                            return false;
                        }

                        removeTag(sender, playerTarget.getUniqueId().toString(), targetTag.getIdentifier());
                    }
                }
            }
        } else {
            sender.sendMessage(Tags.TAG + ChatColor.RED + "You do not have permission to execute this command");
        }

        return false;
    }

    private void awardTag(CommandSender sender, String uuid, String tag) {
        Tag targetTag = Tags.get().getTag(tag);

        if (targetTag == null) {
            sender.sendMessage(ChatColor.RED + "Invalid tag specified; cannot find tag with identifier '" + tag + "'");
            return;
        }

            try {
                UUID targetUUID = UUID.fromString(uuid);

                // Check if the tagholder is already loaded within the plugin
                if (Tags.get().getTagHolder(targetUUID) != null) {
                    TagHolder loadedTagHolder = Tags.get().getTagHolder(targetUUID);

                    loadedTagHolder.addTag(targetTag);

                    sender.sendMessage(ChatColor.GREEN + "Added the '" + tag + "' tag to " + uuid);
                    return;
                }

                TagHolder targetHolder = Tags.get().getTagHolder(targetUUID);

                if (targetHolder == null) {
                    // Load the tag holder data
                    Tags.get().getDataStrategy().loadTagData(targetUUID);

                    targetHolder = Tags.get().getTagHolder(targetUUID);

                    // If the target is still null even after registering the uuid's object from the database, then alert the player
                    if (targetHolder == null) {
                        sender.sendMessage(ChatColor.RED + "Unable to load tag data, either it does not exist in the database or the database cannot be reached. " + (Tags.get().getDataStrategy().isAlive() ? "(Database connection: ALIVE)" : "(Database connection: DEAD)"));

                        return;
                    }
                }

                targetHolder.addTag(targetTag);
                sender.sendMessage(ChatColor.GREEN + "Added the '" + tag + "' tag to " + uuid);

            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Illegal uuid specified.");
            }
    }

    private void removeTag(CommandSender sender, String uuid, String tag) {
        Tag targetTag = Tags.get().getTag(tag);

        if (targetTag == null) {
            sender.sendMessage(ChatColor.RED + "Invalid tag specified; cannot find tag with identifier '" + tag + "'");
            return;
        }

        try {
            UUID targetUUID = UUID.fromString(uuid);

            // Check if the tagholder is already loaded within the plugin
            if (Tags.get().getTagHolder(targetUUID) != null) {
                TagHolder loadedTagHolder = Tags.get().getTagHolder(targetUUID);

                loadedTagHolder.removeTag(targetTag);

                sender.sendMessage(ChatColor.GREEN + "Removed the '" + tag + "' tag from " + uuid);
                return;
            }

            TagHolder targetHolder = Tags.get().getTagHolder(targetUUID);

            if (targetHolder == null) {
                // Load the tag holder data
                Tags.get().getDataStrategy().loadTagData(targetUUID);

                targetHolder = Tags.get().getTagHolder(targetUUID);

                // If the target is still null even after registering the uuid's object from the database, then alert the player
                if (targetHolder == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to load tag data, either it does not exist in the database or the database cannot be reached. " + (Tags.get().getDataStrategy().isAlive() ? "(Database connection: ALIVE)" : "(Database connection: DEAD)"));

                    return;
                }
            }

            targetHolder.removeTag(targetTag);
            sender.sendMessage(ChatColor.GREEN + "Removed the '" + tag + "' tag from " + uuid);

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Illegal uuid specified.");
        }
    }

}
