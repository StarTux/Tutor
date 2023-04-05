package com.cavetale.tutor.sql;

import com.winthier.sql.SQLRow;
import lombok.Data;

/**
 * Store one daily quest in the database.  Only the manager server
 * gets to create or edit rows in this table.
 */
@Data
@SQLRow.NotNull
    @SQLRow.UniqueKey({"dayId", "dailyIndex"})
@SQLRow.Name("daily_quests")
public final class SQLDailyQuest implements SQLRow {
    @Id private Integer id;
    private int dayId; // e.g. 20221228
    private int dailyIndex; // e.g. 01, aka group!
    private boolean active;
    @VarChar(255) private String questType;
    @Text private String details; // json
    private int total;

    public SQLDailyQuest() { }
}
