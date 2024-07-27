package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.item.ItemKinds;
import com.cavetale.inventory.mail.ItemMail;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.util.Gui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import static com.cavetale.tutor.TutorPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;

/**
 * A DailyGameGoody Chest was rolled so now we are playing a little
 * game of chance to determine the prize.  This exists mainly to
 * visualize the prize pool
 */
@RequiredArgsConstructor
public final class DailyGameChest {
    protected final Player player;
    protected final DailyGameTag tag;
    protected final Session session;
    protected final long seed;
    private Random random;
    private boolean closed = false;
    private BukkitTask task;
    private Gui gui;
    private List<ItemStack> items = new ArrayList<>();
    private static final int[] ITEM_INDEXES = {
        4, 5, 6, 15,
        16, 25, 34, 43,
        42, 51, 50, 49, 48, 47, 38,
        37, 28, 19, 10,
        11, 2, 3,
    };
    private static final int TOTAL_ITEM_COUNT = ITEM_INDEXES.length;
    private State state = State.INIT;
    private int spinningTicks = 0;
    private int spinningDelay = 0;
    private int spinningCooldown = 0;

    private enum State {
        INIT,
        SPINNING,
        DONE,
        ;
    }

    public void start() {
        // Prep random items
        random = new Random(seed);
        final List<ItemStack> allItems = getChestLootPool();
        Collections.shuffle(allItems, random);
        for (int i = 0; i < TOTAL_ITEM_COUNT; i += 1) {
            items.add(allItems.get(i % allItems.size()));
        }
        this.task = Bukkit.getScheduler().runTaskTimer(plugin(), this::tick, 1L, 1L);
        open();
    }

    private void open() {
        // Set up gui
        final int size = 6 * 9;
        final int arrowSlot = 13;
        this.gui = new Gui().size(size);
        gui.setOverlay(GuiOverlay.BLANK.builder(size, BLUE)
                       .title(text("Daily Game Secret Chest", GOLD))
                       .highlightSlot(ITEM_INDEXES[0], GOLD));
        for (int i = 1; i < TOTAL_ITEM_COUNT; i += 1) {
            gui.getOverlay().highlightSlot(ITEM_INDEXES[i], color(0x3333DD));
        }
        gui.onClose(evt -> closed = true);
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                session.openMenu(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            });
        placeItemsInGui();
        gui.setItem(arrowSlot, Mytems.ARROW_UP.createIcon(List.of(text("Click here to spin", GRAY))), click -> {
                if (state != State.INIT) return;
                state = State.SPINNING;
                gui.setItem(arrowSlot, Mytems.ARROW_UP.createIcon(List.of(text("Spinning...", GRAY))));
            });
        gui.open(player);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1f);
    }

    private void placeItemsInGui() {
        for (int i = 0; i < TOTAL_ITEM_COUNT; i += 1) {
            gui.setItem(ITEM_INDEXES[i], items.get(i));
        }
    }

    private void tick() {
        if (closed || !player.isValid() || session.isDisabled() || Gui.of(player) != gui) {
            task.cancel();
            return;
        }
        switch (state) {
        case SPINNING: {
            if (spinningCooldown > 0) {
                spinningCooldown -= 1;
                return;
            } else if (spinningDelay > 0) {
                spinningCooldown = (spinningDelay - 1) * 2 + 1;
            }
            ItemStack last = items.remove(items.size() - 1);
            items.add(0, last);
            placeItemsInGui();
            final int maxDelay = 20;
            final float pitch = 2f - (((float) spinningDelay / (float) maxDelay) * 0.75f);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, pitch);
            if (spinningTicks++ < 100) return;
            boolean doSlowDown = false;
            for (int i = 0; i <= spinningDelay; i += 1) {
                if (random.nextInt(20) == 0) {
                    doSlowDown = true;
                    break;
                }
            }
            if (doSlowDown) {
                // Slow down
                if (spinningDelay == 0) {
                    spinningDelay = 1;
                } else {
                    spinningDelay *= 2;
                }
                if (spinningDelay > maxDelay) {
                    quit();
                }
            }
            break;
        }
        case DONE: break;
        default: break;
        }
    }

    private void quit() {
        tag.setChestSeed(0L);
        state = State.DONE;
        final ItemStack reward = items.get(0);
        plugin().getLogger().info("[DailyChest] " + player.getName() + " wins " + ItemKinds.name(reward));
        for (int i = 1; i < TOTAL_ITEM_COUNT; i += 1) {
            gui.setItem(ITEM_INDEXES[i], null);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 2.0f);
        session.saveDailyGameAsync(session.getPlayerRow().getDailyGameRolls(), tag, () -> {
                final Component message = text("Daily Game Secret Chest", GOLD);
                ItemMail.send(player.getUniqueId(), List.of(reward), message);
                plugin().getLogger().info("[DailyChest] " + player.getName() + " delivered");
            });
        return;
    }

    private static List<ItemStack> getChestLootPool() {
        final List<ItemStack> result = new ArrayList<>();
        result.add(Mytems.RUBY_COIN.createItemStack());
        result.add(Mytems.MAGIC_CAPE.createItemStack());
        result.add(Mytems.MOBSLAYER.createItemStack());
        result.add(Mytems.BINGO_BUKKIT.createItemStack());
        result.add(Mytems.WITCH_BROOM.createItemStack());
        // result.add(Mytems.BLUNDERBUSS.createItemStack());
        // result.add(Mytems.CAPTAINS_CUTLASS.createItemStack());
        // result.add(Mytems.ENDERBALL.createItemStack());
        // result.add(Mytems.MAGNIFYING_GLASS.createItemStack());
        // result.add(Mytems.FERTILIZER.createItemStack(64));
        // result.add(Mytems.SNOW_SHOVEL.createItemStack());
        // result.add(Mytems.SNEAKERS.createItemStack());
        result.add(Mytems.UNICORN_HORN.createItemStack());
        result.add(Mytems.SEALED_CAVEBOY.createItemStack());
        result.add(Mytems.SCISSORS.createItemStack());
        // result.add(Mytems.COLORFALL_HOURGLASS.createItemStack());
        result.add(Mytems.STRUCTURE_FINDER.createItemStack());
        result.add(Mytems.DEFLECTOR_SHIELD.createItemStack());
        result.add(Mytems.COPPER_SPLEEF_SHOVEL.createItemStack());
        result.add(Mytems.DIVIDERS.createItemStack());
        result.add(Mytems.YARDSTICK.createItemStack());
        result.add(Mytems.LUMINATOR.createItemStack());
        result.add(Mytems.SCUBA_HELMET.createItemStack());
        result.add(Mytems.MINER_HELMET.createItemStack());
        result.add(Mytems.EMPTY_WATERING_CAN.createItemStack());
        result.add(Mytems.IRON_SCYTHE.createItemStack());
        result.add(Mytems.TREE_CHOPPER.createItemStack());
        result.add(Mytems.HASTY_PICKAXE.createItemStack());
        return result;
    }
}
