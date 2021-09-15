package com.cavetale.tutor.util;

import com.cavetale.tutor.TutorPlugin;
import org.bukkit.Bukkit;

public final class Console {
    private Console() { }

    public static void command(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public static void delayedCommand(long delay, String cmd) {
        Bukkit.getScheduler().runTaskLater(TutorPlugin.getInstance(), () -> command(cmd), delay);
    }
}
