package com.cavetale.tutor.goal;

import com.cavetale.tutor.session.PlayerQuest;
import com.winthier.connect.Connect;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public final class MainServerConstraint implements Constraint.Simple {
    @Getter
    private final Component missedMessage = Component.text("Must be on main server!", RED);

    @Override
    public boolean doesMeet(PlayerQuest playerQuest) {
        return Connect.getInstance().getServerName().equals("cavetale");
    }
}
