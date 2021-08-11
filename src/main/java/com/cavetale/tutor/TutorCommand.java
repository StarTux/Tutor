package com.cavetale.tutor;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class TutorCommand implements TabExecutor {
    private final TutorPlugin plugin;
    private CommandNode rootNode = new CommandNode("tutor");

    public void enable() {
        rootNode.description("Tutorial Menu")
            .playerCaller(this::tutor);
        rootNode.addChild("click").hidden(true)
            .playerCaller(this::click);
        plugin.getCommand("tutor").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    private boolean tutor(Player player, String[] args) {
        if (args.length != 0) return false;
        if (!plugin.sessions.openQuestBook(player)) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        return true;
    }

    private boolean click(Player player, String[] args) {
        if (args.length != 1) return true;
        plugin.sessions.applyClick(player, args[0]);
        return true;
    }
}
