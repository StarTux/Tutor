package com.cavetale.tutor.daily;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;

/**
 * Empty implementation of the DailyQuest.  Might come in handy for
 * copy and paste purposes.
 */
public final class DailyQuestDummy extends DailyQuest<DailyQuestDummy.Details, DailyQuestDummy.Progress> {
    public DailyQuestDummy() {
        super(DailyQuestType.DUMMY,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return text("?");
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return text("?");
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(Material.STICK);
    }

    public static final class Details extends DailyQuest.Details {
    }

    public static final class Progress extends DailyQuest.Progress {
    }
}
