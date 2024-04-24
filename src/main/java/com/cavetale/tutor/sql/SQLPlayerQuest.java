package com.cavetale.tutor.sql;

import com.cavetale.tutor.Quest;
import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
@Name("player_quests")
@NotNull
@UniqueKey({"player", "quest"})
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerQuest implements SQLRow {
    @Id private Integer id;
    private UUID player;
    private String quest;
    private String goal; // current goal id
    @Nullable private String progress; // GoalProgress json
    private Date updated;

    public SQLPlayerQuest() { }

    public SQLPlayerQuest(final UUID playerUuid, final Quest quest) {
        this.player = playerUuid;
        this.quest = quest.getName().key;
    }

    public void setNow() {
        this.updated = new Date();
    }
}
