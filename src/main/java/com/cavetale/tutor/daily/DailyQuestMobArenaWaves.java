package com.cavetale.tutor.daily;

import com.cavetale.core.event.mobarena.MobArenaWaveCompleteEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Skull;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestMobArenaWaves extends DailyQuest<DailyQuest.Details, DailyQuest.Progress> {
    private ItemStack skullItem;

    public DailyQuestMobArenaWaves() {
        super(DailyQuestType.MOB_ARENA_WAVES,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    public void onGenerate(final String name) {
        this.total = 10 * (1 + random.nextInt(5));
    }

    @Override @SuppressWarnings("LineLength")
    public void onEnable() {
        this.skullItem = Skull.create("CreepyClown",
                                      UUID.fromString("7d792e23-8853-4f7e-9b05-5e60babfe7f3"),
                                      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQwYmJhMzliOGM1MDA0NDk3Y2IzYWI2MDM3OWMwOTM1ZmY0ZGEzZjY3NDYzZDQyNmJlNDMyMWRlZDNiMzhkNyJ9fX0=",
                                      null);
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("MobArena"));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return text("Defeat " + total + " waves in MobArena."
                    + " It's best to go with some friends.");
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = skullItem.clone();
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        result.setAmount(total);
        return result;
    }

    protected void onMobArenaWaveComplete(Player player, PlayerDailyQuest playerDailyQuest, MobArenaWaveCompleteEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return switch (random.nextInt(2)) {
        case 0 -> List.of(Mytems.DIAMOND_COIN.createItemStack());
        case 1 -> List.of(Mytems.KITTY_COIN.createItemStack());
        default -> List.of();
        };
    }
}
