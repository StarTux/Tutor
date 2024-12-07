package com.cavetale.tutor;

import com.cavetale.core.menu.MenuItemEvent;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.mytems.util.Items.tooltip;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class MenuListener implements Listener {
    public static final String MENU_KEY = "tutor:tutor";
    public static final String MENU_PERMISSION = "tutor.tutor";

    protected void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.plugin());
    }

    @EventHandler
    private void onMenuItem(MenuItemEvent event) {
        if (event.getPlayer().hasPermission(MENU_PERMISSION)) {
            event.addItem(builder -> builder
                          .key(MENU_KEY)
                          .command("tutor")
                          .icon(tooltip(new ItemStack(Material.BOOK),
                                        List.of(text("Tutorials", YELLOW),
                                                text("Daily Quests", WHITE),
                                                text("Collections", GREEN),
                                                text("Tutor Pet", BLUE)))));
        }
    }
}
