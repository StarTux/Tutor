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
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.exploits.PlayerPlacedBlocks.isPlayerPlaced;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestChopTrees extends DailyQuest<DailyQuestChopTrees.Details, DailyQuest.Progress> {
    public DailyQuestChopTrees() {
        super(DailyQuestType.CHOP_TREES,
              DailyQuestChopTrees.Details.class, DailyQuestChopTrees.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Wood {
        ACACIA("Acacia", VanillaItems.ACACIA_LOG, Material.ACACIA_LOG, Tag.ACACIA_LOGS.getValues()),
        BIRCH("Birch", VanillaItems.BIRCH_LOG, Material.BIRCH_LOG, Tag.BIRCH_LOGS.getValues()),
        CHERRY("Cherry", VanillaItems.CHERRY_LOG, Material.CHERRY_LOG, Tag.CHERRY_LOGS.getValues()),
        DARK_OAK("Dark Oak", VanillaItems.DARK_OAK_LOG, Material.DARK_OAK_LOG, Tag.DARK_OAK_LOGS.getValues()),
        JUNGLE("Jungle", VanillaItems.JUNGLE_LOG, Material.JUNGLE_LOG, Tag.JUNGLE_LOGS.getValues()),
        MANGROVE("Mangrove", VanillaItems.MANGROVE_LOG, Material.MANGROVE_LOG, Tag.MANGROVE_LOGS.getValues()),
        OAK("Oak", VanillaItems.OAK_LOG, Material.OAK_LOG, Tag.OAK_LOGS.getValues()),
        SPRUCE("Spruce", VanillaItems.SPRUCE_LOG, Material.SPRUCE_LOG, Tag.SPRUCE_LOGS.getValues()),
        ;

        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material material;
        protected final Set<Material> blockMaterials;
    }

    @Override
    public void onGenerate(final String name) {
        this.total = (1 + random.nextInt(10)) * 10;
        this.details.wood = Wood.valueOf(name.toUpperCase());
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Chop " + total + Unicode.MULTIPLICATION.string), details.wood.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Chop " + total + " "),
                              details.wood.chatIcon,
                              text(" " + details.wood.displayName + "."
                                   + " Tree chopping must be done in survival mode."
                                   + " The tree blocks must not be player placed."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.wood.material, total);
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        Block block = event.getBlock();
        if (!details.wood.blockMaterials.contains(block.getType())) return;
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
        if (!details.wood.blockMaterials.contains(block.getType())) return;
        if (isPlayerPlaced(block)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.wood.material, Math.min(64, total)));
    }

    public static final class Details extends DailyQuest.Details {
        protected Wood wood = Wood.OAK;
    }
}
