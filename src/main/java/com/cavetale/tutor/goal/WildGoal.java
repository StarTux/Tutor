package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
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
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condWild;
    protected final CheckboxCondition condClaim;
    protected final ClickableCondition condSkip;

    public WildGoal() {
        this.id = "wild";
        this.displayName = Component.text("Find a place to build");
        condWild = new CheckboxCondition(Component.text("Type /wild"),
                                         playerQuest -> getProgress(playerQuest).wild,
                                         playerQuest -> getProgress(playerQuest).wild = true);
        condClaim = new CheckboxCondition(Component.text("Make a claim"),
                                          playerQuest -> getProgress(playerQuest).claim,
                                          playerQuest -> getProgress(playerQuest).claim = true);
        condSkip = new ClickableCondition(Component.text("I live with a friend!"), "ILiveWithAFriend",
                                          this::skip,
                                          playerQuest -> !getProgress(playerQuest).isComplete());
        condWild.setBookPageIndex(0);
        condClaim.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condWild,
                condSkip,
                condClaim,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("You can type "),
                        Component.text("/wild", NamedTextColor.BLUE),
                        Component.text(" in order to find a nice place for you to start your base."
                                       + " This will teleport you to a random place in the main build world."
                                       + " You can repeat the command until you find a nice place."),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Once you have found a place you like, type "),
                        Component.text("/claim new", NamedTextColor.BLUE),
                        Component.text(" to claim the area as your own."
                                       + "You can grow the claim further out later on."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        WildProgress progress = getProgress(playerQuest);
        if (!progress.isComplete() && PluginPlayerQuery.Name.CLAIM_COUNT.call(playerQuest.getPlugin(), playerQuest.getPlayer(), 0) > 0) {
            progress.setComplete();
            playerQuest.onProgress();
        }
        if (!progress.isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("Let's find a place to"),
                                        TextComponent.ofChildren(Component.text("call our home! "),
                                                                 Mytems.SMILE.component));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("Then make a claim"),
                                        Component.text("there so nobody"),
                                        Component.text("else can build."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("You can share your"),
                                        Component.text("claim with friends,"),
                                        Component.text("of course."));
                });
        }
    }

    private void skip(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        if (!PluginPlayerQuery.Name.INSIDE_TRUSTED_CLAIM.call(playerQuest.getPlugin(), player, false)) {
            player.sendMessage(Component.text("Please stand in your shared claim", NamedTextColor.RED));
        } else {
            if (getProgress(playerQuest).setComplete()) {
                playerQuest.onProgress();
            }
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_WILD:
            condWild.progress(playerQuest);
            break;
        case USE_WILD_WITH_CLAIM:
            if (getProgress(playerQuest).setComplete()) {
                playerQuest.onProgress();
            }
            break;
        case CREATE_CLAIM:
            condClaim.progress(playerQuest);
            break;
        default: break;
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

        public boolean setComplete() {
            if (isComplete()) return false;
            wild = true;
            claim = true;
            return true;
        }
    }
}
