package com.cavetale.tutor.session;

import com.cavetale.tutor.Quest;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.goal.GoalProgress;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

@Getter @RequiredArgsConstructor
public final class PlayerQuest {
    protected final Session session;
    protected final SQLPlayerQuest row;
    protected final Quest quest;
    protected Goal currentGoal;
    protected GoalProgress currentProgress;
    private static final long SECS = 3;

    protected void disable() {
        if (currentGoal != null) {
            currentGoal.onDisable(this);
        }
    }

    /**
     * Initialize after new quest creation. Select the first goal.
     */
    protected void onQuestStart() {
        currentGoal = quest.getGoals().get(0);
        initializeCurrentGoal();
        currentGoal.onEnable(this);
        Player player = getPlayer();
        if (player != null) {
            Component msg = Component.text()
                .append(Component.text("New goal: ", NamedTextColor.DARK_AQUA))
                .append(currentGoal.getDisplayName())
                .build();
            player.sendMessage(msg);
            player.showTitle(Title.title(Component.empty(), msg,
                                         Title.Times.of(Duration.ZERO, Duration.ofSeconds(SECS), Duration.ZERO)));
        }
    }

    /**
     * Setup after a new goal has been selected.
     */
    private void initializeCurrentGoal() {
        currentProgress = currentGoal.newProgress();
        row.setGoal(currentGoal.getId());
        row.setProgress(currentProgress.serialize());
    }

    /**
     * Initialize after loading from database.
     * The row will remain unchanged.
     */
    protected void loadRow() {
        currentGoal = quest.findGoal(row.getGoal());
        if (currentGoal == null) {
            session.sessions.plugin.getLogger().warning("Goal not found: " + row);
            currentGoal = quest.getGoals().get(0);
            initializeCurrentGoal();
        }
        currentGoal.getProgress(this);
        currentGoal.onEnable(this);
    }

    public void save() {
        row.setNow();
        row.setProgress(currentProgress != null ? currentProgress.serialize() : null);
        if (row.getId() == null) {
            session.sessions.plugin.getDatabase().insertAsync(row, null);
        } else {
            session.sessions.plugin.getDatabase().updateAsync(row, null, "goal", "progress", "updated");
        }
    }

    public Player getPlayer() {
        return session.getPlayer();
    }

    public TutorPlugin getPlugin() {
        return session.sessions.plugin;
    }

    /**
     * Get desired progress. This will clear the current progress if
     * necessary!
     */
    public <P extends GoalProgress> P getProgress(Class<P> progressType, Supplier<P> dfl) {
        if (!progressType.isInstance(currentProgress)) {
            String json = row.getProgress();
            currentProgress = json != null
                ? GoalProgress.deserialize(json, progressType, dfl)
                : dfl.get();
        }
        return progressType.cast(currentProgress);
    }

    public void onGoalComplete() {
        final int index = quest.goalIndex(currentGoal.getId());
        currentGoal.onComplete(this);
        currentGoal.onDisable(this);
        currentGoal = null;
        final int newIndex = index + 1;
        if (newIndex >= quest.getGoals().size()) {
            onQuestComplete();
        } else {
            Goal newGoal = quest.getGoals().get(newIndex);
            currentGoal = newGoal;
            initializeCurrentGoal();
            currentGoal.onEnable(this);
            save();
            Player player = getPlayer();
            if (player != null) {
                Component msg = Component.text("Goal complete", NamedTextColor.DARK_AQUA);
                player.showTitle(Title.title(Component.empty(), msg,
                                             Title.Times.of(Duration.ZERO, Duration.ofSeconds(SECS), Duration.ZERO)));
                player.sendMessage(msg);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.2f, 2.0f);
                Bukkit.getScheduler().runTaskLater(session.sessions.plugin, () -> {
                        if (!player.isOnline()) return;
                        Component msg2 = Component.text()
                            .append(Component.text("New goal: ", NamedTextColor.DARK_AQUA))
                            .append(newGoal.getDisplayName())
                            .build();
                        player.sendMessage(msg2);
                        player.showTitle(Title.title(Component.empty(), msg2,
                                                     Title.Times.of(Duration.ZERO, Duration.ofSeconds(SECS), Duration.ZERO)));
                    }, 20L * SECS);
            }
        }
    }

    public void onQuestComplete() {
        session.removeQuest(quest.getName());
        SQLCompletedQuest newRow = new SQLCompletedQuest(session.uuid, quest);
        session.sessions.plugin.getDatabase().saveAsync(newRow, null);
        Player player = getPlayer();
        if (player != null) {
            Component msg = Component.text("Tutorial complete", NamedTextColor.DARK_AQUA);
            player.sendMessage(msg);
            player.showTitle(Title.title(Component.empty(), msg,
                                         Title.Times.of(Duration.ZERO, Duration.ofSeconds(SECS), Duration.ZERO)));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.2f, 2.0f);
        }
    }

    public void onProgress(GoalProgress progress) {
        currentProgress = progress;
        if (progress.isComplete()) {
            onGoalComplete();
        } else {
            save();
            Player player = getPlayer();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 0.5f);
        }
    }
}
