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
    // 0 Peaceful
    MINING(DailyQuestMining::new, Group.PEACEFUL),
    HARVEST(DailyQuestHarvest::new, Group.PEACEFUL),
    FISHING(DailyQuestFishing::new, Group.PEACEFUL),
    TREE_CHOPPER(DailyQuestTreeChopper::new, Group.PEACEFUL),
    SHEAR_SHEEP(DailyQuestShearSheep::new, Group.PEACEFUL),
    // Breed Animals
    // Pick Flowers
    // Craft Cake, etc

    // 1 Adventure
    KILL_MONSTER(DailyQuestKillMonster::new, Group.ADVENTURE),
    FIND_DUNGEON(DailyQuestFindDungeon::new, Group.ADVENTURE),
    FORAGING(DailyQuestForaging::new, Group.TESTING),
    // Capture Monster
    // Mob Arena

    // 2 Community
    // Minigame
    // Friendship Gifts
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

    // Dodge the forward reference error.
    public static final class Group {
        public static final List<Integer> PEACEFUL = List.of(0);
        public static final List<Integer> ADVENTURE = List.of(1);
        public static final List<Integer> COMMUNITY = List.of(2);
        public static final List<Integer> TESTING = List.of(0, 1, 2);
    }
}
