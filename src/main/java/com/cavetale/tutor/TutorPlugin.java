package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Pets;
import com.cavetale.tutor.session.Sessions;
import com.cavetale.tutor.util.Gui;
import com.winthier.sql.SQLDatabase;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import static com.cavetale.tutor.sql.SQLStatic.getAllTableClasses;

@Getter
public final class TutorPlugin extends JavaPlugin {
    protected SQLDatabase database;
    protected TutorCommand tutorCommand = new TutorCommand(this);
    protected TutorAdminCommand adminCommand = new TutorAdminCommand(this);
    protected Map<QuestName, Quest> quests = new EnumMap<>(QuestName.class);
    protected Sessions sessions = new Sessions(this);
    protected Pets pets = new Pets(this);
    @Getter protected static TutorPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        database = new SQLDatabase(this);
        database.registerTables(getAllTableClasses());
        if (!database.createAllTables()) {
            throw new IllegalStateException("Table creation failed!");
        }
        // We make sure that all quests are set (non null)
        for (QuestName questName : QuestName.values()) {
            Quest quest = questName.create(); // throws
            for (Goal goal : quest.getGoals()) {
                goal.enable();
            }
            quests.put(questName, quest);
        }
        tutorCommand.enable();
        adminCommand.enable();
        sessions.enable();
        pets.enable();
        Gui.enable();
    }

    @Override
    public void onDisable() {
        pets.disable();
        sessions.disable();
    }

    public static SQLDatabase database() {
        return instance.database;
    }
}
