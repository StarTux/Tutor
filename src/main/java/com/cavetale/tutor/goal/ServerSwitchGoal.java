package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class ServerSwitchGoal implements Goal {
    @Getter private final String id;
    @Getter private final List<Condition> conditions;
    @Getter private final Component displayName;
    private final Component serverDisplayName;
    @Getter private final List<Component> additionalBookPages;
    private final String serverName;

    public ServerSwitchGoal(final String id, final String serverName, final Component displayName,
                            final Component serverDisplayName) {
        this.id = id;
        this.serverName = serverName;
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Type /" + serverName),
                                      playerQuest -> false),
            });
        this.displayName = displayName;
        this.serverDisplayName = serverDisplayName;
        this.additionalBookPages = Arrays.asList(new Component[] {
                Component.text()
                .append(Component.text("Switch to the "))
                .append(serverDisplayName)
                .append(Component.text(" via "))
                .append(Component.text("/" + serverName, NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("."))
                .append(Component.newline())
                .append(Component.text("View a list of all available servers with the "))
                .append(Component.text("/server", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(" command."))
                .build(),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.SWITCH_SERVER) {
            if (serverName.equals(event.getDetail("server_name", String.class, null))) {
                playerQuest.onGoalComplete();
            }
        }
    }
}
