package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.session.MenuSection;
import com.cavetale.tutor.session.Session;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public final class DailyCommand extends AbstractCommand<TutorPlugin> {
    public DailyCommand(final TutorPlugin plugin) {
        super(plugin, "daily");
    }

    @Override
    protected void onEnable() {
        rootNode.playerCaller(this::daily);
        rootNode.addChild("back").denyTabCompletion()
            .hidden(true)
            .playerCaller(this::dailyBack);
        rootNode.addChild("game").denyTabCompletion()
            .hidden(true)
            .playerCaller(this::dailyGame);
    }

    private void daily(Player player) {
        Session session = plugin.getSessions().find(player);
        if (session == null) throw new CommandWarn("Session not loaded. Try again later.");
        session.openMenu(player, MenuSection.DAILY);
    }

    private void dailyBack(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
        daily(player);
    }

    private void dailyGame(Player player) {
        Session session = plugin.getSessions().find(player);
        if (session == null) throw new CommandWarn("Session not loaded. Try again later.");
        session.openDailyGame(player);
    }
}
