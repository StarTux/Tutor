package com.cavetale.tutor.daily;

import com.cavetale.core.util.Json;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.sql.SQLPlayerDailyQuest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import static com.cavetale.tutor.TutorPlugin.database;

@Getter @RequiredArgsConstructor @ToString
public final class PlayerDailyQuest {
    protected final Session session;
    protected final DailyQuest<?, ?> dailyQuest;
    protected SQLPlayerDailyQuest row;
    protected DailyQuest.Progress progress;
    protected int score;
    @Setter protected boolean complete;
    protected boolean ready; // Implies row != null!
    protected long progressTimer;

    public boolean isCompletable() {
        return !complete && dailyQuest.active;
    }

    /**
     * Call in async task!
     */
    public boolean makeRow() {
        if (progress == null) {
            progress = dailyQuest.newProgress();
        }
        row = new SQLPlayerDailyQuest();
        row.setPlayer(session.getUuid());
        row.setDailyQuestId(dailyQuest.getRowId());
        row.setDayId(dailyQuest.getDayId());
        row.setProgress(Json.serialize(progress));
        row.setScore(score);
        row.setComplete(complete);
        if (0 == database().insert(row)) {
            this.row = null;
            return false;
        }
        return true;
    }

    /**
     * Call in async task!
     */
    public boolean loadRow() {
        row = SQLPlayerDailyQuest.findByDailyQuestId(session.getUuid(), dailyQuest.getRowId());
        if (row == null) return false;
        this.progress = row.getProgress() != null
            ? dailyQuest.parseProgress(row.getProgress())
            : dailyQuest.newProgress();
        this.score = row.getScore();
        this.complete = row.isComplete();
        return true;
    }

    public void saveAsync() {
        database().update(SQLPlayerDailyQuest.class)
            .row(row)
            .set("progress", Json.serialize(progress))
            .atomic("score", score)
            .atomic("complete", complete)
            .async(null); // Fault recovery?
    }

    public void enable() {
        dailyQuest.onEnablePlayer(this);
        ready = true;
    }

    public void disable() {
        dailyQuest.onDisablePlayer(this);
    }
}
