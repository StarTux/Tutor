package com.cavetale.tutor;

import com.cavetale.core.menu.MenuItemEvent;
import com.cavetale.mytems.Mytems;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.mytems.util.Items.iconize;
import static com.cavetale.mytems.util.Items.tooltip;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class MenuListener implements Listener {
    protected void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.plugin());
    }

    @EventHandler
    private void onMenuItem(MenuItemEvent event) {
        if (event.getPlayer().hasPermission("tutor.tutor")) {
            event.addItem(builder -> builder
                          .key("tutor:tutor")
                          .command("tutor")
                          .icon(tooltip(new ItemStack(Material.BOOK),
                                        List.of(text("Tutorials", YELLOW),
                                                text("Tutor Pet", BLUE)))));
        }
        if (event.getPlayer().hasPermission("tutor.daily")) {
            event.addItem(builder -> builder
                          .key("tutor:daily")
                          .command("daily")
                          .icon(Mytems.COLORFALL_HOURGLASS.createIcon(List.of(text("Daily Quests", BLUE),
                                                                              text("Daily Game", GREEN)))));
        }
        if (event.getPlayer().hasPermission("tutor.collect")) {
            event.addItem(builder -> builder
                          .key("tutor:collect")
                          .command("collect")
                          .icon(iconize(tooltip(new ItemStack(Material.BUNDLE),
                                                List.of(text("Collections", GREEN))))));
        }
    }
}
