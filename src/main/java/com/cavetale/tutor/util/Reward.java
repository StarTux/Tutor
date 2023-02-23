package com.cavetale.tutor.util;

import com.cavetale.core.event.item.PlayerReceiveItemsEvent;
import com.cavetale.core.font.GuiOverlay;
import java.util.ArrayList;
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
    private static final int SIZE = 6 * 9;
    private static final List<Integer> SLOTS = new ArrayList<>();

    static {
        // Build a list of slots closest to the "center".  We use a
        // 6x9 inventory so there is no dead center.
        for (int i = 0; i < SIZE; i += 1) SLOTS.add(i);
        SLOTS.sort((a, b) -> {
                final int ax = 4 - (a % 9);
                final int ay = 2 - (a / 9);
                final int bx = 4 - (b % 9);
                final int by = 2 - (b / 9);
                return Integer.compare(ax * ax + ay * ay,
                                       bx * bx + by * by);
            });
    }

    public static void give(Player player, List<ItemStack> itemStackList, String title, TextColor color) {
        Gui gui = new Gui()
            .size(SIZE)
            .title(GuiOverlay.HOLES.builder(SIZE, color)
                   .layer(GuiOverlay.TITLE_BAR, GRAY)
                   .title(text(title, color))
                   .build());
        gui.setEditable(true);
        for (int i = 0; i < itemStackList.size(); i += 1) {
            ItemStack it = itemStackList.get(i);
            if (SLOTS.size() > i) {
                int slot = SLOTS.get(i);
                gui.getInventory().setItem(slot, it);
            } else {
                gui.getInventory().addItem(it);
            }
        }
        gui.onClose(cle -> {
                PlayerReceiveItemsEvent event = new PlayerReceiveItemsEvent(player, gui.getInventory());
                if (event.isEmpty()) return;
                event.giveItems();
                event.callEvent();
                event.dropItems();
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.MASTER, 1.0f, 1.0f);
            });
        gui.open(player);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.MASTER, 1.0f, 1.0f);
        player.sendMessage(text(title, color));
    }

    private Reward() { }
}
