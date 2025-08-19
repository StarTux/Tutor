package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestHarvest extends DailyQuest<DailyQuestHarvest.Details, DailyQuest.Progress> {
    public DailyQuestHarvest() {
        super(DailyQuestType.HARVEST,
              DailyQuestHarvest.Details.class, DailyQuestHarvest.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    protected enum Growth {
        /**
         * Aged crops need to be in their final growth stage.
         */
        AGE,
        /**
         * These crops poof into existence and need to be not player
         * placed.
         */
        POOF,
        /**
         * Regrowing crops are not to be broken but harvested with the
         * appropriate event.
         */
        REGROW;
    }

    /**
     * These are all crops which can be reasonably harvested by a well
     * established player.  Chorus flower could be too tricky.
     */
    @RequiredArgsConstructor
    protected enum Crop {
        WHEAT(Growth.AGE, 150, "Wheat", VanillaItems.WHEAT, Material.WHEAT, Set.of(Material.WHEAT)),
        BEETROOT(Growth.AGE, 100, "Beetroots", VanillaItems.BEETROOT, Material.BEETROOT, Set.of(Material.BEETROOTS)),
        POTATO(Growth.AGE, 100, "Potatoes", VanillaItems.POTATO, Material.POTATO, Set.of(Material.POTATOES)),
        CARROT(Growth.AGE, 100, "Carrots", VanillaItems.CARROT, Material.CARROT, Set.of(Material.CARROTS)),
        NETHER_WART(Growth.AGE, 20, "Nether Warts", VanillaItems.NETHER_WART, Material.NETHER_WART, Set.of(Material.NETHER_WART)),
        CACTUS(Growth.POOF, 10, "Cacti", VanillaItems.CACTUS, Material.CACTUS, Set.of(Material.CACTUS)),
        SUGAR_CANE(Growth.POOF, 10, "Sugar Cane", VanillaItems.SUGAR_CANE, Material.SUGAR_CANE, Set.of(Material.SUGAR_CANE)),
        MELON(Growth.POOF, 10, "Melons", VanillaItems.MELON_SLICE, Material.MELON_SLICE, Set.of(Material.MELON)),
        PUMPKIN(Growth.POOF, 10, "Pumpkins", VanillaItems.PUMPKIN, Material.PUMPKIN, Set.of(Material.PUMPKIN, Material.CARVED_PUMPKIN)),
        SWEET_BERRY(Growth.REGROW, 10, "Sweet Berries", VanillaItems.SWEET_BERRIES, Material.SWEET_BERRIES, Set.of(Material.SWEET_BERRY_BUSH)),
        COCOA(Growth.AGE, 5, "Cocoa Beans", VanillaItems.COCOA_BEANS, Material.COCOA_BEANS, Set.of(Material.COCOA)),
        GLOW_BERRY(Growth.REGROW, 3, "Glow Berries", VanillaItems.GLOW_BERRIES, Material.GLOW_BERRIES, Set.of(Material.CAVE_VINES)),
        KELP(Growth.POOF, 10, "Kelp", VanillaItems.KELP, Material.KELP, Set.of(Material.KELP, Material.KELP_PLANT)),
        SEA_PICKLE(Growth.POOF, 5, "Sea Pickles", VanillaItems.SEA_PICKLE, Material.SEA_PICKLE, Set.of(Material.SEA_PICKLE)),
        // SPORE_BLOSSOM // HARD
        ;

        protected final Growth growth;
        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material iconMaterial;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate(final String name) {
        this.details.crop = Crop.valueOf(name.toUpperCase());
        this.total = details.crop.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Harvest " + total + Unicode.MULTIPLICATION.string), details.crop.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return switch (details.crop.growth) {
        case POOF -> textOfChildren(text("Harvest " + total + " "),
                                    details.crop.chatIcon,
                                    text(" natural " + details.crop.displayName + " in survival mode."));
        case REGROW -> textOfChildren(text("Harvest " + total + " "),
                                      details.crop.chatIcon,
                                      text(" " + details.crop.displayName + " in survival mode."));
        case AGE -> textOfChildren(text("Harvest " + total + " "),
                                   details.crop.chatIcon,
                                   text(" fully grown " + details.crop.displayName + " in survival mode."));
        };
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.crop.iconMaterial, total);
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (details.crop.growth == Growth.REGROW) {
            return;
        }
        final Block block = event.getBlock();
        if (!details.crop.blockMaterials.contains(block.getType())) return;
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (details.crop.growth == Growth.AGE) {
            if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                return;
            }
        } else if (details.crop.growth == Growth.POOF) {
            if (isPlayerPlaced(block)) return;
        }
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected void onPlayerBreakBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerBreakBlockEvent event) {
        if (details.crop.growth == Growth.REGROW) {
            return;
        }
        final Block block = event.getBlock();
        if (!details.crop.blockMaterials.contains(block.getType())) return;
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (details.crop.growth == Growth.AGE) {
            if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                return;
            }
        } else if (details.crop.growth == Growth.POOF) {
            if (isPlayerPlaced(block)) return;
        }
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected void onPlayerHarvestBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerHarvestBlockEvent event) {
        if (details.crop.growth != Growth.REGROW) {
            return;
        }
        final Block block = event.getHarvestedBlock();
        if (!details.crop.blockMaterials.contains(block.getType())) return;
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.crop.iconMaterial, details.crop.iconMaterial.getMaxStackSize()),
                       new ItemStack(Material.BONE_MEAL, 64));
    }

    public static final class Details extends DailyQuest.Details {
        protected Crop crop = Crop.WHEAT;
    }
}
