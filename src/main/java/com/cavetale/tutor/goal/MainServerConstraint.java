package com.cavetale.tutor.goal;

import com.cavetale.core.connect.ServerCategory;
import com.cavetale.tutor.session.PlayerQuest;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public final class MainServerConstraint implements Constraint.Simple {
    private static final MainServerConstraint INSTANCE = new MainServerConstraint();

    private MainServerConstraint() { }

    @Getter
    private final Component missedMessage = Component.text("Must be on main server!", RED);

    @Override
    public boolean doesMeet(PlayerQuest playerQuest) {
        return isTrue();
    }

    public static boolean isTrue() {
        return ServerCategory.current().isSurvival();
    }

    public static MainServerConstraint instance() {
        return INSTANCE;
    }
}
