package com.cavetale.tutor.goal;

import com.cavetale.tutor.session.PlayerQuest;
import com.winthier.connect.Connect;
import lombok.Value;
import net.kyori.adventure.text.Component;

@Value
public final class ServerNameConstraint implements Constraint.Simple {
    private static final ServerNameConstraint RAID = new ServerNameConstraint("raid");
    private final String name;
    private final Component missedMessage;

    public ServerNameConstraint(final String name) {
        this.name = name;
        this.missedMessage = Component.text("Must be on " + name + " server!", RED);
    }

    @Override
    public boolean doesMeet(PlayerQuest playerQuest) {
        return isTrue();
    }

    public boolean isTrue() {
        return Connect.getInstance().getServerName().equals(name);
    }

    public static ServerNameConstraint raid() {
        return RAID;
    }
}
