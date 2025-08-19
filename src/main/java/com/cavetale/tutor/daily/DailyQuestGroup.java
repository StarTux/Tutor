package com.cavetale.tutor.daily;

import lombok.RequiredArgsConstructor;

/**
 * Each entry corresponds with the dailyIndex in the SQLDailyQuest.
 *
 * The value coincides with the ordinal, except for DISABLED.
 */
@RequiredArgsConstructor
public enum DailyQuestGroup {
    /**
     * Peaceful everyday.
     */
    PEACEFUL(0),
    /**
     * Dangerous or combat.
     */
    ADVENTURE(1),
    /**
     * Community task.
     */
    COMMUNITY(2),
    /**
     * Disabled.
     */
    DISABLED(-1),
    ;

    private final int value;

    public int value() {
        return value;
    }

    public static DailyQuestGroup ofValue(final int theValue) {
        for (var it : values()) {
            if (it.value == theValue) return it;
        }
        return DISABLED;
    }

    public boolean isDisabled() {
        return value < 0;
    }
}
