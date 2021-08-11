package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public final class SetHomeGoal implements Goal {
    @Getter protected final String id = "sethome";
    @Getter protected final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;
    @Getter private final Component displayName;

    public SetHomeGoal() {
        displayName = Component.text("Set your home");
        conditions = Arrays
            .asList(new CheckboxCondition(Component.text("Set your home"), playerQuest -> false),
                    new ClickableCondition(Component.text("I already have a home"), "AlreadyHaveAHome", SetHomeGoal::onSkip));
        additionalBookPages = Arrays
            .asList(Component.text()
                    .append(Component.text("Set your primary home via "))
                    .append(Component.text("/sethome", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                    .append(Component.text("."))
                    .append(Component.space())
                    .append(Component.text("A home is a place you can visit any time via "))
                    .append(Component.text("/home", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                    .append(Component.text("."))
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("If you want, you can also set additional named homes."))
                    .build());
    }

    private static void onSkip(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        if (!PluginPlayerQuery.Name.PRIMARY_HOME_IS_SET.call(playerQuest.getPlugin(), player, false)) {
            player.sendMessage(Component.text("You don't have a primary home set!", TextColor.color(0xFF0000)));
            return;
        }
        playerQuest.onGoalComplete();
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.SET_PRIMARY_HOME) {
            playerQuest.onGoalComplete();
        }
    }
}
