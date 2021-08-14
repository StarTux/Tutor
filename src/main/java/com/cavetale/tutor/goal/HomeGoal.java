package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class HomeGoal implements Goal {
    @Getter protected final String id;
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;

    public HomeGoal() {
        id = "home";
        displayName = Component.text("Use your home");
        Condition[] conds = new Condition[] {
            new CheckboxCondition(Component.text("Type /home"), playerQuest -> false),
        };
        Component[] pages = new Component[] {
            Component.text().append(Component.text("Returning to your primary home is easy. Just type "))
            .append(Component.text("/home", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
            .append(Component.text(".")).build(),
        };
        conditions = Arrays.asList(conds);
        additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.USE_PRIMARY_HOME) {
            playerQuest.onGoalComplete();
        }
    }
}
