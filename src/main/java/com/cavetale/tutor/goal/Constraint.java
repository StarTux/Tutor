package com.cavetale.tutor.goal;

import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public interface Constraint {
    TextColor RED = TextColor.color(0xFF0000);
    boolean doesMeet(PlayerQuest playerQuest);

    Component getMissedMessage(PlayerQuest playerQuest, Background background);

    interface Simple extends Constraint {
        default Component getMissedMessage() {
            return Component.text("You cannot do this right now!");
        }

        @Override
        default Component getMissedMessage(PlayerQuest playerQuest, Background background) {
            return getMissedMessage();
        }
    }
}
