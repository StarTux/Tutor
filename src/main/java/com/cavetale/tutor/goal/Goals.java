package com.cavetale.tutor.goal;

import com.cavetale.tutor.QuestName;
import java.util.List;

/**
 * Utiltiy class which knows how to create the list of goals for each
 * quest.
 */
public final class Goals {
    private Goals() { }

    public static List<Goal> create(QuestName name) {
        switch (name) {
        case BEGINNER: return List.of(new Goal[] {
                new ChoosePetGoal(),
                new WildGoal(),
                new SetHomeGoal(),
            });
        case ORIENTATION: return List.of(new Goal[] {
                new LocalChatGoal(),
                new WarpGoal(),
                new ServerSwitchGoal(),
            });
        case MONEY: return List.of(new Goal[] {
                new MineGoal(),
                new MiningGoal(),
                new StorageGoal(),
                new SellItemGoal(),
                new BuyGoal(),
            });
        case MEMBER: return List.of(new Goal[] {
                new TicketGoal(),
                new MenuGoal(),
            });
        case FRIEND: return List.of(new Goal[] {
                new PrivateChatGoal(),
                new PartyChatGoal(),
                new TelevatorGoal(),
                new ShopChestGoal(),
                new PocketMobGoal(),
                new LinkPortalGoal(),
                new TitleGoal(),
                new MailGoal(),
                new FriendGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + name);
        }
    }
}
