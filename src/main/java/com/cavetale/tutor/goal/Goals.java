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
            });
        case ORIENTATION: return Arrays.asList(new Goal[] {
                new LocalChatGoal(),
                new WarpGoal(),
                new ServerSwitchGoal(),
            });
        case MONEY: return Arrays.asList(new Goal[] {
                new MineGoal(),
                new StorageGoal(),
                new SellItemGoal(),
                new BuyGoal(),
            });
        case MEMBER: return Arrays.asList(new Goal[] {
                new TicketGoal(),
                new MenuGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + name);
        }
    }
}
