package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

public enum QuestName {
    BEGINNER(Component.text("Welcome to Cavetale!"), "group.trusted"),
    ORIENTATION(Component.text("Beginner Tour"), QuestName.BEGINNER),
    MONEY(Component.text("All About Money"), QuestName.BEGINNER),
    MEMBER(Component.text("The Road to Member"), QuestName.MONEY, QuestName.ORIENTATION);
    public static final List<String> KEY_LIST;

    public final String key;
    public final Component displayName;
    public final String autoStartPermission;
    public final Set<QuestName> dependencies;

    QuestName(final Component displayName, final String autoStartPermission, final Set<QuestName> dependencies) {
        this.key = name().toLowerCase();
        this.displayName = displayName;
        this.autoStartPermission = autoStartPermission;
        this.dependencies = dependencies;
    }

    static {
        List<String> keys = new ArrayList<>();
        for (QuestName it : QuestName.values()) {
            keys.add(it.key);
        }
        KEY_LIST = Collections.unmodifiableList(keys);
    }

    private static HashSet<QuestName> setOf(final QuestName dep, final QuestName... deps) {
        HashSet<QuestName> result = new HashSet<>();
        result.add(dep);
        for (var dep1 : deps) {
            result.add(dep1);
        }
        return result;
    }

    QuestName(final Component displayName, final QuestName dep, final QuestName... deps) {
        this(displayName, (String) null, setOf(dep, deps));
    }

    QuestName(final Component displayName, final String autoStartPermission) {
        this(displayName, autoStartPermission, Collections.emptySet());
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
