package com.cavetale.tutor.session;

import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.core.event.hud.PlayerHudPriority;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.QuestType;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.ClickableCondition;
import com.cavetale.tutor.goal.Condition;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Pet;
import com.winthier.perm.event.PlayerPermissionUpdateEvent;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * A session is created for every player who logs in and destroyed
 * when they log out. The same happens for every player when the
 * plugin is enabled or disabled, respectively.
 *
 * The presence of a session does not imply that the player is
 * permitted to run any tutorials. Every player has a session.
 *
 * A player may not be available because it is currently loading. The
 * find message will return null in that case. Clients to this package
 * must respect this and act accordingly.
 */
@RequiredArgsConstructor
public final class Sessions implements Listener {
    protected final TutorPlugin plugin;
    private final Map<UUID, Session> sessionsMap = new HashMap<>();
    protected boolean enabled;

    public void enable() {
        enabled = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            createSession(player);
        }
    }

    public void disable() {
        for (Session session : sessionsMap.values()) {
            session.disable();
        }
        sessionsMap.clear();
        enabled = false;
    }

    /**
     * Find a usable session. (Nullable!)
     * @param player the player
     * @return the session, null if the session does not exist or is
     * not ready.
     */
    public Session find(Player player) {
        Session session = sessionsMap.get(player.getUniqueId());
        return session != null && session.ready ? session : null;
    }

    public void findOrLoad(PlayerCache playerCache, Consumer<Session> callback) {
        Session session = sessionsMap.get(playerCache.uuid);
        if (session != null) {
            session.apply(callback);
            return;
        }
        Session tempSession = new Session(this, playerCache);
        tempSession.loadAsync(() -> callback.accept(tempSession));
    }

    /**
     * Run an operation either immediately, or schedule for later if
     * the session isn't loaded yet.
     */
    public boolean apply(Player player, Consumer<Session> callback) {
        Session session = sessionsMap.get(player.getUniqueId());
        if (session != null) {
            session.apply(callback);
            return true;
        } else {
            return false;
        }
    }

    public boolean applyPet(Player player, Consumer<Pet> callback) {
        Session session = sessionsMap.get(player.getUniqueId());
        if (session != null && session.ready && session.pet != null) {
            callback.accept(session.pet);
            return true;
        } else {
            return false;
        }
    }

    public void applyGoals(Player player,  BiConsumer<PlayerQuest, Goal> callback) {
        // Delaying by a tick because Connect handles joins early (LOWEST)
        Bukkit.getScheduler().runTask(plugin, () -> {
                apply(player, session -> session.applyGoals(callback));
            });
    }

    public int applyClick(Player player, String token) {
        Session session = sessionsMap.get(player.getUniqueId());
        if (session == null) return 0;
        int result = 0;
        for (PlayerQuest playerQuest : session.currentQuests.values()) {
            for (Condition cond : playerQuest.getCurrentGoal().getConditions()) {
                if (cond instanceof ClickableCondition) {
                    ClickableCondition clickable = (ClickableCondition) cond;
                    if (token.equals(clickable.getToken())) {
                        clickable.getClickHandler().accept(playerQuest);
                        result += 1;
                    }
                }
            }
        }
        return result;
    }

    public void createSession(Player player) {
        UUID uuid = player.getUniqueId();
        if (sessionsMap.containsKey(uuid)) {
            throw new IllegalStateException("Session already exists: " + player.getName());
        }
        Session session = new Session(this, player);
        sessionsMap.put(session.uuid, session);
        session.loadAsync(session::enable);
    }

    /**
     * Try to remove a player session and do nothing if the session
     * does not exist.
     */
    public Session removeSession(Player player) {
        Session session = sessionsMap.remove(player.getUniqueId());
        if (session != null) session.disable();
        return session;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createSession(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeSession(player);
    }

    @EventHandler
    void onPluginPlayer(PluginPlayerEvent event) {
        applyGoals(event.getPlayer(), (playerQuest, goal) -> {
                goal.onPluginPlayer(playerQuest, event);
            });
    }

    @EventHandler
    void onPlayerHud(PlayerHudEvent event) {
        if (!event.getPlayer().hasPermission("tutor.tutor")) return;
        Session session = find(event.getPlayer());
        if (session == null) return;
        List<Component> lines = null;
        for (PlayerQuest playerQuest : session.getQuestList()) {
            lines = new ArrayList<>();
            if (playerQuest.getQuest().getName().type == QuestType.TUTORIAL) {
                lines.add(join(noSeparators(), text("Your ", AQUA), text("/tut", YELLOW), text("orial", AQUA)));
            } else {
                lines.add(join(noSeparators(), text("Your ", AQUA), text("/q", YELLOW), text("uest", AQUA)));
            }
            lines.addAll(playerQuest.getCurrentGoal().getSidebarLines(playerQuest));
            break;
        }
        if (lines == null) return;
        event.sidebar(PlayerHudPriority.DEFAULT, lines);
    }

    @EventHandler
    void onPlayerPermissionUpdate(PlayerPermissionUpdateEvent event) {
        Session session = find(event.getPlayer());
        if (session == null) return;
        if (event.getPlayer().hasPermission("tutor.tutor")) {
            session.triggerAutomaticQuests();
        }
    }
}
