package com.cavetale.tutor.goal;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.block.PluginBlockEvent;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
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
 * Learn about the mining world.
 * - Enter the command
 * - Explore
 * - Find resources
 * - Enter dungeons
 */
public final class MineGoal implements Goal, Listener {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;
    private static final int IRON = 24;
    private static final int DIAMOND = 9;

    public MineGoal() {
        this.id = "mine";
        this.displayName = Component.text("The Mining World");
        Condition[] conds = new Condition[] {
            new CheckboxCondition(Component.text("Warp to the mining world"), playerQuest -> getProgress(playerQuest).mine),
            new NumberCondition(Component.text("Mine iron ore"),
                                playerQuest -> NumberProgress.of(getProgress(playerQuest).iron, IRON),
                                playerQuest -> getProgress(playerQuest).mine),
            new NumberCondition(Component.text("Mine diamond ore"),
                                playerQuest -> NumberProgress.of(getProgress(playerQuest).diamond, DIAMOND),
                                playerQuest -> getProgress(playerQuest).mine),
            new CheckboxCondition(Component.text("Loot a Dungeon"),
                                  playerQuest -> getProgress(playerQuest).dungeon,
                                  playerQuest -> getProgress(playerQuest).mine),
        };
        Component[] pages = new Component[] {
            // Mine World
            TextComponent.ofChildren(new Component[] {
                    Component.text("The mining world is there for you to get your resources from."
                                   + " Not only do you keep the home worlds pristine by using this to harvest:"
                                   + " There are also way more ores to be found here!"
                                   + " This world is reset once a week."),
                }),
            TextComponent.ofChildren(new Component[] {
                    Component.text("Mining World Commands:"),
                    Component.text("\n/mine", NamedTextColor.DARK_BLUE),
                    Component.text("\nView biome list. Click for a warp", NamedTextColor.GRAY),
                    Component.newline(),
                    Component.text("\n/mine random", NamedTextColor.DARK_BLUE),
                    Component.text("\nWarp to a random biome", NamedTextColor.GRAY),
                }),
            // Dungeons
            TextComponent.ofChildren(new Component[] {
                    Component.text("Not only are there custom caves with bonus ores in the mining world,"
                                   + " it also offers custom dungeons, built by players just like you!"
                                   + "\nDungeon chests contain special loot."
                                   + " Make sure to pick it up!"),
                }),
            TextComponent.ofChildren(new Component[] {
                    Component.text("Locating a dungeon:\n"),
                    Component.text("right-click", NamedTextColor.DARK_BLUE, TextDecoration.UNDERLINED),
                    Component.text(" a "),
                    VanillaItems.componentOf(Material.COMPASS),
                    Component.text("compass", NamedTextColor.DARK_BLUE, TextDecoration.UNDERLINED),
                    Component.text("\nFollow its directions."
                                   + " You have to be deep enough underground for this to work.", NamedTextColor.GRAY),
                }),
        };
        this.conditions = Arrays.asList(conds);
        this.additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100, new Component[] {
                        Component.text("Have you heard of"),
                        Component.text("the mining world?"),
                    });
                pet.addSpeechBubble(100, new Component[] {
                        Component.text("This is where we gather"),
                        Component.text("most of our resources."),
                    });
                pet.addSpeechBubble(100, new Component[] {
                        Component.text("The world resets weekly,"),
                        Component.text("so there's always more stuff."),
                    });
            });
    }

    @Override
    public MineProgress newProgress() {
        return new MineProgress();
    }

    @Override
    public MineProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(MineProgress.class, MineProgress::new);
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        MineProgress progress = getProgress(playerQuest);
        switch (name) {
        case USE_MINE:
            if (!progress.mine) {
                progress.mine = true;
                playerQuest.onProgress(progress);
                playerQuest.getSession().applyPet(pet -> {
                        pet.addSpeechBubble(100, new Component[] {
                                Component.text("Here we are in the mining world..."),
                            });
                        pet.addSpeechBubble(100, new Component[] {
                                Component.text("Let's find some stuff!"),
                            });
                    });
            }
            break;
        case DUNGEON_LOOT:
            if (!progress.dungeon) {
                progress.dungeon = true;
                playerQuest.onProgress(progress);
            }
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
        String worldName = block.getWorld().getName();
        if (!worldName.equals("mine") && !worldName.startsWith("mine_")) return;
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
                MineProgress progress = getProgress(playerQuest);
                if (mat == Material.DIAMOND_ORE) {
                    if (progress.diamond < DIAMOND) {
                        progress.diamond += 1;
                        playerQuest.onProgress(progress);
                    }
                } else if (mat == Material.IRON_ORE) {
                    if (progress.iron < IRON) {
                        progress.iron += 1;
                        playerQuest.onProgress(progress);
                    }
                }
            });
    }

    protected static final class MineProgress extends GoalProgress {
        protected boolean mine;
        protected int iron;
        protected int diamond;
        protected boolean dungeon;

        @Override
        public boolean isComplete() {
            return mine
                && iron >= IRON
                && diamond >= DIAMOND
                && dungeon;
        }
    }
}
