package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class RaidGoal extends AbstractGoal<RaidProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condStash;
    protected final CheckboxCondition condJoin;
    protected final CheckboxCondition condStart;

    public RaidGoal() {
        super(RaidProgress.class, RaidProgress::new);
        this.id = "raid";
        this.displayName = Component.text("Looking for Raid");
        condStash = new CheckboxCondition(Component.text("Open your Stash"),
                                          playerQuest -> getProgress(playerQuest).stash,
                                          playerQuest -> getProgress(playerQuest).stash = true);
        condJoin = new CheckboxCondition(Component.text("Join the Raid Server"),
                                         playerQuest -> getProgress(playerQuest).join,
                                         playerQuest -> getProgress(playerQuest).join = true);
        condStart = new CheckboxCondition(Component.text("Start any Raid"),
                                          playerQuest -> getProgress(playerQuest).start,
                                          playerQuest -> getProgress(playerQuest).start = true);
        condStash.setBookPageIndex(1);
        condJoin.setBookPageIndex(2);
        condStart.setBookPageIndex(3);
        this.conditions = List.of(new Condition[] {
                condStash,
                condJoin,
                condStart,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("A raid "),
                        Component.text("(not to be confused with village raids)", NamedTextColor.GRAY),
                        Component.text(" is a world with waves of monsters and bosses to fight."
                                       + "\nRaids are difficult so it's best to"
                                       + " go in there with a group."
                                       + "\n\nWiki Page:\n"),
                        (Component.text().content("cavetale.com/wiki/raid-hub")
                         .color(NamedTextColor.BLUE)
                         .decorate(TextDecoration.UNDERLINED)
                         .hoverEvent(HoverEvent.showText(Component.text("cavetale.com/wiki/raid-hub",
                                                                        NamedTextColor.BLUE)))
                         .clickEvent(ClickEvent.openUrl("https://cavetale.com/wiki/raid-hub"))
                         .build()),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("You bring your own equipment to a raid."
                                       + " Don't forget to pack good gear and plenty of food."
                                       + " You can carry it over inside your stash:\n\n"),
                        Component.text("/st", NamedTextColor.BLUE),
                        Component.text("\nOpen your stash", NamedTextColor.GRAY),
                        Component.text("\n\nYour gear will not suffer durability loss"
                                       + " on the raid server."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("Raids are hosted on the raid server:\n\n"),
                        Component.text("/raid", NamedTextColor.BLUE),
                        Component.text("\nJoin the Raid server."
                                       + " It may have to start up first,"
                                       + " so let's be patient.", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("Here you can choose a raid portal."
                                       + " Pick one and enter."
                                       + " After a brief warmup, the raid begins."
                                       + "\n\nThe raid will tell you what to do next..."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 4
                        Component.text("When there's a "),
                        Component.text("goal", NamedTextColor.BLUE),
                        Component.text(", all players must gather around the spinning"
                                       + " banner inside the flame circle."
                                       + "\n\n"),
                        Component.text("Enemy", NamedTextColor.BLUE),
                        Component.text(" waves want you to defeat"
                                       + " all the monsters.\n\n"),
                        Component.text("Timed", NamedTextColor.BLUE),
                        Component.text(" waves challenge you to"
                                       + " survive long enough."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 5
                        Component.text("If you're lost, use your "),
                        VanillaItems.COMPASS.component,
                        Component.text("compass."
                                       + " It will point to the next goal."
                                       + "\n\nThe "),
                        Mytems.MAGIC_MAP.component,
                        Component.text("MagicMap shows enemy locations."
                                       + " Carrying it in your off-hand is"
                                       + " a good idea."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 6
                        Component.text("Raids are tough and there's no shame in"
                                       + " giving up to fight another day:\n\n"),
                        Component.text("/spawn", NamedTextColor.BLUE),
                        Component.text("\nWarp to the raid spawn\n\n", NamedTextColor.GRAY),
                        Component.text("/cavetale", NamedTextColor.BLUE),
                        Component.text("\nReturn to the main server", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 7
                        Component.text("You can get a health buff from starting"
                                       + " the raid together with friends."),
                        Component.text("\n\nAt the end of the raid,"
                                       + " there's a treasure chest."
                                       + " All participants receive"
                                       + " a "),
                        Mytems.HEART.component,
                        Component.text("friendship bonus with each other."),
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
            condStash.progress(playerQuest);
            break;
        case RAID_START:
            condStart.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class RaidProgress extends GoalProgress {
    protected boolean join;
    protected boolean stash;
    protected boolean start;

    @Override
    public boolean isComplete() {
        return join && stash && start;
    }
}
