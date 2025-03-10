package com.cavetale.tutor;

import com.cavetale.tutor.daily.DailyQuests;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Pets;
import com.cavetale.tutor.session.Sessions;
import com.winthier.sql.SQLDatabase;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import static com.cavetale.tutor.sql.SQLStatic.getAllTableClasses;

@Getter
public final class TutorPlugin extends JavaPlugin {
    protected SQLDatabase database;
    protected final TutorCommand tutorCommand = new TutorCommand(this);
    protected final DailyCommand dailyCommand = new DailyCommand(this);
    protected final CollectCommand collectCommand = new CollectCommand(this);
    protected final TutorAdminCommand adminCommand = new TutorAdminCommand(this);
    protected final Map<QuestName, Quest> quests = new EnumMap<>(QuestName.class);
    protected final Sessions sessions = new Sessions(this);
    protected final Pets pets = new Pets(this);
    protected final DailyQuests dailyQuests = new DailyQuests(this);
    @Getter protected static TutorPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        database = new SQLDatabase(this);
        database.registerTables(getAllTableClasses());
        if (!database.createAllTables()) {
            throw new IllegalStateException("Table creation failed!");
        }
        database.getTable(com.cavetale.tutor.sql.SQLPlayer.class).createColumnIfMissing("questReminder");
        // We make sure that all quests are set (non null)
        for (QuestName questName : QuestName.values()) {
            Quest quest = questName.create(); // throws
            for (Goal goal : quest.getGoals()) {
                goal.enable();
            }
            quests.put(questName, quest);
        }
        dailyQuests.enable();
        tutorCommand.enable();
        dailyCommand.enable();
        collectCommand.enable();
        adminCommand.enable();
        sessions.enable();
        pets.enable();
        new MinigameListener().enable();
        new MenuListener().enable();
    }

    @Override
    public void onDisable() {
        pets.disable();
        sessions.disable();
    }

    public static TutorPlugin plugin() {
        return instance;
    }

    public static SQLDatabase database() {
        return instance.database;
    }

    public static DailyQuests dailyQuests() {
        return instance.dailyQuests;
    }
}
