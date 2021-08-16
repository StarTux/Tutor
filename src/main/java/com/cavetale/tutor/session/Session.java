package com.cavetale.tutor.session;

import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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
    protected final TutorPlugin plugin;
    protected final Sessions sessions;
    protected final UUID uuid;
    protected final String name;
    protected final Map<QuestName, PlayerQuest> currentQuests = new EnumMap<>(QuestName.class);
    protected final Map<QuestName, SQLCompletedQuest> completedQuests = new EnumMap<>(QuestName.class);
    protected boolean ready;
    protected boolean disabled;
    private final List<BiConsumer<PlayerQuest, Goal>> deferredCallbacks = new ArrayList<>();

    protected Session(final Sessions sessions, final Player player) {
        this.plugin = sessions.plugin;
        this.sessions = sessions;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    protected void load() {
        plugin.getDatabase().scheduleAsyncTask(() -> {
                List<SQLPlayerQuest> playerQuestRows = plugin.getDatabase().find(SQLPlayerQuest.class)
                    .eq("player", uuid).findList();
                List<SQLCompletedQuest> completedQuestRows = plugin.getDatabase().find(SQLCompletedQuest.class)
                    .eq("player", uuid).findList();
                Bukkit.getScheduler().runTask(plugin, () -> enable(playerQuestRows, completedQuestRows));
            });
    }

    private void enable(List<SQLPlayerQuest> playerQuestRows, List<SQLCompletedQuest> completedQuestRows) {
        if (!sessions.enabled || disabled) return;
        for (SQLPlayerQuest row : playerQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            Quest quest = plugin.getQuests().get(questName);
            PlayerQuest playerQuest = new PlayerQuest(this, row, quest);
            currentQuests.put(playerQuest.quest.getName(), playerQuest);
            playerQuest.loadRow();
        }
        for (SQLCompletedQuest row : completedQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            completedQuests.put(questName, row);
        }
        ready = true;
        for (BiConsumer<PlayerQuest, Goal> callback : deferredCallbacks) {
            applyGoalsNow(callback);
        }
        deferredCallbacks.clear();
    }

    protected void disable() {
        disabled = true;
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
        Quest quest = plugin.getQuests().get(questName);
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
            plugin.getDatabase().deleteAsync(playerQuest.getRow(), null);
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

    protected void applyGoals(BiConsumer<PlayerQuest, Goal> callback) {
        if (ready) {
            applyGoalsNow(callback);
        } else {
            deferredCallbacks.add(callback);
        }
    }

    private void applyGoalsNow(BiConsumer<PlayerQuest, Goal> callback) {
        for (PlayerQuest playerQuest : currentQuests.values()) {
            callback.accept(playerQuest, playerQuest.getCurrentGoal());
        }
    }
}
