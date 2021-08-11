package com.cavetale.tutor.session;

import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Cache data on a player, logged in or not.
 */
@Getter
public final class Session {
    protected final Sessions sessions;
    protected final UUID uuid;
    protected final String name;
    protected final Map<QuestName, PlayerQuest> currentQuests = new EnumMap<>(QuestName.class);
    protected final Map<QuestName, SQLCompletedQuest> completedQuests = new EnumMap<>(QuestName.class);

    protected Session(final Sessions sessions, final Player player) {
        this.sessions = sessions;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    protected void enable(List<SQLPlayerQuest> playerQuestRows,
                          List<SQLCompletedQuest> completedQuestRows) {
        for (SQLPlayerQuest row : playerQuestRows) {
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
        for (SQLCompletedQuest row : completedQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                sessions.plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            completedQuests.put(questName, row);
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
        playerQuest.onQuestStart();
        playerQuest.save();
        return playerQuest;
    }

    public PlayerQuest removeQuest(QuestName questName) {
        PlayerQuest playerQuest = currentQuests.remove(questName);
        if (playerQuest != null) {
            sessions.plugin.getDatabase().deleteAsync(playerQuest.getRow(), null);
        }
        return playerQuest;
    }

    public boolean hasQuest(QuestName questName) {
        return currentQuests.containsKey(questName);
    }

    public List<PlayerQuest> getQuestList() {
        return new ArrayList<>(currentQuests.values());
    }

    public void openQuestBook(Player player) {
        List<PlayerQuest> quests = getQuestList();
        List<Component> pages = new ArrayList<>();
        if (quests.isEmpty()) {
            pages.add(Component.text("No quests to show!", NamedTextColor.DARK_RED));
        } else {
            for (PlayerQuest playerQuest : quests) {
                pages.addAll(playerQuest.getCurrentGoal().getBookPages(playerQuest));
            }
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Quests");
        meta.author(Component.text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.openBook(itemStack);
    }
}
