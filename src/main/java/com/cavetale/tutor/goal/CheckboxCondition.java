package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

/**
 * Condition classes have an inherent look but no builtin
 * action. Actions are supplied by the Goal implementation.
 */
@RequiredArgsConstructor
public final class CheckboxCondition implements Condition {
    @Getter protected final Component description;
    protected final Function<PlayerQuest, Boolean> checkedGetter;
    protected final Function<PlayerQuest, Boolean> visibleGetter;

    public CheckboxCondition(final Component description, final Function<PlayerQuest, Boolean> checkedGetter) {
        this(description, checkedGetter, null);
    }

    @Override
    public Component toComponent(PlayerQuest playerQuest, Background background) {
        boolean checked = checkedGetter.apply(playerQuest);
        return Component.text()
            .append(checked
                    ? Component.text(Unicode.CHECKED_CHECKBOX.character, background.green)
                    : Component.text(Unicode.CHECKBOX.character, background.red))
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
}
