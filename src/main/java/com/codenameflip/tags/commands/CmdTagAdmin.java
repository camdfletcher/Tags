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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender.hasPermission("tags.cmd.admin"))
        {
            if (args.length < 1)
            {
                Stream.of(
                        "" + ChatColor.GOLD + ChatColor.BOLD + "Tags Commands " + ChatColor.GRAY + "(Specify a sub command)",
                        ChatColor.RED + "/tagAdmin " + ChatColor.BOLD + "award " + ChatColor.GRAY + "(player|uuid) (tag)",
                        ChatColor.RED + "/tagAdmin " + ChatColor.BOLD + "remove " + ChatColor.GRAY + "(player|uuid) (tag)",
                        ChatColor.RED + "/tagAdmin " + ChatColor.BOLD + "create " + ChatColor.GRAY + "(identifier) (display name) (isExclusive)"
                ).forEach(sender::sendMessage);
            }
            else
            {
                if (args[0].equalsIgnoreCase("award") && args.length >= 3)
                {
                    /*
                        /tagAdmin award (uuid|player) (tag)
                     */
                    String target = args[1];
                    Tag targetTag = Tags.get().getTag(args[2]);

                    if (targetTag == null)
                    {
                        sender.sendMessage(ChatColor.RED + "Invalid tag name specified.");

                        return false;
                    }

                    try
                    {
                        UUID uuid = UUID.fromString(target);

                        awardTag(sender, uuid.toString(), targetTag.getIdentifier());
                    }
                    catch (IllegalArgumentException e)
                    {
                        // If the target is not a UUID; check if it's a player

                        Player playerTarget = Bukkit.getPlayerExact(target);

                        if (playerTarget == null)
                        {
                            sender.sendMessage(ChatColor.RED + "Invalid player name specified.");

                            return false;
                        }

                        awardTag(sender, playerTarget.getUniqueId().toString(), targetTag.getIdentifier());
                    }
                }
                else if (args[0].equalsIgnoreCase("remove") && args.length >= 3)
                {
                    /*
                        /tagAdmin remove (uuid|player) (tag)
                     */
                    String target = args[1];
                    Tag targetTag = Tags.get().getTag(args[2]);

                    if (targetTag == null)
                    {
                        sender.sendMessage(ChatColor.RED + "Invalid tag name specified.");

                        return false;
                    }

                    try
                    {
                        UUID uuid = UUID.fromString(target);

                        removeTag(sender, uuid.toString(), targetTag.getIdentifier());
                    }
                    catch (IllegalArgumentException e)
                    {
                        // If the target is not a UUID; check if it's a player

                        Player playerTarget = Bukkit.getPlayerExact(target);

                        if (playerTarget == null)
                        {
                            sender.sendMessage(ChatColor.RED + "Invalid player name specified.");

                            return false;
                        }

                        removeTag(sender, playerTarget.getUniqueId().toString(), targetTag.getIdentifier());
                    }
                }
                else if (args[0].equalsIgnoreCase("create") && args.length >= 4)
                {
                    /*
                        /tagAdmin create (identifier) (display name) (isExclusive)
                     */

                    String identifier = args[1];
                    String displayName = args[2];
                    String isExclusive = args[3];

                    createTag(sender, identifier, displayName, isExclusive);
                }
            }
        }
        else
        {
            sender.sendMessage(Tags.TAG + ChatColor.RED + "You do not have permission to execute this command");
        }

        return false;
    }

    /**
     * Gives a player a tag
     *
     * @param sender The sender executing the request
     * @param uuid   The uuid the tag is being given to
     * @param tag    The tag you wish to give to the player
     */
    private void awardTag(CommandSender sender, String uuid, String tag)
    {
        Tag targetTag = Tags.get().getTag(tag);

        if (targetTag == null)
        {
            sender.sendMessage(ChatColor.RED + "Invalid tag specified; cannot find tag with identifier '" + tag + "'");
            return;
        }

        try
        {
            UUID targetUUID = UUID.fromString(uuid);

            // Check if the tagholder is already loaded within the plugin
            if (Tags.get().getTagHolder(targetUUID) != null)
            {
                TagHolder loadedTagHolder = Tags.get().getTagHolder(targetUUID);

                loadedTagHolder.addTag(targetTag);

                sender.sendMessage(ChatColor.GREEN + "Added the '" + tag + "' tag to " + uuid);
                return;
            }

            TagHolder targetHolder = Tags.get().getTagHolder(targetUUID);

            if (targetHolder == null)
            {
                // Load the tag holder data
                Tags.get().getDataStrategy().loadTagData(targetUUID);

                targetHolder = Tags.get().getTagHolder(targetUUID);

                // If the target is still null even after registering the uuid's object from the database, then alert the player
                if (targetHolder == null)
                {
                    sender.sendMessage(ChatColor.RED + "Unable to load tag data, either it does not exist in the database or the database cannot be reached. " + (Tags.get().getDataStrategy().isAlive() ? "(Database connection: ALIVE)" : "(Database connection: DEAD)"));

                    return;
                }
            }

            targetHolder.addTag(targetTag);
            sender.sendMessage(ChatColor.GREEN + "Added the '" + tag + "' tag to " + uuid);

        }
        catch (IllegalArgumentException e)
        {
            sender.sendMessage(ChatColor.RED + "Illegal uuid specified.");
        }
    }

    /**
     * Removes a tag from a player's profile
     *
     * @param sender The sender executing the request
     * @param uuid   The uuid the tag is being removed from
     * @param tag    The tag you wish to remove
     */
    private void removeTag(CommandSender sender, String uuid, String tag)
    {
        Tag targetTag = Tags.get().getTag(tag);

        if (targetTag == null)
        {
            sender.sendMessage(ChatColor.RED + "Invalid tag specified; cannot find tag with identifier '" + tag + "'");
            return;
        }

        try
        {
            UUID targetUUID = UUID.fromString(uuid);

            // Check if the tagholder is already loaded within the plugin
            if (Tags.get().getTagHolder(targetUUID) != null)
            {
                TagHolder loadedTagHolder = Tags.get().getTagHolder(targetUUID);

                // If the tag being removed from the player is their current tag, remove it from their selected tags
                if (loadedTagHolder.getSelectedTag() != null && loadedTagHolder.getSelectedTag().getIdentifier().equalsIgnoreCase(targetTag.getIdentifier()))
                    loadedTagHolder.selectTag(null);

                loadedTagHolder.removeTag(targetTag);

                sender.sendMessage(ChatColor.GREEN + "Removed the '" + tag + "' tag from " + uuid);
                return;
            }

            TagHolder targetHolder = Tags.get().getTagHolder(targetUUID);

            if (targetHolder == null)
            {
                // Load the tag holder data
                Tags.get().getDataStrategy().loadTagData(targetUUID);

                targetHolder = Tags.get().getTagHolder(targetUUID);

                // If the target is still null even after registering the uuid's object from the database, then alert the player
                if (targetHolder == null)
                {
                    sender.sendMessage(ChatColor.RED + "Unable to load tag data, either it does not exist in the database or the database cannot be reached. " + (Tags.get().getDataStrategy().isAlive() ? "(Database connection: ALIVE)" : "(Database connection: DEAD)"));

                    return;
                }
            }

            // If the tag being removed from the player is their current tag, remove it from their selected tags
            if (targetHolder.getSelectedTag() != null && targetHolder.getSelectedTag().getIdentifier().equalsIgnoreCase(targetTag.getIdentifier()))
                targetHolder.selectTag(null);


            targetHolder.removeTag(targetTag);
            sender.sendMessage(ChatColor.GREEN + "Removed the '" + tag + "' tag from " + uuid);

        }
        catch (IllegalArgumentException e)
        {
            sender.sendMessage(ChatColor.RED + "Illegal uuid specified.");
        }
    }

    /**
     * Creates a tag and adds it to the database
     *
     * @param sender     The sender executing the request
     * @param identifier The desired identifier
     * @param display    The desired display name
     * @param exclusive  Is the tag an exclusive tag?
     */
    private void createTag(CommandSender sender, String identifier, String display, String exclusive)
    {
        boolean isExclusive = Boolean.parseBoolean(exclusive);

        Tag tag = new Tag(identifier, display, isExclusive);
        Tags.get().registerTag(tag);

        sender.sendMessage(ChatColor.GREEN + "Created/registered new tag '" + identifier + "'; it may now be distributed to players.");
    }

}
