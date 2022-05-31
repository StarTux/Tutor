package com.cavetale.tutor.goal;

import com.cavetale.core.connect.ServerCategory;
import com.cavetale.tutor.session.PlayerQuest;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public final class SurvivalServerConstraint implements Constraint.Simple {
    private static final SurvivalServerConstraint INSTANCE = new SurvivalServerConstraint();

    private SurvivalServerConstraint() { }

    @Getter
    private final Component missedMessage = Component.text("Must be on a survival server!", RED);

    @Override
    public boolean doesMeet(PlayerQuest playerQuest) {
        return isTrue();
    }

    public static boolean isTrue() {
        return ServerCategory.current().isSurvival();
    }

    public static SurvivalServerConstraint instance() {
        return INSTANCE;
    }
}
