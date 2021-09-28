package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MenuGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condMenu;

    public MenuGoal() {
        this.id = "menu";
        this.displayName = Component.text("More Commands");
        condMenu = new CheckboxCondition(Component.text("View the Menu"),
                                         playerQuest -> getProgress(playerQuest).menu,
                                         playerQuest -> getProgress(playerQuest).menu = true);
        condMenu.setBookPageIndex(0);
        this.conditions = List.of(new Condition[] {
                condMenu,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("There are many more plugins and commands on Cavetale."
                                       + " Explore more of them at your pace."
                                       + " We prepared a menu to help you with that."
                                       + "\n\nCommand:\n"),
                        Component.text("/menu", NamedTextColor.BLUE),
                        Component.text("\nView the main menu", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("There's so much more..."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("It's best you explore a"),
                                        Component.text("little at your own pace."));
                });
        }
    }

    @Override
    public void onComplete(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble("", 0L, 100L, // empty tag!
                                    Component.text("Have a lot of fun, " + pet.getType().speechGimmick + "!"));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        if (event.getName() == PluginPlayerEvent.Name.OPEN_MENU) {
            condMenu.progress(playerQuest);
        }
    }

    @Override
    public MenuProgress newProgress() {
        return new MenuProgress();
    }

    @Override
    public MenuProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(MenuProgress.class, MenuProgress::new);
    }

    protected static final class MenuProgress extends GoalProgress {
        protected boolean menu;

        @Override
        public boolean isComplete() {
            return menu;
        }
    }
}
