package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public final class WildGoal implements Goal {
    @Getter protected final String id;
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;

    public WildGoal() {
        this.id = "wild";
        this.displayName = Component.text("Find a place to build");
        Condition[] conds = new Condition[] {
            new CheckboxCondition(Component.text("Type /wild"),
                                  playerQuest -> getProgress(playerQuest).wild),
            new CheckboxCondition(Component.text("Make a claim").hoverEvent(Component.text("Create a claim")),
                                  playerQuest -> false),
            new ClickableCondition(Component.text("I already have a claim!"), "IHaveAClaim",
                                   WildGoal::onSkip),
            new ClickableCondition(Component.text("I live with a friend!"), "ILiveWithAFriend",
                                   WildGoal::onSkipShare),
        };
        Component[] pages = new Component[] {
            Component.text()
            .append(Component.text("You can type "))
            .append(Component.text("/wild", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
            .append(Component.text(" in order to find a nice place for you to start your base."))
            .append(Component.space())
            .append(Component.text("This will teleport you to a random place in the main build world."))
            .append(Component.space())
            .append(Component.text("You can repeat the command as often as you like.")).build(),
            Component.text()
            .append(Component.text("Once you have found a place you like, type "))
            .append(Component.text("/claim new", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
            .append(Component.text(" to claim the area as your own."))
            .append(Component.space())
            .append(Component.text("You can grow the claim further out later on.")).build(),
        };
        this.conditions = Arrays.asList(conds);
        this.additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100L,
                                    Component.text("Let's find a place to"),
                                    TextComponent.ofChildren(Component.text("call our home! "),
                                                             Mytems.SMILE.component));
                pet.addSpeechBubble(100L,
                                    Component.text("Then make a claim there"),
                                    Component.text("so nobody else can build."));
                pet.addSpeechBubble(100L,
                                    Component.text("You can share your claim"),
                                    Component.text("with friends, of course."));
            });
    }

    static void onSkip(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        if (PluginPlayerQuery.Name.CLAIM_COUNT.call(playerQuest.getPlugin(), player, 0) < 1) {
            player.sendMessage(Component.text("You don't have a claim!", NamedTextColor.RED));
        } else {
            playerQuest.onGoalComplete();
        }
    }

    static void onSkipShare(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        if (!PluginPlayerQuery.Name.INSIDE_TRUSTED_CLAIM.call(playerQuest.getPlugin(), player, false)) {
            player.sendMessage(Component.text("Please stand in your shared claim", NamedTextColor.RED));
        } else {
            playerQuest.onGoalComplete();
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.USE_WILD) {
            WildProgress wildProgress = getProgress(playerQuest);
            if (!wildProgress.wild) {
                wildProgress.wild = true;
                playerQuest.onProgress(wildProgress);
            }
        } else if (name == PluginPlayerEvent.Name.CREATE_CLAIM) {
            WildProgress wildProgress = getProgress(playerQuest);
            playerQuest.onGoalComplete();
        }
    }

    @Override
    public WildProgress newProgress() {
        return new WildProgress();
    }

    @Override
    public WildProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(WildProgress.class, WildProgress::new);
    }

    private static final class WildProgress extends GoalProgress {
        private boolean wild;
    }
}
