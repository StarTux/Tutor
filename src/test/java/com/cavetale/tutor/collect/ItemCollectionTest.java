package com.cavetale.tutor.collect;

import java.util.EnumMap;
import org.junit.Test;

public final class ItemCollectionTest {
    @Test
    public void test() {
        EnumMap<ItemCollectionType, Integer> dependingMap = new EnumMap<>(ItemCollectionType.class);
        for (ItemCollectionType type : ItemCollectionType.values()) {
            for (ItemCollectionType dependency : type.dependencies) {
                int depending = dependingMap.getOrDefault(dependency, 0);
                dependingMap.put(dependency, depending + 1);
            }
        }
        for (ItemCollectionType type : ItemCollectionType.values()) {
            System.out.println(dependingMap.getOrDefault(type, 0) + " " + type);
            for (ItemCollectionType depending : ItemCollectionType.values()) {
                if (!depending.dependencies.contains(type)) continue;
                int count2 = dependingMap.getOrDefault(depending, 0);
                System.out.println("  └─ " + count2 + " " + depending);
            }
        }
        System.out.println(ItemCollectionType.values().length + " Item Collections");
    }
}
