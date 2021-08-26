package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

/**
 * Condition with a score and a goal. Works in conjunction with
 * NumberProgress.
 */
@RequiredArgsConstructor
public final class NumberCondition implements Condition {
    @Getter private final Component description;
    @NonNull protected final Function<PlayerQuest, NumberProgress> progressGetter;
    protected final Function<PlayerQuest, Boolean> visibleGetter;

    NumberCondition(final Component description, final Function<PlayerQuest, NumberProgress> progressGetter) {
        this(description, progressGetter, null);
    }

    @Override
    public Component toComponent(PlayerQuest playerQuest, Background background) {
        NumberProgress progress = progressGetter.apply(playerQuest);
        final int has = Math.min(progress.has, progress.goal);
        final int goal = progress.goal;
        final boolean completed = progress.isComplete();
        return goal == 1
            ? (Component.text()
               .append(completed
                       ? Component.text(Unicode.CHECKED_CHECKBOX.character)
                       : Component.text(Unicode.CHECKBOX.character))
               .append(Component.space())
               .append(description)
               .color(completed ? background.green : background.text)
               .build())
            : (Component.text()
               .append(Component.text("[" + has + "/" + goal + "]"))
               .append(Component.space())
               .append(description)
               .color(completed ? background.green : background.text)
               .build());
    }

    @Override
    public boolean isVisible(PlayerQuest playerQuest) {
        return visibleGetter != null
            ? visibleGetter.apply(playerQuest)
            : true;
    }
}
