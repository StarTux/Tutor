package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ServerSwitchGoal implements Goal {
    @Getter private final String id;
    @Getter private final List<Condition> conditions;
    @Getter private final Component displayName;
    @Getter private final List<Component> additionalBookPages;

    public ServerSwitchGoal() {
        this.id = "server_switch";
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Visit the Hub"),
                                      playerQuest -> getProgress(playerQuest).done.contains(TargetServer.HUB)),
                new CheckboxCondition(Component.text("Visit Creative"),
                                      playerQuest -> getProgress(playerQuest).done.contains(TargetServer.CREATIVE)),
                new CheckboxCondition(Component.text("Return to Cavetale"),
                                      playerQuest -> getProgress(playerQuest).done.contains(TargetServer.CAVETALE)),
            });
        this.displayName = Component.text("Server switching");
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Cavetale is the main survival server, but we offer several others"
                                       + " with various gamemodes on them."
                                       + " There is also the hub, creative, and many mini games."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/server", NamedTextColor.DARK_BLUE),
                        Component.text("\nList all servers", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Server Commands:\n"),
                        Component.text("/hub", NamedTextColor.DARK_BLUE),
                        Component.text("\nWhere you spawn when other servers go down\n\n", NamedTextColor.GRAY),
                        Component.text("/creative", NamedTextColor.DARK_BLUE),
                        Component.text("\nBuild in creative mode\n\n", NamedTextColor.GRAY),
                        Component.text("/cavetale", NamedTextColor.DARK_BLUE),
                        Component.text("\nThe main survival server", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100L,
                                    Component.text("This is the main"),
                                    Component.text("survival server,"),
                                    Component.text("but there are more."));
                pet.addSpeechBubble(100L,
                                    Component.text("Just like warps, you"),
                                    Component.text("should know how to"),
                                    Component.text("get to each one."));
                pet.addSpeechBubble(100L,
                                    Component.text("Remember this command:"),
                                    Component.text("/server", NamedTextColor.YELLOW));
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
                targetServer = TargetServer.valueOf(Detail.NAME.get(event, "").toUpperCase());
            } catch (IllegalArgumentException iae) {
                return;
            }
            ServerSwitchProgress progress = getProgress(playerQuest);
            if (!progress.done.contains(targetServer)) {
                progress.done.add(targetServer);
                playerQuest.onProgress(progress);
            }
        }
    }

    protected enum TargetServer {
        HUB,
        CREATIVE,
        CAVETALE;
    }

    protected static final class ServerSwitchProgress extends GoalProgress {
        Set<TargetServer> done = EnumSet.noneOf(TargetServer.class);

        @Override
        public boolean isComplete() {
            return done.size() == TargetServer.values().length;
        }
    }
}
