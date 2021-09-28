package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Learn about the mining world.
 * - Enter the command
 * - The rest was moved to MiningGoal
 */
public final class MineGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condMine;

    public MineGoal() {
        this.id = "mine";
        this.displayName = Component.text("The Mining World");
        condMine = new CheckboxCondition(Component.text("Warp to the mining world"),
                                         playerQuest -> getProgress(playerQuest).mine,
                                         playerQuest -> getProgress(playerQuest).mine = true);
        condMine.setBookPageIndex(0);
        this.conditions = List.of(condMine);
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("The mining world is there for you to get your resources from."
                                       + " Not only do you keep the home worlds pristine by using this to harvest:"
                                       + " There are also way more ores to be found here!"
                                       + " This world is reset once a week."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("Mining World Commands:"),
                        Component.text("\n/mine", NamedTextColor.BLUE),
                        Component.text("\nView biome list. Click for a warp", NamedTextColor.GRAY),
                        Component.newline(),
                        Component.text("\n/mine random", NamedTextColor.BLUE),
                        Component.text("\nWarp to a random biome", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100, new Component[] {
                            Component.text("Have you heard of"),
                            Component.text("the mining world?"),
                        });
                    pet.addSpeechBubble(id, 0L, 100, new Component[] {
                            Component.text("This is where we gather"),
                            Component.text("most of our resources."),
                        });
                    pet.addSpeechBubble(id, 0L, 100, new Component[] {
                            Component.text("The world resets"),
                            Component.text("weekly, so there's"),
                            Component.text("always more stuff."),
                        });
                });
        }
    }

    @Override
    public MineProgress newProgress() {
        return new MineProgress();
    }

    @Override
    public MineProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(MineProgress.class, MineProgress::new);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        if (event.getName() == PluginPlayerEvent.Name.USE_MINE) {
            condMine.progress(playerQuest);
        }
    }

    protected static final class MineProgress extends GoalProgress {
        protected boolean mine;

        @Override
        public boolean isComplete() {
            return mine;
        }
    }
}
