package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.VanillaItems;
import com.cavetale.core.money.Money;
import com.cavetale.inventory.mail.ItemMail;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.tutor.TutorPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.bukkit.Sound.*;
import static org.bukkit.SoundCategory.MASTER;

/**
 * Json structure.
 */
@Data
public final class DailyGameGoody {
    protected int index;
    protected Type type;

    public DailyGameGoody() { }

    public DailyGameGoody(final int index, final Type type) {
        this.index = index;
        this.type = type;
    }

    @RequiredArgsConstructor
    public enum Type {
        ROLL(3, Mytems.DICE::createIcon, Mytems.DICE, text("Bonus Dice Roll", GRAY)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
            }
        },
        REPEAT(3, Mytems.TURN_RIGHT::createIcon, Mytems.TURN_RIGHT, text("Move Previous Roll Again", AQUA)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
            }
        },
        REDO(1, () -> new ItemStack(Material.TNT), VanillaItems.TNT, text("Back to the Start", RED)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_GENERIC_EXPLODE, MASTER, 1.0f, 1.0f);
            }
        },
        DIAMOND(2, () -> new ItemStack(Material.DIAMOND), VanillaItems.DIAMOND, text("Free Diamonds", GREEN)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                ItemStack item = new ItemStack(Material.DIAMOND, 1 + ThreadLocalRandom.current().nextInt(64));
                ItemMail.send(player.getUniqueId(), List.of(item), text("Daily Game Bonus"));
            }
        },
        EMERALD(2, () -> new ItemStack(Material.EMERALD), VanillaItems.EMERALD, text("Free Emeralds", GREEN)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                ItemStack item = new ItemStack(Material.EMERALD, 1 + ThreadLocalRandom.current().nextInt(64));
                ItemMail.send(player.getUniqueId(), List.of(item), text("Daily Game Bonus"));
            }
        },
        RUBY(1, Mytems.RUBY::createIcon, Mytems.RUBY, text("Free Rubies", GREEN)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                ItemStack item = Mytems.RUBY.createItemStack(1 + ThreadLocalRandom.current().nextInt(10));
                ItemMail.send(player.getUniqueId(), List.of(item), text("Daily Game Bonus"));
            }
        },
        KITTY_COIN(1, Mytems.KITTY_COIN::createIcon, Mytems.KITTY_COIN, text("Free Kitty Coin", GREEN)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                ItemStack item = Mytems.KITTY_COIN.createItemStack();
                ItemMail.send(player.getUniqueId(), List.of(item), text("Daily Game Bonus"));
            }
        },
        CHEST(0, Mytems.BOSS_CHEST::createIcon, Mytems.QUESTION_MARK, text("Secret Chest", BLUE)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), BLOCK_CHEST_OPEN, MASTER, 1.0f, 1.0f);
            }
        },
        SILVER_COIN(1, Mytems.SILVER_COIN::createIcon, Mytems.SILVER_COIN, text("Bonus Coins", GOLD)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                int amount = 100 * (1 + ThreadLocalRandom.current().nextInt(10));
                Money.get().give(player.getUniqueId(), (double) amount, plugin(), "Daily Game Bonus");
            }
        },
        GOLD_COIN(1, Mytems.GOLDEN_COIN::createIcon, Mytems.GOLDEN_COIN, text("Bonus Coins", GOLD)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                int amount = 1000 * (1 + ThreadLocalRandom.current().nextInt(10));
                Money.get().give(player.getUniqueId(), (double) amount, plugin(), "Daily Game Bonus");
            }
        },
        DIAMOND_COIN(1, Mytems.DIAMOND_COIN::createIcon, Mytems.DIAMOND_COIN, text("Bonus Coins", GOLD)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, MASTER, 0.5f, 2.0f);
                int amount = 10_000 * (1 + ThreadLocalRandom.current().nextInt(10));
                Money.get().give(player.getUniqueId(), (double) amount, plugin(), "Daily Game Bonus");
            }
        },
        WARP(0, () -> new ItemStack(Material.ENDER_PEARL), VanillaItems.ENDER_PEARL, text("Warp Zone", LIGHT_PURPLE)) {
            @Override public void deliver(Player player) {
                player.playSound(player.getLocation(), ENTITY_ENDERMAN_TELEPORT, MASTER, 1.0f, 1.0f);
            }
        },
        ;

        public final int chances; // on board
        public final Supplier<ItemStack> iconSupplier;
        public final ComponentLike chatIcon;
        public final Component description;

        public ItemStack createIcon() {
            ItemStack result = iconSupplier.get();
            result.editMeta(meta -> {
                    Items.text(meta, List.of(description));
                    meta.addItemFlags(ItemFlag.values());
                });
            return result;
        }

        protected abstract void deliver(Player player);
    }
}
