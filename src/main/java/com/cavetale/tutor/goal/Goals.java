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
        case BEGINNER:
            return List.of(new ChoosePetGoal(),
                           new WildGoal(),
                           new SetHomeGoal());
        case WARP:
            return List.of(new PublicHomeGoal(),
                           new WarpGoal(),
                           WarpPlaceGoal.bazaar(),
                           WarpPlaceGoal.dwarven(),
                           WarpPlaceGoal.cloud(),
                           WarpPlaceGoal.witch(),
                           new ServerSwitchGoal());
        case CHAT:
            return List.of(new LocalChatGoal(),
                           new PrivateChatGoal(),
                           new PartyChatGoal());
        case MONEY:
            return List.of(new MineGoal(),
                           new MiningGoal(),
                           new StorageGoal(),
                           new SellItemGoal(),
                           new BuyGoal());
        case MEMBER:
            return List.of(new TicketGoal(),
                           new MenuGoal(),
                           new MemberAcceptGoal());
        case MEMBER_INTRO:
            return List.of(new MailGoal(),
                           new TitleGoal(),
                           new FriendsGoal(),
                           new RaidGoal());
        case HOME:
            return List.of(new ClaimGrowGoal(),
                           new ClaimTrustGoal(),
                           new SetNamedHomeGoal(),
                           new InviteHomeGoal());
        case MOBILITY:
            return List.of(new TelevatorGoal(),
                           new LinkPortalGoal(),
                           new PocketMobGoal());
        case SPELEOLOGIST:
            return List.of(new SpeleologistGoal(),
                           new SpeleologistAcceptGoal());
            // new ShopChestGoal(),
        default:
            throw new IllegalStateException("Quest not defined: " + name);
        }
    }
}
