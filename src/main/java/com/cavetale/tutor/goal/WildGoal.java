package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public final class WildGoal implements Goal {
    @Getter protected final List<Condition> conditions;
    private final Condition condTypeWild;
    private final Condition condClaimNew;
    private final Condition condSkip;
    private final Condition condSkipShare;

    public WildGoal() {
        condTypeWild = new CheckboxCondition(Component.text("Type /wild"), playerQuest -> getProgress(playerQuest).wild);
        condClaimNew = new CheckboxCondition(Component.text("Make a claim").hoverEvent(Component.text("Type /claim new")),
                                             pg -> false);
        condSkip = new ClickableCondition(Component.text("I already have a claim!"),
                                          "IHaveAClaim", WildGoal::onSkip);
        condSkipShare = new ClickableCondition(Component.text("I live with a friend!"),
                                               "ILiveWithAFriend", WildGoal::onSkipShare);
        conditions = Arrays.asList(condTypeWild,
                                   condClaimNew,
                                   condSkip,
                                   condSkipShare);
    }

    @Override
    public String getId() {
        return "wild";
    }

    @Override
    public WildProgress newProgress() {
        return new WildProgress();
    }

    @Override
    public WildProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(WildProgress.class, WildProgress::new);
    }

    public static class WildProgress extends GoalProgress {
        boolean wild = false;

        public static WildProgress deserialize(String json) {
            return GoalProgress.deserialize(json, WildProgress.class, WildProgress::new);
        }
    }

    public void onUseWild(PlayerQuest playerQuest) {
        WildProgress wildProgress = getProgress(playerQuest);
        if (wildProgress.wild) return;
        wildProgress.wild = true;
        playerQuest.save();
    }

    static void onSkip(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        Integer claimCount = PluginPlayerQuery.Name.CLAIM_COUNT.call(playerQuest.getPlugin(), player);
        if (claimCount == null || claimCount < 1) {
            player.sendMessage(Component.text("You don't have a claim!", TextColor.color(0xFF0000)));
        } else {
            playerQuest.onGoalComplete();
        }
    }

    static void onSkipShare(PlayerQuest playerQuest) {
        Player player = playerQuest.getPlayer();
        Boolean insideTrustedClaim = PluginPlayerQuery.Name.INSIDE_TRUSTED_CLAIM.call(playerQuest.getPlugin(), player);
        if (insideTrustedClaim == null || !insideTrustedClaim) {
            player.sendMessage(Component.text("Please stand in your shared claim", TextColor.color(0xFF0000)));
        } else {
            playerQuest.onGoalComplete();
        }
    }
}
