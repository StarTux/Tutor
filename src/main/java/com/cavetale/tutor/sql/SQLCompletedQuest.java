package com.cavetale.tutor.sql;

import com.cavetale.tutor.QuestName;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import com.winthier.sql.SQLRow;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
@NotNull
@Name("completed_quests")
@UniqueKey({"player", "quest"})
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLCompletedQuest implements SQLRow {
    @Id private Integer id;
    private UUID player;
    private String quest;
    private Date time;

    public SQLCompletedQuest() { }

    public SQLCompletedQuest(final UUID playerUuid, final QuestName questName) {
        this.player = playerUuid;
        this.quest = questName.key;
        this.time = new Date();
    }
}
