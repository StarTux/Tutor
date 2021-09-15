package com.cavetale.tutor.goal;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.block.PluginBlockEvent;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Explore the mining world.
 * - Find resources
 * - Raid dungeons
 */
public final class MiningGoal implements Goal, Listener {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    private static final int IRON = 24;
    private static final int DIAMOND = 9;
    private static final int DUNGEONS = 2;
    protected final NumberCondition condIron;
    protected final NumberCondition condDiamond;
    protected final NumberCondition condDungeon;

    public MiningGoal() {
        this.id = "mining";
        this.displayName = Component.text("Explore the Mining World");
        condIron = new NumberCondition(Component.text("Mine iron ore"), IRON,
                                       playerQuest -> getProgress(playerQuest).iron,
                                       (playerQuest, amount) -> getProgress(playerQuest).iron = amount);
        condDiamond = new NumberCondition(Component.text("Mine diamond ore"), DIAMOND,
                                          playerQuest -> getProgress(playerQuest).diamond,
                                          (playerQuest, amount) -> getProgress(playerQuest).diamond = amount);
        condDungeon = new NumberCondition(Component.text("Loot some Dungeon"), DUNGEONS,
                                          playerQuest -> getProgress(playerQuest).dungeons,
                                          (playerQuest, amount) -> getProgress(playerQuest).dungeons = amount);
        condIron.setBookPageIndex(0);
        condDiamond.setBookPageIndex(0);
        condDungeon.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condIron,
                condDiamond,
                condDungeon,
            });
        this.constraints = List.of(new MiningWorldConstraint());
        this.additionalBookPages = List.of(new Component[] {
                // Mining
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("The mining world is filled with special caves,"
                                       + " where the walls are are covered with veins of valuable ores."
                                       + " Maybe we will across one of them."
                                       + "\n\nThis is the best place to gather resources,"
                                       + " because it resets every week. Let's get to it!"),
                    }),
                // Dungeons
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Not only are there custom caves with bonus ores in the mining world,"
                                       + " it also offers custom dungeons,"
                                       + " built in the past by players just like you!"
                                       + "\nDungeon chests contain special loot."
                                       + " Make sure to pick it up!"),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Locating a dungeon:\n"),
                        Component.text("right-click", NamedTextColor.BLUE, TextDecoration.UNDERLINED),
                        Component.text(" a "),
                        VanillaItems.componentOf(Material.COMPASS),
                        Component.text("compass", NamedTextColor.BLUE, TextDecoration.UNDERLINED),
                        Component.text("\nFollow its directions."
                                       + " You have to be deep enough underground"
                                       + " in the mining world for this to work", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 0L, 100, new Component[] {
                            Component.text("Here we are in the"),
                            Component.text("mining world..."),
                        });
                    pet.addSpeechBubble(id, 0L, 100, new Component[] {
                            Component.text("Let's find some"),
                            Component.text("stuff, " + pet.getType().speechGimmick + "!"),
                        });
                });
        }
    }

    @Override
    public MiningProgress newProgress() {
        return new MiningProgress();
    }

    @Override
    public MiningProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(MiningProgress.class, MiningProgress::new);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case DUNGEON_LOOT:
            condDungeon.progress(playerQuest);
            break;
        default: break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockBreak(event.getPlayer(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreakBlock(PlayerBreakBlockEvent event) {
        onBlockBreak(event.getPlayer(), event.getBlock());
    }

    private void onBlockBreak(Player player, Block block) {
        final Material blockMat = block.getType();
        final Material mat;
        if (Tag.IRON_ORES.isTagged(blockMat)) {
            mat = Material.IRON_ORE;
        } else if (Tag.DIAMOND_ORES.isTagged(blockMat)) {
            mat = Material.DIAMOND_ORE;
        } else {
            return;
        }
        if (!PluginBlockEvent.Action.NATURAL.call(TutorPlugin.getInstance(), block)) {
            return;
        }
        TutorPlugin.getInstance().getSessions().applyGoals(player, (playerQuest, goal) -> {
                if (goal != this) return;
                if (mat == Material.DIAMOND_ORE) {
                    condDiamond.progress(playerQuest);
                } else if (mat == Material.IRON_ORE) {
                    condIron.progress(playerQuest);
                }
            });
    }

    protected static final class MiningProgress extends GoalProgress {
        protected int iron;
        protected int diamond;
        protected int dungeons;

        @Override
        public boolean isComplete() {
            return iron >= IRON
                && diamond >= DIAMOND
                && dungeons >= DUNGEONS;
        }
    }
}
