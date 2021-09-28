package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Condition with a score and a goal.
 * This must be constructed with a getter for the current progress,
 * and a setter for the new progress.
 */
@RequiredArgsConstructor
public final class NumberCondition implements Condition {
    @Getter protected final Component description;
    protected final int goal;
    @NonNull protected final Function<PlayerQuest, Integer> progressGetter;
    @NonNull protected final BiConsumer<PlayerQuest, Integer> progressSetter;
    protected final Function<PlayerQuest, Boolean> visibleGetter;
    @Getter @Setter protected int bookPageIndex = -1;

    NumberCondition(final Component description,
                    final int goal,
                    final Function<PlayerQuest, Integer> progressGetter,
                    final BiConsumer<PlayerQuest, Integer> progressSetter) {
        this(description, goal, progressGetter, progressSetter, null);
    }

    @Override
    public Component toComponent(PlayerQuest playerQuest, Background background) {
        final int has = progressGetter.apply(playerQuest);
        final boolean completed = has >= goal;
        return Component.text()
            .append(Component.text("(" + Unicode.superscript(has) + "/" + Unicode.subscript(goal) + ")",
                                   (completed ? background.green : NamedTextColor.BLUE)))
            .append(Component.space())
            .append(description)
            .build();
    }

    @Override
    public boolean isVisible(PlayerQuest playerQuest) {
        return visibleGetter != null
            ? visibleGetter.apply(playerQuest)
            : true;
    }

    public int getProgress(PlayerQuest playerQuest) {
        return progressGetter.apply(playerQuest);
    }

    public boolean isComplete(PlayerQuest playerQuest) {
        return getProgress(playerQuest) >= goal;
    }

    public boolean progress(PlayerQuest playerQuest, int amount) {
        if (amount < 1) return false;
        if (!isVisible(playerQuest)) return false;
        int has = getProgress(playerQuest);
        if (has >= goal) return false;
        int progress = Math.min(goal, has + amount);
        progressSetter.accept(playerQuest, progress);
        playerQuest.onProgress();
        return true;
    }

    public boolean progress(PlayerQuest playerQuest) {
        return progress(playerQuest, 1);
    }

    @Override
    public boolean hasBookPage() {
        return bookPageIndex >= 0;
    }

    @Override
    public boolean complete(PlayerQuest playerQuest) {
        return progress(playerQuest, goal - getProgress(playerQuest));
    }
}
