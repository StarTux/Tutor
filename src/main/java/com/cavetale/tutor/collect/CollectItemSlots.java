package com.cavetale.tutor.collect;

import java.util.ArrayList;
import java.util.List;

public final class CollectItemSlots {
    /**
     * Slots order in a 5x9 inventory.
     */
    public static List<Integer> slotsForSize(int size) {
        return switch (size) {
        case 1 -> List.of(22);
        case 2 -> List.of(21, 23);
        case 3 -> List.of(20, 22, 24);
        case 4 -> List.of(12, 14, 30, 32);
        case 5 -> List.of(22, 11, 15, 29, 33);
        case 6 -> List.of(11, 13, 15, 29, 31, 33);
        case 7 -> List.of(22, 3, 5, 20, 24, 39, 41);
        case 8 -> List.of(10, 12, 14, 16, 28, 30, 32, 34);
        case 9 -> List.of(2, 4, 6, 20, 22, 24, 38, 40, 42);
        case 10 -> List.of(9, 11, 13, 15, 17, 27, 29, 31, 33, 35);
        default -> {
            List<Integer> result = new ArrayList<>(size);
            for (int i = 0; i < size; i += 1) result.add(i);
            yield result;
        }
        };
    }

    public static List<Integer> rewards() {
        return List.of(49, 50, 48, 51, 47, 52, 46, 53, 45);
    }

    private CollectItemSlots() { }
}
