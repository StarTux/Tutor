package com.cavetale.tutor.session;

import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.ClickableCondition;
import com.cavetale.tutor.goal.Condition;
import com.cavetale.tutor.goal.Goal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

    // Nullable!
    public Session find(Player player) {
        Session session = sessionsMap.get(player.getUniqueId());
        return session != null && session.ready ? session : null;
    }

    public <G extends Goal> void applyGoals(Player player,  BiConsumer<PlayerQuest, Goal> callback) {
        Session session = sessionsMap.get(player.getUniqueId());
        session.applyGoals(callback);
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

    private void createSession(Player player) {
        UUID uuid = player.getUniqueId();
        if (sessionsMap.containsKey(uuid)) {
            throw new IllegalStateException("Session already exists: " + player.getName());
        }
        Session session = new Session(this, player);
        sessionsMap.put(session.uuid, session);
        session.load();
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

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createSession(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeSession(player);
    }

    public boolean openQuestBook(Player player) {
        Session session = find(player);
        if (session == null) return false;
        session.openQuestBook(player);
        return true;
    }
}
