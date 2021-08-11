package com.cavetale.tutor;

import com.cavetale.tutor.goal.WildGoal;
import com.cavetale.tutor.goal.SetHomeGoal;
import java.util.Arrays;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    MEMBER;

    public final String key;

    QuestName() {
        this.key = name().toLowerCase();
    }

    public static QuestName of(@NonNull String key) {
        for (QuestName questName : QuestName.values()) {
            if (key.equals(questName.key)) return questName;
        }
        return null;
    }

    public Quest create() {
        switch (this) {
        case MEMBER:
            return new Quest(this, Component.text("The Path to Member"),
                             Arrays.asList(new WildGoal(),
                                           new SetHomeGoal()));
        default:
            throw new IllegalStateException("Quest not defined: " + this);
        }
    }
}
