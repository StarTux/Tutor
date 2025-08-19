package com.cavetale.tutor.daily;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyQuestType {
    DUMMY(DailyQuestDummy::new, DailyQuestGroup.DISABLED, () -> List.of()),
    // 0 Peaceful
    MINING(DailyQuestMining::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestMining.Ore.class)),
    HARVEST(DailyQuestHarvest::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestHarvest.Crop.class)),
    FISHING(DailyQuestFishing::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestFishing.Fish.class)),
    TREE_CHOPPER(DailyQuestTreeChopper::new, DailyQuestGroup.DISABLED, () -> enumsToStringList(DailyQuestTreeChopper.getAllTypes())),
    CHOP_TREES(DailyQuestChopTrees::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestChopTrees.Wood.class)),
    SHEAR_SHEEP(DailyQuestShearSheep::new, DailyQuestGroup.PEACEFUL, () -> enumsToStringList(DailyQuestShearSheep.getAllColors())),
    CRAFTING(DailyQuestCrafting::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestCrafting.Materials.class)),
    EATING(DailyQuestEating::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestEating.Foods.class)),
    BREEDING(DailyQuestBreeding::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestBreeding.BreedMob.class)),
    PICK_FLOWERS(DailyQuestPickFlowers::new, DailyQuestGroup.PEACEFUL, () -> enumToStringList(DailyQuestPickFlowers.Flower.class)),

    // 1 Adventure
    KILL_MONSTER(DailyQuestKillMonster::new, DailyQuestGroup.ADVENTURE, () -> enumToStringList(DailyQuestKillMonster.TargetMob.class)),
    FIND_DUNGEON(DailyQuestFindDungeon::new, DailyQuestGroup.ADVENTURE, () -> List.of("any")),
    FORAGING(DailyQuestForaging::new, DailyQuestGroup.ADVENTURE, () -> enumToStringList(DailyQuestForaging.Forage.class)),
    // Capture Monster

    // 2 Community
    MINIGAME_MATCH(DailyQuestMinigameMatch::new, DailyQuestGroup.COMMUNITY, () -> enumToStringList(DailyQuestMinigameMatch.Game.class)),
    FRIENDSHIP_GIFT(DailyQuestFriendshipGift:: new, DailyQuestGroup.COMMUNITY, () -> List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")),
    MOB_ARENA_WAVE(DailyQuestMobArenaWave::new, DailyQuestGroup.DISABLED, () -> List.of("any")),
    MOB_ARENA_WAVES(DailyQuestMobArenaWaves::new, DailyQuestGroup.COMMUNITY, () -> List.of("any")),
    SKYBLOCK(DailyQuestSkyblock::new, DailyQuestGroup.COMMUNITY, () -> enumToStringList(DailyQuestSkyblock.Task.class)),
    ;

    protected final String key = name().toLowerCase();
    protected final Supplier<? extends DailyQuest> ctor;
    protected final DailyQuestGroup group;
    protected final Supplier<List<String>> optionNameSupplier;

    /**
     * Get all quest types with the given group.  The result will be
     * weighted!
     */
    public static List<DailyQuestIndex> getAllWithGroup(DailyQuestGroup group) {
        List<DailyQuestIndex> list = new ArrayList<>();
        for (DailyQuestType type : values()) {
            if (type.group != group) continue;
            for (String name : type.getOptionNames()) {
                list.add(new DailyQuestIndex(type, name));
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

    public List<String> getOptionNames() {
        return optionNameSupplier.get();
    }

    private static <T extends Enum> List<String> enumToStringList(Class<T> enume) {
        final List<String> result = new ArrayList<>();
        for (T it : enume.getEnumConstants()) {
            result.add(it.name().toLowerCase());
        }
        return result;
    }

    private static List<String> enumsToStringList(List<? extends Enum> enums) {
        final List<String> result = new ArrayList<>(enums.size());
        for (Enum it : enums) {
            result.add(it.name().toLowerCase());
        }
        return result;
    }
}
