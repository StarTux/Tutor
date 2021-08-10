package com.cavetale.tutor.session;

import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Cache data on a player, logged in or not.
 */
@Getter
public final class Session {
    protected final Sessions sessions;
    protected final UUID uuid;
    protected final String name;
    protected final Map<QuestName, PlayerQuest> currentQuests = new EnumMap<>(QuestName.class);

    protected Session(Sessions sessions, Player player) {
        this.sessions = sessions;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    protected void enable(List<SQLPlayerQuest> rows) {
        for (SQLPlayerQuest row : rows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                sessions.plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            Quest quest = sessions.plugin.getQuests().get(questName);
            PlayerQuest playerQuest = new PlayerQuest(this, row, quest);
            currentQuests.put(playerQuest.quest.getName(), playerQuest);
            playerQuest.loadRow();
        }
    }

    protected void disable() {
        // ?
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public PlayerQuest getQuest(QuestName questName) {
        return currentQuests.get(questName);
    }

    public PlayerQuest startQuest(QuestName questName) {
        if (currentQuests.containsKey(questName)) {
            throw new IllegalStateException("Duplicate player quest: " + questName);
        }
        Quest quest = sessions.plugin.getQuests().get(questName);
        return startQuest(quest);
    }

    public PlayerQuest startQuest(Quest quest) {
        if (currentQuests.containsKey(quest.getName())) {
            throw new IllegalStateException("Duplicate player quest: " + quest.getName());
        }
        PlayerQuest playerQuest = new PlayerQuest(this, new SQLPlayerQuest(uuid, quest), quest);
        currentQuests.put(quest.getName(), playerQuest);
        playerQuest.initialize();
        playerQuest.save();
        return playerQuest;
    }

    public PlayerQuest removeQuest(QuestName questName) {
        PlayerQuest playerQuest = currentQuests.remove(questName);
        return playerQuest;
    }

    public boolean hasQuest(QuestName questName) {
        return currentQuests.containsKey(questName);
    }

    public List<PlayerQuest> getQuestList() {
        return new ArrayList<>(currentQuests.values());
    }
}
