package com.cavetale.tutor;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class TutorCommand implements TabExecutor {
    private final TutorPlugin plugin;
    private CommandNode rootNode = new CommandNode("tutor");

    public void enable() {
        rootNode.description("Tutorial Menu")
            .playerCaller(this::tutor);
        rootNode.addChild("click").hidden(true)
            .playerCaller(this::click);
        rootNode.addChild("menu").denyTabCompletion()
            .description("Open the tutorial menu")
            .playerCaller(this::menu);
        rootNode.addChild("rename").denyTabCompletion()
            .description("Rename your pet")
            .playerCaller(this::rename);
        plugin.getCommand("tutor").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    private boolean tutor(Player player, String[] args) {
        if (args.length != 0) return false;
        if (!plugin.sessions.apply(player, session -> session.clickPet(player))) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        return true;
    }

    private boolean click(Player player, String[] args) {
        if (args.length == 0) return true;
        if (args.length == 1) {
            plugin.sessions.applyClick(player, args[0]);
        } else if (args.length == 2) {
            if (args[0].equals("complete")) {
                QuestName questName = QuestName.of(args[1]);
                if (questName == null) return true;
                Session session = plugin.sessions.find(player);
                if (session == null) return true;
                PlayerQuest playerQuest = session.getCurrentQuests().get(questName);
                if (!playerQuest.getCurrentProgress().isComplete()) return true;
                playerQuest.onGoalComplete(player);
            }
        }
        return true;
    }

    private boolean menu(Player player, String[] args) {
        if (args.length != 0) return false;
        if (!plugin.sessions.apply(player, session -> session.openPetMenu(player))) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        return true;
    }

    private boolean rename(Player player, String[] args) {
        if (args.length == 0) return false;
        Session session = plugin.sessions.find(player);
        if (session == null) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        if (session.getPet() == null) {
            throw new CommandWarn("You don't have a pet yet!");
        }
        String name = String.join(" ", args);
        if (name.length() < 3) {
            throw new CommandWarn("Invalid too short: " + name);
        }
        if (name.length() > 32) {
            throw new CommandWarn("Invalid too long: " + name);
        }
        session.renamePet(name);
        session.applyGoals((playerQuest, goal) -> {
                goal.onTutorEvent(playerQuest, TutorEvent.RENAME_PET);
            });
        return true;
    }
}
