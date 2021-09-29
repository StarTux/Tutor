package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class WarpPlaceGoal extends AbstractGoal<WarpPlaceProgress> {
    private final String name;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condWarp;
    protected final CheckboxCondition condNPC;

    public WarpPlaceGoal(final String name, final String displayName, final String merchantName, final Component secondPage) {
        super(WarpPlaceProgress.class, WarpPlaceProgress::new);
        this.name = name;
        this.id = "warp_" + name.toLowerCase();
        this.displayName = Component.text("Visit " + displayName);
        condWarp = new CheckboxCondition(Component.text("Warp to " + displayName),
                                         playerQuest -> getProgress(playerQuest).warp,
                                         playerQuest -> getProgress(playerQuest).warp = true);
        condNPC = new CheckboxCondition(Component.text("Find the " + merchantName),
                                        playerQuest -> getProgress(playerQuest).npc,
                                        playerQuest -> getProgress(playerQuest).npc = true);
        condWarp.setBookPageIndex(0);
        condNPC.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condWarp,
                condNPC,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Like all warps, find this place in the warp list.\n\n"),
                        Component.text("/warp", NamedTextColor.BLUE),
                        Component.text("\nView the warp list\n\n", NamedTextColor.GRAY),
                        Component.text("/warp " + name, NamedTextColor.BLUE),
                        Component.text("\nWarp to " + displayName, NamedTextColor.GRAY),
                    }),
                secondPage,
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_WARP:
            if (Detail.NAME.is(event, name)) {
                condWarp.progress(playerQuest);
            }
            break;
        case INTERACT_NPC:
            if (Detail.NAME.is(event, name)) {
                condNPC.progress(playerQuest);
            }
            break;
        default: break;
        }
    }

    public static WarpPlaceGoal bazaar() {
        return new WarpPlaceGoal("Bazaar", "Bazaar", "Dune Item Shop", Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("This desert market is the result of a build event."
                                   + "\n\nIt is home to the merchant who sells the Dune item set."
                                   + " Let's find them!"),
                }));
    }

    public static WarpPlaceGoal dwarven() {
        return new WarpPlaceGoal("DwarvenVillage", "Dwarven Village", "Dwarf Armor Shop", Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("This underground city was made during a weeklong build event."
                                   + "\n\nSomewhere around here, there's a merchant"
                                   + " who has the Dwarven Armor set in stock."
                                   + " Finding him may prove challenging, but it's worth it!"),
                }));
    }

    public static WarpPlaceGoal cloud() {
        return new WarpPlaceGoal("CloudVillage", "Cloud Village", "Cloud Item Shop", Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("This floating miracle was made by players for a build event."
                                   + "\n\nIt's home to a merchant who sells"
                                   + " two magical items."
                                   + " Elytra is recommended to explore,"
                                   + " but not required to find this guy."),
                }));
    }

    public static WarpPlaceGoal witch() {
        return new WarpPlaceGoal("WitchLair", "Witch Lair", "Swampy Armor Shop", Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("This mysterious marsh stems from a build event."
                                   + "\n\nOne of local merchants offers the Swampy Set."
                                   + " Where could they be?"),
                }));
    }
}

final class WarpPlaceProgress extends GoalProgress {
    protected boolean warp;
    protected boolean npc;

    @Override
    public boolean isComplete() {
        return warp && npc;
    }
}
