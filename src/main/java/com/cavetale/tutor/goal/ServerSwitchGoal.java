package com.cavetale.tutor.goal;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class ServerSwitchGoal extends AbstractGoal<ServerSwitchProgress> implements Listener {
    @Getter private final String id;
    @Getter private final Component displayName;
    @Getter private final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condList;
    protected final CheckboxCondition condHub;
    protected final CheckboxCondition condCreative;
    protected final CheckboxCondition condCavetale;

    public ServerSwitchGoal() {
        super(ServerSwitchProgress.class, ServerSwitchProgress::new);
        this.id = "server_switch";
        condList = new CheckboxCondition(Component.text("View Server List"),
                                        playerQuest -> getProgress(playerQuest).list,
                                        playerQuest -> getProgress(playerQuest).list = true);
        condHub = new CheckboxCondition(Component.text("Visit the Hub"),
                                        playerQuest -> getProgress(playerQuest).hub,
                                        playerQuest -> getProgress(playerQuest).hub = true,
                                        playerQuest -> getProgress(playerQuest).list);
        condCreative = new CheckboxCondition(Component.text("Visit Creative"),
                                             playerQuest -> getProgress(playerQuest).creative,
                                             playerQuest -> getProgress(playerQuest).creative = true,
                                             playerQuest -> getProgress(playerQuest).list);
        condCavetale = new CheckboxCondition(Component.text("Return to Cavetale"),
                                             playerQuest -> getProgress(playerQuest).cavetale,
                                             playerQuest -> getProgress(playerQuest).cavetale = true,
                                             playerQuest -> getProgress(playerQuest).readyForCavetale());
        condList.setBookPageIndex(0);
        condHub.setBookPageIndex(1);
        condCreative.setBookPageIndex(1);
        condCavetale.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condList,
                condHub,
                condCreative,
                condCavetale,
            });
        this.constraints = List.of();
        this.displayName = Component.text("Server switching");
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("Cavetale is the main server, but we offer several others"
                                       + " with various gamemodes on them."
                                       + " There is also the hub, creative, and many mini games."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/server", NamedTextColor.BLUE),
                        Component.text("\nList all servers", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("Server Commands:\n"),
                        Component.text("/hub", NamedTextColor.BLUE),
                        Component.text("\nThe lobby between our servers\n\n", NamedTextColor.GRAY),
                        Component.text("/creative", NamedTextColor.BLUE),
                        Component.text("\nOur creative mode server\n\n", NamedTextColor.GRAY),
                        Component.text("/cavetale", NamedTextColor.BLUE),
                        Component.text("\nThe main server", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onBegin(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(id, 50L, 100L,
                                    Component.text("You know the main"),
                                    Component.text("survival server,"),
                                    Component.text("but there are more."));
                pet.addSpeechBubble(id, 0L, 100L,
                                    Component.text("Just like warps, you"),
                                    Component.text("should know how to"),
                                    Component.text("get to each one."));
                pet.addSpeechBubble(id, 0L, 100L,
                                    Component.text("Remember this command:"),
                                    Component.text("/server", NamedTextColor.YELLOW));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case VIEW_SERVER_LIST:
            condList.progress(playerQuest);
            break;
        case SWITCH_SERVER:
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
            break;
        default: break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerJoin(PlayerJoinEvent event) {
        switch (NetworkServer.current()) {
        case HUB:
            TutorPlugin.getInstance().getSessions().applyGoals(event.getPlayer(), (playerQuest, goal) -> {
                    if (goal == this) condHub.progress(playerQuest);
                });
            break;
        case CREATIVE:
            TutorPlugin.getInstance().getSessions().applyGoals(event.getPlayer(), (playerQuest, goal) -> {
                    if (goal == this) condCreative.progress(playerQuest);
                });
            break;
        case CAVETALE:
            TutorPlugin.getInstance().getSessions().applyGoals(event.getPlayer(), (playerQuest, goal) -> {
                    if (goal == this) condCavetale.progress(playerQuest);
                });
            break;
        default: break;
        }
    }
}

final class ServerSwitchProgress extends GoalProgress {
    protected boolean list;
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
