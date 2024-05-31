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

public final class DailyQuestMobArenaWave extends DailyQuest<DailyQuestMobArenaWave.Details, DailyQuest.Progress> {
    private ItemStack skullItem;

    public DailyQuestMobArenaWave() {
        super(DailyQuestType.MOB_ARENA_WAVE,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    public void onGenerate(final int index) {
        this.total = 1;
        details.wave = 10 * (1 + random.nextInt(5));
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
        return textOfChildren(text("Beat MA Wave " + details.wave));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return text("Defeat Wave " + details.wave + " or higher in MobArena."
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
        if (event.getWave() < details.wave) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return switch (random.nextInt(4)) {
        case 0 -> List.of(Mytems.RUBY.createItemStack(1 + random.nextInt(5)));
        case 1 -> List.of(Mytems.KITTY_COIN.createItemStack());
        case 3 -> List.of(Mytems.GOLDEN_COIN.createItemStack());
        default -> List.of(Mytems.SILVER_COIN.createItemStack());
        };
    }

    public static final class Details extends DailyQuest.Details {
        protected int wave;
    }
}
