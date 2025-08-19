package com.cavetale.tutor.daily;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyQuestType {
    DUMMY(DailyQuestDummy::new, DailyQuestGroup.DISABLED, () -> 0),
    // 0 Peaceful
    MINING(DailyQuestMining::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestMining.Ore.values().length),
    HARVEST(DailyQuestHarvest::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestHarvest.Crop.values().length),
    FISHING(DailyQuestFishing::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestFishing.Fish.values().length),
    TREE_CHOPPER(DailyQuestTreeChopper::new, DailyQuestGroup.DISABLED, () -> DailyQuestTreeChopper.getAllTypes().size()),
    CHOP_TREES(DailyQuestChopTrees::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestChopTrees.Wood.values().length),
    SHEAR_SHEEP(DailyQuestShearSheep::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestShearSheep.getAllColors().length),
    CRAFTING(DailyQuestCrafting::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestCrafting.MATERIALS.length),
    EATING(DailyQuestEating::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestEating.FOODS.length),
    BREEDING(DailyQuestBreeding::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestBreeding.BreedMob.values().length),
    PICK_FLOWERS(DailyQuestPickFlowers::new, DailyQuestGroup.PEACEFUL, () -> DailyQuestPickFlowers.Flower.values().length),

    // 1 Adventure
    KILL_MONSTER(DailyQuestKillMonster::new, DailyQuestGroup.ADVENTURE, () -> DailyQuestKillMonster.TargetMob.values().length),
    FIND_DUNGEON(DailyQuestFindDungeon::new, DailyQuestGroup.ADVENTURE, () -> 1),
    FORAGING(DailyQuestForaging::new, DailyQuestGroup.ADVENTURE, () -> DailyQuestForaging.Forage.values().length),
    // Capture Monster

    // 2 Community
    MINIGAME_MATCH(DailyQuestMinigameMatch::new, DailyQuestGroup.COMMUNITY, () -> DailyQuestMinigameMatch.Game.values().length),
    FRIENDSHIP_GIFT(DailyQuestFriendshipGift:: new, DailyQuestGroup.COMMUNITY, () -> 7),
    MOB_ARENA_WAVE(DailyQuestMobArenaWave::new, DailyQuestGroup.DISABLED, () -> 1),
    MOB_ARENA_WAVES(DailyQuestMobArenaWaves::new, DailyQuestGroup.COMMUNITY, () -> 1),
    SKYBLOCK(DailyQuestSkyblock::new, DailyQuestGroup.COMMUNITY, () -> DailyQuestSkyblock.Task.values().length),
    ;

    protected final String key = name().toLowerCase();
    protected final Supplier<? extends DailyQuest> ctor;
    protected final DailyQuestGroup group;
    protected final Supplier<Integer> optionCountGetter;

    /**
     * Get all quest types with the given group.  The result will be
     * weighted!
     */
    public static List<DailyQuestIndex> getAllWithGroup(DailyQuestGroup group) {
        List<DailyQuestIndex> list = new ArrayList<>();
        for (DailyQuestType it : values()) {
            if (it.group != group) continue;
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
}
