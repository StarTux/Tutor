package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SpeleologistGoal extends AbstractGoal<SpeleologistProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condDefeatRaid;
    protected final CheckboxCondition condMakeFriend;

    public SpeleologistGoal() {
        super(SpeleologistProgress.class, SpeleologistProgress::new);
        this.id = "speleologist";
        this.displayName = Component.text("");
        condDefeatRaid = new CheckboxCondition(Component.text("Defeat a Raid"),
                                               playerQuest -> getProgress(playerQuest).defeatRaid,
                                               playerQuest -> getProgress(playerQuest).defeatRaid = true);
        condMakeFriend = new CheckboxCondition(Component.text("Make a Friend"),
                                               playerQuest -> getProgress(playerQuest).makeFriend,
                                               playerQuest -> getProgress(playerQuest).makeFriend = true);
        condDefeatRaid.setBookPageIndex(0);
        condMakeFriend.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condDefeatRaid,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Return to the Raid server to defeat any raid.\n\n"),
                        Component.text("/raid", NamedTextColor.BLUE),
                        Component.text("Join the raid server", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("At "),
                        Component.text("3" + Unicode.HEART.character, NamedTextColor.RED),
                        Component.text(", you can send a "),
                        Component.text("Friend", NamedTextColor.BLUE),
                        Component.text(" request."
                                       + "\n\nCommand:"),
                        Component.text("/friend <player>", NamedTextColor.BLUE),
                        Component.text("\nSend a friend request."
                                       + " They can accept by clicking"
                                       + " the message in chat.", NamedTextColor.GRAY),
                    }),
            });
    }

    protected void checkPassiveProgress(PlayerQuest playerQuest) {
        if (PluginPlayerQuery.Name.FRIEND_COUNT.call(playerQuest.getPlugin(), playerQuest.getPlayer(), 0) > 0) {
            condMakeFriend.progress(playerQuest);
        }
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        checkPassiveProgress(playerQuest);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case RAID_VICTORY:
            condDefeatRaid.progress(playerQuest);
            break;
        case MAKE_FRIEND:
            condMakeFriend.progress(playerQuest);
            break;
        case PLAYER_SESSION_LOADED:
            checkPassiveProgress(playerQuest);
            break;
        default: break;
        }
    }
}

final class SpeleologistProgress extends GoalProgress {
    protected boolean defeatRaid;
    protected boolean makeFriend;

    @Override
    public boolean isComplete() {
        return defeatRaid && makeFriend;
    }
}
