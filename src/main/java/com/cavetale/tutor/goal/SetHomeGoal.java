package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class SetHomeGoal implements Goal {
    @Getter protected final String id = "sethome";
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condSetHome;
    protected final CheckboxCondition condHome;

    public SetHomeGoal() {
        this.displayName = Component.text("Set your home");
        condSetHome = new CheckboxCondition(Component.text("Set your Home"),
                                            playerQuest -> getProgress(playerQuest).sethome,
                                            playerQuest -> getProgress(playerQuest).sethome = true);
        condHome = new CheckboxCondition(Component.text("Use your Home"),
                                         playerQuest -> getProgress(playerQuest).home,
                                         playerQuest -> getProgress(playerQuest).home = true,
                                         playerQuest -> getProgress(playerQuest).sethome);
        condSetHome.setBookPageIndex(0);
        condHome.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condSetHome,
                condHome,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("A home is a place you can port to any time:\n\n"),
                        Component.text("/sethome", NamedTextColor.BLUE),
                        Component.text("\nSet your primary home\n\n", NamedTextColor.GRAY),
                        Component.text("You can change it any time with the same command."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("To visit it, use this command:\n\n"),
                        Component.text("/home", NamedTextColor.BLUE),
                        Component.text("\nVisit your primary home\n\n", NamedTextColor.GRAY),
                        Component.text("You can also set named homes,"
                                       + " which is explained in a later tutorial."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        SetHomeProgress progress = getProgress(playerQuest);
        if (!progress.sethome && PluginPlayerQuery.Name.PRIMARY_HOME_IS_SET.call(playerQuest.getPlugin(), playerQuest.getPlayer(), false)) {
            progress.sethome = true;
            playerQuest.onProgress();
        }
        if (!progress.isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("You can port to your"),
                                        Component.text("claim, but it's much"),
                                        Component.text("simpler to set a home!"));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("You have one default home"),
                                        Component.text("and any number of named homes."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("Remember these commands:"),
                                        Component.text("/sethome", NamedTextColor.YELLOW),
                                        Component.text("/home", NamedTextColor.YELLOW));
                    pet.addSpeechBubble(id, 0L, 100L, Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                Component.text("This will come up a lot "),
                                Mytems.WINK.component,
                            }));
                });
        }
    }

    private void onSkip(PlayerQuest playerQuest) {
        if (condSetHome.isVisible(playerQuest)) {
            Player player = playerQuest.getPlayer();
            if (!PluginPlayerQuery.Name.PRIMARY_HOME_IS_SET.call(playerQuest.getPlugin(), player, false)) {
                player.sendMessage(Component.text("You don't have a primary home set!", NamedTextColor.RED));
                return;
            }
            condSetHome.skip(playerQuest);
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        if (event.getName() == PluginPlayerEvent.Name.SET_PRIMARY_HOME) {
            condSetHome.progress(playerQuest);
        } else if (event.getName() == PluginPlayerEvent.Name.USE_PRIMARY_HOME) {
            condHome.progress(playerQuest);
        }
    }

    @Override
    public SetHomeProgress newProgress() {
        return new SetHomeProgress();
    }

    @Override
    public SetHomeProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(SetHomeProgress.class, SetHomeProgress::new);
    }

    public static final class SetHomeProgress extends GoalProgress {
        boolean sethome;
        boolean home;

        @Override
        public boolean isComplete() {
            return sethome && home;
        }
    }
}
