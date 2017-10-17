package com.codenameflip.tags;

import com.codenameflip.tags.commands.CmdTagAdmin;
import com.codenameflip.tags.commands.CmdTags;
import com.codenameflip.tags.commands.CmdTagsDebug;
import com.codenameflip.tags.data.DataStrategy;
import com.codenameflip.tags.data.MongoDataStrategy;
import com.codenameflip.tags.listeners.PlayerJoin;
import com.codenameflip.tags.objects.Tag;
import com.codenameflip.tags.objects.TagHolder;
import com.google.common.collect.Sets;
import com.simplexitymc.command.api.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public final class Tags extends JavaPlugin {

    public static final String TAG = "" + ChatColor.GOLD + "[Tags] " + ChatColor.WHITE;
    private static Tags instance;

    private DataStrategy dataStrategy;
    private Set<Tag> tagSet = Sets.newHashSet();
    private Set<TagHolder> tagHolderSet = Sets.newHashSet();
    private Scoreboard tagScoreboard;

    public static Tags get()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;

        if (!(new File(getDataFolder(), "config.yml")).exists())
            saveDefaultConfig();

        // Check the configuration file for the preferred data storage method
        String configStrategy = getConfig().getString("data-strategy");

        if (configStrategy.equalsIgnoreCase("mongodb"))
            this.dataStrategy = new MongoDataStrategy(getConfig());

        dataStrategy.connect();

        // Initialize the scoreboard before the tags, so that when the Tag instances get created they can reference the board
        tagScoreboard = getServer().getScoreboardManager().getNewScoreboard();

        dataStrategy.loadTags();

        // If the server has not been populated with proper tag data then register them here.
        if (tagSet.size() == 0)
        {
            registerDefaultTags();
        }

        // Register commands and listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);

        CommandHandler commandHandler = new CommandHandler(this);
        commandHandler.message(TAG + ChatColor.RED + "You do not have permission to execute this command");
        commandHandler.addCommand(new CmdTags(commandHandler, "tags.cmd", "tags"));
        commandHandler.addCommand(new CmdTagsDebug(commandHandler, "tags.cmd.debug", "tagsDebug", "tdebug"));
        getCommand("tagAdmin").setExecutor(new CmdTagAdmin());

        // Handle reloads safely (if any occur)
        Bukkit.getOnlinePlayers().forEach(player -> {

            // Load the TagHolder data for the player
            Tags.get().getDataStrategy().loadTagData(player.getUniqueId());
        });
    }

    @Override
    public void onDisable()
    {
        if (dataStrategy.isAlive())
            dataStrategy.disconnect();

        instance = null;
    }

    /**
     * Registers all default tags in the database
     */
    private void registerDefaultTags()
    {
        Tag sun = new Tag("sun", "&6[☼]", false);
        registerTag(sun);

        Tag star = new Tag("star", "&e[☆]", false);
        registerTag(star);

        Tag money = new Tag("money", "&a[$]", false);
        registerTag(money);

        Tag heart = new Tag("heart", "&c[♥]", false);
        registerTag(heart);
    }

    public DataStrategy getDataStrategy()
    {
        return dataStrategy;
    }

    public Set<Tag> getTagSet()
    {
        return tagSet;
    }

    /**
     * Registers a Tag instance to memory
     *
     * @param tag The Tag instance
     */
    public void registerTag(Tag tag)
    {
        getTagSet().add(tag);

        dataStrategy.saveTag(tag);

        System.out.println("Tags> Registered new tag (" + tag.getIdentifier() + ")...");
    }

    public Set<TagHolder> getTagHolderSet()
    {
        return tagHolderSet;
    }

    /**
     * Registers a TagHolder instance to memory
     *
     * @param tagHolder The TagHolder instance
     */
    public void registerTagHolder(TagHolder tagHolder)
    {
        getTagHolderSet().add(tagHolder);

        dataStrategy.saveTagData(tagHolder);

        System.out.println("Tag Data> Registered new tag holder (" + tagHolder.getUuid() + ")...");
    }

    /**
     * Gets the instance of Scoreboard containing all Tag teams
     *
     * @return The scoreboard
     */
    public Scoreboard getTagScoreboard()
    {
        return tagScoreboard;
    }

    /**
     * Gets a Tag instance referencing the tag's identifier
     *
     * @param tag The identifier of the desired tag
     * @return The Tag instance (or null)
     */
    public Tag getTag(String tag)
    {
        return getTagSet().stream()
                .filter(tags -> tags.getIdentifier().equalsIgnoreCase(tag))
                .findAny()
                .orElse(null);
    }

    /**
     * Gets a TagHolder instance referencing the tag holders's uuid
     *
     * @param uuid The uuid of the desired tag holder
     * @return The TagHolder instance (or null)
     */
    public TagHolder getTagHolder(UUID uuid)
    {
        return getTagHolderSet().stream()
                .filter(tagHolders -> tagHolders.getUuid().equals(uuid))
                .findAny()
                .orElse(null);
    }

}
