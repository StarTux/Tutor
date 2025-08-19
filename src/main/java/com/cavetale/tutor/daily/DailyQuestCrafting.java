package com.cavetale.tutor.daily;

import com.cavetale.core.item.ItemKinds;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @RequiredArgsConstructor
    public enum Materials {
        // GOOD
        CAKE(Material.CAKE),
        PUMPKIN_PIE(Material.PUMPKIN_PIE),
        RABBIT_STEW(Material.RABBIT_STEW),
        GOLDEN_APPLE(Material.GOLDEN_APPLE),
        GOLDEN_CARROT(Material.GOLDEN_CARROT),
        COOKIE(Material.COOKIE),
        BEETROOT_SOUP(Material.BEETROOT_SOUP),
        // Decoration
        BARREL(Material.BARREL),
        BOOKSHELF(Material.BOOKSHELF),
        PAINTING(Material.PAINTING),
        SCAFFOLDING(Material.SCAFFOLDING),
        // Useful
        BOOK(Material.BOOK),
        FIREWORK_ROCKET(Material.FIREWORK_ROCKET),
        TNT(Material.TNT),
        LODESTONE(Material.LODESTONE),
        // Redstone
        REPEATER(Material.REPEATER),
        COMPARATOR(Material.COMPARATOR),
        PISTON(Material.PISTON),
        OBSERVER(Material.OBSERVER),
        DISPENSER(Material.DISPENSER),
        ;

        private final Material material;
    };

    @Override
    public void onGenerate(final String name) {
        this.details.material = Materials.valueOf(name.toUpperCase());
        this.total = 1;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Craft "),
                              ItemKinds.icon(new ItemStack(details.material.material)));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Craft one "),
                              ItemKinds.chatDescription(new ItemStack(details.material.material)),
                              text(" in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = new ItemStack(details.material.material);
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    protected void onCraftItem(Player player, PlayerDailyQuest playerDailyQuest, CraftItemEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        ItemStack result = event.getInventory().getResult();
        if (result == null || details.material.material != result.getType()) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.material.material, Math.min(details.material.material.getMaxStackSize(), 5)));
    }

    public static final class Details extends DailyQuest.Details {
        protected Materials material = Materials.CAKE;
    }
}
