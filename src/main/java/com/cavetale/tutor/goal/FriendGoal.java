package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerQuery;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class FriendGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condBday;
    protected final CheckboxCondition condGift;
    protected final CheckboxCondition condFriend;

    public FriendGoal() {
        this.id = "friend";
        this.displayName = Component.text("Making Friends");
        condBday = new CheckboxCondition(Component.text("Share your Birthday"),
                                         playerQuest -> getProgress(playerQuest).bday,
                                         playerQuest -> getProgress(playerQuest).bday = true);
        condGift = new CheckboxCondition(Component.text("Share a Friendship Gift"),
                                         playerQuest -> getProgress(playerQuest).gift,
                                         playerQuest -> getProgress(playerQuest).gift = true);
        condFriend = new CheckboxCondition(Component.text("Make a Friend"),
                                           playerQuest -> getProgress(playerQuest).friend,
                                           playerQuest -> getProgress(playerQuest).friend = true);
        condBday.setBookPageIndex(0);
        condGift.setBookPageIndex(1);
        condFriend.setBookPageIndex(3);
        this.conditions = List.of(new Condition[] {
                condBday,
                condGift,
                condFriend,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("If you share your birthday,"
                                       + " it will be visible on everyone's"
                                       + " profile page as well as the website."
                                       + "\n\nCommand:\n"),
                        Component.text("/profile", NamedTextColor.BLUE),
                        Component.text("\nView your profile,"
                                       + " where you can set your birthday",
                                       NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("You can increase your friendship with one another by"
                                       + " sharing the daily friendship item."
                                       + " There is a different item every day of the week"
                                       + "\n\nCommand:"),
                        Component.text("/friends", NamedTextColor.BLUE),
                        Component.text("\nView your friends list, along with the friendship item",
                                       NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("You can also increase friendship by"
                                       + " participating in our events,"
                                       + " which happen every Saturday and Sunday."
                                       + " All participants will gain half a heart worth of"
                                       + " frienship with each other."
                                       + "\nFrienship decays slowly over time, so keep it up!"),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("Once you have at least 3 hearts with someone,"
                                       + " you can send a friendship request."
                                       + "\n\nCommand:\n"),
                        Component.text("/friend <player>", NamedTextColor.BLUE),
                        Component.text("\nSend a friend request."
                                       + " They can accept by clicking the message in their chat",
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
        FriendProgress progress = getProgress(playerQuest);
        if (PluginPlayerQuery.Name.DID_ENTER_BIRTHDAY.call(playerQuest.getPlugin(), playerQuest.getPlayer(), false)) {
            condBday.progress(playerQuest);
        }
        if (PluginPlayerQuery.Name.FRIEND_COUNT.call(playerQuest.getPlugin(), playerQuest.getPlayer(), 0) > 0) {
            condFriend.progress(playerQuest);
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case ENTER_BIRTHDAY:
            condBday.progress(playerQuest);
            break;
        case SHARE_FRIENDSHIP_ITEM:
            condGift.progress(playerQuest);
            break;
        case MAKE_FRIEND:
            condFriend.progress(playerQuest);
            break;
        case PLAYER_SESSION_LOADED:
            if (event.getPlugin().getName().equals("Fam")) {
                checkPassiveProgress(playerQuest);
            }
            break;
        default: break;
        }
    }

    @Override
    public FriendProgress newProgress() {
        return new FriendProgress();
    }

    @Override
    public FriendProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(FriendProgress.class, FriendProgress::new);
    }

    protected static final class FriendProgress extends GoalProgress {
        protected boolean bday;
        protected boolean gift;
        protected boolean friend;

        @Override
        public boolean isComplete() {
            return bday && gift && friend;
        }
    }
}
