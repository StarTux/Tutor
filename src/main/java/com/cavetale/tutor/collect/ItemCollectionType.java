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
import org.bukkit.inventory.ItemStack;
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

    TALL_FLOWERS(Set.of(FLOWERS), "Tall Flowers",
                 "The tall ones are easy to reproduce."
                 + " Just apply some bone meal.",
                 color(0xFFC0CB), DARK_GREEN,
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
           color(0xFFC0CB), DARK_GREEN,
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
}
