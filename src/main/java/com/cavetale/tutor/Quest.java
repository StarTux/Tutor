package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@Getter @RequiredArgsConstructor
public final class Quest {
    protected final QuestName name;
    protected final Component displayName;
    protected final List<Goal> goals = new ArrayList<>();

    public Quest(@NonNull final QuestName questName, @NonNull final Component displayName, @NonNull final List<Goal> goals) {
        if (goals.isEmpty()) {
            throw new IllegalArgumentException("goals is empty!");
        }
        this.name = questName;
        this.displayName = displayName;
        this.goals.addAll(goals);
    }

    /**
     * Get index of named goal.
     * @return index of -1
     */
    public int goalIndex(String id) {
        for (int i = 0; i < goals.size(); i += 1) {
            if (goals.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    public Goal findGoal(@NonNull String id) {
        for (Goal goal : goals) {
            if (id.equals(goal.getId())) return goal;
        }
        return null;
    }
}
