package com.cavetale.tutor.collect;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.mytems.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;

@Getter
public enum ItemCollectionType {
    BEGINNER(Set.of(), "Punching Trees",
             "We all start off small. Punch some trees and grass.",
             GREEN, GRAY,
             () -> new ItemStack(Material.WHEAT_SEEDS)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.OAK_LOG, 1),
                           new CollectMaterial(Material.WHEAT_SEEDS, 1),
                           new CollectMaterial(Material.APPLE, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.IRON_PICKAXE),
                           new ItemStack(Material.FURNACE),
                           new ItemStack(Material.TORCH, 16));
        }
    },

    MINING(Set.of(BEGINNER), "Mining 101",
           "Bring some basic items from the caves of Cavetale."
           + " You will have most luck in the mining world.",
           color(0x11727A), color(0x303030),
           () -> new ItemStack(Material.DIAMOND)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.DIAMOND, 1),
                           new CollectMaterial(Material.EMERALD, 1),
                           new CollectMaterial(Material.GOLD_INGOT, 1),
                           new CollectMaterial(Material.IRON_INGOT, 1),
                           new CollectMaterial(Material.COPPER_INGOT, 1),
                           new CollectMaterial(Material.LAPIS_LAZULI, 1),
                           new CollectMaterial(Material.REDSTONE, 1),
                           new CollectMaterial(Material.COAL, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.ENCHANTING_TABLE),
                           Mytems.RUBY.createItemStack(),
                           Mytems.RUBY.createItemStack());
        }
    },

    STONES(Set.of(BEGINNER), "Stones",
           "Granite, Diorite, Andesite, Amirite? Found under ground, so get digging!",
           DARK_GRAY, GRAY,
           () -> new ItemStack(Material.ANDESITE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.STONE, 64),
                           new CollectMaterial(Material.GRANITE, 64),
                           new CollectMaterial(Material.DIORITE, 64),
                           new CollectMaterial(Material.ANDESITE, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.CHISELED_STONE_BRICKS, 64),
                           new ItemStack(Material.STONE_BRICKS, 64),
                           new ItemStack(Material.STONE_BRICKS, 64));
        }
    },

    ORES(Set.of(STONES), "Ores",
         "With the Silk Touch enchantment, you can pick up raw ores. Let's try it out!",
         WHITE, GRAY,
         () -> new ItemStack(Material.DIAMOND_ORE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.DIAMOND_ORE),
                           new CollectMaterial(Material.EMERALD_ORE),
                           new CollectMaterial(Material.GOLD_ORE),
                           new CollectMaterial(Material.IRON_ORE),
                           new CollectMaterial(Material.COPPER_ORE),
                           new CollectMaterial(Material.LAPIS_ORE),
                           new CollectMaterial(Material.REDSTONE_ORE),
                           new CollectMaterial(Material.COAL_ORE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.BLAST_FURNACE, 8),
                           Mytems.RUBY.createItemStack(),
                           Mytems.RUBY.createItemStack());
        }
    },

    DEEPSLATE_ORES(Set.of(ORES), "Deepslate Ores",
                   "Deeper underground, deepslate ores are found.",
                   color(0x25A7AB), color(0x404040),
                   () -> new ItemStack(Material.DEEPSLATE_DIAMOND_ORE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.DEEPSLATE_DIAMOND_ORE),
                           new CollectMaterial(Material.DEEPSLATE_EMERALD_ORE),
                           new CollectMaterial(Material.DEEPSLATE_GOLD_ORE),
                           new CollectMaterial(Material.DEEPSLATE_IRON_ORE),
                           new CollectMaterial(Material.DEEPSLATE_COPPER_ORE),
                           new CollectMaterial(Material.DEEPSLATE_LAPIS_ORE),
                           new CollectMaterial(Material.DEEPSLATE_REDSTONE_ORE),
                           new CollectMaterial(Material.DEEPSLATE_COAL_ORE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.BLAST_FURNACE, 8),
                           new ItemStack(Material.COAL, 64),
                           new ItemStack(Material.COAL, 64));
        }
    },

    NETHER_ORES(Set.of(ORES), "Nether Ores",
                "Yet even deeper, we find the Nether.",
                color(0xC6AC3E), color(0x5B2828),
                () -> new ItemStack(Material.NETHER_GOLD_ORE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.NETHER_QUARTZ_ORE),
                           new CollectMaterial(Material.ANCIENT_DEBRIS),
                           new CollectMaterial(Material.NETHER_GOLD_ORE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.NETHERITE_INGOT),
                           new ItemStack(Material.NETHERITE_SCRAP),
                           new ItemStack(Material.NETHERITE_SCRAP));
        }
    },

    MONSTER_DROPS(Set.of(MINING), "Monster Drops",
                  "Slay monsters to get their precious loot."
                  + " They like to spawn in dark spots, usually at night.",
                  color(0xFF0000), GRAY,
                  () -> new ItemStack(Material.GUNPOWDER)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.GUNPOWDER),
                           new CollectMaterial(Material.BONE),
                           new CollectMaterial(Material.SPIDER_EYE),
                           new CollectMaterial(Material.ARROW),
                           new CollectMaterial(Material.ROTTEN_FLESH),
                           new CollectMaterial(Material.ENDER_PEARL),
                           new CollectMaterial(Material.SLIME_BALL));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.DIAMOND_SWORD),
                           new ItemStack(Material.BOW),
                           new ItemStack(Material.SHIELD));
        }
    },

    DUNGEON_LOOT(Set.of(MONSTER_DROPS), "Dungeon Loot",
                 "Cavetale has custom dungeons, but vanilla dungeons still exist."
                 + " See if you can loot some of them for their cool blocks and items."
                 + " Try not to cheat. ;)",
                 color(0xA00000), DARK_GRAY,
                 () -> new ItemStack(Material.SPAWNER)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.MOSSY_COBBLESTONE, 16),
                           new CollectMaterial(Material.MUSIC_DISC_13),
                           new CollectMaterial(Material.MUSIC_DISC_CAT),
                           new CollectMaterial(Material.SADDLE),
                           new CollectMaterial(Material.NAME_TAG),
                           new CollectMaterial(Material.GOLDEN_APPLE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.NAME_TAG, 16),
                           new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
                           new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        }
    },

    MINESHAFT(Set.of(MONSTER_DROPS), "Mineshaft",
              "Mineshafts are everywhere."
              + " See if you can find one and pick it for resources."
              + " Going underground in the mining world is recommended.",
              GRAY, color(0x900000),
              () -> new ItemStack(Material.RAIL)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.COBWEB, 64),
                           new CollectMaterial(Material.CHAIN, 16),
                           new CollectMaterial(Material.RAIL, 64),
                           new CollectMaterial(Material.CHEST_MINECART, 1),
                           new CollectMaterial(Material.POWERED_RAIL, 1),
                           new CollectMaterial(Material.ACTIVATOR_RAIL, 1),
                           new CollectMaterial(Material.DETECTOR_RAIL, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.TNT, 64),
                           new ItemStack(Material.FLINT_AND_STEEL),
                           new ItemStack(Material.FLINT_AND_STEEL));
        }
    },

    TOPSOIL(Set.of(BEGINNER), "Topsoil",
            "Grab your shovel and dig up the top layers of your world.",
            color(0x79553A), color(0x808080),
            () -> new ItemStack(Material.DIRT)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.DIRT, 64),
                           new CollectMaterial(Material.GRAVEL, 64),
                           new CollectMaterial(Material.SAND, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.GRASS_BLOCK, 64),
                           new ItemStack(Material.WHEAT_SEEDS, 16),
                           new ItemStack(Material.WHEAT_SEEDS, 16));
        }
    },

    CROPS(Set.of(TOPSOIL), "Crops",
          "Grab a hoe, till some soil, and start sowing seeds."
          + " Make sure to water your fields.",
          color(0xDCBB65), DARK_GREEN,
          () -> new ItemStack(Material.WHEAT)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.WHEAT, 64),
                           new CollectMaterial(Material.CARROT, 64),
                           new CollectMaterial(Material.POTATO, 64),
                           new CollectMaterial(Material.BEETROOT, 64),
                           new CollectMaterial(Material.MELON_SLICE, 64),
                           new CollectMaterial(Material.PUMPKIN, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.WATERING_CAN.createItemStack(),
                           new ItemStack(Material.HAY_BLOCK, 16),
                           new ItemStack(Material.HAY_BLOCK, 16));
        }
    },

    FISHING(Set.of(CROPS), "Fishing",
            "Make a fishing rod and start fishing.",
            color(0x8080FF), color(0x008080),
            () -> new ItemStack(Material.FISHING_ROD)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.COD),
                           new CollectMaterial(Material.SALMON),
                           new CollectMaterial(Material.PUFFERFISH),
                           new CollectMaterial(Material.TROPICAL_FISH));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.FISHING_ROD),
                           enchantedBook(Enchantment.LURE, 3),
                           enchantedBook(Enchantment.LUCK, 3));
        }
    },

    CORAL(Set.of(FISHING), "Coral",
          "Corals appear in warm oceans."
          + " To collect them without harm, Silk Touch will be required.",
          color(0x8080FF), color(0xE8E74A),
          () -> new ItemStack(Material.TUBE_CORAL_BLOCK)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.TUBE_CORAL_BLOCK),
                           new CollectMaterial(Material.BRAIN_CORAL_BLOCK),
                           new CollectMaterial(Material.BUBBLE_CORAL_BLOCK),
                           new CollectMaterial(Material.FIRE_CORAL_BLOCK),
                           new CollectMaterial(Material.HORN_CORAL_BLOCK),
                           new CollectMaterial(Material.TUBE_CORAL),
                           new CollectMaterial(Material.BRAIN_CORAL),
                           new CollectMaterial(Material.BUBBLE_CORAL),
                           new CollectMaterial(Material.FIRE_CORAL),
                           new CollectMaterial(Material.HORN_CORAL));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(enchantedBook(Enchantment.WATER_WORKER, 1),
                           Mytems.RUBY.createItemStack(),
                           Mytems.RUBY.createItemStack());
        }
    },

    BUCKET_MOBS(Set.of(FISHING), "Bucket Mobs",
                "Some waterborne animals can be caught alive in buckets."
                + " Let's try that!",
                color(0x8080FF), GRAY,
                () -> new ItemStack(Material.AXOLOTL_BUCKET)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.COD_BUCKET),
                           new CollectMaterial(Material.SALMON_BUCKET),
                           new CollectMaterial(Material.TROPICAL_FISH_BUCKET),
                           new CollectMaterial(Material.PUFFERFISH_BUCKET),
                           new CollectMaterial(Material.AXOLOTL_BUCKET),
                           new CollectMaterial(Material.TADPOLE_BUCKET));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.BUCKET, 16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    FARM_ANIMALS(Set.of(CROPS), "Farm Animals",
                 "Breeding farm animals is one of the most rewarding farming activities in the game."
                 + " You may have to kill a few of them in the end.",
                 color(0xC4A484), DARK_AQUA,
                 () -> new ItemStack(Material.BEEF)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.BEEF, 64),
                           new CollectMaterial(Material.PORKCHOP, 64),
                           new CollectMaterial(Material.EGG, 64),
                           new CollectMaterial(Material.MUTTON, 64),
                           new CollectMaterial(Material.RABBIT, 64),
                           new CollectMaterial(Material.CHICKEN, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.SMOKER, 8),
                           new ItemStack(Material.CHARCOAL, 64),
                           new ItemStack(Material.CHARCOAL, 64));
        }
    },

    FLOWERS(Set.of(TOPSOIL), "Flowers",
            "Go pick some flowers."
            + " Using a flower forest biome in the mining world is recommended.",
            color(0xFFC0CB), DARK_GREEN,
            () -> new ItemStack(Material.OXEYE_DAISY)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.ALLIUM, 1),
                           new CollectMaterial(Material.AZURE_BLUET, 1),
                           new CollectMaterial(Material.BLUE_ORCHID, 1),
                           new CollectMaterial(Material.CORNFLOWER, 1),
                           new CollectMaterial(Material.DANDELION, 1),
                           new CollectMaterial(Material.LILY_OF_THE_VALLEY, 1),
                           new CollectMaterial(Material.OXEYE_DAISY, 1),
                           new CollectMaterial(Material.POPPY, 1),
                           new CollectMaterial(Material.WITHER_ROSE, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.FERTILIZER.createItemStack(64));
        }
    },

    MUSHROOMS(Set.of(TOPSOIL), "Mushrooms",
              "Collecting mushrooms can be very relaxing."
              + " As long as they don't grow in the nether, that is.",
              color(0xFF00FF), color(0x804000),
              () -> new ItemStack(Material.RED_MUSHROOM)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.RED_MUSHROOM, 64),
                           new CollectMaterial(Material.BROWN_MUSHROOM, 64),
                           new CollectMaterial(Material.CRIMSON_FUNGUS, 64),
                           new CollectMaterial(Material.WARPED_FUNGUS, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.RED_MUSHROOM_BLOCK, 64),
                           new ItemStack(Material.BROWN_MUSHROOM_BLOCK, 64),
                           new ItemStack(Material.MUSHROOM_STEM, 64));
        }
    },

    TALL_FLOWERS(Set.of(FLOWERS), "Tall Flowers",
                 "The tall ones are easy to reproduce."
                 + " Just apply some bone meal.",
                 color(0xFF69B4), DARK_GREEN,
                 () -> new ItemStack(Material.PEONY)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.LILAC, 16),
                           new CollectMaterial(Material.PEONY, 16),
                           new CollectMaterial(Material.ROSE_BUSH, 16),
                           new CollectMaterial(Material.SUNFLOWER, 16));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.BONE_MEAL, 64),
                           new ItemStack(Material.BONE_MEAL, 64),
                           new ItemStack(Material.BONE_MEAL, 64));
        }
    },

    TULIPS(Set.of(FLOWERS), "Tulips",
           "Tulips come in four colors. Can your find them all?",
           color(DARK_GREEN), color(0xFFC0CB),
           () -> new ItemStack(Material.WHITE_TULIP)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.ORANGE_TULIP, 16),
                           new CollectMaterial(Material.PINK_TULIP, 16),
                           new CollectMaterial(Material.RED_TULIP, 16),
                           new CollectMaterial(Material.WHITE_TULIP, 16));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.FERTILIZER.createItemStack(64),
                           Mytems.RUBY.createItemStack(),
                           Mytems.RUBY.createItemStack());
        }
    },
    ;

    protected final String key;
    protected final Set<ItemCollectionType> dependencies;
    protected final String displayName;
    protected final String description;
    protected final TextColor color;
    protected final TextColor background;
    protected final Supplier<ItemStack> iconSupplier;

    ItemCollectionType(final Set<ItemCollectionType> dependencies,
                       final String displayName,
                       final String description,
                       final TextColor color,
                       final TextColor background,
                       final Supplier<ItemStack> iconSupplier) {
        this.key = name().toLowerCase();
        this.dependencies = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.background = background;
        this.iconSupplier = iconSupplier;
    }

    public abstract List<CollectItem> getItems();

    public abstract List<ItemStack> getRewards();

    public ItemStack makeIcon() {
        ItemStack icon = iconSupplier.get();
        icon.editMeta(meta -> {
                List<Component> txt = new ArrayList<>();
                txt.add(text(displayName, color));
                txt.addAll(Text.wrapLore(description, c -> c.color(GRAY)));
                txt.add(empty());
                txt.add(textOfChildren(Mytems.MOUSE_LEFT, text(" View Collection", GRAY)));
                Items.text(meta, txt);
            });
        return icon;
    }

    private static ItemStack enchantedBook(Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        item.editMeta(meta -> ((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, level, true));
        return item;
    }
}
