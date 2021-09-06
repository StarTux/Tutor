package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ServerSwitchGoal implements Goal {
    @Getter private final String id;
    @Getter private final Component displayName;
    @Getter private final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condHub;
    protected final CheckboxCondition condCreative;
    protected final CheckboxCondition condCavetale;

    public ServerSwitchGoal() {
        this.id = "server_switch";
        condHub = new CheckboxCondition(Component.text("Visit the Hub"),
                                        playerQuest -> getProgress(playerQuest).hub,
                                        playerQuest -> getProgress(playerQuest).hub = true);
        condCreative = new CheckboxCondition(Component.text("Visit Creative"),
                                             playerQuest -> getProgress(playerQuest).creative,
                                             playerQuest -> getProgress(playerQuest).creative = true);
        condCavetale = new CheckboxCondition(Component.text("Return to Cavetale"),
                                             playerQuest -> getProgress(playerQuest).cavetale,
                                             playerQuest -> getProgress(playerQuest).cavetale = true,
                                             playerQuest -> getProgress(playerQuest).readyForCavetale());
        condHub.setBookPageIndex(0);
        condCreative.setBookPageIndex(0);
        condCavetale.setBookPageIndex(0);
        this.conditions = Arrays.asList(new Condition[] {
                condHub,
                condCreative,
                condCavetale,
            });
        this.constraints = Collections.emptyList();
        this.displayName = Component.text("Server switching");
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Cavetale is the main server, but we offer several others"
                                       + " with various gamemodes on them."
                                       + " There is also the hub, creative, and many mini games."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/server", NamedTextColor.DARK_BLUE),
                        Component.text("\nList all servers", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Server Commands:\n"),
                        Component.text("/hub", NamedTextColor.DARK_BLUE),
                        Component.text("\nThe lobby when you disconnect from other servers\n\n", NamedTextColor.GRAY),
                        Component.text("/creative", NamedTextColor.DARK_BLUE),
                        Component.text("\nOur creative mode server\n\n", NamedTextColor.GRAY),
                        Component.text("/cavetale", NamedTextColor.DARK_BLUE),
                        Component.text("\nThe main server", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onBegin(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(50L, 100L,
                                    Component.text("You know the main"),
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
            switch (Detail.NAME.get(event, "")) {
            case "hub":
                condHub.progress(playerQuest);
                break;
            case "creative":
                condCreative.progress(playerQuest);
                break;
            case "cavetale":
                condCavetale.progress(playerQuest);
                break;
            default: break;
            }
        }
    }

    protected static final class ServerSwitchProgress extends GoalProgress {
        protected boolean hub;
        protected boolean creative;
        protected boolean cavetale;

        @Override
        public boolean isComplete() {
            return hub
                && creative
                && cavetale;
        }

        protected boolean readyForCavetale() {
            return hub && creative;
        }
    }
}
