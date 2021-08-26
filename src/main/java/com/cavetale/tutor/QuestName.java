package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goals;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    BEGINNER(Component.text("Beginner"), "group.trusted"),
    TEST(Component.text("Test!!!"));

    public final String key;
    public final Component displayName;
    public final String autoStartPermission;

    QuestName(final Component displayName, final String autoStartPermission) {
        this.key = name().toLowerCase();
        this.displayName = displayName;
        this.autoStartPermission = autoStartPermission;
    }

    QuestName(final Component displayName) {
        this(displayName, null);
    }

    public static QuestName of(@NonNull String key) {
        for (QuestName questName : QuestName.values()) {
            if (key.equals(questName.key)) return questName;
        }
        return null;
    }

    protected Quest create() {
        return new Quest(this, displayName, Goals.create(this));
    }
}
