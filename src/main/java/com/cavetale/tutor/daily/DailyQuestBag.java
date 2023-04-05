package com.cavetale.tutor.daily;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public final class DailyQuestBag {
    protected List<DailyQuestIndex> indexes = new ArrayList<>();
}
