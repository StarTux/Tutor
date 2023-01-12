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
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestMining extends DailyQuest<DailyQuestMining.Details, DailyQuest.Progress> {
    public DailyQuestMining() {
        super(DailyQuestType.MINING,
              DailyQuestMining.Details.class, DailyQuestMining.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Ore {
        COAL("Coal Ore", VanillaItems.COAL_ORE, Tag.COAL_ORES.getValues()),
        COPPER("Copper Ore", VanillaItems.COPPER_ORE, Tag.COPPER_ORES.getValues()),
        DIAMOND("Diamond Ore", VanillaItems.DIAMOND_ORE, Tag.DIAMOND_ORES.getValues()),
        EMERALD("Emerald Ore", VanillaItems.EMERALD_ORE, Tag.EMERALD_ORES.getValues()),
        GOLD("Gold Ore", VanillaItems.GOLD_ORE, Tag.GOLD_ORES.getValues()),
        IRON("Iron Ore", VanillaItems.IRON_ORE, Tag.IRON_ORES.getValues()),
        LAPIS("Lapis Ore", VanillaItems.LAPIS_ORE, Tag.LAPIS_ORES.getValues()),
        QUARTZ("Nether Quartz Ore", VanillaItems.NETHER_QUARTZ_ORE, Set.of(Material.NETHER_QUARTZ_ORE)),
        REDSTONE("Redstone Ore", VanillaItems.REDSTONE_ORE, Tag.REDSTONE_ORES.getValues()),
        ;

        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate() {
        this.total = 10;
        Ore[] ores = Ore.values();
        this.details.ore = ores[ThreadLocalRandom.current().nextInt(ores.length)];
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Mine " + total + Unicode.MULTIPLICATION.string), details.ore.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Mine " + total + " "),
                              details.ore.chatIcon,
                              text(" " + details.ore.displayName + "."
                                   + " Mining must be done in survival mode."
                                   + " The block must not be player placed."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = Mytems.HASTY_PICKAXE.createIcon();
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        Block block = event.getBlock();
        if (!details.ore.blockMaterials.contains(block.getType())) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || !block.isPreferredTool(tool)) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected void onPlayerBreakBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerBreakBlockEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        Block block = event.getBlock();
        if (!details.ore.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    public static final class Details extends DailyQuest.Details {
        protected Ore ore = Ore.DIAMOND;
    }
}
