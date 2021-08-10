package com.cavetale.tutor;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@RequiredArgsConstructor
public final class TutorCommand implements TabExecutor {
    private final TutorPlugin plugin;
    private CommandNode rootNode = new CommandNode("tutor");

    public void enable() {
        rootNode.description("Tutorial Menu")
            .playerCaller(this::tutor);
        rootNode.addChild("click").hidden(true)
            .playerCaller(this::click);
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
        Session session = plugin.sessions.find(player);
        if (session == null) {
            throw new CommandWarn("Session loading. Please try again later!");
        }
        List<PlayerQuest> quests = session.getQuestList();
        // empty?
        List<Component> pages = new ArrayList<>();
        for (PlayerQuest playerQuest : quests) {
            pages.addAll(playerQuest.getCurrentGoal().getBookPages(playerQuest));
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Quests");
        meta.author(Component.text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.openBook(itemStack);
        return true;
    }

    private boolean click(Player player, String[] args) {
        if (args.length != 1) return true;
        plugin.sessions.applyClick(player, args[0]);
        return true;
    }
}
