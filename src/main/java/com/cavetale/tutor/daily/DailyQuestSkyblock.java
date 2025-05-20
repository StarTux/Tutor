package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
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
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;

public final class DailyQuestSkyblock extends DailyQuest<DailyQuestSkyblock.Details, DailyQuest.Progress> {
    public DailyQuestSkyblock() {
        super(DailyQuestType.SKYBLOCK,
              DailyQuestSkyblock.Details.class, DailyQuestSkyblock.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Task {
        PUNCH_TREE(4, textOfChildren(text("Punch "), VanillaItems.GRASS_BLOCK, text("Skyblock Tree"))),
        COBBLE(3, textOfChildren(text("Mine "), VanillaItems.GRASS_BLOCK, text("Skyblock Cobble"))),
        ;

        protected final int total;
        protected final Component description;
    }

    @Override
    public void onGenerate(final int index) {
        Task[] tasks = Task.values();
        this.details.task = tasks[index];
        this.total = details.task.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return details.task.description;
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(details.task.description,
                              text(". Join the Skyblock server via "),
                              (text("/skyblock", BLUE)
                               .hoverEvent(showText(text("/skyblock", BLUE)))
                               .clickEvent(runCommand("/skyblock"))),
                              text("."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(Material.GRASS_BLOCK);
    }

    @Override
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) {
        if (!NetworkServer.SKYBLOCK.isThisServer()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        final Block block = event.getBlock();
        if (isPlayerPlaced(block)) return;
        switch (details.task) {
        case PUNCH_TREE:
            if (Tag.LOGS.isTagged(event.getBlock().getType())) {
                makeProgress(playerDailyQuest, 1);
            }
            break;
        case COBBLE:
            if (Material.COBBLESTONE == event.getBlock().getType()) {
                makeProgress(playerDailyQuest, 1);
            }
            break;
        default: break;
        }
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(Mytems.RUBY.createItemStack(),
                       new ItemStack(Material.GRASS_BLOCK, 64),
                       Mytems.RUBY.createItemStack());
    }

    public static final class Details extends DailyQuest.Details {
        protected Task task = Task.PUNCH_TREE;
    }
}
