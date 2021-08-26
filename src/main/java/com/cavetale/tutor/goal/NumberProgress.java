package com.cavetale.tutor.goal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Progress with a score and a goal. Works in conjunction with
 * NumberCondition. While this extends GoalProgress, it will
 * oftentimes be part of a larger GoalProgress object.
 */
@Data @EqualsAndHashCode(callSuper = true)
public final class NumberProgress extends GoalProgress {
    protected int has;
    protected int goal;

    public NumberProgress() { }

    public NumberProgress(final int goal) {
        this.goal = goal;
    }

    public NumberProgress(final int has, final int goal) {
        this.has = has;
        this.goal = goal;
    }

    public static NumberProgress of(final int theHas, final int theGoal) {
        return new NumberProgress(theHas, theGoal);
    }

    public static NumberProgress deserialize(String json) {
        return deserialize(json, NumberProgress.class, NumberProgress::new);
    }

    @Override
    public boolean isComplete() {
        return has >= goal;
    }
}
