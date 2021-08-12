package com.cavetale.tutor;

import com.cavetale.tutor.goal.*;
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
            return new Quest(this, Component.text("The Path to Member"), Arrays.asList(new Goal[] {
                        new WildGoal(),
                        new SetHomeGoal(),
                        new SpawnGoal(),
                        new HomeGoal(),
                    }));
        default:
            throw new IllegalStateException("Quest not defined: " + this);
        }
    }
}
