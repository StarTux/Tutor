package com.cavetale.tutor.daily;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestPickFlowers extends DailyQuest<DailyQuestPickFlowers.Details, DailyQuest.Progress> {
    public DailyQuestPickFlowers() {
        super(DailyQuestType.PICK_FLOWERS,
              DailyQuestPickFlowers.Details.class, DailyQuestPickFlowers.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Flower {
        DANDELION(10, Material.DANDELION),
        POPPY(10, Material.POPPY),
        BLUE_ORCHID(10, Material.BLUE_ORCHID),
        ALLIUM(10, Material.ALLIUM),
        AZURE_BLUET(10, Material.AZURE_BLUET),
        TULIP(10, "Tulip", VanillaItems.PINK_TULIP, Material.PINK_TULIP, Set.of(Material.RED_TULIP,
                                                                                Material.ORANGE_TULIP,
                                                                                Material.WHITE_TULIP,
                                                                                Material.PINK_TULIP)),
        OXEYE_DAISY(10, Material.OXEYE_DAISY),
        CORNFLOWER(10, Material.CORNFLOWER),
        LILY_OF_THE_VALLEY(10, Material.LILY_OF_THE_VALLEY),
        WITHER_ROSE(3, Material.WITHER_ROSE),
        SUNFLOWER(10, Material.SUNFLOWER),
        LILAC(10, Material.LILAC),
        ROSE_BUSH(10, Material.ROSE_BUSH),
        PEONY(10, Material.PEONY),
        ;

        Flower(final int total, final Material material) {
            this(total, toCamelCase(" ", material), VanillaItems.componentOf(material), material, Set.of(material));
        }

        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material iconMaterial;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate(final String name) {
        this.details.flower = Flower.valueOf(name.toUpperCase());
        this.total = details.flower.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Pick " + total + Unicode.MULTIPLICATION.string), details.flower.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Pick " + total + " "),
                              details.flower.chatIcon,
                              text(" natural " + details.flower.displayName + " flowers in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.flower.iconMaterial, total);
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        final Block block = event.getBlock();
        if (!details.flower.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected void onPlayerBreakBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerBreakBlockEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        final Block block = event.getBlock();
        if (!details.flower.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.flower.iconMaterial, details.flower.iconMaterial.getMaxStackSize()));
    }

    public static final class Details extends DailyQuest.Details {
        protected Flower flower = Flower.DANDELION;
    }
}
