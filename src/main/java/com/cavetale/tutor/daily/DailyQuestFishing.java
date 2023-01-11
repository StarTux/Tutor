package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestFishing extends DailyQuest<DailyQuestFishing.Details, DailyQuest.Progress> {
    public DailyQuestFishing() {
        super(DailyQuestType.FISHING,
              DailyQuestFishing.Details.class, DailyQuestFishing.Details::new,
              DailyQuest.Progress.class, DailyQuest.Progress::new);
    }

    @RequiredArgsConstructor
    protected enum Fish {
        COD(3, "Cod", VanillaItems.COD, Material.COD),
        SALMON(3, "Salmon", VanillaItems.SALMON, Material.SALMON),
        PUFFERFISH(1, "Pufferfish", VanillaItems.PUFFERFISH, Material.PUFFERFISH),
        TROPICAL_FISH(1, "Tropical Fish", VanillaItems.TROPICAL_FISH, Material.TROPICAL_FISH),
        ;

        protected final int total;
        protected final String displayName;
        protected final ComponentLike chatIcon;
        protected final Material fishMaterial;
    }

    @Override
    public void onGenerate() {
        Fish[] fishes = Fish.values();
        this.details.fish = fishes[ThreadLocalRandom.current().nextInt(fishes.length)];
        this.total = details.fish.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Catch " + total + Unicode.MULTIPLICATION.string), details.fish.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Catch " + total + " "),
                              details.fish.chatIcon,
                              text(" " + details.fish.displayName + "."
                                   + " Fishing must be done in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = new ItemStack(Material.FISHING_ROD);
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    public void onCatch(PlayerDailyQuest playerDailyQuest, Player player, ItemStack item) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (details.fish.fishMaterial != item.getType()) return;
        makeProgress(playerDailyQuest, 1);
    }

    public static final class Details extends DailyQuest.Details {
        protected Fish fish = Fish.COD;
    }
}
