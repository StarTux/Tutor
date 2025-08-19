package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.tutor.collect.CollectItem;
import com.cavetale.tutor.collect.ItemCollectionType;
import com.cavetale.tutor.collect.PlayerItemCollection;
import com.cavetale.tutor.daily.DailyQuest;
import com.cavetale.tutor.daily.DailyQuestGroup;
import com.cavetale.tutor.daily.DailyQuestIndex;
import com.cavetale.tutor.daily.DailyQuestType;
import com.cavetale.tutor.daily.PlayerDailyQuest;
import com.cavetale.tutor.daily.game.DailyGame;
import com.cavetale.tutor.daily.game.DailyGameTag;
import com.cavetale.tutor.goal.Condition;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerPet;
import com.cavetale.tutor.sql.SQLPlayerPetUnlock;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class TutorAdminCommand extends AbstractCommand<TutorPlugin> {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");

    protected TutorAdminCommand(final TutorPlugin plugin) {
        super(plugin, "tutoradmin");
    }

    @Override
    public void onEnable() {
        rootNode.addChild("reload").denyTabCompletion()
            .description("Reload data")
            .senderCaller(this::reload);
        rootNode.addChild("start").arguments("<player> <quest>")
            .description("Start quest for player")
            .completers(CommandArgCompleter.NULL, CommandArgCompleter.list(QuestName.KEY_LIST))
            .senderCaller(this::start);
        rootNode.addChild("stop").arguments("<player> <quest>")
            .description("Stop quest for player")
            .completers(CommandArgCompleter.NULL, CommandArgCompleter.list(QuestName.KEY_LIST))
            .senderCaller(this::stop);
        rootNode.addChild("restart").arguments("<player> <quest>")
            .description("Restart quest for player")
            .completers(CommandArgCompleter.NULL, CommandArgCompleter.list(QuestName.KEY_LIST))
            .senderCaller(this::restart);
        rootNode.addChild("skip").arguments("<player> <quest>")
            .description("Skip current goal")
            .completers(CommandArgCompleter.NULL, CommandArgCompleter.list(QuestName.KEY_LIST))
            .senderCaller(this::skip);
        rootNode.addChild("complete").arguments("<player> <quest>")
            .description("Complete current goal condition")
            .completers(CommandArgCompleter.NULL, CommandArgCompleter.list(QuestName.KEY_LIST))
            .senderCaller(this::complete);
        rootNode.addChild("info").arguments("<player>")
            .description("Player quest info")
            .completers(CommandArgCompleter.NULL)
            .senderCaller(this::info);
        rootNode.addChild("transfer").arguments("<from> <to>")
            .description("Account transfer")
            .completers(PlayerCache.NAME_COMPLETER, PlayerCache.NAME_COMPLETER)
            .senderCaller(this::transfer);
        CommandNode dailyNode = rootNode.addChild("daily")
            .description("Daily quest commands");
        dailyNode.addChild("list").denyTabCompletion()
            .description("List daily quests")
            .senderCaller(this::dailyList);
        dailyNode.addChild("test").denyTabCompletion()
            .description("Test daily GUI")
            .playerCaller(this::dailyTest);
        dailyNode.addChild("addrolls").arguments("<player> <value>")
            .completers(CommandArgCompleter.PLAYER_CACHE,
                        CommandArgCompleter.integer(i -> i != 0))
            .description("Add Daily Game Rolls")
            .senderCaller(this::dailyAddRolls);
        dailyNode.addChild("makeprogress").arguments("<player> <group> <amount>")
            .completers(CommandArgCompleter.PLAYER_CACHE,
                        CommandArgCompleter.enumLowerList(DailyQuestGroup.class),
                        CommandArgCompleter.integer(i -> i > 0))
            .description("Make daily quest progress")
            .senderCaller(this::dailyMakeProgress);
        dailyNode.addChild("reset").arguments("<player>")
            .description("Reset daily game")
            .completers(CommandArgCompleter.PLAYER_CACHE)
            .playerCaller(this::dailyReset);
        dailyNode.addChild("setrolls").arguments("<player> <rolls...>")
            .description("Reset daily game")
            .completers(CommandArgCompleter.PLAYER_CACHE,
                        CommandArgCompleter.integer(i -> i >= 1 && i <= 6),
                        CommandArgCompleter.REPEAT)
            .playerCaller(this::dailySetRolls);
        dailyNode.addChild("debug").arguments("<player>")
            .description("Daily game debug")
            .completers(CommandArgCompleter.PLAYER_CACHE)
            .playerCaller(this::dailyDebug);
        dailyNode.addChild("generate").arguments("<group> <type>")
            .description("Generate a new daily quest")
            .completers(CommandArgCompleter.enumLowerList(DailyQuestGroup.class),
                        CommandArgCompleter.enumLowerList(DailyQuestType.class))
            .playerCaller(this::dailyGenerate);
        dailyNode.addChild("testmonthchange").arguments("<year> <month>")
            .description("Test a month change")
            .completers(CommandArgCompleter.integer(i -> i >= 2012 && i <= 9999),
                        CommandArgCompleter.integer(i -> i >= 1 && i <= 12))
            .senderCaller(this::dailyTestMonthChange);
        CommandNode collectNode = rootNode.addChild("collect")
            .description("Collection commands");
        collectNode.addChild("give").arguments("<collection>")
            .completers(CommandArgCompleter.enumLowerList(ItemCollectionType.class))
            .description("Give all items of a collection")
            .playerCaller(this::collectGive);
        collectNode.addChild("unlockall").denyTabCompletion()
            .description("Unlock all collections")
            .playerCaller(this::collectUnlockAll);
    }

    private Player requirePlayer(String arg) {
        Player result = Bukkit.getPlayerExact(arg);
        if (result == null) throw new CommandWarn("Player not found: " + arg);
        return result;
    }

    private PlayerCache requirePlayerCache(String arg) {
        PlayerCache result = PlayerCache.forArg(arg);
        if (result == null) throw new CommandWarn("Player not found: " + arg);
        return result;
    }

    private Session requireSession(String arg) {
        Player player = requirePlayer(arg);
        Session session = plugin.sessions.find(player);
        if (session == null) throw new CommandWarn("Session not ready: " + arg);
        return session;
    }

    private Quest requireQuest(String arg) {
        QuestName questName = QuestName.of(arg);
        if (questName == null) throw new CommandWarn("Quest not found: " + arg);
        return plugin.quests.get(questName);
    }

    private void reload(CommandSender sender) {
        plugin.getDailyQuests().reload();
        plugin.getSessions().reload();
    }

    private boolean start(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Session session = requireSession(args[0]);
        Quest quest = requireQuest(args[1]);
        if (session.hasQuest(quest.getName())) {
            throw new CommandWarn(session.getName() + " already has quest " + quest.getName().key + "!");
        }
        session.startQuest(quest);
        sender.sendMessage(text().color(YELLOW)
                           .append(text(session.getName()))
                           .append(text(" started quest: "))
                           .append(quest.name.displayName)
                           .build());
        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Session session = requireSession(args[0]);
        Quest quest = requireQuest(args[1]);
        if (session.removeQuest(quest.name) == null) {
            throw new CommandWarn(session.getName() + " does not have quest " + quest.getName().key + "!");
        }
        sender.sendMessage(text().color(YELLOW)
                           .append(text(session.getName()))
                           .append(text(" stopped quest: "))
                           .append(quest.name.displayName)
                           .build());
        return true;
    }

    private boolean restart(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Session session = requireSession(args[0]);
        Quest quest = requireQuest(args[1]);
        if (session.removeQuest(quest.name) == null) {
            throw new CommandWarn(session.getName() + " does not have quest " + quest.getName().key + "!");
        }
        session.startQuest(quest);
        sender.sendMessage(text().color(YELLOW)
                           .append(text(session.getName()))
                           .append(text(" restarted quest: "))
                           .append(quest.name.displayName)
                           .build());
        return true;
    }

    private boolean skip(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Session session = requireSession(args[0]);
        Quest quest = requireQuest(args[1]);
        PlayerQuest playerQuest = session.getQuest(quest.name);
        if (playerQuest == null) {
            throw new CommandWarn(session.getName() + " does not have quest " + quest.getName().key + "!");
        }
        sender.sendMessage(text().color(YELLOW)
                           .append(text(session.getName()))
                           .append(text(" skipping quest goal: "))
                           .append(quest.name.displayName)
                           .append(text(", "))
                           .append(playerQuest.getCurrentGoal().getDisplayName())
                           .build());
        playerQuest.onGoalComplete(session.getPlayer());
        return true;
    }

    private boolean complete(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Session session = requireSession(args[0]);
        Quest quest = requireQuest(args[1]);
        PlayerQuest playerQuest = session.getQuest(quest.name);
        if (playerQuest == null) {
            throw new CommandWarn(session.getName() + " does not have quest " + quest.getName().key + "!");
        }
        if (playerQuest.isComplete()) {
            throw new CommandWarn("Quest is already complete!");
        }
        Condition completedCondition = null;
        for (Condition condition : playerQuest.getCurrentGoal().getConditions()) {
            if (condition.complete(playerQuest)) {
                completedCondition = condition;
                break;
            }
        }
        if (completedCondition == null) {
            throw new CommandWarn("No completable goal was found!");
        }
        sender.sendMessage(text().color(YELLOW)
                           .append(text(session.getName()))
                           .append(text(" completed condition: "))
                           .append(completedCondition.getDescription())
                           .build());
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache playerCache = requirePlayerCache(args[0]);
        plugin.sessions.findOrLoad(playerCache, session -> {
                List<Component> lines = new ArrayList<>();
                lines.add(text("Quest info for " + playerCache.name, YELLOW));
                lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                            text("Completed ", GRAY),
                            text("" + session.getCompletedQuests().size() + " ", YELLOW),
                            Component.join(JoinConfiguration.separator(text(", ", GRAY)),
                                           session.getCompletedQuests().entrySet().stream()
                                           .map(entry -> {
                                                   Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                           plugin.getQuests().get(entry.getKey()).name.displayName,
                                                           text("\nCompleted "
                                                                          + dateFormat.format(entry.getValue().getTime()),
                                                                          GRAY),
                                                       });
                                                   return text().content(entry.getKey().key)
                                                       .color(GOLD)
                                                       .hoverEvent(HoverEvent.showText(tooltip))
                                                       .build();
                                               })
                                           .collect(Collectors.toList())),
                        }));
                lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                            text("Current Quests ", GRAY),
                            text("" + session.getCurrentQuests().size(), YELLOW),
                        }));
                for (PlayerQuest playerQuest : session.getCurrentQuests().values()) {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                text(Unicode.BULLET_POINT.character + " ", GRAY),
                                (text().content(playerQuest.getQuest().getName().key)
                                 .color(YELLOW)
                                 .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                 playerQuest.getQuest().name.displayName,
                                                 text("\n" + playerQuest.getQuest().getName().type.upper, GRAY),
                                             })))
                                 .build()),
                                Component.space(),
                                (text().content(playerQuest.getCurrentGoal().getId())
                                 .color(GOLD)
                                 .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                 playerQuest.getCurrentGoal().getDisplayName(),
                                                 text("" + (1 + playerQuest.getQuest().goalIndex(playerQuest.getCurrentGoal().getId()))
                                                                + "/" + playerQuest.getQuest().getGoals().size(),
                                                                GRAY),
                                             })))
                                 .build()),
                                Component.space(),
                                text(playerQuest.getCurrentProgress().serialize(), GRAY),
                            }));
                }
                if (session.getPet() == null) {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                text("Pet ", GRAY),
                                text("None", DARK_GRAY),
                            }));
                } else {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                text("Pet", GRAY),
                                Component.space(),
                                (text()
                                 .append(session.getPlayerPetRow().parsePetType().displayName)
                                 .color(YELLOW)
                                 .build()),
                                Component.space(),
                                session.getPlayerPetRow().getNameComponent(),
                                Component.space(),
                                text(session.getPet().isSpawned()
                                               ? stringify(session.getPet().getEntity().getLocation())
                                               : "despawned", GRAY),
                                Component.space(),
                                text("autoSpawn=" + session.getPlayerPetRow().isAutoSpawn(), YELLOW),
                            }));
                }
                sender.sendMessage(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                            Component.newline(),
                            Component.join(JoinConfiguration.separator(Component.newline()), lines),
                            Component.newline(),
                        }));
            });
        return true;
    }

    /**
     * Account transfer.
     * This will corrupt cached sessions in case any of the players
     * are online!
     */
    private boolean transfer(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache from = PlayerCache.forArg(args[0]);
        if (from == null) throw new CommandWarn("Player not found: " + args[0]);
        PlayerCache to = PlayerCache.forArg(args[1]);
        if (to == null) throw new CommandWarn("Player not found: " + args[1]);
        if (from.equals(to)) throw new CommandWarn("Players are identical: " + from.getName());
        Player fromPlayer = Bukkit.getPlayer(from.uuid);
        Player toPlayer = Bukkit.getPlayer(to.uuid);
        if (fromPlayer != null) plugin.sessions.removeSession(fromPlayer);
        if (toPlayer != null) plugin.sessions.removeSession(toPlayer);
        List<SQLCompletedQuest> completedQuestList = plugin.database.find(SQLCompletedQuest.class).eq("player", from.uuid).findList();
        List<SQLPlayerPet> playerPetList = plugin.database.find(SQLPlayerPet.class).eq("player", from.uuid).findList();
        List<SQLPlayerPetUnlock> playerPetUnlockList = plugin.database.find(SQLPlayerPetUnlock.class).eq("player", from.uuid).findList();
        List<SQLPlayerQuest> playerQuestList = plugin.database.find(SQLPlayerQuest.class).eq("player", from.uuid).findList();
        if (completedQuestList.isEmpty()
            && playerPetList.isEmpty()
            && playerPetUnlockList.isEmpty()
            && playerQuestList.isEmpty()) {
            throw new CommandWarn(from.name + " does not have any quest data");
        }
        int deleted = 0;
        deleted += plugin.database.delete(completedQuestList);
        deleted += plugin.database.delete(playerPetList);
        deleted += plugin.database.delete(playerPetUnlockList);
        deleted += plugin.database.delete(playerQuestList);
        for (SQLCompletedQuest it : completedQuestList) {
            it.setId(null);
            it.setPlayer(to.uuid);
        }
        plugin.database.save(completedQuestList);
        for (SQLPlayerPet it : playerPetList) {
            it.setId(null);
            it.setPlayer(to.uuid);
        }
        plugin.database.save(playerPetList);
        for (SQLPlayerPetUnlock it : playerPetUnlockList) {
            it.setId(null);
            it.setPlayer(to.uuid);
        }
        plugin.database.save(playerPetUnlockList);
        for (SQLPlayerQuest it : playerQuestList) {
            it.setId(null);
            it.setPlayer(to.uuid);
        }
        plugin.database.save(playerQuestList);
        sender.sendMessage(text("Transferred quest data from " + from.name + " to " + to.name + ":"
                                + " completed=" + completedQuestList.size()
                                + " pets=" + playerPetList.size()
                                + " petUnlocks=" + playerPetUnlockList.size()
                                + " quests=" + playerQuestList.size()
                                + " deleted=" + deleted,
                                YELLOW));
        if (fromPlayer != null) plugin.sessions.createSession(fromPlayer);
        if (toPlayer != null) plugin.sessions.createSession(toPlayer);
        return true;
    }

    private static String stringify(Location location) {
        return location.getWorld().getName()
            + " " + location.getBlockX()
            + " " + location.getBlockY()
            + " " + location.getBlockZ();
    }

    private void dailyList(CommandSender sender) {
        int index = 0;
        for (DailyQuest it : plugin.getDailyQuests().getDailyQuests()) {
            sender.sendMessage((index++) + ") " + it);
        }
        sender.sendMessage("Total " + index);
    }

    private void dailyTest(Player player) {
        DailyGame game = new DailyGame(player, new DailyGameTag().randomize());
        game.start();
        game.test.setup();
    }

    private boolean dailyAddRolls(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        int value = CommandArgCompleter.requireInt(args[1]);
        plugin.sessions.findOrLoad(target, session -> session.addDailyRollsAsync(value, null));
        sender.sendMessage(text("Added " + value + " daily rolls for " + target.name, YELLOW));
        return true;
    }

    private boolean dailyMakeProgress(CommandSender sender, String[] args) {
        if (args.length != 3) return false;
        PlayerCache target = PlayerCache.require(args[0]);
        final DailyQuestGroup group = CommandArgCompleter.requireEnum(DailyQuestGroup.class, args[1]);
        final int amount = CommandArgCompleter.requireInt(args[2], i -> i > 0);
        plugin.sessions.findOrLoad(target, session -> {
                for (PlayerDailyQuest it : session.getDailyQuests()) {
                    DailyQuest dailyQuest = it.getDailyQuest();
                    if (dailyQuest.getGroup() != group) continue;
                    if (it.isComplete()) {
                        sender.sendMessage(text("Already completed" + group, RED));
                        return;
                    }
                    dailyQuest.makeProgress(it, amount);
                    sender.sendMessage(text("Progress made: " + amount, AQUA));
                    return;
                }
                sender.sendMessage(text("Daily quest not found: group=" + group, RED));
            });
        return true;
    }

    private boolean dailyReset(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        final PlayerCache target = PlayerCache.require(args[0]);
        plugin.sessions.findOrLoad(target, session -> {
                final DailyGameTag tag = new DailyGameTag().randomize();
                session.saveDailyGameAsync(0, tag, () -> {
                        sender.sendMessage(text("Daily game of " + target.name + " was reset", YELLOW));
                    });
            });
        return true;
    }

    private boolean dailyDebug(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        final PlayerCache target = PlayerCache.require(args[0]);
        plugin.sessions.findOrLoad(target, session -> {
                final DailyGameTag tag = session.getPlayerRow().parseDailyGameTag();
                tag.debug();
                session.saveDailyGameAsync(session.getPlayerRow().getDailyGameRolls(), tag, () -> {
                        sender.sendMessage(text("Daily game of " + target.name + " 'debugged'", YELLOW));
                    });
            });
        return true;
    }

    private boolean dailyGenerate(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        final DailyQuestGroup group = CommandArgCompleter.requireEnum(DailyQuestGroup.class, args[0]);
        final DailyQuestType type = CommandArgCompleter.requireEnum(DailyQuestType.class, args[1]);
        final DailyQuest<?, ?> oldQuest = plugin.getDailyQuests().deleteDailyQuest(group);
        if (oldQuest == null) {
            sender.sendMessage(text("Could not delete daily with group " + group, RED));
        }
        final int index = ThreadLocalRandom.current().nextInt(type.getOptionCount());
        final DailyQuest<?, ?> newQuest = plugin.getDailyQuests().generateNewQuest(group, new DailyQuestIndex(type, index));
        plugin.getSessions().cleanDailyQuests();
        plugin.getDailyQuests().broadcastUpdate(newQuest);
        return true;
    }

    private boolean dailySetRolls(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        final PlayerCache target = PlayerCache.require(args[0]);
        final List<Integer> rolls = new ArrayList<>();
        for (int i = 1; i < args.length; i += 1) {
            rolls.add(CommandArgCompleter.requireInt(args[i], roll -> roll >= roll && roll <= 6));
        }
        plugin.sessions.findOrLoad(target, session -> {
                final DailyGameTag tag = session.getPlayerRow().parseDailyGameTag();
                tag.setRolls(rolls);
                session.saveDailyGameAsync(session.getPlayerRow().getDailyGameRolls(), tag, () -> {
                        sender.sendMessage(text("Daily game rolls of " + target.name
                                                + " set to " + rolls, YELLOW));
                    });
            });
        return true;
    }

    private boolean dailyTestMonthChange(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        final int year = CommandArgCompleter.requireInt(args[0], i -> i >= 2012 && i <= 9999);
        final int month = CommandArgCompleter.requireInt(args[1], i -> i >= 1 && i <= 12);
        sender.sendMessage(text("Testing month change: " + year + " " + month + ". See console", YELLOW));
        plugin.getDailyQuests().onMonthChange(year, month, true);
        return true;
    }

    private boolean collectGive(Player player, String[] args) {
        if (args.length != 1) return false;
        ItemCollectionType type = CommandArgCompleter.requireEnum(ItemCollectionType.class, args[0]);
        int total = 0;
        for (CollectItem item : type.getItems()) {
            player.getInventory().addItem(item.createItemStack(item.getTotalAmount()));
            total += 1;
        }
        player.sendMessage(text(total + " items added to inventory", AQUA));
        return true;
    }

    private void collectUnlockAll(Player player) {
        Session session = plugin.sessions.find(player);
        if (session == null) throw new CommandWarn("Session not ready");
        int total = 0;
        for (PlayerItemCollection it : session.getCollections().values()) {
            if (it.isUnlocked()) continue;
            it.unlock();
            total += 1;
        }
        if (total == 0) throw new CommandWarn("Nothing to unlock");
        player.sendMessage(text(total + " collections unlocked", AQUA));
    }
}
