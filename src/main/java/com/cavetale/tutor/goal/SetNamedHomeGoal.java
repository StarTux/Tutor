package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SetNamedHomeGoal extends AbstractGoal<SetNamedHomeProgress> {
    @Getter protected final String id = "set_named_home";
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condSetHome;
    protected final CheckboxCondition condHome;
    protected final CheckboxCondition condList;

    public SetNamedHomeGoal() {
        super(SetNamedHomeProgress.class, SetNamedHomeProgress::new);
        this.displayName = Component.text("Named Homes");
        condSetHome = new CheckboxCondition(Component.text("Set a Named Home"),
                                            playerQuest -> getProgress(playerQuest).sethome,
                                            playerQuest -> getProgress(playerQuest).sethome = true);
        condHome = new CheckboxCondition(Component.text("Use your Named Home"),
                                         playerQuest -> getProgress(playerQuest).home,
                                         playerQuest -> getProgress(playerQuest).home = true);
        condList = new CheckboxCondition(Component.text("List your Homes"),
                                         playerQuest -> getProgress(playerQuest).list,
                                         playerQuest -> getProgress(playerQuest).list = true);
        condSetHome.setBookPageIndex(0);
        condHome.setBookPageIndex(1);
        condList.setBookPageIndex(2);
        this.conditions = List.of(new Condition[] {
                condSetHome,
                condHome,
                condList,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("You can set as many homes as you like."
                                       + " Each needs a "),
                        Component.text("unique name", NamedTextColor.BLUE),
                        Component.text(" and they need proper "),
                        Component.text("distance", NamedTextColor.BLUE),
                        Component.text(" to one another."
                                       + "\n\nCommand:\n"),
                        Component.text("/sethome <name>", NamedTextColor.BLUE),
                        Component.text("\nSet a named home", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("Using a named home works analogously:\n\n"),
                        Component.text("/home <name>", NamedTextColor.BLUE),
                        Component.text("\nVisit your named home", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("It's easy to lose track of all your homes."
                                       + " Luckily there's a list:\n\n"),
                        Component.text("/homes list", NamedTextColor.BLUE),
                        Component.text("\nor\n"),
                        Component.text("/listhomes", NamedTextColor.BLUE),
                        Component.text("\nList all your homes. Click any of them to port there",
                                       NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("If you no longer need a home, you can delete it:\n\n"),
                        Component.text("/homes delete <name>", NamedTextColor.BLUE),
                        Component.text("\nDelete a named home", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("On Cavetale, you can"),
                                        Component.text("set as many homes as"),
                                        Component.text("you like."));
                    pet.addSpeechBubble(id, 0L, 120L,
                                        Component.text("Give each its"),
                                        Component.text("unique name and"),
                                        Component.text("mind proper"),
                                        Component.text("distance."));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        if (event.getName() == PluginPlayerEvent.Name.SET_NAMED_HOME) {
            condSetHome.progress(playerQuest);
        } else if (event.getName() == PluginPlayerEvent.Name.USE_NAMED_HOME) {
            condHome.progress(playerQuest);
        } else if (event.getName() == PluginPlayerEvent.Name.LIST_HOMES) {
            condList.progress(playerQuest);
        }
    }
}

final class SetNamedHomeProgress extends GoalProgress {
    protected boolean sethome;
    protected boolean home;
    protected boolean list;

    @Override
    public boolean isComplete() {
        return sethome && home && list;
    }
}
