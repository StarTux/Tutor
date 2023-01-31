package com.cavetale.tutor.daily;

import com.cavetale.core.item.ItemKinds;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

/**
 * Crafting Quest.  We only demand one item crafted.  All items should:
 * - Be continually useful, not a one time thing
 * - Not be farmed more easily than crafted
 * - Have an interesting recipe
 */
public final class DailyQuestCrafting extends DailyQuest<DailyQuestCrafting.Details, DailyQuest.Progress> {
    public DailyQuestCrafting() {
        super(DailyQuestType.CRAFTING,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    public static final Material[] MATERIALS = {
        // GOOD
        Material.CAKE,
        Material.PUMPKIN_PIE,
        Material.RABBIT_STEW,
        Material.GOLDEN_APPLE,
        Material.GOLDEN_CARROT,
        Material.COOKIE,
        Material.BEETROOT_SOUP,
        // Decoration
        Material.BARREL,
        Material.BOOKSHELF,
        Material.PAINTING,
        Material.SCAFFOLDING,
        // Useful
        Material.BOOK,
        Material.FIREWORK_ROCKET,
        Material.TNT,
        // Redstone
        Material.REPEATER,
        Material.COMPARATOR,
        Material.PISTON,
        Material.OBSERVER,
        Material.DISPENSER,
    };

    @Override
    public void onGenerate() {
        this.details.material = MATERIALS[random.nextInt(MATERIALS.length)];
        this.total = 1;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Craft "),
                              ItemKinds.icon(new ItemStack(details.material)));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Craft one "),
                              ItemKinds.chatDescription(new ItemStack(details.material)),
                              text(" in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = new ItemStack(details.material);
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    protected void onCraftItem(Player player, PlayerDailyQuest playerDailyQuest, CraftItemEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        ItemStack result = event.getInventory().getResult();
        if (result == null || details.material != result.getType()) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.material, Math.min(details.material.getMaxStackSize(), 5)));
    }

    public static final class Details extends DailyQuest.Details {
        protected Material material = Material.CAKE;
    }
}
