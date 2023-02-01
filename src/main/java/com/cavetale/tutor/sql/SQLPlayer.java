package com.cavetale.tutor.sql;

import com.cavetale.core.util.Json;
import com.cavetale.tutor.daily.game.DailyGameTag;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow;
import java.util.UUID;
import lombok.Data;

@Data
@Name("players")
@NotNull
public final class SQLPlayer implements SQLRow {
    @Id private Integer id;
    @Unique private UUID player;
    private int tutorials;
    private int quests;
    private int dailies;
    private int dailyGames;
    @Text @Nullable private String dailyGame; // DailyGameTag json
    private int dailyGameRolls;
    private boolean ignoreQuests;
    private boolean ignoreDailies;
    @Default("0") private int totalRolls;

    public SQLPlayer() { }

    public SQLPlayer(final UUID uuid) {
        this.player = uuid;
    }

    public DailyGameTag parseDailyGameTag() {
        if (dailyGame == null) {
            dailyGame = Json.serialize(new DailyGameTag().randomize());
        }
        return Json.deserialize(dailyGame, DailyGameTag.class);
    }

    public void setDailyGameTag(DailyGameTag tag) {
        this.dailyGame = Json.serialize(tag);
    }
}
