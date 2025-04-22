package com.cavetale.tutor.daily;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All daily quest types are known here.
 * Tasks for groups:
 * - 0) Peaceful everyday
 * - 1) Dangerous or combat
 * - 2) Community task
 */
@Getter
@RequiredArgsConstructor
public enum DailyQuestType {
    DUMMY(DailyQuestDummy::new, List.of(), () -> 0),
    // 0 Peaceful
    MINING(DailyQuestMining::new, Group.PEACEFUL, () -> DailyQuestMining.Ore.values().length),
    HARVEST(DailyQuestHarvest::new, Group.PEACEFUL, () -> DailyQuestHarvest.Crop.values().length),
    FISHING(DailyQuestFishing::new, Group.PEACEFUL, () -> DailyQuestFishing.Fish.values().length),
    TREE_CHOPPER(DailyQuestTreeChopper::new, Group.PEACEFUL, () -> DailyQuestTreeChopper.getAllTypes().size()), // disabled
    CHOP_TREES(DailyQuestChopTrees::new, Group.PEACEFUL, () -> DailyQuestChopTrees.Wood.values().length),
    SHEAR_SHEEP(DailyQuestShearSheep::new, Group.PEACEFUL, () -> DailyQuestShearSheep.getAllColors().length),
    CRAFTING(DailyQuestCrafting::new, Group.PEACEFUL, () -> DailyQuestCrafting.MATERIALS.length),
    EATING(DailyQuestEating::new, Group.PEACEFUL, () -> DailyQuestEating.FOODS.length),
    BREEDING(DailyQuestBreeding::new, Group.PEACEFUL, () -> DailyQuestBreeding.BreedMob.values().length),
    PICK_FLOWERS(DailyQuestPickFlowers::new, Group.PEACEFUL, () -> DailyQuestPickFlowers.Flower.values().length),

    // 1 Adventure
    KILL_MONSTER(DailyQuestKillMonster::new, Group.ADVENTURE, () -> DailyQuestKillMonster.TargetMob.values().length),
    FIND_DUNGEON(DailyQuestFindDungeon::new, Group.ADVENTURE, () -> 1),
    FORAGING(DailyQuestForaging::new, Group.ADVENTURE, () -> DailyQuestForaging.Forage.values().length),
    // Capture Monster

    // 2 Community
    MINIGAME_MATCH(DailyQuestMinigameMatch::new, Group.COMMUNITY, () -> DailyQuestMinigameMatch.Game.values().length),
    FRIENDSHIP_GIFT(DailyQuestFriendshipGift:: new, Group.COMMUNITY, () -> 7),
    MOB_ARENA_WAVE(DailyQuestMobArenaWave::new, Group.COMMUNITY, () -> 1), // disabled
    MOB_ARENA_WAVES(DailyQuestMobArenaWaves::new, Group.COMMUNITY, () -> 1),
    SKYBLOCK(DailyQuestSkyblock::new, Group.COMMUNITY, () -> DailyQuestSkyblock.Task.values().length),
    ;

    protected final String key = name().toLowerCase();
    protected final Supplier<? extends DailyQuest> ctor;
    protected final List<Integer> groups;
    protected final Supplier<Integer> optionCountGetter;
    private boolean disabled = false;

    static {
        TREE_CHOPPER.disabled = true;
        MOB_ARENA_WAVE.disabled = true;
    }

    /**
     * Get all quest types with the given group.  The result will be
     * weighted!
     */
    public static List<DailyQuestIndex> getAllWithGroup(int group) {
        List<DailyQuestIndex> list = new ArrayList<>();
        for (var it : values()) {
            if (it.disabled) continue;
            if (!it.groups.contains(group)) continue;
            int count = it.getOptionCount();
            for (int i = 0; i < count; i += 1) {
                list.add(new DailyQuestIndex(it, i));
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

    public int getOptionCount() {
        return optionCountGetter.get();
    }

    // Dodge the forward reference error.
    public static final class Group {
        public static final List<Integer> PEACEFUL = List.of(0);
        public static final List<Integer> ADVENTURE = List.of(1);
        public static final List<Integer> COMMUNITY = List.of(2);
        public static final List<Integer> TESTING = List.of(0, 1, 2);
    }
}
