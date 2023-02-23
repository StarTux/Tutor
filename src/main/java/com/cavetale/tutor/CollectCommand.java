package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.session.MenuSection;
import com.cavetale.tutor.session.Session;
import org.bukkit.entity.Player;

public final class CollectCommand extends AbstractCommand<TutorPlugin> {
    public CollectCommand(final TutorPlugin plugin) {
        super(plugin, "collect");
    }

    @Override
    protected void onEnable() {
        rootNode.playerCaller(this::collect);
    }

    private void collect(Player player) {
        Session session = plugin.getSessions().find(player);
        if (session == null) throw new CommandWarn("Session not loaded. Try again later.");
        session.openMenu(player, MenuSection.COLLECT);
    }
}
