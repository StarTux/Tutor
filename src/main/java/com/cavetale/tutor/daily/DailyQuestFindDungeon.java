package com.cavetale.tutor.daily;

import com.cavetale.core.event.dungeon.DungeonDiscoverEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
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
    public void onGenerate() {
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

    protected void onDungeonDiscover(Player player, PlayerDailyQuest playerDailyQuest, DungeonDiscoverEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        makeProgress(playerDailyQuest, 1);
    }
}
