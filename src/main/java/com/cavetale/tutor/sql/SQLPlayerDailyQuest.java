package com.cavetale.tutor.sql;

import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import com.winthier.sql.SQLRow;
import java.util.UUID;
import lombok.Data;
import static com.cavetale.tutor.TutorPlugin.database;

@Data
@Name("player_daily_quests")
@NotNull
@UniqueKey({"player", "dailyQuestId"})
/**
 * Progress for one SQLDailyQuest of one player.
 */
public final class SQLPlayerDailyQuest implements SQLRow {
    @Id private Integer id;
    private UUID player;
    @Keyed private int dailyQuestId;
    private int dayId;
    @Text private String progress;
    private int score;
    private boolean complete;

    public SQLPlayerDailyQuest() { }

    public static SQLPlayerDailyQuest findByDailyQuestId(UUID uuid, int dqid) {
        return database().find(SQLPlayerDailyQuest.class)
            .eq("player", uuid)
            .eq("dailyQuestId", dqid).findUnique();
    }
}
