package com.codenameflip.tags.commands;

import com.codenameflip.tags.Tags;
import com.simplexitymc.command.api.ChildCommand;
import com.simplexitymc.command.api.Command;
import org.bukkit.entity.Player;

/**
 * @author codenameflip
 * @since 4/30/17
 */

public class CmdDebugDataDump extends ChildCommand {

    public CmdDebugDataDump(Command parent, String... executors) {
        super(parent, executors);
    }

    @Override
    public void execute(Player player, String... strings) {
        Tags.get().getDataStrategy().dumpData(player);
    }

}
