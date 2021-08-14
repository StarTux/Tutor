package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class SpawnGoal implements Goal {
    @Getter protected final String id;
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;

    public SpawnGoal() {
        id = "spawn";
        displayName = Component.text("Go to spawn");
        Condition[] conds = new Condition[] {
            new CheckboxCondition(Component.text("Type /spawn"), playerQuest -> false),
        };
        Component[] pages = new Component[] {
            Component.text().append(Component.text("Get back to the place you started via "))
            .append(Component.text("/spawn", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
            .append(Component.text("."))
            .append(Component.newline())
            .append(Component.newline())
            .append(Component.text("Explore the server spawn together with other players."))
            .append(Component.space())
            .append(Component.text("There are many warps, merchants, and secrets to be discovered.")).build(),
        };
        conditions = Arrays.asList(conds);
        additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.USE_SPAWN) {
            playerQuest.onGoalComplete();
        }
    }
}
