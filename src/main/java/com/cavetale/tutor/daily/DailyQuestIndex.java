package com.cavetale.tutor.daily;

import lombok.Value;

/**
 * Quest type and index.
 */
@Value
public final class DailyQuestIndex {
    public final DailyQuestType type;
    public final String name;
}
