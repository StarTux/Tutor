package com.cavetale.tutor.goal;

import com.cavetale.tutor.QuestName;
import java.util.Arrays;
import java.util.List;

/**
 * Utiltiy class which knows how to create the list of goals for each
 * quest.
 */
public final class Goals {
    private Goals() { }

    public static List<Goal> create(QuestName name) {
        switch (name) {
        case BEGINNER: return Arrays.asList(new Goal[] {
                new ChoosePetGoal(),
                new WildGoal(),
                new SetHomeGoal(),
                new SpawnGoal(),
                new ServerSwitchGoal(),
                new LocalChatGoal(),
            });
        case TEST: return Arrays.asList(new Goal[] {
                new WarpGoal(),
                new StorageGoal(),
                new SellItemGoal(),
                new MineGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + name);
        }
    }
}
