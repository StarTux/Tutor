package com.cavetale.tutor;

import com.cavetale.tutor.goal.WildGoal;
import java.util.Arrays;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    FRIENDLY,
    BEGINNER;

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
        case FRIENDLY:
            return new Quest(this, Component.text("Friendly"),
                             Arrays.asList(new WildGoal()));
        case BEGINNER:
            return new Quest(this, Component.text("Beginner"),
                             Arrays.asList(new WildGoal()));
        default:
            throw new IllegalStateException("Quest not defined: " + this);
        }
    }
}
