package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MenuGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;

    public MenuGoal() {
        this.id = "menu";
        this.displayName = Component.text("More Commands");
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("View the Menu"),
                                      playerQuest -> false),
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                Component.text("There are many more plugins and commands on Cavetale."
                               + " Explore more of them at your pace."
                               + " We prepared a menu to help you with that."
                               + "\n\nCommand:\n"),
                Component.text("/menu", NamedTextColor.DARK_BLUE),
                Component.text("\nView the menu", NamedTextColor.GRAY),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100L, Component.text("There's so much more..."));
                pet.addSpeechBubble(100L,
                                    Component.text("It's best you explore a"),
                                    Component.text("little at your own pace."));
            });
    }

    @Override
    public void onComplete(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100L, Component.text("Have a lot of fun, " + pet.getType().speechGimmick + "!"));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.OPEN_MENU) {
            playerQuest.onGoalComplete();
        }
    }
}
