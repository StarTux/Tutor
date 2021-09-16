package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class RaidGoal extends AbstractGoal<RaidProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condJoin;
    protected final CheckboxCondition condStash;
    protected final CheckboxCondition condStart;
    protected final CheckboxCondition condVictory;

    public RaidGoal() {
        super(RaidProgress.class, RaidProgress::new);
        this.id = "raid";
        this.displayName = Component.text("Looking for Raid");
        condJoin = new CheckboxCondition(Component.text("Join the Raid Server"),
                                         playerQuest -> getProgress(playerQuest).join,
                                         playerQuest -> getProgress(playerQuest).join = true);
        condStash = new CheckboxCondition(Component.text("Open your Stash"),
                                          playerQuest -> getProgress(playerQuest).stash,
                                          playerQuest -> getProgress(playerQuest).stash = true);
        condStart = new CheckboxCondition(Component.text("Start any Raid"),
                                          playerQuest -> getProgress(playerQuest).start,
                                          playerQuest -> getProgress(playerQuest).start = true);
        condVictory = new CheckboxCondition(Component.text("Defeat a Raid"),
                                            playerQuest -> getProgress(playerQuest).victory,
                                            playerQuest -> getProgress(playerQuest).victory = true);
        condJoin.setBookPageIndex(0);
        condStart.setBookPageIndex(1);
        condVictory.setBookPageIndex(2);
        this.conditions = List.of(new Condition[] {
                condJoin,
                condStash,
                condStart,
                condVictory,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("A raid "),
                        Component.text("(not to be confused with village raids)", NamedTextColor.GRAY),
                        Component.text(" is a world with waves of mobs and boss fights."
                                       + " They are hosted on the raid server:\n\n"),
                        Component.text("/raid", NamedTextColor.BLUE),
                        Component.text("\nJoin the Raid server."
                                       + " It may have to start up first,"
                                       + " so let's be patient.", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("You bring your own equipment to a raid."
                                       + " Don't forget to pack good gear and plenty of food."
                                       + " You can carry it over inside your stash:\n\n"),
                        Component.text("/st", NamedTextColor.BLUE),
                        Component.text("\nOpen your stash", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Here you can choose a raid portal."
                                       + " Pick one and enter."
                                       + "After a brief warmup, the raid begins."
                                       + "\n\nFriends "),
                        Component.text("(/friend)", NamedTextColor.GRAY),
                        Component.text(" starting the same raid"
                                       + " will give each other a combat buff."),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("The raid will let you what to do next."
                                       + "\n\nFollow the instructions on screen."
                                       + "\n\nWhen there's a goal,"
                                       + "all players must gather around the spinning banner"
                                       + " inside the flame circle."
                                       + "\n\nIf you're lost, use your compass."
                                       + " The MagicMap shows enemy locations."),
                    }),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case SWITCH_SERVER:
            if (Detail.NAME.is(event, "raid")) {
                condJoin.progress(playerQuest);
            }
            break;
        case OPEN_STASH:
            if (ServerNameConstraint.raid().isTrue()) {
                condStash.progress(playerQuest);
            }
            break;
        case RAID_START:
            condStart.progress(playerQuest);
            break;
        case RAID_VICTORY:
            condVictory.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class RaidProgress extends GoalProgress {
    protected boolean join;
    protected boolean stash;
    protected boolean start;
    protected boolean victory;

    @Override
    public boolean isComplete() {
        return join && stash && start && victory;
    }
}
