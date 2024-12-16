package com.cavetale.tutor;

import com.cavetale.core.menu.MenuItemEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.Session;
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
        final Session session = TutorPlugin.plugin().getSessions().find(event.getPlayer());
        if (session == null) return;
        if (event.getPlayer().hasPermission("tutor.tutor")) {
            event.addItem(builder -> builder
                          .key("tutor:tutor")
                          .command("tutor")
                          .icon(tooltip(new ItemStack(Material.KNOWLEDGE_BOOK),
                                        List.of(text("Tutorials", YELLOW),
                                                text("Tutor Pet", BLUE)))));
        }
        if (event.getPlayer().hasPermission("tutor.daily")) {
            event.addItem(builder -> builder
                          .key("tutor:daily")
                          .command("daily")
                          .highlightColor(session.countUnfinishedDailies(session.getVisibleDailies()) > 0
                                          ? BLUE
                                          : null)
                          .icon(Mytems.COLORFALL_HOURGLASS.createIcon(List.of(text("Daily Quests", BLUE)))));
            event.addItem(builder -> builder
                          .key("tutor:dailygame")
                          .command("daily game")
                          .highlightColor(session.getPlayerRow().isRollReminder() && session.getPlayerRow().getDailyGameRolls() > 0
                                          ? WHITE
                                          : null)
                          .icon(Mytems.DICE.createIcon(List.of(text("Daily Game", GRAY)))));
        }
        if (event.getPlayer().hasPermission("tutor.collect")) {
            event.addItem(builder -> builder
                          .key("tutor:collect")
                          .command("collect")
                          .highlightColor(session.getPlayerRow().isCollectionReminder()
                                          ? GREEN
                                          : null)
                          .icon(iconize(tooltip(new ItemStack(Material.BUNDLE),
                                                List.of(text("Collections", GREEN))))));
        }
    }
}
