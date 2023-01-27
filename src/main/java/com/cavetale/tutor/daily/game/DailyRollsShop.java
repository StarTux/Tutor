package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.util.Gui;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.tutor.TutorPlugin.plugin;
import static com.cavetale.tutor.daily.DailyQuest.checkGameModeAndSurvivalServer;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class DailyRollsShop {
    public static void open(Player player, Session session) {
        final int size = 4 * 9;
        GuiOverlay.Builder builder = GuiOverlay.BLANK.builder(size, GRAY)
            .title(textOfChildren(DefaultFont.CAVETALE, text(" Daily Roll Shop")));
        Gui gui = new Gui().size(size);
        builder.highlightSlot(3, 1, DARK_GRAY);
        gui.setItem(3, 1, Mytems.KITTY_COIN.createIcon(3, List.of(text("Exchange 3 Kitty Coins ...", GRAY))), null);
        gui.setItem(4, 1, Mytems.ARROW_RIGHT.createIcon(List.of(text("... for ...", GRAY))), null);
        builder.highlightSlot(5, 1, DARK_GRAY);
        gui.setItem(5, 1, Mytems.DICE.createIcon(List.of(textOfChildren(text("... one more Daily Game ", GRAY), Mytems.DICE))), null);
        gui.setItem(0, 3, Mytems.OK.createIcon(List.of(textOfChildren(Mytems.MOUSE_LEFT, text(" Confirm this Exchange", GRAY)),
                                                       text(tiny("No refunds!"), GRAY),
                                                       text(tiny("Daily Rolls are best"), GRAY),
                                                       text(tiny("earned by completing"), GRAY),
                                                       text(tiny("Daily Quests every day."), GRAY))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        if (session.isDailyGameLocked()) return;
                        if (confirm(player, session)) {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 2.0f);
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1.0f, 1.0f);
                            player.sendMessage(text("You do not have enough Kitty Coins", RED));
                            session.openDailyGame(player);
                        }
                    });
        gui.setItem(8, 3, Mytems.NO.createIcon(List.of(textOfChildren(Mytems.MOUSE_LEFT, text(" Cancel", GRAY)))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        if (session.isDailyGameLocked()) return;
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                        session.openDailyGame(player);
                    });
        gui.setItem(Gui.OUTSIDE, null,
                    click -> {
                        if (session.isDailyGameLocked()) return;
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                        session.openDailyGame(player);
                    });
        gui.title(builder.build());
        gui.open(player);
    }

    private static boolean confirm(Player player, Session session) {
        if (session.isDailyGameLocked()) return false;
        if (!checkGameModeAndSurvivalServer(player)) return false;
        int found = 0;
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType().isAir()) continue;
            if (Mytems.KITTY_COIN.isItem(item)) found += item.getAmount();
            if (found >= 3) break;
        }
        if (found < 3) return false;
        int todo = 3;
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType().isAir()) continue;
            if (Mytems.KITTY_COIN.isItem(item)) {
                int take = Math.min(todo, item.getAmount());
                item.subtract(take);
                todo -= take;
            }
        }
        session.addDailyRollsAsync(1, () -> session.openDailyGame(player));
        plugin().getLogger().info(player.getName() + "[DailyRollsShop] bought Daily Roll for 3 Kitty Coins");
        return true;
    }

    private DailyRollsShop() { }
}
