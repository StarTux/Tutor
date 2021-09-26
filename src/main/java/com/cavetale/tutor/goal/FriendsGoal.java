package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class FriendsGoal extends AbstractGoal<FriendsProgress> {
    protected static final int GIFTS = 3;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condList;
    protected final NumberCondition condGift;
    protected final CheckboxCondition condPriest;
    protected final CheckboxCondition condBday;

    public FriendsGoal() {
        super(FriendsProgress.class, FriendsProgress::new);
        this.id = "friends";
        this.displayName = Component.text("Making Friends");
        condList = new CheckboxCondition(Component.text("View your friends list"),
                                         playerQuest -> getProgress(playerQuest).list,
                                         playerQuest -> getProgress(playerQuest).list = true);
        condGift = new NumberCondition(Component.text("Share Friendship Gifts"), GIFTS,
                                       playerQuest -> getProgress(playerQuest).gifts,
                                       (playerQuest, amount) -> getProgress(playerQuest).gifts = amount);
        condPriest = new CheckboxCondition(Component.text("Find the Priest"),
                                           playerQuest -> getProgress(playerQuest).priest,
                                           playerQuest -> getProgress(playerQuest).priest = true);
        condBday = new CheckboxCondition(Component.text("Share your Birthday"),
                                         playerQuest -> getProgress(playerQuest).bday,
                                         playerQuest -> getProgress(playerQuest).bday = true);
        condList.setBookPageIndex(0);
        condGift.setBookPageIndex(1);
        condPriest.setBookPageIndex(5);
        condBday.setBookPageIndex(7);
        this.conditions = List.of(new Condition[] {
                condList,
                condGift,
                condPriest,
                condBday,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 0
                        Component.text("You can grow your friendship with every other player"
                                       + " on Cavetale."
                                       + "\n\nCommand:\n"),
                        Component.text("/friends", NamedTextColor.BLUE),
                        Component.text("\nView your friends list, along with the daily friendship gift",
                                       NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 1
                        Component.text("To gain friendship with somebody, drop the "),
                        Component.text("friendship gift", NamedTextColor.BLUE),
                        Component.text(" so that they pick it up."),
                        Component.text("\n\nYou can find the gift on the "),
                        Component.text("/friends", NamedTextColor.BLUE),
                        Component.text(" list."
                                       + "\n\nIt changes every day and includes:"),
                        VanillaItems.MELON_SLICE.component,
                        VanillaItems.APPLE.component,
                        VanillaItems.COOKIE.component,
                        VanillaItems.PUMPKIN_PIE.component,
                        VanillaItems.SWEET_BERRIES.component,
                        VanillaItems.CAKE.component,
                        VanillaItems.GOLDEN_APPLE.component,
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 2
                        Component.text("There are many ways to increase friendship with each other:\n"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Daily friendship gift"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Bonus for gifting on birthdays"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Play server events"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Complete raids"),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 3
                        Component.text("At "),
                        Component.text("3" + Unicode.HEART.character, NamedTextColor.RED),
                        Component.text(", you can send a "),
                        Component.text("Friend", NamedTextColor.BLUE),
                        Component.text(" request."
                                       + "\n\nCommand:"),
                        Component.text("/friend <player>", NamedTextColor.BLUE),
                        Component.text("\nSend a friend request."
                                       + " They can accept by clicking"
                                       + " the message in chat.", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 4
                        Component.text("Making "),
                        Component.text("Friends", NamedTextColor.BLUE),
                        Component.text(" has its perks:\n"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Bonus hearts when starting a raid together"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Friendship never drops below "),
                        Component.text("2" + Unicode.HEART.character, NamedTextColor.RED),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 5
                        Component.text("At "),
                        Component.text("5" + Unicode.HEART.character, NamedTextColor.RED),
                        Component.text(", you can ask to get "),
                        Component.text("married", NamedTextColor.BLUE),
                        Component.text(". All you need is two "),
                        Mytems.WEDDING_RING.component,
                        Component.text("Wedding Rings."
                                       + "\n\nThe priest in the cathedral at spawn"
                                       + " will sell them to you."
                                       + " Can you find them?"),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 6
                        Component.text("Marriage", NamedTextColor.BLUE),
                        Component.text(" comes with perks:\n"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Share hunger points when eating"),
                        Component.text("\n" + Unicode.BULLET_POINT.character),
                        Component.text(" Use the "),
                        Component.text("/love", NamedTextColor.BLUE),
                        Component.text(" command to share your love"),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 7
                        Component.text("If you share your birthday,"
                                       + " it will be visible on everyone's"
                                       + " profile page as well as the website."
                                       + "\n\nCommand:\n"),
                        Component.text("/profile", NamedTextColor.BLUE),
                        Component.text("\nView your profile,"
                                       + " where you can set your birthday",
                                       NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        checkPassiveProgress(playerQuest);
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                            Component.text("What's more important"),
                            Component.text("in life than friends?"),
                        });
                    pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                            Component.text("Let's find out how to"),
                            Component.text("make friends on Cavetale!"),
                        });
                });
        }
    }

    void checkPassiveProgress(PlayerQuest playerQuest) {
        FriendsProgress progress = getProgress(playerQuest);
        if (PluginPlayerQuery.Name.DID_ENTER_BIRTHDAY.call(playerQuest.getPlugin(), playerQuest.getPlayer(), false)) {
            condBday.progress(playerQuest);
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case ENTER_BIRTHDAY:
            condBday.progress(playerQuest);
            break;
        case VIEW_FRIENDS_LIST:
            condList.progress(playerQuest);
            break;
        case SHARE_FRIENDSHIP_ITEM:
            condGift.progress(playerQuest);
            break;
        case INTERACT_NPC:
            if (Detail.NAME.is(event, "WeddingRing") && MainServerConstraint.isTrue()) {
                condPriest.progress(playerQuest);
            }
            break;
        case PLAYER_SESSION_LOADED:
            if (event.getPlugin().getName().equals("Fam")) {
                checkPassiveProgress(playerQuest);
            }
            break;
        default: break;
        }
    }

}

final class FriendsProgress extends GoalProgress {
    protected boolean list;
    protected int gifts;
    protected boolean priest;
    protected boolean bday;

    @Override
    public boolean isComplete() {
        return bday
            && list
            && gifts >= FriendsGoal.GIFTS
            && priest;
    }
}
