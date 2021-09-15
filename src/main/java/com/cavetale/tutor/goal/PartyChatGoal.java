package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class PartyChatGoal extends AbstractGoal<PartyChatProgress> {
    private static final String PARTY = "beginner";
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condJoin;
    protected final CheckboxCondition condTalk;

    public PartyChatGoal() {
        super(PartyChatProgress.class, PartyChatProgress::new);
        this.id = "party_chat";
        this.displayName = Component.text("Party Chat");
        condJoin = new CheckboxCondition(Component.text("Join the " + PARTY + " Party"),
                                         playerQuest -> getProgress(playerQuest).join,
                                         playerQuest -> getProgress(playerQuest).join = true);
        condTalk = new CheckboxCondition(Component.text("Talk in the Party"),
                                         playerQuest -> getProgress(playerQuest).talk,
                                         playerQuest -> getProgress(playerQuest).talk = true);
        condJoin.setBookPageIndex(0);
        condTalk.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condJoin,
                condTalk,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("A party is a named group chat where"
                                       + " everyone in the same party can hear you."
                                       + "\n\nCommands:"),
                        Component.text("/party", NamedTextColor.BLUE),
                        Component.text("\nInfo about your party\n\n", NamedTextColor.GRAY),
                        Component.text("/party <name>", NamedTextColor.BLUE),
                        Component.text("\nJoin or create a party", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Once you have joined, talking works just like in"
                                       + " global or local."
                                       + " You can even focus your party.\n\n"),
                        Component.text("/p", NamedTextColor.BLUE),
                        Component.text("\nFocus your party\n\n", NamedTextColor.GRAY),
                        Component.text("/p <message>", NamedTextColor.BLUE),
                        Component.text("\nSend one message to your party", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Tell your friends to join your party"
                                       + " so you guys can group up easily."),
                        Component.text("\n\nAll your group needs is a party name,"
                                      + " and you're set."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 60L,
                                        Component.text("Life's a party!"));
                    pet.addSpeechBubble(id, 0L, 140L,
                                        Component.text("With party chat,"),
                                        Component.text("you and your"),
                                        Component.text("friends can talk"),
                                        Component.text("in your own chat"),
                                        Component.text("channel, " + pet.getType().speechGimmick + "!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case JOIN_CHAT_PARTY:
            if (Detail.NAME.is(event, PARTY)) {
                condJoin.progress(playerQuest);
            }
            break;
        case USE_CHAT_PARTY:
            if (Detail.NAME.is(event, PARTY)) {
                condTalk.progress(playerQuest);
            }
            break;
        default: break;
        }
    }
}

final class PartyChatProgress extends GoalProgress {
    protected boolean join;
    protected boolean talk;

    @Override
    public boolean isComplete() {
        return join && talk;
    }
}
