package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.goal.Condition;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import com.winthier.playercache.PlayerCache;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        sender.sendMessage(Component.text().color(NamedTextColor.YELLOW)
                           .append(Component.text(session.getName()))
                           .append(Component.text(" started quest: "))
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
        sender.sendMessage(Component.text().color(NamedTextColor.YELLOW)
                           .append(Component.text(session.getName()))
                           .append(Component.text(" stopped quest: "))
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
        sender.sendMessage(Component.text().color(NamedTextColor.YELLOW)
                           .append(Component.text(session.getName()))
                           .append(Component.text(" restarted quest: "))
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
        sender.sendMessage(Component.text().color(NamedTextColor.YELLOW)
                           .append(Component.text(session.getName()))
                           .append(Component.text(" skipping quest goal: "))
                           .append(quest.name.displayName)
                           .append(Component.text(", "))
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
        sender.sendMessage(Component.text().color(NamedTextColor.YELLOW)
                           .append(Component.text(session.getName()))
                           .append(Component.text(" completed condition: "))
                           .append(completedCondition.getDescription())
                           .build());
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache playerCache = requirePlayerCache(args[0]);
        plugin.sessions.findOrLoad(playerCache, session -> {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.text("Quest info for " + playerCache.name, NamedTextColor.YELLOW));
                lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                            Component.text("Completed ", NamedTextColor.GRAY),
                            Component.text("" + session.getCompletedQuests().size() + " ", NamedTextColor.YELLOW),
                            Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                                           session.getCompletedQuests().entrySet().stream()
                                           .map(entry -> {
                                                   Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                           plugin.getQuests().get(entry.getKey()).name.displayName,
                                                           Component.text("\nCompleted "
                                                                          + dateFormat.format(entry.getValue().getTime()),
                                                                          NamedTextColor.GRAY),
                                                       });
                                                   return Component.text().content(entry.getKey().key)
                                                       .color(NamedTextColor.GOLD)
                                                       .hoverEvent(HoverEvent.showText(tooltip))
                                                       .build();
                                               })
                                           .collect(Collectors.toList())),
                        }));
                lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                            Component.text("Current Quests ", NamedTextColor.GRAY),
                            Component.text("" + session.getCurrentQuests().size(), NamedTextColor.YELLOW),
                        }));
                for (PlayerQuest playerQuest : session.getCurrentQuests().values()) {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                Component.text(Unicode.BULLET_POINT.character + " ", NamedTextColor.GRAY),
                                (Component.text().content(playerQuest.getQuest().getName().key)
                                 .color(NamedTextColor.YELLOW)
                                 .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                 playerQuest.getQuest().name.displayName,
                                                 Component.text("\n" + playerQuest.getQuest().getName().type.upper, NamedTextColor.GRAY),
                                             })))
                                 .build()),
                                Component.space(),
                                (Component.text().content(playerQuest.getCurrentGoal().getId())
                                 .color(NamedTextColor.GOLD)
                                 .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                                 playerQuest.getCurrentGoal().getDisplayName(),
                                                 Component.text("" + (1 + playerQuest.getQuest().goalIndex(playerQuest.getCurrentGoal().getId()))
                                                                + "/" + playerQuest.getQuest().getGoals().size(),
                                                                NamedTextColor.GRAY),
                                             })))
                                 .build()),
                                Component.space(),
                                Component.text(playerQuest.getCurrentProgress().serialize(), NamedTextColor.GRAY),
                            }));
                }
                if (session.getPet() == null) {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                Component.text("Pet ", NamedTextColor.GRAY),
                                Component.text("None", NamedTextColor.DARK_GRAY),
                            }));
                } else {
                    lines.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                                Component.text("Pet", NamedTextColor.GRAY),
                                Component.space(),
                                (Component.text()
                                 .append(session.getPlayerPetRow().parsePetType().displayName)
                                 .color(NamedTextColor.YELLOW)
                                 .build()),
                                Component.space(),
                                session.getPlayerPetRow().getNameComponent(),
                                Component.space(),
                                Component.text(session.getPet().isSpawned()
                                               ? stringify(session.getPet().getEntity().getLocation())
                                               : "despawned", NamedTextColor.GRAY),
                                Component.space(),
                                Component.text("autoSpawn=" + session.getPlayerPetRow().isAutoSpawn(), NamedTextColor.YELLOW),
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

    private static String stringify(Location location) {
        return location.getWorld().getName()
            + " " + location.getBlockX()
            + " " + location.getBlockY()
            + " " + location.getBlockZ();
    }
}
