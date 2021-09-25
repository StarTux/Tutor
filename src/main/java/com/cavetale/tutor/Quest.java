package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A POJO object holding descriptive information about a quest.
 */
@Getter @RequiredArgsConstructor
public final class Quest {
    public final QuestName name;
    public final List<Goal> goals = new ArrayList<>();

    public Quest(@NonNull final QuestName questName, @NonNull final List<Goal> goals) {
        if (goals.isEmpty()) {
            throw new IllegalArgumentException("goals is empty!");
        }
        Set<String> goalIds = new HashSet<>();
        for (Goal goal : goals) {
            if (goalIds.contains(goal.getId())) {
                throw new IllegalStateException(questName + ": Duplicate goal id: " + goal.getId());
            }
            goalIds.add(goal.getId());
        }
        this.name = questName;
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
