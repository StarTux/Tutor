package com.cavetale.tutor.session;

import com.cavetale.tutor.Quest;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.goal.GoalProgress;
import com.cavetale.tutor.sql.SQLCompletedQuests;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter @RequiredArgsConstructor
public final class PlayerQuest {
    protected final Session session;
    protected final SQLPlayerQuest row;
    protected final Quest quest;
    protected Goal currentGoal;
    protected GoalProgress currentProgress;

    /**
     * Initialize after new quest creation. Select the first goal.
     */
    public void initialize() {
        currentGoal = quest.getGoals().get(0);
        initializeCurrentGoal();
    }

    /**
     * Setup after a new goal has been selected.
     */
    void initializeCurrentGoal() {
        currentProgress = currentGoal.newProgress();
        row.setGoal(currentGoal.getId());
        row.setProgress(currentProgress.serialize());
    }

    /**
     * Initialize after loading from database.
     * The row will remain unchanged.
     */
    public void loadRow() {
        currentGoal = quest.findGoal(row.getGoal());
        if (currentGoal == null) {
            session.sessions.plugin.getLogger().warning("Goal not found: " + row);
            initialize();
        }
        currentGoal.getProgress(this);
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
        final int newIndex = index + 1;
        if (newIndex >= quest.getGoals().size()) {
            onQuestComplete();
        } else {
            currentGoal = quest.getGoals().get(newIndex);
            initializeCurrentGoal();
            save();
        }
    }

    public void onQuestComplete() {
        session.removeQuest(quest.getName());
        SQLCompletedQuests newRow = new SQLCompletedQuests(session.uuid, quest);
        session.sessions.plugin.getDatabase().saveAsync(newRow, null);
    }
}
