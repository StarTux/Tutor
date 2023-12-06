package com.cavetale.tutor;

import com.cavetale.core.event.minigame.MinigameMatchCompleteEvent;
import com.cavetale.inventory.mail.ItemMail;
import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import static com.cavetale.tutor.TutorPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class MinigameListener implements Listener {
    protected void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onMinigameMatchComplete(MinigameMatchCompleteEvent event) {
        if (!event.getType().isPubliclyAvailable()) return;
        if (event.getPlayerUuids().size() < 3) return;
        for (UUID player : event.getPlayerUuids()) {
            if (event.getWinnerUuids().contains(player)) {
                // Winner
                ItemMail.send(player,
                              List.of(Mytems.KITTY_COIN.createItemStack()),
                              text("Winning a game of " + event.getType().getDisplayName(), GOLD));
            } else {
                // Regular Player
                ItemMail.send(player,
                              List.of(Mytems.RUBY.createItemStack()),
                              text("Playing a game of " + event.getType().getDisplayName(), GREEN));
            }
        }
    }
}

