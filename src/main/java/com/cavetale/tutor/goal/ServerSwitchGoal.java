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
    @Getter private final List<Component> additionalBookPages;

    public ServerSwitchGoal() {
        this.id = "server_switch";
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Visit the Hub"),
                                      playerQuest -> getProgress(playerQuest).stage > 0),
                new CheckboxCondition(Component.text("Visit Creative"),
                                      playerQuest -> getProgress(playerQuest).stage > 1),
                new CheckboxCondition(Component.text("Return to Cavetale"),
                                      playerQuest -> getProgress(playerQuest).stage > 2),
            });
        this.displayName = Component.text("Server switching");
        this.additionalBookPages = Arrays.asList(new Component[] {
                Component.text().content("Cavetale offers several servers, "
                                         + "with various gamemodes on them.")
                .append(Component.space())
                .append(Component.text("There is main, the hub, creative, and several mini games."))
                .append(Component.space())
                .append(Component.text("To visit each server, use the following commands:"))
                .append(Component.newline())
                .append(Component.text("/cavetale", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("/hun", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("/creative", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .build(),
                Component.text().content("View a list of all available servers with the ")
                .append(Component.text("/server", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(" command."))
                .build(),
            });
    }

    @Override
    public ServerSwitchProgress newProgress() {
        return new ServerSwitchProgress();
    }

    @Override
    public ServerSwitchProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(ServerSwitchProgress.class, ServerSwitchProgress::new);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.SWITCH_SERVER) {
            TargetServer targetServer;
            try {
                targetServer = TargetServer.valueOf(event.getDetail("server_name", String.class, null));
            } catch (IllegalArgumentException iae) {
                return;
            }
            ServerSwitchProgress progress = getProgress(playerQuest);
            if (progress.stage == targetServer.ordinal()) {
                progress.stage += 1;
                if (progress.stage > TargetServer.values().length) {
                    playerQuest.onGoalComplete();
                } else {
                    playerQuest.save();
                }
            }
        }
    }

    protected enum TargetServer {
        HUB,
        CREATIVE,
        CAVETALE;
    }

    protected static final class ServerSwitchProgress extends GoalProgress {
        protected int stage = 0;
    }
}
