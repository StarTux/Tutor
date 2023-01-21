package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.tutor.daily.DailyQuest;
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
        DailyGameTag tag = new DailyGameTag();
        tag.randomize();
        DailyGame game = new DailyGame(player, tag);
        game.start();
        game.test.setup();
    }
}
