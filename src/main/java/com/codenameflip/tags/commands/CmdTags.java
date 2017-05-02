package com.codenameflip.tags.commands;

import com.codenameflip.tags.Tags;
import com.codenameflip.tags.objects.Tag;
import com.codenameflip.tags.objects.TagHolder;
import com.codenameflip.tags.utilities.ItemStackBuilder;
import com.simplexitymc.command.api.Command;
import com.simplexitymc.command.api.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class CmdTags extends Command {

    public CmdTags(CommandHandler handler, String permission, String... executors) {
        super(handler, permission, executors);
    }

    @Override
    public void execute(Player player, String... strings) {
        player.sendMessage(Tags.TAG + ChatColor.GREEN + "Opening tag selector...");

        constructGUI(player).show(player);
    }

    private InventoryMenuBuilder constructGUI(Player player) {
        TagHolder tagHolder = Tags.get().getTagHolder(player.getUniqueId());

        int size = tagHolder.getTags() != null && tagHolder.getTags().size() > 0 ? ((tagHolder.getTags().size() / 9) + 1) * 9 : 9;
        InventoryMenuBuilder inv = new InventoryMenuBuilder(size, "Select a tag...");

        System.out.println(size);
        System.out.println(inv);

        if (tagHolder.getTags().size() > 0) {
            // For each tag in the player's storage, add an item to the inventory
            for (int i = 0; i < tagHolder.getTags().size(); i++) {
                Tag tag = tagHolder.getTags().get(i);

                ItemStackBuilder itemStackBuilder = new ItemStackBuilder(Material.NAME_TAG)
                        .withName(tag.getDisplayName() + " " + tag.getIdentifier().toUpperCase()+ " &6&lTag");

                if (tag.isExclusive())
                    itemStackBuilder.withType(Material.DIAMOND).withGlow().withLore("&b&oExclusive Tag");
                else
                    itemStackBuilder.withLore("&8Common Tag");

                if (tagHolder.getSelectedTag() != null && tagHolder.getSelectedTag().equals(tag))
                    itemStackBuilder.withGlow();

                itemStackBuilder.withLore(" ");
                itemStackBuilder.withLore("&fStatus " + (tagHolder.getSelectedTag() != null && tagHolder.getSelectedTag().equals(tag) ? "&aSelected" : "&cNot selected"));
                itemStackBuilder.withLore(" ");
                itemStackBuilder.withLore("&6&lRight Click &fto &aSelect Tag");

                inv.withItem(i, itemStackBuilder.build(), (clicker, clickType, itemStack) -> {
                    if (tagHolder.getSelectedTag() != null && tagHolder.getSelectedTag().equals(tag)) {
                        clicker.sendMessage(ChatColor.RED + "You already have this tag selected!");
                        return;
                    }

                    tagHolder.selectTag(tag);
                    clicker.sendMessage(Tags.TAG + "You selected the " + tag.getDisplayName() + " " + tag.getIdentifier().toUpperCase() + " " + ChatColor.GOLD + "Tag");

                    // Update the GUI
                    constructGUI(player).show(player);
                }, ClickType.RIGHT);
            }

            ItemStack resetItem = new ItemStackBuilder(Material.TNT)
                    .withName("&c&lRESET TAG")
                    .withLore("&7De-selects the currently selected tag")
                    .build();

            inv.withItem(inv.getInventory().getSize() - 1, resetItem, (clicker, clickType, itemStack) -> {
                if (tagHolder.getSelectedTag() != null) {
                    tagHolder.selectTag(null);

                    player.sendMessage(ChatColor.RED + "De-selected all tags.");

                    player.closeInventory();
                    inv.dispose();
                }
            }, ClickType.LEFT, ClickType.RIGHT);

            return inv;
        } else {
            System.out.println(inv);

            for (int i = 0; i < 9; i++) {inv.withItem(i, new ItemStackBuilder(Material.REDSTONE_BLOCK)
                            .withName("&c&lYou do not have any tags :(")
                            .withLore("&7Purchase some tags on the store to fill up this inventory!")
                            .build(),
                    (player1, clickType, itemStack) -> {

                player1.closeInventory();
                inv.dispose();

                }, ClickType.RIGHT, ClickType.LEFT, ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT, ClickType.DROP);
            }

            return inv;
        }
    }

}
