package com.cavetale.tutor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.SpawnRule;
import com.cavetale.tutor.session.MenuSection;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public final class TutorCommand extends AbstractCommand<TutorPlugin> {
    public TutorCommand(final TutorPlugin plugin) {
        super(plugin, "tutor");
    }

    @Override
    protected void onEnable() {
        rootNode.description("Tutor Menu")
            .playerCaller(this::tutor);
        rootNode.addChild("click").hidden(true)
            .playerCaller(this::click);
        rootNode.addChild("menu").denyTabCompletion()
            .description("Open the tutor menu")
            .playerCaller(this::menu);
        rootNode.addChild("rename").denyTabCompletion()
            .description("Rename your pet")
            .playerCaller(this::rename);
        rootNode.addChild("spawn").denyTabCompletion()
            .description("Spawn your pet")
            .playerCaller(this::spawn);
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
            switch (args[0]) {
            case "complete": {
                QuestName questName = QuestName.of(args[1]);
                if (questName == null) return true;
                Session session = plugin.sessions.find(player);
                if (session == null) return true;
                PlayerQuest playerQuest = session.getCurrentQuests().get(questName);
                if (playerQuest == null) return true;
                if (!playerQuest.getCurrentProgress().isComplete()) return true;
                if (playerQuest.getCurrentGoal().hasMissedConstraints(playerQuest)) return true;
                playerQuest.onGoalComplete(player);
                return true;
            }
            case "redo": {
                QuestName questName = QuestName.of(args[1]);
                if (questName == null) return true;
                Session session = plugin.sessions.find(player);
                if (session == null) return true;
                if (!session.getCurrentQuests().isEmpty()) {
                    throw new CommandWarn("You already have an active quest");
                }
                session.startQuest(questName);
                return true;
            }
            case "quit": {
                QuestName questName = QuestName.of(args[1]);
                if (questName == null) return true;
                Session session = plugin.sessions.find(player);
                if (session == null) return true;
                PlayerQuest playerQuest = session.getCurrentQuests().get(questName);
                if (playerQuest == null) return true;
                if (!session.getCompletedQuests().containsKey(questName) && !questName.isQuittable()) {
                    throw new CommandWarn("You cannot abandon this " + questName.type.lower);
                }
                session.removeQuest(questName);
                player.sendMessage(textOfChildren(text(questName.type.upper + " abandoned: ", YELLOW),
                                                  playerQuest.getQuest().name.displayName));
                if (questName.type == QuestType.TUTORIAL) {
                    session.openMenu(player, MenuSection.TUTORIALS);
                }
                return true;
            }
            default: return true;
            }
        }
        return true;
    }

    private boolean menu(Player player, String[] args) {
        if (args.length != 0) return false;
        if (!plugin.sessions.apply(player, session -> session.openMenu(player))) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        return true;
    }

    private boolean rename(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(text("\n  Click here to change the name of your pet\n", GREEN, BOLD)
                               .clickEvent(suggestCommand("/tutor rename "))
                               .hoverEvent(showText(text("/tutor rename", YELLOW))));
            return true;
        }
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

    private boolean spawn(Player player, String[] args) {
        if (args.length != 0) return false;
        Session session = plugin.sessions.find(player);
        if (session == null) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        Pet pet = session.getPet();
        if (pet == null) {
            throw new CommandWarn("You don't have a pet yet!");
        }
        if (!pet.tryToSpawn(player, SpawnRule.LOOKAT)) {
            if (!pet.tryToSpawn(player, SpawnRule.NEARBY)) {
                throw new CommandWarn("Could not spawn your pet!");
            }
        }
        pet.setAutoDespawn(false);
        player.sendMessage(textOfChildren(pet.getCustomName(), text(" appeared!")).color(GREEN));
        return true;
    }
}
