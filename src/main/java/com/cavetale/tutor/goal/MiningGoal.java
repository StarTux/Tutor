package com.cavetale.tutor.goal;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.block.PluginBlockEvent;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Explore the mining world.
 * - Find Resources
 * - Raid Cavetale Dungeons
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
        this.displayName = text("Explore the Mining World");
        condIron = new NumberCondition(text("Mine iron ore"), IRON,
                                       playerQuest -> getProgress(playerQuest).iron,
                                       (playerQuest, amount) -> getProgress(playerQuest).iron = amount);
        condDiamond = new NumberCondition(text("Mine diamond ore"), DIAMOND,
                                          playerQuest -> getProgress(playerQuest).diamond,
                                          (playerQuest, amount) -> getProgress(playerQuest).diamond = amount);
        condDungeon = new NumberCondition(textOfChildren(text("Loot "), Mytems.CAVETALE_DUNGEON, text("Dungeons")),
                                          DUNGEONS,
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
                // Mining, pg 0
                textOfChildren(text("The mining world is filled with special caves,"
                                    + " where the walls are are covered with veins of valuable ores."
                                    + " Maybe we will come across one of them."
                                    + "\n\nThis is the best place to gather resources,"
                                    + " because it resets every week. Let's get to it!")),
                // Dungeons, pg 1
                textOfChildren(text("Not only are there custom caves with bonus ores in the mining world,"
                                    + " it also offers "),
                               Mytems.CAVETALE_DUNGEON,
                               text("Cavetale Dungeons", BLUE),
                               text(", built during past build events."),
                               newline(), newline(),
                               text("Dungeon chests contain special loot."
                                    + " Make sure to pick it up.")),
                // pg 2
                textOfChildren(text("Locating ", BLUE), Mytems.CAVETALE_DUNGEON, text("Cavetale Dungeons", BLUE),
                               newline(), newline(),
                               Mytems.MOUSE_RIGHT, text(" a "),
                               VanillaItems.COMPASS, text("Compass", BLUE, TextDecoration.UNDERLINED),
                               newline(),
                               text("Follow its directions."
                                    + " You have to be deep enough underground"
                                    + " in the mining world for this to work", GRAY)),
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
                            text("Here we are in the"),
                            text("mining world..."),
                        });
                    pet.addSpeechBubble(id, 0L, 100, new Component[] {
                            text("Let's find some"),
                            text("stuff, " + pet.getType().speechGimmick + "!"),
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockBreak(event.getPlayer(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
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
