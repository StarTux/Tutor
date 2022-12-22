package com.cavetale.tutor.sql;

import com.winthier.sql.SQLRow;
import java.util.List;

public final class SQLStatic {
    private SQLStatic() { }

    public static List<Class<? extends SQLRow>> getAllTableClasses() {
        return List.of(SQLPlayerQuest.class,
                       SQLCompletedQuest.class,
                       SQLPlayerPet.class,
                       SQLPlayerPetUnlock.class);
    }
}
