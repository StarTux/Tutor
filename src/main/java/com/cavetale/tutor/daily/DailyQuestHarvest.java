package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
import org.bukkit.inventory.ItemFlag;
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
        AGE,
        POOF,
        REGROW;
    }

    @RequiredArgsConstructor
    protected enum Crop {
        WHEAT(Growth.AGE, 150, "Wheat", VanillaItems.WHEAT, Set.of(Material.WHEAT)),
        BEETROOT(Growth.AGE, 100, "Beetroots", VanillaItems.BEETROOT, Set.of(Material.BEETROOTS)),
        POTATO(Growth.AGE, 100, "Potatoes", VanillaItems.POTATO, Set.of(Material.POTATOES)),
        CARROT(Growth.AGE, 100, "Carrots", VanillaItems.CARROT, Set.of(Material.CARROTS)),
        NETHER_WART(Growth.AGE, 20, "Nether Warts", VanillaItems.NETHER_WART, Set.of(Material.NETHER_WART)),
        CACTUS(Growth.POOF, 10, "Cacti", VanillaItems.CACTUS, Set.of(Material.CACTUS)),
        SUGAR_CANE(Growth.POOF, 10, "Sugar Cane", VanillaItems.SUGAR_CANE, Set.of(Material.SUGAR_CANE)),
        CHORUS_FLOWER(Growth.POOF, 5, "Chorus Flower", VanillaItems.CHORUS_FLOWER, Set.of(Material.CHORUS_FLOWER)),
        KELP(Growth.POOF, 10, "Kelp", VanillaItems.KELP, Set.of(Material.KELP, Material.KELP_PLANT)),
        MELON(Growth.POOF, 10, "Melons", VanillaItems.MELON_SLICE, Set.of(Material.MELON)),
        PUMPKIN(Growth.POOF, 10, "Pumpkins", VanillaItems.PUMPKIN, Set.of(Material.PUMPKIN, Material.CARVED_PUMPKIN)),
        SWEET_BERRY(Growth.REGROW, 10, "Sweet Berries", VanillaItems.SWEET_BERRIES, Set.of(Material.SWEET_BERRY_BUSH)),
        COCOA(Growth.AGE, 5, "Cocoa Beans", VanillaItems.COCOA_BEANS, Set.of(Material.COCOA_BEANS)),
        GLOW_BERRY(Growth.REGROW, 3, "Glow Berries", VanillaItems.GLOW_BERRIES, Set.of(Material.CAVE_VINES)),
        BROWN_MUSHROOM(Growth.POOF, 5, "Brown Mushrooms", VanillaItems.BROWN_MUSHROOM, Set.of(Material.BROWN_MUSHROOM)),
        RED_MUSHROOM(Growth.POOF, 5, "Red Mushrooms", VanillaItems.RED_MUSHROOM, Set.of(Material.RED_MUSHROOM)),
        CRIMSON_FUNGUS(Growth.POOF, 5, "Crimson Fungi", VanillaItems.CRIMSON_FUNGUS, Set.of(Material.CRIMSON_FUNGUS)),
        WARPED_FUNGUS(Growth.POOF, 5, "Warped Fungi", VanillaItems.WARPED_FUNGUS, Set.of(Material.WARPED_FUNGUS)),
        SEA_PICKLE(Growth.POOF, 5, "Sea Pickles", VanillaItems.SEA_PICKLE, Set.of(Material.SEA_PICKLE)), // up to 3?
        ;

        protected final Growth growth;
        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate() {
        Crop[] crops = Crop.values();
        this.details.crop = crops[ThreadLocalRandom.current().nextInt(crops.length)];
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
        ItemStack result = Mytems.IRON_SCYTHE.createIcon();
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
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

    public static final class Details extends DailyQuest.Details {
        protected Crop crop = Crop.WHEAT;
    }
}
