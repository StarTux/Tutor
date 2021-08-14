package com.cavetale.tutor;

import com.cavetale.tutor.goal.*;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    MEMBER(Component.text("The Path to Member"));

    public final String key;
    public final Component displayName;

    QuestName(final Component displayName) {
        this.key = name().toLowerCase();
        this.displayName = displayName;
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
        case MEMBER: return Arrays.asList(new Goal[] {
                new WildGoal(),
                new SetHomeGoal(),
                new SpawnGoal(),
                new HomeGoal(),
                new ServerSwitchGoal("server_hub", "hub", Component.text("Visit the hub"),
                                     Component.text("Hub")),
                new ServerSwitchGoal("server_cavetale", "cavetale", Component.text("Back to main"),
                                     Component.text("Cavetale")),
                new LocalChatGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + this);
        }
    }
}
