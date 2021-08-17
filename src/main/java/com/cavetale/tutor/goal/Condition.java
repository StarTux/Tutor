package com.cavetale.tutor.goal;

import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import net.kyori.adventure.text.Component;

/**
 * Condition classes have an inherent look but no builtin
 * action. Actions are supplied by the Goal implementation.
 */
public interface Condition {
    Component getDescription();

    Component toComponent(PlayerQuest playerQuest, Background background);

    default boolean isVisible(PlayerQuest playerQuest) {
        return true;
    }

    default boolean isOnSidebar(PlayerQuest playerQuest) {
        return true;
    }
}
