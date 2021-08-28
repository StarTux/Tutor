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
import org.bukkit.entity.Player;

public final class WildGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condWild;
    protected final CheckboxCondition condClaim;
    protected final ClickableCondition condSkip;
    protected final ClickableCondition condSkipShare;

    public WildGoal() {
        this.id = "wild";
        this.displayName = Component.text("Find a place to build");
        condWild = new CheckboxCondition(Component.text("Type /wild"),
                                         playerQuest -> getProgress(playerQuest).wild,
                                         playerQuest -> getProgress(playerQuest).wild = true);
        condClaim = new CheckboxCondition(Component.text("Make a claim"),
                                          playerQuest -> getProgress(playerQuest).claim,
                                          playerQuest -> getProgress(playerQuest).claim = true);
        condSkip = new ClickableCondition(Component.text("I already have a claim!"), "IHaveAClaim",
                                          WildGoal::onSkip);
        condSkipShare = new ClickableCondition(Component.text("I live with a friend!"), "ILiveWithAFriend",
                                               WildGoal::onSkipShare);
        this.conditions = Arrays.asList(new Condition[] {
                condWild,
                condSkip,
                condSkipShare,
                condClaim,
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("You can type "),
                        Component.text("/wild", NamedTextColor.DARK_BLUE),
                        Component.text(" in order to find a nice place for you to start your base."),
                        Component.space(),
                        Component.text("This will teleport you to a random place in the main build world."),
                        Component.space(),
                        Component.text("You can repeat the command as often as you like."),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Once you have found a place you like, type "),
                        Component.text("/claim new", NamedTextColor.DARK_BLUE),
                        Component.text(" to claim the area as your own."),
                        Component.space(),
                        Component.text("You can grow the claim further out later on."),
                    }),
            });
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
            condWild.progress(playerQuest);
        } else if (name == PluginPlayerEvent.Name.CREATE_CLAIM) {
            condClaim.progress(playerQuest);
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

    protected static final class WildProgress extends GoalProgress {
        protected boolean wild;
        protected boolean claim;

        @Override
        public boolean isComplete() {
            return wild && claim;
        }
    }
}
