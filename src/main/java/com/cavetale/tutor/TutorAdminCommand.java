package com.cavetale.tutor;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.session.Session;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class TutorAdminCommand implements TabExecutor {
    private final TutorPlugin plugin;
    private CommandNode rootNode = new CommandNode("tutoradmin");

    public void enable() {
        rootNode.addChild("start").arguments("<player> <quest>")
            .description("Start tutorial for player")
            .senderCaller(this::start);
        rootNode.addChild("stop").arguments("<player> <quest>")
            .description("Stop tutorial for player")
            .senderCaller(this::stop);
        rootNode.addChild("createpet").arguments("<player> <type>")
            .description("Spawn the player's pet")
            .senderCaller(this::createPet);
        plugin.getCommand("tutoradmin").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    private Player requirePlayer(String arg) {
        Player result = Bukkit.getPlayerExact(arg);
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
                           .append(Component.text(" starting quest: "))
                           .append(quest.getDisplayName())
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
                           .append(quest.getDisplayName())
                           .build());
        return true;
    }

    private boolean createPet(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Player player = requirePlayer(args[0]);
        PetType petType = PetType.valueOf(args[1].toUpperCase());
        Pet pet = plugin.pets.createPet(player, petType);
        pet.setExclusive(true);
        pet.setAutoRespawn(true);
        sender.sendMessage(Component.text("Pet created!", NamedTextColor.YELLOW));
        return true;
    }
}
