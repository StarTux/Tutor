package com.cavetale.tutor.sql;

import com.winthier.sql.SQLRow;
import java.util.List;

public final class SQLStatic {
    private SQLStatic() { }

    public static List<Class<? extends SQLRow>> getAllTableClasses() {
        return List.of(SQLPlayer.class,
                       SQLPlayerQuest.class,
                       SQLCompletedQuest.class,
                       SQLPlayerPet.class,
                       SQLPlayerPetUnlock.class,
                       SQLDailyQuest.class,
                       SQLPlayerDailyQuest.class,
                       SQLPlayerItemCollection.class);
    }
}
