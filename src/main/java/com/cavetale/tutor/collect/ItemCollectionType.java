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
            return List.of(enchantedBook(Enchantment.SILK_TOUCH, 1),
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

    MONUMENT(Set.of(CORAL), "Ocean Monument",
             "Ocean Monuments can be hard to find because they are under water."
             + " Beware of Guardians!",
             color(0x4A6C85), color(0x43447B),
             () -> new ItemStack(Material.PRISMARINE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.PRISMARINE, 64),
                           new CollectMaterial(Material.PRISMARINE_BRICKS, 64),
                           new CollectMaterial(Material.DARK_PRISMARINE, 64),
                           new CollectMaterial(Material.SEA_LANTERN, 16),
                           new CollectMaterial(Material.WET_SPONGE, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(new ItemStack(Material.SPONGE, 64),
                           new ItemStack(Material.SEA_LANTERN, 16),
                           new ItemStack(Material.SEA_LANTERN, 16));
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

    WOOL(Set.of(FARM_ANIMALS), "Wool",
         "There are 16 colors of wool."
         + " You can dye sheep and breed them"
         + " and they will pass their color on to their babies.",
         color(0xF38BAA), color(0x3AB3DA),
         () -> new ItemStack(Material.PINK_WOOL)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.BLACK_WOOL, 64),
                           new CollectMaterial(Material.RED_WOOL, 64),
                           new CollectMaterial(Material.GREEN_WOOL, 64),
                           new CollectMaterial(Material.BROWN_WOOL, 64),
                           new CollectMaterial(Material.BLUE_WOOL, 64),
                           new CollectMaterial(Material.PURPLE_WOOL, 64),
                           new CollectMaterial(Material.CYAN_WOOL, 64),
                           new CollectMaterial(Material.LIGHT_GRAY_WOOL, 64),
                           new CollectMaterial(Material.GRAY_WOOL, 64),
                           new CollectMaterial(Material.PINK_WOOL, 64),
                           new CollectMaterial(Material.LIME_WOOL, 64),
                           new CollectMaterial(Material.YELLOW_WOOL, 64),
                           new CollectMaterial(Material.LIGHT_BLUE_WOOL, 64),
                           new CollectMaterial(Material.MAGENTA_WOOL, 64),
                           new CollectMaterial(Material.ORANGE_WOOL, 64),
                           new CollectMaterial(Material.WHITE_WOOL, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.SCISSORS.createItemStack(),
                           new ItemStack(Material.WHEAT, 64),
                           new ItemStack(Material.WHEAT, 64));
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

    DYE(Set.of(TALL_FLOWERS), "Dye",
        "Flowers can be turned into dyes."
        + " There are 16 different colors.",
        color(0xC74EBD), color(0x169C9C),
        () -> new ItemStack(Material.MAGENTA_DYE)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.BLACK_DYE, 64),
                           new CollectMaterial(Material.RED_DYE, 64),
                           new CollectMaterial(Material.GREEN_DYE, 64),
                           new CollectMaterial(Material.BROWN_DYE, 64),
                           new CollectMaterial(Material.BLUE_DYE, 64),
                           new CollectMaterial(Material.PURPLE_DYE, 64),
                           new CollectMaterial(Material.CYAN_DYE, 64),
                           new CollectMaterial(Material.LIGHT_GRAY_DYE, 64),
                           new CollectMaterial(Material.GRAY_DYE, 64),
                           new CollectMaterial(Material.PINK_DYE, 64),
                           new CollectMaterial(Material.LIME_DYE, 64),
                           new CollectMaterial(Material.YELLOW_DYE, 64),
                           new CollectMaterial(Material.LIGHT_BLUE_DYE, 64),
                           new CollectMaterial(Material.MAGENTA_DYE, 64),
                           new CollectMaterial(Material.ORANGE_DYE, 64),
                           new CollectMaterial(Material.WHITE_DYE, 64));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MAGENTA_PAINTBRUSH.createItemStack(),
                           new ItemStack(Material.BONE_MEAL, 64),
                           new ItemStack(Material.BONE_MEAL, 64));
        }
    },

    RAW_METAL(Set.of(MINING), "Raw Metal",
              "Raw metals can be turned into blocks.",
              color(0xAF8E77), color(0xe97a52),
              () -> new ItemStack(Material.RAW_COPPER)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.RAW_COPPER, 64),
                           new CollectMaterial(Material.RAW_IRON, 64),
                           new CollectMaterial(Material.RAW_GOLD, 64),
                           new CollectMaterial(Material.RAW_COPPER_BLOCK, 1),
                           new CollectMaterial(Material.RAW_IRON_BLOCK, 1),
                           new CollectMaterial(Material.RAW_GOLD_BLOCK, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(enchantedBook(Enchantment.LOOT_BONUS_BLOCKS, 3),
                           new ItemStack(Material.IRON_INGOT, 64),
                           new ItemStack(Material.GOLD_INGOT, 64));
        }
    },

    METAL_BLOCKS(Set.of(RAW_METAL), "Metal Blocks",
                 "Ingots can be turned into decorative blocks as well.",
                 color(0x11727A), color(0x303030),
                 () -> new ItemStack(Material.IRON_BLOCK)) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMaterial(Material.DIAMOND_BLOCK, 1),
                           new CollectMaterial(Material.EMERALD_BLOCK, 1),
                           new CollectMaterial(Material.GOLD_BLOCK, 1),
                           new CollectMaterial(Material.IRON_BLOCK, 1),
                           new CollectMaterial(Material.COPPER_BLOCK, 1),
                           new CollectMaterial(Material.LAPIS_BLOCK, 1),
                           new CollectMaterial(Material.REDSTONE_BLOCK, 1),
                           new CollectMaterial(Material.COAL_BLOCK, 1),
                           new CollectMaterial(Material.NETHERITE_BLOCK, 1));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.KITTY_COIN.createItemStack(3),
                           new ItemStack(Material.NETHERITE_INGOT),
                           new ItemStack(Material.NETHERITE_INGOT));
        }
    },

    POCKET_FARM_ANIMAL(Set.of(BUCKET_MOBS), "Farm Catcher",
                       "The Animal Catcher can reliably catch non-tameable animals"
                       + " and store them as an item."
                       + " It has a wide area of effect, so you can catch more than one.",
                       color(0xE8A09D), color(0xC05552),
                       Mytems.POCKET_PIG::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_PIG),
                           new CollectMytems(Mytems.POCKET_CHICKEN),
                           new CollectMytems(Mytems.POCKET_COW),
                           new CollectMytems(Mytems.POCKET_GOAT),
                           new CollectMytems(Mytems.POCKET_RABBIT),
                           new CollectMytems(Mytems.POCKET_SHEEP));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.ANIMAL_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_WILD_ANIMAL(Set.of(POCKET_FARM_ANIMAL), "Wildlife Catcher",
                       "The animal kingdom does not end at the garden fence."
                       + " let's explore and catch them in their natural habitat!",
                       color(0xc7c7c7), color(0x008383),
                       Mytems.POCKET_TURTLE::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_TURTLE),
                           new CollectMytems(Mytems.POCKET_AXOLOTL),
                           new CollectMytems(Mytems.POCKET_BAT),
                           new CollectMytems(Mytems.POCKET_FROG),
                           new CollectMytems(Mytems.POCKET_FOX),
                           new CollectMytems(Mytems.POCKET_OCELOT),
                           new CollectMytems(Mytems.POCKET_PANDA),
                           new CollectMytems(Mytems.POCKET_POLAR_BEAR),
                           new CollectMytems(Mytems.POCKET_BEE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.ANIMAL_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_FANTASY_ANIMAL(Set.of(POCKET_WILD_ANIMAL), "Fable Catcher",
                          "Not all Minecraft animals exist in the real world."
                          + " We can catch them anyway.",
                          color(0x00BBDB), color(0x006FA3),
                          Mytems.POCKET_ALLAY::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_ALLAY),
                           new CollectMytems(Mytems.POCKET_STRIDER),
                           new CollectMytems(Mytems.POCKET_MUSHROOM_COW));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.ANIMAL_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_PET(Set.of(POCKET_FARM_ANIMAL), "Pet Catcher",
               "Tamed animals are best caught with the Pet Catcher."
               + " Success is guaranteed, as long as the pet is yours.",
               color(0xE1BC86), color(0x785B42),
               Mytems.POCKET_CAT::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_CAT),
                           new CollectMytems(Mytems.POCKET_WOLF),
                           new CollectMytems(Mytems.POCKET_PARROT));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.PET_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_HORSE(Set.of(POCKET_PET), "Horse Catcher",
                 "Horses are tamed by riding them."
                 + " Then they are easily caught with the Pet Catcher.",
                 color(0xB59576), color(0xD0C800),
                 Mytems.POCKET_HORSE::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_HORSE),
                           new CollectMytems(Mytems.POCKET_DONKEY),
                           new CollectMytems(Mytems.POCKET_MULE),
                           new CollectMytems(Mytems.POCKET_SKELETON_HORSE));
            // new CollectMytems(Mytems.POCKET_ZOMBIE_HORSE)
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.POCKET_ZOMBIE_HORSE.createItemStack(),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_CAMELID(Set.of(POCKET_HORSE), "Camelid Catcher",
                   "Did you know that Llamas and Camels are related?",
                   color(0xDB9A2D), color(0x42577F),
                   Mytems.POCKET_CAMEL::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_LLAMA),
                           new CollectMytems(Mytems.POCKET_TRADER_LLAMA));
            //new CollectMytems(Mytems.POCKET_CAMEL),
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.PET_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_FISH(Set.of(BUCKET_MOBS), "Fish Catcher",
                "Another way to catch fish alive is the Aquatic Catcher.",
                color(0x289CC2), color(0xEDAD00),
                Mytems.POCKET_PUFFERFISH::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_PUFFERFISH),
                           new CollectMytems(Mytems.POCKET_TROPICAL_FISH),
                           new CollectMytems(Mytems.POCKET_COD),
                           new CollectMytems(Mytems.POCKET_SALMON));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.FISH_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_AQUATIC(Set.of(POCKET_FISH), "Aquatic Catcher",
                   "The Aquatic Catcher also works on other underwater creatures,"
                   + " as long as they're not hostile.",
                   color(0xC7C7C7), color(0x173040),
                   Mytems.POCKET_DOLPHIN::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_DOLPHIN),
                           new CollectMytems(Mytems.POCKET_GLOW_SQUID),
                           new CollectMytems(Mytems.POCKET_SQUID),
                           new CollectMytems(Mytems.POCKET_TADPOLE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.FISH_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_VILLAGER(Set.of(POCKET_AQUATIC), "Villager Catcher",
                    "The Villager Catcher is ideal to catch villagers and wandering traders.",
                    color(0xA57A63), color(0x4A3129),
                    Mytems.POCKET_VILLAGER::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_VILLAGER),
                           new CollectMytems(Mytems.POCKET_WANDERING_TRADER));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.VILLAGER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_MONSTER(Set.of(POCKET_FANTASY_ANIMAL), "Monster Catcher",
                   "The Monster Catcher works best on monsters."
                   + " The catch chance is increased if the mob is damaged.",
                   color(0x049903), color(0x000000),
                   Mytems.POCKET_CREEPER::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_CREEPER),
                           new CollectMytems(Mytems.POCKET_SKELETON),
                           new CollectMytems(Mytems.POCKET_SPIDER),
                           new CollectMytems(Mytems.POCKET_ZOMBIE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_UNDEAD(Set.of(POCKET_MONSTER), "Undead Catcher",
                  "Some of the undead Minecraft monsters have odd variations."
                  + " Find them and catch them!",
                  color(0x6B8F61), color(0x85E0C8),
                  Mytems.POCKET_DROWNED::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_DROWNED),
                           new CollectMytems(Mytems.POCKET_HUSK),
                           new CollectMytems(Mytems.POCKET_STRAY),
                           new CollectMytems(Mytems.POCKET_ZOMBIE_VILLAGER));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_SCARY(Set.of(POCKET_UNDEAD), "Scary Catcher",
                 "Some monsters hide in dark and eerie places."
                 + " Fear not, because catching them all is worth it.",
                 color(0x384479), color(0x003743),
                 Mytems.POCKET_CAVE_SPIDER::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_CAVE_SPIDER),
                           new CollectMytems(Mytems.POCKET_GUARDIAN),
                           new CollectMytems(Mytems.POCKET_PHANTOM),
                           new CollectMytems(Mytems.POCKET_SILVERFISH));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_GOLEM(Set.of(POCKET_SCARY), "Golem Catcher",
                 "These can be built but you can also catch them in an egg."
                 + " They will often attack hostile mobs but still count as monsters.",
                 color(0xCDC0B6), color(0x658E2C),
                 Mytems.POCKET_IRON_GOLEM::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_IRON_GOLEM),
                           new CollectMytems(Mytems.POCKET_SNOWMAN));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_SLIME(Set.of(POCKET_GOLEM), "Slime Catcher",
                 "The Nether and the overworld have one thing in common:"
                 + " Slimes. These count as monsters.",
                 color(0x6EA760), color(0x489335),
                 Mytems.POCKET_SLIME::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_SLIME),
                           new CollectMytems(Mytems.POCKET_MAGMA_CUBE));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_NETHER(Set.of(POCKET_MONSTER), "Nether Catcher",
                  "The Nether has some particularly hard to catch monsters."
                  + " Damaging them before you cast your Monster Catcher will prove useful.",
                  color(0x3C4141), color(0x090909),
                  Mytems.POCKET_WITHER_SKELETON::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_WITHER_SKELETON),
                           new CollectMytems(Mytems.POCKET_BLAZE),
                           new CollectMytems(Mytems.POCKET_GHAST));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_ENDER(Set.of(POCKET_NETHER), "Ender Catcher",
                 "Some monsters come from the end dimension."
                 + " When trapped in an egg, it doesn't matter where they're from.",
                 color(0x9E6392), color(0x472D46),
                 Mytems.POCKET_ENDERMAN::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_ENDERMAN),
                           new CollectMytems(Mytems.POCKET_ENDERMITE),
                           new CollectMytems(Mytems.POCKET_SHULKER));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_PIGLIN(Set.of(POCKET_NETHER), "Piglin Catcher",
                  "There are a lot of pig monsters in the Nether."
                  + " Some will require bringing into another dimension to transform.",
                color(0xDCD992), color(0xD68787),
                  Mytems.POCKET_PIGLIN::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_PIGLIN),
                           new CollectMytems(Mytems.POCKET_PIGLIN_BRUTE),
                           new CollectMytems(Mytems.POCKET_ZOMBIFIED_PIGLIN),
                           new CollectMytems(Mytems.POCKET_HOGLIN),
                           new CollectMytems(Mytems.POCKET_ZOGLIN));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_PILLAGER(Set.of(POCKET_PIGLIN), "Pillager Catcher",
                    "They appear in raids, witch huts, or woodland mansions."
                    + " All look somewhat similar.",
                    color(0x838888), color(0x1C5153),
                    Mytems.POCKET_PILLAGER::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_EVOKER),
                           new CollectMytems(Mytems.POCKET_PILLAGER),
                           new CollectMytems(Mytems.POCKET_RAVAGER),
                           new CollectMytems(Mytems.POCKET_VEX),
                           new CollectMytems(Mytems.POCKET_VINDICATOR),
                           new CollectMytems(Mytems.POCKET_WITCH));
            //new CollectMytems(Mytems.POCKET_ILLUSIONER),
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
        }
    },

    POCKET_BOSS(Set.of(POCKET_PILLAGER), "Boss Catcher",
                "Minecraft has no final boss but there are some boss monsters."
                + " Finding them will be tricky, let alone catching them."
                + " They cannot even be released from their egg.",
                color(0xC369DA), color(0x191919),
                Mytems.POCKET_ENDER_DRAGON::createItemStack) {
        @Override public List<CollectItem> getItems() {
            return List.of(new CollectMytems(Mytems.POCKET_ENDER_DRAGON),
                           new CollectMytems(Mytems.POCKET_WITHER),
                           new CollectMytems(Mytems.POCKET_ELDER_GUARDIAN),
                           new CollectMytems(Mytems.POCKET_WARDEN));
        }
        @Override public List<ItemStack> getRewards() {
            return List.of(Mytems.MONSTER_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16),
                           Mytems.MOB_CATCHER.createItemStack(16));
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
