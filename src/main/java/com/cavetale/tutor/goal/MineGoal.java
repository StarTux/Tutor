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
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    private static final int IRON = 24;
    private static final int DIAMOND = 9;
    protected final CheckboxCondition condMine;
    protected final NumberCondition condIron;
    protected final NumberCondition condDiamond;
    protected final CheckboxCondition condDungeon;

    public MineGoal() {
        this.id = "mine";
        this.displayName = Component.text("The Mining World");
        condMine = new CheckboxCondition(Component.text("Warp to the mining world"),
                                         playerQuest -> getProgress(playerQuest).mine,
                                         playerQuest -> getProgress(playerQuest).mine = true);
        condIron = new NumberCondition(Component.text("Mine iron ore"), IRON,
                                       playerQuest -> getProgress(playerQuest).iron,
                                       (playerQuest, amount) -> getProgress(playerQuest).iron = amount,
                                       playerQuest -> getProgress(playerQuest).mine);
        condDiamond = new NumberCondition(Component.text("Mine diamond ore"), DIAMOND,
                                          playerQuest -> getProgress(playerQuest).diamond,
                                          (playerQuest, amount) -> getProgress(playerQuest).diamond = amount,
                                          playerQuest -> getProgress(playerQuest).mine);
        condDungeon = new CheckboxCondition(Component.text("Loot a Dungeon"),
                                            playerQuest -> getProgress(playerQuest).dungeon,
                                            playerQuest -> getProgress(playerQuest).dungeon = true,
                                            playerQuest -> getProgress(playerQuest).mine);
        condMine.setBookPageIndex(0);
        condDungeon.setBookPageIndex(2);
        this.conditions = Arrays.asList(new Condition[] {
                condMine,
                condIron,
                condDiamond,
                condDungeon,
            });
        this.constraints = Arrays.asList(new MiningWorldConstraint());
        this.additionalBookPages = Arrays.asList(new Component[] {
                // Mine World
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("The mining world is there for you to get your resources from."
                                       + " Not only do you keep the home worlds pristine by using this to harvest:"
                                       + " There are also way more ores to be found here!"
                                       + " This world is reset once a week."),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Mining World Commands:"),
                        Component.text("\n/mine", NamedTextColor.DARK_BLUE),
                        Component.text("\nView biome list. Click for a warp", NamedTextColor.GRAY),
                        Component.newline(),
                        Component.text("\n/mine random", NamedTextColor.DARK_BLUE),
                        Component.text("\nWarp to a random biome", NamedTextColor.GRAY),
                    }),
                // Dungeons
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Not only are there custom caves with bonus ores in the mining world,"
                                       + " it also offers custom dungeons,"
                                       + " built in the past by players just like you!"
                                       + "\nDungeon chests contain special loot."
                                       + " Make sure to pick it up!"),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("Locating a dungeon:\n"),
                        Component.text("right-click", NamedTextColor.DARK_BLUE, TextDecoration.UNDERLINED),
                        Component.text(" a "),
                        VanillaItems.componentOf(Material.COMPASS),
                        Component.text("compass", NamedTextColor.DARK_BLUE, TextDecoration.UNDERLINED),
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
                    pet.addSpeechBubble(50L, 100, new Component[] {
                            Component.text("Have you heard of"),
                            Component.text("the mining world?"),
                        });
                    pet.addSpeechBubble(100, new Component[] {
                            Component.text("This is where we gather"),
                            Component.text("most of our resources."),
                        });
                    pet.addSpeechBubble(100, new Component[] {
                            Component.text("The world resets"),
                            Component.text("weekly, so there's"),
                            Component.text("always more stuff."),
                        });
                });
        }
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
        switch (name) {
        case USE_MINE:
            if (condMine.progress(playerQuest)) {
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
                    condDiamond.progress(playerQuest, 1);
                } else if (mat == Material.IRON_ORE) {
                    condIron.progress(playerQuest, 1);
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
