package com.cavetale.tutor.daily;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * All daily quest types are known here.
 * Tasks for indexes:
 * - 0) Peaceful everyday
 * - 1) Dangerous or combat
 * - 2) Community task
 */
@RequiredArgsConstructor
public enum DailyQuestType {
    DUMMY(DailyQuestDummy::new, List.of(-1)),
    MINING(DailyQuestMining::new, List.of(0)),
    HARVEST(DailyQuestHarvest::new, List.of(0)),
    FISHING(DailyQuestFishing::new, List.of(0)),
    TREE_CHOPPER(DailyQuestTreeChopper::new, List.of(0)),
    SHEAR_SHEEP(DailyQuestShearSheep::new, List.of(0)),
    ;

    protected final String key = name().toLowerCase();
    protected final Supplier<? extends DailyQuest> ctor;
    protected final List<Integer> indexes;

    public static List<DailyQuestType> getAllWithIndex(int index) {
        List<DailyQuestType> list = new ArrayList<>();
        for (var it : values()) {
            if (it.indexes.contains(index)) {
                list.add(it);
            }
        }
        return list;
    }

    public DailyQuest<?, ?> create() {
        return ctor.get();
    }

    public static DailyQuestType ofKey(String k) {
        for (DailyQuestType it : values()) {
            if (k.equals(it.key)) return it;
        }
        return null;
    }
}
