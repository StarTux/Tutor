package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Condition classes have an inherent look but no builtin
 * action. Actions are supplied by the Goal implementation.
 */
@RequiredArgsConstructor
public final class CheckboxCondition implements Condition {
    @Getter protected final Component description;
    @NonNull protected final Function<PlayerQuest, Boolean> checkedGetter;
    @NonNull protected final Consumer<PlayerQuest> checkedSetter;
    protected final Function<PlayerQuest, Boolean> visibleGetter;
    @Getter @Setter protected int bookPageIndex = -1;

    public CheckboxCondition(final Component description,
                             final Function<PlayerQuest, Boolean> checkedGetter,
                             final Consumer<PlayerQuest> checkedSetter) {
        this(description, checkedGetter, checkedSetter, null);
    }

    @Override
    public Component toComponent(PlayerQuest playerQuest, Background background) {
        boolean checked = checkedGetter.apply(playerQuest);
        return Component.text()
            .append(checked
                    ? Component.text(Unicode.CHECKED_CHECKBOX.character, background.green)
                    : Component.text(Unicode.CHECKBOX.character, NamedTextColor.BLUE))
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

    public boolean isComplete(PlayerQuest playerQuest) {
        return checkedGetter.apply(playerQuest);
    }

    public void setComplete(PlayerQuest playerQuest) {
        checkedSetter.accept(playerQuest);
    }

    public boolean progress(PlayerQuest playerQuest) {
        if (isComplete(playerQuest)) return false;
        if (!isVisible(playerQuest)) return false;
        setComplete(playerQuest);
        playerQuest.onProgress();
        return true;
    }

    public boolean skip(PlayerQuest playerQuest) {
        if (isComplete(playerQuest)) return false;
        setComplete(playerQuest);
        playerQuest.onProgress();
        return true;
    }

    @Override
    public boolean hasBookPage() {
        return bookPageIndex >= 0;
    }

    @Override
    public boolean complete(PlayerQuest playerQuest) {
        return progress(playerQuest);
    }
}
