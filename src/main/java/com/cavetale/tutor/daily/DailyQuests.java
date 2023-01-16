package com.cavetale.tutor.daily;

import com.cavetale.core.connect.Connect;
import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.connect.ServerGroup;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.cavetale.mytems.item.treechopper.TreeChopEvent;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.sql.SQLDailyQuest;
import com.cavetale.tutor.time.Timer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import static com.cavetale.tutor.TutorPlugin.database;

@Getter @RequiredArgsConstructor
public final class DailyQuests implements Listener {
    private final TutorPlugin plugin;
    private final List<DailyQuest> dailyQuests = new ArrayList<>();
    private final Timer timer = new Timer("UTC-11");
    private boolean manager;
    private static final String DAILY_QUEST_UPDATE = "tutor:daily_quest_update";
    private boolean ready;

    public void enable() {
        this.manager = NetworkServer.current() == NetworkServer.current().getManager();
        timer.enable(plugin);
        timer.setOnDayBreak(this::onDayBreak);
        timer.setOnHourChange(this::onHourChange);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        database().scheduleAsyncTask(() -> {
                loadDailyQuestsSync();
                Bukkit.getScheduler().runTask(plugin, () -> {
                        ready = true;
                        checkDailyQuestExpiry();
                    });
            });
    }

    private void onDayBreak() {
        if (!ready) return;
        // ...
    }

    private void onHourChange() {
        if (!ready) return;
        checkDailyQuestExpiry();
    }

    /**
     * Call in async thread!
     * Called on enable or when the update message is received.  This
     * is stable to be called many times.
     */
    private void loadDailyQuestsSync() {
        final int dayId = timer.getDayId();
        List<SQLDailyQuest> rows = database().find(SQLDailyQuest.class)
            .eq("dayId", dayId)
            .findList();
        for (SQLDailyQuest row : rows) {
            if (forRowId(row.getId()) != null) continue;
            final DailyQuestType type = DailyQuestType.ofKey(row.getQuestType());
            if (type == null) {
                plugin.getLogger().severe("Unknown quest type: " + row);
                continue;
            }
            DailyQuest dailyQuest = type.create();
            Bukkit.getScheduler().runTask(plugin, () -> {
                    dailyQuest.load(row);
                    dailyQuests.add(dailyQuest);
                    dailyQuest.enable();
                    plugin.getSessions().loadDailyQuest(dailyQuest);
                    dailyQuests.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));
                });
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("[Daily] " + dailyQuests.size() + " quests loaded");
            });
    }

    private void checkDailyQuestExpiry() {
        final int dayId = timer.getDayId();
        for (DailyQuest dailyQuest : List.copyOf(dailyQuests)) {
            if (dailyQuest.dayId != dayId) {
                dailyQuest.setActive(false);
                dailyQuests.remove(dailyQuest);
                plugin.getSessions().expireDailyQuests(dayId);
                if (manager) {
                    dailyQuest.saveAsync(null);
                }
            }
        }
        if (manager) generateNewQuests();
    }

    private int generateNewQuests() {
        int result = 0;
        Set<DailyQuestType> exclusions = EnumSet.noneOf(DailyQuestType.class);
        for (DailyQuest dailyQuest : dailyQuests) {
            exclusions.remove(dailyQuest.getType());
        }
        for (int index = 0; index < 3; index += 1) {
            DailyQuest dailyQuest = generateNewQuest(index, exclusions);
            if (dailyQuest == null) continue;
            result += 1;
            exclusions.add(dailyQuest.getType());
        }
        return result;
    }

    public DailyQuest generateNewQuest(final int index, Set<DailyQuestType> exclusion) {
        final DailyQuest old = forDailyIndex(index);
        if (old != null) {
            plugin.getLogger().info("[Daily] Quest already exists for index " + index + ", " + old);
            return null;
        }
        List<DailyQuestType> types = DailyQuestType.getAllWithIndex(index);
        if (types.isEmpty()) {
            plugin.getLogger().severe("[Daily] No types for index " + index);
            return null;
        }
        final DailyQuestType type = types.get(ThreadLocalRandom.current().nextInt(types.size()));
        DailyQuest<?, ?> quest = type.create();
        quest.generate(index);
        dailyQuests.add(quest);
        dailyQuests.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));
        database().scheduleAsyncTask(() -> {
                if (!quest.makeRow()) {
                    plugin.getLogger().severe("[Daily] Failed make row " + quest);
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                        quest.enable();
                        plugin.getSessions().loadDailyQuest(quest);
                        Connect.get().broadcastMessage(ServerGroup.current(), DAILY_QUEST_UPDATE, null);
                        plugin.getLogger().info("Quest generated: " + quest);
                    });
            });
        return quest;
    }

    public DailyQuest forRowId(int id) {
        for (DailyQuest it : dailyQuests) {
            if (it.getRowId() == id) return it;
        }
        return null;
    }

    public DailyQuest forDailyIndex(int index) {
        for (DailyQuest it : dailyQuests) {
            if (it.getDailyIndex() == index) return it;
        }
        return null;
    }

    @EventHandler
    private void onConnectMessage(ConnectMessageEvent event) {
        switch (event.getChannel()) {
        case DAILY_QUEST_UPDATE:
            database().scheduleAsyncTask(this::loadDailyQuestsSync);
            break;
        default: break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onBlockBreak(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onBlockDropItem(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerBreakBlock(PlayerBreakBlockEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onPlayerBreakBlock(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerHarvestBlock(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onPlayerHarvestBlock(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onPlayerFish(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onTreeChop(TreeChopEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestTreeChopper treeChopper) {
                    treeChopper.onTreeChop(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestShearSheep shearSheep) {
                    shearSheep.shearSheep(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent event) {
        System.out.println(event.getEventName() + " " + event.getEntity().getKiller());
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestKillMonster killMonster) {
                    killMonster.onEntityDeath(player, playerDailyQuest, event);
                }
            });
    }
}
