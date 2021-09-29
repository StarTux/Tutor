package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Console;
import com.winthier.chat.ChatPlugin;
import com.winthier.perm.rank.PlayerRank;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class SpeleologistAcceptGoal extends AbstractGoal<SpeleologistAcceptProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final ClickableCondition condRankUp;

    public SpeleologistAcceptGoal() {
        super(SpeleologistAcceptProgress.class, SpeleologistAcceptProgress::new);
        this.id = "speleologist_accept";
        this.displayName = Component.text("Welcome to Speleologist");
        condRankUp = new ClickableCondition(Component.text("Promote me to Speleologist!"), "RankUp", this::clickRankUp);
        this.conditions = List.of(new Condition[] {
                condRankUp,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("The "),
                        Component.text("Speleologist", NamedTextColor.BLUE),
                        Component.text(" rank comes with many new perks:"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Buy a Market Plot"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Start Auctions"),
                    }),
            });
    }

    private void clickRankUp(PlayerQuest playerQuest) {
        SpeleologistAcceptProgress progress = getProgress(playerQuest);
        if (progress.rankUp) return;
        if (!playerQuest.getSession().getCompletedQuests().containsKey(QuestName.SPELEOLOGIST)) {
            UUID uuid = playerQuest.getSession().getUuid();
            String name = playerQuest.getSession().getName();
            if (PlayerRank.SPELEOLOGIST.promote(uuid)) {
                Console.command("titles set " + name + " Speleologist");
            }
            Component announcement = Component.text(name + " Finished the Speleologist Tutorial!",
                                                    NamedTextColor.GREEN, TextDecoration.BOLD);
            ChatPlugin.getInstance().announce("info", announcement);
        }
        progress.rankUp = true;
        playerQuest.onProgress();
    }
}

final class SpeleologistAcceptProgress extends GoalProgress {
    protected boolean rankUp;

    @Override
    public boolean isComplete() {
        return rankUp;
    }
}
