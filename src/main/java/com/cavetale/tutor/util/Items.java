package com.cavetale.tutor.util;

import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

public final class Items {
    private Items() { }

    public static Component nonItalic(Component in) {
        return Component.text().append(in).decoration(TextDecoration.ITALIC, false).build();
    }

    public static void text(ItemStack item, List<Component> text) {
        item.editMeta(meta -> {
                meta.displayName(text.isEmpty() ? Component.empty() : nonItalic(text.get(0)));
                meta.lore(text.isEmpty() ? List.of() : text.subList(1, text.size())
                          .stream().map(Items::nonItalic).collect(Collectors.toList()));
            });
    }
}
