package com.cavetale.tutor.util;

import com.cavetale.core.event.item.PlayerReceiveItemsEvent;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.util.Gui;
import java.util.List;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;

public final class Reward {
    private static final int[] SLOTS = {4, 5, 3, 6, 2, 7, 1, 8, 0};
    private static final int SIZE = 9;

    public static Gui give(Player player, List<ItemStack> itemStackList, String title, TextColor color) {
        Gui gui = new Gui()
            .size(SIZE)
            .title(GuiOverlay.HOLES.builder(SIZE, color)
                   .layer(GuiOverlay.TITLE_BAR, GRAY)
                   .title(text(title, color))
                   .build());
        gui.setEditable(true);
        for (int i = 0; i < itemStackList.size(); i += 1) {
            ItemStack it = itemStackList.get(i);
            if (SLOTS.length > i) {
                int slot = SLOTS[i];
                gui.getInventory().setItem(slot, it);
            } else {
                gui.getInventory().addItem(it);
            }
        }
        gui.onClose(cle -> {
                PlayerReceiveItemsEvent.receiveInventory(player, gui.getInventory());
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.MASTER, 1.0f, 1.0f);
            });
        gui.open(player);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.MASTER, 1.0f, 1.0f);
        player.sendMessage(text(title, color));
        return gui;
    }

    private Reward() { }
}
