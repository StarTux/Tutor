package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class InviteHomeGoal extends AbstractGoal<InviteHomeProgress> {
    protected static final String HOME = "TUTOR";
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condSetHome;
    protected final CheckboxCondition condInvite;
    protected final CheckboxCondition condUninvite;
    protected final CheckboxCondition condDelete;

    public InviteHomeGoal() {
        super(InviteHomeProgress.class, InviteHomeProgress::new);
        this.id = "invite_home";
        this.displayName = Component.text("Home Invites");
        condSetHome = new CheckboxCondition(Component.text("Set the " + HOME + " Home"),
                                            playerQuest -> getProgress(playerQuest).setHome,
                                            playerQuest -> getProgress(playerQuest).setHome = true);
        condInvite = new CheckboxCondition(Component.text("Invite someone"),
                                           playerQuest -> getProgress(playerQuest).invite,
                                           playerQuest -> getProgress(playerQuest).invite = true);
        condUninvite = new CheckboxCondition(Component.text("Uninvite them"),
                                             playerQuest -> getProgress(playerQuest).uninvite,
                                             playerQuest -> getProgress(playerQuest).uninvite = true);
        condDelete = new CheckboxCondition(Component.text("Delete the TUTOR home"),
                                             playerQuest -> getProgress(playerQuest).delete,
                                             playerQuest -> getProgress(playerQuest).delete = true);
        condSetHome.setBookPageIndex(0);
        condInvite.setBookPageIndex(1);
        condUninvite.setBookPageIndex(3);
        condDelete.setBookPageIndex(5);
        this.conditions = List.of(new Condition[] {
                condSetHome,
                condInvite,
                condUninvite,
                condDelete,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("You can invite anyone to your primary or named homes."
                                       + " Let's start by creating a home named " + HOME + ".\n\n"),
                        Component.text("/sethome " + HOME, NamedTextColor.BLUE),
                        Component.text("\nCreate a home named " + HOME, NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("To invite someone, use this command:\n\n"),
                        Component.text("/invitehome <player> [home]"),
                        Component.text("\nInvite someone to your named home.",
                                       NamedTextColor.GRAY),
                        Component.text("\n\nThe home name is "),
                        Component.text(HOME, NamedTextColor.BLUE),
                        Component.text(". If you can't think of a player, use "),
                        Component.text("Cavetale", NamedTextColor.BLUE),
                        Component.text(". They're our mascot. "),
                        Mytems.WINK.component,
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("To use someone's invite, one would type:\n\n"),
                        Component.text("/home <owner>:<name>", NamedTextColor.BLUE),
                        Component.text("\n\nThis is hard to remember so there's a list:\n\n"),
                        Component.text("/homes invites", NamedTextColor.BLUE),
                        Component.text("\nList invites for you. Click in chat to port", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("Sometimes you want to undo said invite."
                                       + " Nothing to worry about:\n\n"),
                        Component.text("/uninvitehome <player> [home]", NamedTextColor.BLUE),
                        Component.text("\nRetract the invitation", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 4
                        Component.text("So far, we've invited people to named homes."
                                       + " It's possible to invite to or uninivte"
                                       + " from your primary home as well:\n\n"),
                        Component.text("/invitehome <player>", NamedTextColor.BLUE),
                        Component.text("\nand\n"),
                        Component.text("/uninvitehome <player>", NamedTextColor.BLUE),
                        Component.text("\nJust skip the home name", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 5
                        Component.text("Since you no longer need the home, delete it:\n\n"),
                        Component.text("/homes delete TUTOR", NamedTextColor.BLUE),
                        Component.text("\nDelete the home", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                            Component.text("You can share your"),
                            Component.text("homes with your"),
                            Component.text("friends."),
                        });
                    pet.addSpeechBubble(id, 0, 100L, new Component[] {
                            Component.text("Just send them an"),
                            Component.text("invite."),
                        });
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case SET_NAMED_HOME:
            if (HOME.equalsIgnoreCase(Detail.NAME.get(event, ""))) {
                condSetHome.progress(playerQuest);
            }
            break;
        case INVITE_HOME:
            if (HOME.equalsIgnoreCase(Detail.NAME.get(event, ""))) {
                condInvite.progress(playerQuest);
            }
            break;
        case UNINVITE_HOME:
            if (HOME.equalsIgnoreCase(Detail.NAME.get(event, ""))) {
                condUninvite.progress(playerQuest);
            }
            break;
        case DELETE_HOME:
            if (HOME.equalsIgnoreCase(Detail.NAME.get(event, ""))) {
                condDelete.progress(playerQuest);
            }
            break;
        default: break;
        }
    }
}

final class InviteHomeProgress extends GoalProgress {
    protected boolean setHome;
    protected boolean invite;
    protected boolean uninvite;
    protected boolean delete;

    @Override
    public boolean isComplete() {
        return setHome && invite && uninvite && delete;
    }
}
