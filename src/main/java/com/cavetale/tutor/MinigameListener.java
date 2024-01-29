package com.cavetale.tutor;

import com.cavetale.core.event.minigame.MinigameMatchCompleteEvent;
import com.cavetale.inventory.mail.ItemMail;
import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
        final int chance = event.getWinnerUuids().size();
        for (UUID player : event.getPlayerUuids()) {
            if (event.getWinnerUuids().contains(player)) {
                final boolean prime = chance <= 1
                    || ThreadLocalRandom.current().nextInt(chance) == 0;
                // Winner
                ItemMail.send(player,
                              List.of(prime
                                      ? (ThreadLocalRandom.current().nextBoolean()
                                         ? Mytems.KITTY_COIN.createItemStack()
                                         : Mytems.DIAMOND_COIN.createItemStack())
                                      : (ThreadLocalRandom.current().nextBoolean()
                                         ? Mytems.RUBY.createItemStack(3)
                                         : Mytems.DIAMOND_COIN.createItemStack())),
                              text("Winning a game of " + event.getType().getDisplayName(), GOLD));
            } else {
                // Regular Player
                ItemMail.send(player,
                              List.of(ThreadLocalRandom.current().nextBoolean()
                                      ? Mytems.RUBY.createItemStack()
                                      : Mytems.GOLDEN_COIN.createItemStack()),
                              text("Playing a game of " + event.getType().getDisplayName(), GREEN));
            }
        }
    }
}

