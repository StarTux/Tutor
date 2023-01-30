package com.cavetale.tutor.daily;

import com.cavetale.core.item.ItemKinds;
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

    public static final Material[] FOODS = {
        Material.BAKED_POTATO,
        Material.BEETROOT,
        Material.BEETROOT_SOUP,
        Material.BREAD,
        Material.COOKED_BEEF,
        Material.COOKED_CHICKEN,
        Material.COOKED_COD,
        Material.COOKED_MUTTON,
        Material.COOKED_PORKCHOP,
        Material.COOKED_RABBIT,
        Material.COOKED_SALMON,
        Material.COOKIE,
        Material.DRIED_KELP,
        Material.GOLDEN_APPLE,
        Material.GOLDEN_CARROT,
        Material.MUSHROOM_STEW,
        Material.PUMPKIN_PIE,
    };

    @Override
    public void onGenerate() {
        this.details.food = FOODS[random.nextInt(FOODS.length)];
        this.total = (random.nextInt(3) + 1) * 10;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Eat "),  ItemKinds.iconDescription(new ItemStack(details.food, total)));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Eat "),
                              ItemKinds.chatDescription(new ItemStack(details.food, total)),
                              text(" in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = new ItemStack(details.food, total);
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    public static final class Details extends DailyQuest.Details {
        protected Material food = Material.APPLE;
    }

    protected void onPlayerItemConsume(Player player, PlayerDailyQuest playerDailyQuest, PlayerItemConsumeEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        if (event.getItem().getType() != details.food) return;
        makeProgress(playerDailyQuest, 1);
    }
}
