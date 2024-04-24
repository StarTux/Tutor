package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class WarpGoal extends AbstractGoal<WarpProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;

    protected final CheckboxCondition condSpawn;
    protected final CheckboxCondition condRepairman;

    protected final CheckboxCondition condListWarps;

    public WarpGoal() {
        super(WarpProgress.class, WarpProgress::new);
        this.id = "warp";
        this.displayName = Component.text("Getting Around");
        condSpawn = new CheckboxCondition(Component.text("Visit spawn"),
                                          playerQuest -> getProgress(playerQuest).spawn,
                                          playerQuest -> getProgress(playerQuest).spawn = true);
        condRepairman = new CheckboxCondition(Component.text("Find the Repairman"),
                                              playerQuest -> getProgress(playerQuest).repairman,
                                              playerQuest -> getProgress(playerQuest).repairman = true);
        condListWarps = new CheckboxCondition(Component.text("List warps"),
                                              playerQuest -> getProgress(playerQuest).listWarps,
                                              playerQuest -> getProgress(playerQuest).listWarps = true);
        condSpawn.setBookPageIndex(0);
        condRepairman.setBookPageIndex(1);
        condListWarps.setBookPageIndex(2);
        this.conditions = List.of(new Condition[] {
                condSpawn,
                condRepairman,
                condListWarps,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Get back to the place you started any time."
                                       + " There are portals, merchants, and secrets to be discovered."
                                       + "\n\nCommand:\n"),
                        Component.text("/spawn", NamedTextColor.BLUE),
                        Component.text("\nTeleport to spawn", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("There are many villagers at spawn."
                                       + " One of them can "),
                        Component.text("repair your gear", NamedTextColor.BLUE),
                        Component.text(" in exchange for diamonds."
                                       + "\n\nThis villager is an expert and can repair anything."
                                       + " Let's find out where to find them,"
                                       + " for future reference."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("Warps can take you to key locations on the server."
                                       + " They are public places set up by our staff."
                                       + "\n\nCommand:\n"),
                        Component.text("/warp", NamedTextColor.BLUE),
                        Component.text("\nView the warp list. Click to warp", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 150L,
                                        Component.text("Cavetale has lots"),
                                        Component.text("of colorful places"),
                                        Component.text("to offer."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("Let's look around"),
                                        Component.text("a little, " + pet.getType().speechGimmick + "..."));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_SPAWN:
            condSpawn.progress(playerQuest);
            break;
        case INTERACT_NPC:
            if (Detail.NAME.is(event, "Repairman")) {
                condRepairman.progress(playerQuest);
            }
            break;
        case LIST_WARPS:
            condListWarps.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class WarpProgress extends GoalProgress {
    protected boolean spawn;
    protected boolean repairman;
    protected boolean listWarps;

    @Override
    public boolean isComplete() {
        return spawn && repairman && listWarps;
    }
}
