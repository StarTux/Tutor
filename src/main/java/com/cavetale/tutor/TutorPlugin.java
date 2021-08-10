package com.cavetale.tutor;

import com.cavetale.tutor.session.Sessions;
import com.cavetale.tutor.sql.SQLCompletedQuests;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import com.winthier.sql.SQLDatabase;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TutorPlugin extends JavaPlugin {
    protected SQLDatabase database;
    protected TutorCommand tutorCommand = new TutorCommand(this);
    protected TutorAdminCommand adminCommand = new TutorAdminCommand(this);
    protected EventListener eventListener = new EventListener(this);
    protected Map<QuestName, Quest> quests = new EnumMap<>(QuestName.class);
    protected Sessions sessions = new Sessions(this);

    @Override
    public void onEnable() {
        database = new SQLDatabase(this);
        database.registerTables(SQLPlayerQuest.class,
                                SQLCompletedQuests.class);
        if (!database.createAllTables()) {
            throw new IllegalStateException("Table creation failed!");
        }
        // We make sure that all quests are set (non null)
        for (QuestName questName : QuestName.values()) {
            Quest quest = questName.create(); // throws
            quests.put(questName, quest);
        }
        tutorCommand.enable();
        adminCommand.enable();
        eventListener.enable();
        sessions.enable();
    }

    @Override
    public void onDisable() {
    }
}
