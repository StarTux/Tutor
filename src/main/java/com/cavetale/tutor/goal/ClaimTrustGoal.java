package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ClaimTrustGoal extends AbstractGoal<ClaimTrustProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condTrust;
    protected final CheckboxCondition condUntrust;
    protected final CheckboxCondition condInteractTrust;
    protected final CheckboxCondition condContainerTrust;

    public ClaimTrustGoal() {
        super(ClaimTrustProgress.class, ClaimTrustProgress::new);
        this.id = "claim_trust";
        this.displayName = Component.text("Claim Trust");
        condTrust = new CheckboxCondition(Component.text("Give someone Trust"),
                                          playerQuest -> getProgress(playerQuest).trust,
                                          playerQuest -> getProgress(playerQuest).trust = true);
        condUntrust = new CheckboxCondition(Component.text("Untrust them"),
                                            playerQuest -> getProgress(playerQuest).untrust,
                                            playerQuest -> getProgress(playerQuest).untrust = true);
        condInteractTrust = new CheckboxCondition(Component.text("Give Interact Trust"),
                                                  playerQuest -> getProgress(playerQuest).interactTrust,
                                                  playerQuest -> getProgress(playerQuest).interactTrust = true);
        condContainerTrust = new CheckboxCondition(Component.text("Give Container Trust"),
                                                   playerQuest -> getProgress(playerQuest).containerTrust,
                                                   playerQuest -> getProgress(playerQuest).containerTrust = true);
        condTrust.setBookPageIndex(0);
        condUntrust.setBookPageIndex(1);
        condInteractTrust.setBookPageIndex(2);
        condContainerTrust.setBookPageIndex(3);
        this.conditions = List.of(new Condition[] {
                condTrust,
                condUntrust,
                condInteractTrust,
                condContainerTrust,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("In order to give someone trust in a claim,"
                                       + " you must:"),
                        Component.text("\n\n" + Unicode.BULLET_POINT.character
                                       + " be the "),
                        Component.text("owner", NamedTextColor.BLUE),
                        Component.text(" of that claim,"),
                        Component.text("\n\n" + Unicode.BULLET_POINT.character
                                       + " or have at least "),
                        Component.text("co-owner", NamedTextColor.BLUE),
                        Component.text(" trust."
                                       + "\n\nIf you don't, make a claim or"
                                       + " ask your friend to promote you."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("To let someone build in your claim,"
                                       + " give them trust:\n\n"),
                        Component.text("/claim trust <player>", NamedTextColor.BLUE),
                        Component.text("\nTrust someone in your claim", NamedTextColor.GRAY),
                        Component.text("\n\nIf you can't think of anyone, trust "),
                        Component.text("Cavetale", NamedTextColor.BLUE),
                        Component.text(". They're our mascot. "),
                        Mytems.WINK.component,
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("Sometimes, trust is not forever."
                                       + " To revoke any trust level, do this:\n\n"),
                        Component.text("/claim untrust <player>", NamedTextColor.BLUE),
                        Component.text("\nRevoke any trust", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("It's wise to be very careful with trust. "),
                        Component.text("Interact Trust", NamedTextColor.BLUE),
                        Component.text(" only gives access to buttons, levers, doors,"
                                       + " and a few other noninvasive actions:\n\n"),
                        Component.text("/claim interact-trust <player>", NamedTextColor.BLUE),
                        Component.text("\nGive someone Interact Trust", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("The next level up is "),
                        Component.text("Container Trust", NamedTextColor.BLUE),
                        Component.text(", which involves opening storage containers"
                                       + " and shearing sheep."
                                       + " Players in this group can steal from you,"
                                       + " so do be careful:\n\n"),
                        Component.text("/claim container-trust <player>", NamedTextColor.BLUE),
                        Component.text("\nGive someone Container Trust", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("Choose wisely if you want to give someone trust to "),
                        Component.text("interact", NamedTextColor.BLUE),
                        Component.text(", "),
                        Component.text("open containers", NamedTextColor.BLUE),
                        Component.text(", or event "),
                        Component.text("build", NamedTextColor.BLUE),
                        Component.text(". Only trust people you trust, literally."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 4
                        Component.text("If someone bothers you,"
                                       + " you can kick or ban them from your claim:\n\n"),
                        Component.text("/claim kick <player>", NamedTextColor.BLUE),
                        Component.text("\nWarp them outside your claim borders\n", NamedTextColor.GRAY),
                        Component.text("/claim ban <player>", NamedTextColor.BLUE),
                        Component.text("\nStop them from entering\n", NamedTextColor.GRAY),
                        Component.text("/claim unban <player>", NamedTextColor.BLUE),
                        Component.text("\nLift the ban again", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                            Component.text("Your claim is your"),
                            Component.text("castle."),
                        });
                    pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                            Component.text("Or so they say."),
                        });
                    pet.addSpeechBubble(id, 0L, 60L, new Component[] {
                            Component.text("Or so they say."),
                        });
                    pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                            Component.text("Be careful whome you"),
                            Component.text("trust inside your"),
                            Component.text("claim, " + pet.getType().speechGimmick + "!"),
                        });
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case CLAIM_TRUST:
            if (Detail.NAME.is(event, "build")) {
                condTrust.progress(playerQuest);
            } else if (Detail.NAME.is(event, "interact")) {
                condInteractTrust.progress(playerQuest);
            } else if (Detail.NAME.is(event, "container")) {
                condContainerTrust.progress(playerQuest);
            }
            break;
        case CLAIM_UNTRUST:
            condUntrust.progress(playerQuest);
        default: break;
        }
    }
}

final class ClaimTrustProgress extends GoalProgress {
    protected boolean trust;
    protected boolean untrust;
    protected boolean interactTrust;
    protected boolean containerTrust;

    @Override
    public boolean isComplete() {
        return trust && untrust && interactTrust && containerTrust;
    }
}
