package com.cavetale.tutor;

import com.cavetale.tutor.goal.*;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    BEGINNER(Component.text("Beginner"), "group.trusted");

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
        return new Quest(this, displayName, createGoals());
    }

    private List<Goal> createGoals() {
        switch (this) {
        case BEGINNER: return Arrays.asList(new Goal[] {
                new ChoosePetGoal(),
                new WildGoal(),
                new SetHomeGoal(),
                new SpawnGoal(),
                new ServerSwitchGoal(),
                new LocalChatGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + this);
        }
    }
}
