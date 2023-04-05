package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class DailyQuestFishing extends DailyQuest<DailyQuestFishing.Details, DailyQuest.Progress> {
    public DailyQuestFishing() {
        super(DailyQuestType.FISHING,
              DailyQuestFishing.Details.class, DailyQuestFishing.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Fish {
        COD(5, "Cod", VanillaItems.COD, Material.COD),
        SALMON(3, "Salmon", VanillaItems.SALMON, Material.SALMON),
        PUFFERFISH(1, "Pufferfish", VanillaItems.PUFFERFISH, Material.PUFFERFISH),
        TROPICAL_FISH(1, "Tropical Fish", VanillaItems.TROPICAL_FISH, Material.TROPICAL_FISH),
        TRASH(3, "Trash Items", text("Trash", RED, ITALIC), Material.FISHING_ROD),
        ;

        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material fishMaterial;
    }

    @Override
    public void onGenerate(final int index) {
        Fish[] fishes = Fish.values();
        this.details.fish = fishes[index];
        this.total = details.fish.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Catch " + total + Unicode.MULTIPLICATION.string), details.fish.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        if (details.fish == Fish.TRASH) {
            return text("Catch " + total + " items which are not fish in survival mode.");
        } else {
            return textOfChildren(text("Catch " + total + " "),
                                  details.fish.chatIcon,
                                  text(" " + details.fish.displayName + " in survival mode."));
        }
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.fish.fishMaterial, total);
    }

    @Override
    protected void onPlayerFish(Player player, PlayerDailyQuest playerDailyQuest, PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!(event.getCaught() instanceof Item entity)) return;
        final ItemStack item = entity.getItemStack();
        if (details.fish == Fish.TRASH) {
            if (Tag.ITEMS_FISHES.isTagged(item.getType())) return;
        } else {
            if (details.fish.fishMaterial != item.getType()) return;
        }
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        if (details.fish == Fish.TRASH) {
            return switch (random.nextInt(4)) {
            case 0 -> {
                final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.editMeta(m -> {
                        List<Enchantment> enchs = new ArrayList<>();
                        for (Enchantment enchantment : Enchantment.values()) {
                            if (enchantment.isCursed()) continue;
                            enchs.add(enchantment);
                        }
                        final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) m;
                        Enchantment enchantment = enchs.get(random.nextInt(enchs.size()));
                        meta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), true);
                    });
                yield List.of(book);
            }
            case 1 -> List.of(new ItemStack(Material.SADDLE));
            case 2 -> List.of(new ItemStack(Material.NAUTILUS_SHELL));
            default -> List.of(new ItemStack(Material.NAME_TAG));
            };
        } else {
            return List.of(new ItemStack(details.fish.fishMaterial, total),
                           new ItemStack(Material.FISHING_ROD));
        }
    }

    public static final class Details extends DailyQuest.Details {
        protected Fish fish = Fish.COD;
    }
}
