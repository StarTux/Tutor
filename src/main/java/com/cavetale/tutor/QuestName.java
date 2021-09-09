package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goals;
import com.winthier.perm.PlayerRank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public enum QuestName {
    BEGINNER(Type.TUTORIAL, Component.text("Welcome to Cavetale!"), "group.trusted"),
    ORIENTATION(Type.TUTORIAL, Component.text("Beginner Tour"), QuestName.BEGINNER),
    MONEY(Type.TUTORIAL, Component.text("All About Money"), QuestName.BEGINNER),
    MEMBER(Type.TUTORIAL, Component.text("The Road to Member"), QuestName.MONEY, QuestName.ORIENTATION);
    public static final List<String> KEY_LIST;

    public final Type type;
    public final String key;
    public final Component displayName;
    public final String autoStartPermission;
    public final Set<QuestName> dependencies;

    public enum Type {
        TUTORIAL("Tutorial"),
        QUEST("Quest");

        public final String upper;
        public final String lower;

        Type(final String upper) {
            this.upper = upper;
            this.lower = upper.toLowerCase();
        }
    }

    QuestName(final Type type, final Component displayName, final String autoStartPermission, final Set<QuestName> dependencies) {
        this.type = type;
        this.key = name().toLowerCase();
        this.displayName = displayName;
        this.autoStartPermission = autoStartPermission;
        this.dependencies = dependencies;
    }

    QuestName(final Type type, final Component displayName, final QuestName dep, final QuestName... deps) {
        this(type, displayName, (String) null, setOf(dep, deps));
    }

    QuestName(final Type type, final Component displayName, final String autoStartPermission) {
        this(type, displayName, autoStartPermission, Collections.emptySet());
    }

    static {
        List<String> keys = new ArrayList<>();
        for (QuestName it : QuestName.values()) {
            keys.add(it.key);
        }
        KEY_LIST = Collections.unmodifiableList(keys);
    }

    /**
     * Called for first-time quest completion.
     */
    public void deliverQuestReward(Player player) {
        switch (this) {
        case MEMBER:
            PlayerRank.MEMBER.promote(player.getUniqueId());
            break;
        default: break;
        }
    }

    private static HashSet<QuestName> setOf(final QuestName dep, final QuestName... deps) {
        HashSet<QuestName> result = new HashSet<>();
        result.add(dep);
        for (var dep1 : deps) {
            result.add(dep1);
        }
        return result;
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

    public boolean isQuittable() {
        return this != QuestName.BEGINNER;
    }
}
