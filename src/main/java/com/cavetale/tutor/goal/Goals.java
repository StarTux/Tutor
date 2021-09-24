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
        case WARP: return List.of(new Goal[] {
                new PublicHomeGoal(),
                new WarpGoal(),
                WarpPlaceGoal.bazaar(),
                WarpPlaceGoal.dwarven(),
                WarpPlaceGoal.cloud(),
                WarpPlaceGoal.witch(),
                new ServerSwitchGoal(),
            });
        case CHAT: return List.of(new Goal[] {
                new LocalChatGoal(),
                new PrivateChatGoal(),
                new PartyChatGoal(),
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
        case BUILD: return List.of(new Goal[] {
                new TelevatorGoal(),
                new LinkPortalGoal(),
                new ShopChestGoal(),
            });
        case FRIEND: return List.of(new Goal[] {
                new SetNamedHomeGoal(),
                new InviteHomeGoal(),
                new MailGoal(),
                new RaidGoal(),
                new PocketMobGoal(),
                new TitleGoal(),
                new FriendsGoal(),
            });
        default:
            throw new IllegalStateException("Quest not defined: " + name);
        }
    }
}
