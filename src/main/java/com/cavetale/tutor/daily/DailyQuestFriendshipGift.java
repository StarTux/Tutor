package com.cavetale.tutor.daily;

import com.cavetale.core.event.friends.PlayerShareFriendshipGiftEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.friends.Friends;
import com.cavetale.core.item.ItemKinds;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestFriendshipGift extends DailyQuest<DailyQuest.Details, DailyQuest.Progress> {
    public DailyQuestFriendshipGift() {
        super(DailyQuestType.FRIENDSHIP_GIFT,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    protected void onGenerate() {
        total = 3;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Share " + total + Unicode.MULTIPLICATION.string),
                              ItemKinds.icon(Friends.getDailyFriendshipGift()));
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Drop "), ItemKinds.chatDescription(Friends.getDailyFriendshipGift(), total),
                              text(" to " + total + " fellow players on the Cavetale server"
                                   + " to increase your friendship with one another."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return Friends.getDailyFriendshipGift();
    }

    protected void onPlayerShareFriendshipGift(Player player, PlayerDailyQuest playerDailyQuest, PlayerShareFriendshipGiftEvent event) {
        makeProgress(playerDailyQuest, 1);
    }
}
