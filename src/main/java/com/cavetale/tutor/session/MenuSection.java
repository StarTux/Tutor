package com.cavetale.tutor.session;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.mytems.util.Items.tooltip;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;

@RequiredArgsConstructor
public enum MenuSection {
    TUTORIALS(text("Tutorials", WHITE), color(0x77DD77)) {
        @Override protected ItemStack createIcon(Session session) {
            ItemStack tutorialItem = new ItemStack(Material.WRITTEN_BOOK);
            tutorialItem.editMeta(meta -> {
                    tooltip(meta, List.of(text("Tutorials", YELLOW),
                                          text("Tutorials help you learn", GRAY),
                                          text("the ropes of playing on", GRAY),
                                          text("Cavetale. Each completed", GRAY),
                                          text("tutorial grants you a tier", GRAY),
                                          text("point.", GRAY)));
                    meta.addItemFlags(ItemFlag.values());
                });
            return tutorialItem;
        }

        @Override protected void makeGui(Gui gui, Player player, Session session) {
            session.makeTutorialMenu(gui, player);
        }
    },
    DAILY(text("Daily Quests", WHITE), color(0xADD8E6)) {
        @Override protected ItemStack createIcon(Session session) {
            final int total = session.getPlayerRow().getDailies();
            ItemStack dailyItem = Mytems.COLORFALL_HOURGLASS
                .createIcon(List.of(title,
                                    text("Complete up to three", GRAY),
                                    text("quests every day. Each", GRAY),
                                    text("completed quest is", GRAY),
                                    text("rewarded with a tier point", GRAY),
                                    text("and a dice roll in the Daily", GRAY),
                                    text("Game.", GRAY),
                                    empty(),
                                    textOfChildren(text(tiny("completed "), GRAY), text(total, GOLD))));
            return dailyItem;
        }

        @Override protected void makeGui(Gui gui, Player player, Session session) {
            session.makeDailyQuestGui(gui, player);
        }
    },
    COLLECT(text("Collections", WHITE), color(0xB99976)) {
        @Override protected ItemStack createIcon(Session session) {
            ItemStack collectItem = new ItemStack(Material.BUNDLE);
            collectItem.editMeta(meta -> {
                    final int total = session.getPlayerRow().getCollections();
                    tooltip(meta, List.of(text("Collections", YELLOW),
                                          text("Complete collections,", GRAY),
                                          text("one at a time.", GRAY),
                                          empty(),
                                          textOfChildren(text(tiny("completed "), GRAY), text(total, YELLOW))));
                    meta.addItemFlags(ItemFlag.values());
                });
            return collectItem;
        }

        @Override protected void makeGui(Gui gui, Player player, Session session) {
            session.makeCollectMenu(gui, player);
        }
    },
    //QUESTS(text("Quests", WHITE), color(0x40000000)),
    PET(text("Pet", WHITE), color(0xFFC0CB)) {
        @Override protected ItemStack createIcon(Session session) {
            ItemStack petItem = session.pet.getType().mytems.createIcon();
            petItem.editMeta(meta -> {
                    tooltip(meta, List.of(session.playerPetRow.getNameComponent(),
                                          text("Access Pet Options", GRAY)));
                });
            return petItem;
        }

        @Override protected void makeGui(Gui gui, Player player, Session session) {
            session.makePetMenu(gui, player);
        }
    },
    ;

    protected final Component title;
    protected final TextColor backgroundColor;

    protected abstract ItemStack createIcon(Session session);
    protected void makeGui(Gui gui, Player player, Session session) { }
}

