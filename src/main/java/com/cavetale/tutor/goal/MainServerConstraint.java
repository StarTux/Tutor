package com.cavetale.tutor.goal;

import com.cavetale.tutor.session.PlayerQuest;
import com.winthier.connect.Connect;
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
        return Connect.getInstance().getServerName().equals("cavetale");
    }

    public static MainServerConstraint instance() {
        return INSTANCE;
    }
}
