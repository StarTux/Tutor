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
    DUMMY(0, DailyQuestDummy::new, List.of()),
    // 0 Peaceful
    MINING(1, DailyQuestMining::new, Group.PEACEFUL),
    HARVEST(1, DailyQuestHarvest::new, Group.PEACEFUL),
    FISHING(1, DailyQuestFishing::new, Group.PEACEFUL),
    TREE_CHOPPER(1, DailyQuestTreeChopper::new, Group.PEACEFUL),
    SHEAR_SHEEP(1, DailyQuestShearSheep::new, Group.PEACEFUL),
    CRAFTING(1, DailyQuestCrafting::new, Group.PEACEFUL),
    EATING(1, DailyQuestEating::new, Group.PEACEFUL),
    BREEDING(1, DailyQuestBreeding::new, Group.PEACEFUL),
    // Pick Flowers

    // 1 Adventure
    KILL_MONSTER(1, DailyQuestKillMonster::new, Group.ADVENTURE),
    FIND_DUNGEON(1, DailyQuestFindDungeon::new, Group.ADVENTURE),
    FORAGING(1, DailyQuestForaging::new, Group.TESTING),
    // Capture Monster
    // Mob Arena

    // 2 Community
    MINIGAME_MATCH(6, DailyQuestMinigameMatch::new, Group.COMMUNITY),
    FRIENDSHIP_GIFT(1, DailyQuestFriendshipGift:: new, Group.COMMUNITY),
    ;

    protected final int weight; // Correspond with internal options
    protected final String key = name().toLowerCase();
    protected final Supplier<? extends DailyQuest> ctor;
    protected final List<Integer> indexes;

    /**
     * Get all quest types with the given index.  The result will be
     * weighted!
     */
    public static List<DailyQuestType> getAllWithIndex(int index) {
        List<DailyQuestType> list = new ArrayList<>();
        for (var it : values()) {
            if (it.indexes.contains(index)) {
                for (int i = 0; i < it.weight; i += 1) {
                    list.add(it);
                }
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
