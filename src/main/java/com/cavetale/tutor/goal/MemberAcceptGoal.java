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

public final class MemberAcceptGoal extends AbstractGoal<MemberAcceptProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final ClickableCondition condRankUp;

    public MemberAcceptGoal() {
        super(MemberAcceptProgress.class, MemberAcceptProgress::new);
        this.id = "member_accept";
        this.displayName = Component.text("Welcome to Member");
        condRankUp = new ClickableCondition(Component.text("Promote me to Member!"), "RankUp", this::clickRankUp);
        this.conditions = List.of(new Condition[] {
                condRankUp,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("The "),
                        Component.text("Member", NamedTextColor.BLUE),
                        Component.text(" rank comes with many new perks:"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Daily Member Kit"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Use ArmorStandEditor"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Buy Creative Worlds"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Get Married"),
                        Component.text("\n" + Unicode.BULLET_POINT.character
                                       + " Use the Beta Server"),
                    }),
            });
    }

    private void clickRankUp(PlayerQuest playerQuest) {
        MemberAcceptProgress progress = getProgress(playerQuest);
        if (progress.rankUp) return;
        if (!playerQuest.getSession().getCompletedQuests().containsKey(QuestName.MEMBER)) {
            UUID uuid = playerQuest.getSession().getUuid();
            String name = playerQuest.getSession().getName();
            if (PlayerRank.MEMBER.promote(uuid)) {
                Console.command("titles set " + name + " Member");
            }
            Component announcement = Component.text(name + "Completed the Member Tutorial!",
                                                    NamedTextColor.GREEN, TextDecoration.BOLD);
            ChatPlugin.getInstance().announce("info", announcement);
        }
        progress.rankUp = true;
        playerQuest.onProgress();
    }
}

final class MemberAcceptProgress extends GoalProgress {
    protected boolean rankUp;

    @Override
    public boolean isComplete() {
        return rankUp;
    }
}
