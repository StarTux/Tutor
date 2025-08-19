package com.cavetale.tutor.daily;

import com.cavetale.core.event.structure.PlayerDiscoverStructureEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestFindDungeon extends DailyQuest<DailyQuest.Details, DailyQuest.Progress> {
    public DailyQuestFindDungeon() {
        super(DailyQuestType.FIND_DUNGEON,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    public void onGenerate(final String name) {
        total = 3;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Find " + total + Unicode.MULTIPLICATION.string), Mytems.CAVETALE_DUNGEON);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Find " + total + " "),
                              Mytems.CAVETALE_DUNGEON, text("Cavetale Dungeons in the Mining World."
                                                            + " You can locate them using a "),
                              VanillaItems.COMPASS, text("Compass."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = Mytems.CAVETALE_DUNGEON.createIcon();
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        result.setAmount(total);
        return result;
    }

    protected void onPlayerDiscoverStructure(Player player, PlayerDailyQuest playerDailyQuest, PlayerDiscoverStructureEvent event) {
        if (!"dungeons".equals(event.getStructure().getKey().getNamespace()) || !"dungeon".equals(event.getStructure().getKey().getKey())) {
            return;
        }
        if (!checkGameModeAndSurvivalServer(player)) return;
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
}
