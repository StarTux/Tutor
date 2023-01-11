package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.session.MenuSection;
import com.cavetale.tutor.session.Session;
import org.bukkit.entity.Player;

public final class DailyCommand extends AbstractCommand<TutorPlugin> {
    public DailyCommand(final TutorPlugin plugin) {
        super(plugin, "daily");
    }

    @Override
    protected void onEnable() {
        rootNode.playerCaller(this::daily);
    }

    private void daily(Player player) {
        Session session = plugin.getSessions().find(player);
        if (session == null) throw new CommandWarn("Session not loaded. Try again later.");
        session.openMenu(player, MenuSection.DAILY);
    }
}
