package com.cavetale.tutor.daily;

import com.cavetale.core.connect.Connect;
import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.connect.ServerGroup;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.cavetale.core.event.dungeon.DungeonDiscoverEvent;
import com.cavetale.core.event.friends.PlayerShareFriendshipGiftEvent;
import com.cavetale.core.event.minigame.MinigameMatchCompleteEvent;
import com.cavetale.core.event.mobarena.MobArenaWaveCompleteEvent;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.util.Json;
import com.cavetale.mytems.item.treechopper.TreeChopEvent;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.sql.SQLDailyQuest;
import com.cavetale.tutor.sql.SQLPlayerDailyQuest;
import com.cavetale.tutor.time.Timer;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import java.io.File;
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
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
        timer.setOnHourChange(this::onHourChange);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        database().scheduleAsyncTask(() -> {
                loadDailyQuestsSync(timer.getDayId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                        ready = true;
                        checkDailyQuestExpiry();
                    });
            });
    }

    private void onHourChange() {
        if (!ready) return;
        checkDailyQuestExpiry();
    }

    public void reload() {
        dailyQuests.clear();
        loadDailyQuestsSync(timer.getDayId());
        Bukkit.getScheduler().runTask(plugin, () -> {
                checkDailyQuestExpiry();
            });
    }

    /**
     * Call in async thread!
     * Called on enable or when the update message is received.  This
     * is stable to be called many times.
     */
    private void loadDailyQuestsSync(final int dayId) {
        List<SQLDailyQuest> rows = database().find(SQLDailyQuest.class)
            .eq("dayId", dayId)
            .findList();
        for (SQLDailyQuest row : rows) {
            if (forRowId(row.getId()) != null) continue;
            final DailyQuestType type = DailyQuestType.ofKey(row.getQuestType());
            if (type == null) {
                plugin.getLogger().severe("[Daily] Unknown quest type: " + row);
                continue;
            }
            DailyQuest dailyQuest = type.create();
            Bukkit.getScheduler().runTask(plugin, () -> {
                    dailyQuest.load(row);
                    dailyQuests.add(dailyQuest);
                    dailyQuest.enable();
                    plugin.getSessions().loadDailyQuest(dailyQuest);
                    dailyQuests.sort((a, b) -> Integer.compare(a.getGroup(), b.getGroup()));
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
            exclusions.add(dailyQuest.getType());
        }
        for (int group = 0; group < 3; group += 1) {
            DailyQuest dailyQuest = generateNewQuest(group, exclusions);
            if (dailyQuest == null) continue;
            result += 1;
            exclusions.add(dailyQuest.getType());
        }
        return result;
    }

    public DailyQuest generateNewQuest(final int group, Set<DailyQuestType> exclusion) {
        // Delete old
        final DailyQuest old = forGroup(group);
        if (old != null) {
            plugin.getLogger().info("[Daily] Quest already exists for group " + group);
            return null;
        }
        // Build new bag, exclusions avoid duplicates
        List<DailyQuestIndex> types = DailyQuestType.getAllWithGroup(group);
        types.removeIf(it -> exclusion.contains(it.type));
        if (types.isEmpty()) {
            plugin.getLogger().severe("[Daily] No types for group " + group);
            return null;
        }
        List<DailyQuestIndex> bag = new ArrayList<>(types);
        // Eliminate all recently done types in this group
        File doneFile = new File(plugin.getDataFolder(), "dailyDone" + group + ".json");
        DailyQuestBag done = Json.load(doneFile, DailyQuestBag.class, DailyQuestBag::new);
        bag.removeAll(done.indexes);
        if (bag.isEmpty()) {
            bag.addAll(types);
            done.indexes.clear();
        }
        // Pick one, save new bag
        final DailyQuestIndex index = bag.get(ThreadLocalRandom.current().nextInt(bag.size()));
        done.indexes.add(index);
        plugin.getDataFolder().mkdirs();
        Json.save(doneFile, done, true);
        // Create the quest
        DailyQuest<?, ?> quest = generateNewQuest(group, index);
        return quest;
    }

    public DailyQuest deleteDailyQuest(int group) {
        DailyQuest oldQuest = null;
        for (DailyQuest it : dailyQuests) {
            if (it.getGroup() == group) {
                oldQuest = it;
                break;
            }
        }
        if (oldQuest == null) return null;
        final SQLDailyQuest oldRow = oldQuest.getRow();
        plugin.getDatabase().delete(oldRow);
        dailyQuests.remove(oldQuest);
        plugin.getDatabase().find(SQLPlayerDailyQuest.class)
            .eq("dailyQuestId", oldQuest.getRow().getId())
            .deleteAsync(null);
        return oldQuest;
    }

    public DailyQuest generateNewQuest(int group, DailyQuestIndex index) {
        final DailyQuest<?, ?> quest = index.type.create();
        quest.setGroup(group);
        quest.generate(index.index);
        dailyQuests.add(quest);
        dailyQuests.sort((a, b) -> Integer.compare(a.getGroup(), b.getGroup()));
        database().scheduleAsyncTask(() -> {
                if (!quest.makeRow()) {
                    plugin.getLogger().severe("[Daily] Failed make row " + quest);
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                        quest.enable();
                        plugin.getSessions().loadDailyQuest(quest);
                        broadcastUpdate(quest);
                        plugin.getLogger().info("[Daily] Quest generated: " + index.type + ", " + index.index);
                    });
            });
        return quest;
    }

    public void broadcastUpdate(DailyQuest<?, ?> quest) {
        Connect.get().broadcastMessage(ServerGroup.current(), DAILY_QUEST_UPDATE, "" + quest.getDayId());
    }

    public DailyQuest forRowId(int id) {
        for (DailyQuest it : dailyQuests) {
            if (it.getRowId() == id) return it;
        }
        return null;
    }

    public DailyQuest forGroup(int group) {
        for (DailyQuest it : dailyQuests) {
            if (it.getGroup() == group) return it;
        }
        return null;
    }

    @EventHandler
    private void onConnectMessage(ConnectMessageEvent event) {
        switch (event.getChannel()) {
        case DAILY_QUEST_UPDATE:
            int dayId = Integer.parseInt(event.getPayload());
            plugin.getLogger().info("[Daily] Received Daily Quest Update: " + dayId);
            database().scheduleAsyncTask(() -> {
                    loadDailyQuestsSync(dayId);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getSessions().cleanDailyQuests();
                        });
                });
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
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestKillMonster killMonster) {
                    killMonster.onEntityDeath(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onDungeonDiscover(DungeonDiscoverEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestFindDungeon dungeonDiscover) {
                    dungeonDiscover.onDungeonDiscover(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onMinigameMatchComplete(MinigameMatchCompleteEvent event) {
        plugin.getLogger().info("[Daily] " + event.getEventName() + " " + event.getType() + " " + event.getPlayerUuids());
        for (Player player : event.getPlayers()) {
            plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                    DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                    if (dailyQuest instanceof DailyQuestMinigameMatch minigameMatch) {
                        minigameMatch.onMinigameMatchComplete(player, playerDailyQuest, event);
                    }
                });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onMobArenaWaveComplete(MobArenaWaveCompleteEvent event) {
        plugin.getLogger().info("[Daily] " + event.getEventName() + " " + event.getPlayerUuids());
        for (Player player : event.getPlayers()) {
            plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                    DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                    if (dailyQuest instanceof DailyQuestMobArenaWave mobArenaQuest) {
                        mobArenaQuest.onMobArenaWaveComplete(player, playerDailyQuest, event);
                    } else if (dailyQuest instanceof DailyQuestMobArenaWaves mobArenaQuest) {
                        mobArenaQuest.onMobArenaWaveComplete(player, playerDailyQuest, event);
                    }
                });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPluginPlayer(PluginPlayerEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                dailyQuest.onPluginPlayer(player, playerDailyQuest, event);
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerShareFriendshipGift(PlayerShareFriendshipGiftEvent event) {
        for (Player player : event.getBothPlayers()) {
            plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                    DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                    if (dailyQuest instanceof DailyQuestFriendshipGift friendshipGift) {
                        friendshipGift.onPlayerShareFriendshipGift(player, playerDailyQuest, event);
                    }
                });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestCrafting crafting) {
                    crafting.onCraftItem(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestEating eating) {
                    eating.onPlayerItemConsume(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestBreeding breeding) {
                    breeding.onEntityBreed(player, playerDailyQuest, event);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityFertilizeEgg(EntityFertilizeEggEvent event) {
        Player player = event.getBreeder();
        plugin.getSessions().applyDailyQuests(player, playerDailyQuest -> {
                DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
                if (dailyQuest instanceof DailyQuestBreeding breeding) {
                    breeding.onEntityFertilizeEgg(player, playerDailyQuest, event);
                }
            });
    }
}
