package com.cavetale.tutor.pet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum PetGender {
    OTHER('?', new ItemStack(Material.GRAY_CONCRETE), NamedTextColor.GRAY),
    FEMALE('\u2640', new ItemStack(Material.PINK_CONCRETE), TextColor.color(0xFFC0CB)),
    MALE('\u2642', new ItemStack(Material.BLUE_CONCRETE), NamedTextColor.BLUE);

    public final char character;
    public final Component component;
    public final ItemStack itemStack;
    public final TextColor textColor;

    PetGender(final char character, final ItemStack itemStack, final TextColor textColor) {
        this.character = character;
        this.component = Component.text("" + character, textColor);
        this.itemStack = itemStack;
        this.textColor = textColor;
    }
}
