package com.cavetale.tutor.goal;

import com.cavetale.tutor.session.PlayerQuest;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public final class MiningWorldConstraint implements Constraint.Simple {
    @Getter
    private final Component missedMessage = Component.text("Must be in mining world!", RED);

    @Override
    public boolean doesMeet(PlayerQuest playerQuest) {
        String worldName = playerQuest.getPlayer().getWorld().getName();
        return worldName.equals("mine") || worldName.startsWith("mine_");
    }
}
