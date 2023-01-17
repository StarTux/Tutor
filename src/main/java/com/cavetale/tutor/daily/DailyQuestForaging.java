package com.cavetale.tutor.daily;

import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestForaging extends DailyQuest<DailyQuestForaging.Details, DailyQuest.Progress> {
    public DailyQuestForaging() {
        super(DailyQuestType.FORAGING,
              DailyQuestForaging.Details.class, DailyQuestForaging.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Forage {
        BROWN_MUSHROOM(5, "Brown Mushrooms", VanillaItems.BROWN_MUSHROOM, Material.BROWN_MUSHROOM, Set.of(Material.BROWN_MUSHROOM)),
        RED_MUSHROOM(5, "Red Mushrooms", VanillaItems.RED_MUSHROOM, Material.RED_MUSHROOM, Set.of(Material.RED_MUSHROOM)),
        CRIMSON_FUNGUS(5, "Crimson Fungi", VanillaItems.CRIMSON_FUNGUS, Material.CRIMSON_FUNGUS, Set.of(Material.CRIMSON_FUNGUS)),
        WARPED_FUNGUS(5, "Warped Fungi", VanillaItems.WARPED_FUNGUS, Material.WARPED_FUNGUS, Set.of(Material.WARPED_FUNGUS)),
        CHORUS_FLOWER(5, "Chorus Flower", VanillaItems.CHORUS_FLOWER, Material.CHORUS_FLOWER, Set.of(Material.CHORUS_FLOWER)),
        LILY_PAD(5, "Lily Pads", VanillaItems.LILY_PAD, Material.LILY_PAD, Set.of(Material.LILY_PAD)),
        SPORE_BLOSSOM(1, "Spore Blossom", VanillaItems.SPORE_BLOSSOM, Material.SPORE_BLOSSOM, Set.of(Material.SPORE_BLOSSOM)),
        ;

        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material iconMaterial;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate() {
        Forage[] forages = Forage.values();
        this.details.forage = forages[ThreadLocalRandom.current().nextInt(forages.length)];
        this.total = details.forage.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Forage " + total + Unicode.MULTIPLICATION.string), details.forage.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Forage " + total + " "),
                              details.forage.chatIcon,
                              text(" natural " + details.forage.displayName + " in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.forage.iconMaterial, total);
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        final Block block = event.getBlock();
        if (!details.forage.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected void onPlayerBreakBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerBreakBlockEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        final Block block = event.getBlock();
        if (!details.forage.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    public static final class Details extends DailyQuest.Details {
        protected Forage forage = Forage.BROWN_MUSHROOM;
    }
}
