package com.cavetale.tutor;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import com.cavetale.tutor.goal.WildGoal;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    private final TutorPlugin plugin;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void onPluginPlayer(PluginPlayerEvent event) {
        switch (event.parseName()) {
        case USE_WILD:
            plugin.sessions.applyGoals(event.getPlayer(), WildGoal.class,
                                       (playerQuest, goal) -> goal.onUseWild(playerQuest));
            break;
        case CREATE_CLAIM:
            plugin.sessions.applyGoals(event.getPlayer(), WildGoal.class,
                                       (playerQuest, goal) -> playerQuest.onGoalComplete());
            break;
        default: break;
        }
    }

    @EventHandler
    void onPlayerSidebar(PlayerSidebarEvent event) {
        Player player = event.getPlayer();
        Session session = plugin.sessions.find(player);
        if (session == null) return;
        List<Component> lines = null;
        for (PlayerQuest playerQuest : session.getQuestList()) {
            lines = new ArrayList<>();
            lines.add(Component.text()
                      .append(Component.text("Your ", NamedTextColor.AQUA))
                      .append(Component.text("/tutorial", NamedTextColor.YELLOW))
                      .build());
            lines.addAll(playerQuest.getCurrentGoal().getSidebarLines(playerQuest));
            break;
        }
        if (lines == null) return;
        event.add(plugin, Priority.DEFAULT, lines);
    }
}
