package com.cavetale.tutor.daily;

import com.cavetale.core.item.ItemKinds;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestEating extends DailyQuest<DailyQuestEating.Details, DailyQuest.Progress> {
    public DailyQuestEating() {
        super(DailyQuestType.EATING,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @RequiredArgsConstructor
    public enum Foods {
        BAKED_POTATO(Material.BAKED_POTATO),
        BEETROOT(Material.BEETROOT),
        BEETROOT_SOUP(Material.BEETROOT_SOUP),
        BREAD(Material.BREAD),
        COOKED_BEEF(Material.COOKED_BEEF),
        COOKED_CHICKEN(Material.COOKED_CHICKEN),
        COOKED_COD(Material.COOKED_COD),
        COOKED_MUTTON(Material.COOKED_MUTTON),
        COOKED_PORKCHOP(Material.COOKED_PORKCHOP),
        COOKED_RABBIT(Material.COOKED_RABBIT),
        COOKED_SALMON(Material.COOKED_SALMON),
        COOKIE(Material.COOKIE),
        DRIED_KELP(Material.DRIED_KELP),
        GOLDEN_APPLE(Material.GOLDEN_APPLE),
        GOLDEN_CARROT(Material.GOLDEN_CARROT),
        MUSHROOM_STEW(Material.MUSHROOM_STEW),
        PUMPKIN_PIE(Material.PUMPKIN_PIE),
        ;

        private final Material material;
    };

    @Override
    public void onGenerate(String name) {
        this.details.food = Foods.valueOf(name.toUpperCase());
        this.total = random.nextInt(10) + 1;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Eat "),  ItemKinds.iconDescription(new ItemStack(details.food.material, total)));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Eat "),
                              ItemKinds.chatDescription(new ItemStack(details.food.material, total)),
                              text(" in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = new ItemStack(details.food.material, total);
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    protected void onPlayerItemConsume(Player player, PlayerDailyQuest playerDailyQuest, PlayerItemConsumeEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        if (event.getItem().getType() != details.food.material) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.food.material, Math.min(details.food.material.getMaxStackSize(), total)));
    }

    public static final class Details extends DailyQuest.Details {
        protected Foods food = Foods.values()[0];
    }
}
