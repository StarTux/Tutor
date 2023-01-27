package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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

    public FriendsGoal() {
        super(FriendsProgress.class, FriendsProgress::new);
        this.id = "friends";
        this.displayName = text("Making Friends");
        condList = new CheckboxCondition(text("View your friends list"),
                                         playerQuest -> getProgress(playerQuest).list,
                                         playerQuest -> getProgress(playerQuest).list = true);
        condGift = new NumberCondition(text("Share Friendship Gifts"), GIFTS,
                                       playerQuest -> getProgress(playerQuest).gifts,
                                       (playerQuest, amount) -> getProgress(playerQuest).gifts = amount);
        condPriest = new CheckboxCondition(text("Find the Priest"),
                                           playerQuest -> getProgress(playerQuest).priest,
                                           playerQuest -> getProgress(playerQuest).priest = true);
        condList.setBookPageIndex(0);
        condGift.setBookPageIndex(1);
        condPriest.setBookPageIndex(5);
        this.conditions = List.of(condList,
                                  condGift,
                                  condPriest);
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                // 0
                textOfChildren(text("You can grow your friendship with every other player"
                                    + " on Cavetale."
                                    + "\n\nCommand:\n"),
                               text("/friends", BLUE),
                               text("\nView your friends list, along with the daily friendship gift",
                                    GRAY)),
                // 1
                textOfChildren(text("To gain friendship with somebody, drop the "),
                               text("friendship gift", BLUE),
                               text(" so that they pick it up."),
                               text("\n\nYou can find the gift on the "),
                               text("/friends", BLUE),
                               text(" list."
                                    + "\n\nIt changes every day and includes:"),
                               VanillaItems.MELON_SLICE.component,
                               VanillaItems.APPLE.component,
                               VanillaItems.COOKIE.component,
                               VanillaItems.PUMPKIN_PIE.component,
                               VanillaItems.SWEET_BERRIES.component,
                               VanillaItems.CAKE.component,
                               VanillaItems.GOLDEN_APPLE.component),
                // 2
                textOfChildren(text("There are many ways to increase friendship with each other:\n"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Daily friendship gift"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Bonus for gifting on birthdays"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Play server events"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Complete raids")),
                // 3
                textOfChildren(text("At "),
                               text("3" + Unicode.HEART.character, RED),
                               text(", you can send a "),
                               text("Friend", BLUE),
                               text(" request."
                                    + "\n\nCommand:"),
                               text("/friend <player>", BLUE),
                               text("\nSend a friend request."
                                    + " They can accept by clicking"
                                    + " the message in chat.", GRAY)),
                // 4
                textOfChildren(text("Making "),
                               text("Friends", BLUE),
                               text(" has its perks:\n"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Bonus hearts when starting a raid together"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Friendship never drops below "),
                               text("2" + Unicode.HEART.character, RED)),
                // 5
                textOfChildren(text("At "),
                               text("5" + Unicode.HEART.character, RED),
                               text(", you can ask to get "),
                               text("married", BLUE),
                               text(". All you need is two "),
                               Mytems.WEDDING_RING.component,
                               text("Wedding Rings."
                                    + "\n\nThe priest in the cathedral at spawn"
                                    + " will sell them to you."
                                    + " Can you find them?")),
                // 6
                textOfChildren(
                               text("Marriage", BLUE),
                               text(" comes with perks:\n"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Share hunger points when eating"),
                               text("\n" + Unicode.BULLET_POINT.character),
                               text(" Use the "),
                               text("/love", BLUE),
                               text(" command to share your love")),
                // 7
                textOfChildren(text("If you share your birthday,"
                                    + " it will be visible on everyone's"
                                    + " profile page as well as the website,"
                                    + " unless you wish to keep it a secret."
                                    + "\n\nCommand:\n"),
                               text("/profile", BLUE),
                               text("\nView your profile,"
                                    + " where you can set your birthday"
                                    + " by clicking the ", GRAY),
                               Mytems.STAR.component),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                            text("What's more important"),
                            text("in life than friends?"),
                        });
                    pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                            text("Let's find out how to"),
                            text("make friends on Cavetale!"),
                        });
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case VIEW_FRIENDS_LIST:
            condList.progress(playerQuest);
            break;
        case SHARE_FRIENDSHIP_ITEM:
            condGift.progress(playerQuest);
            break;
        case INTERACT_NPC:
            if (Detail.NAME.is(event, "WeddingRing")) {
                condPriest.progress(playerQuest);
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

    @Override
    public boolean isComplete() {
        return list
            && gifts >= FriendsGoal.GIFTS
            && priest;
    }
}
