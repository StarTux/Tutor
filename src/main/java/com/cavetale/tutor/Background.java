package com.cavetale.tutor;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Background {
    DARK(true),
    LIGHT(false);

    public final TextColor text;
    public final TextColor green;
    public final TextColor red;
    public final TextColor gray;

    Background(final boolean dark) {
        if (dark) {
            text = NamedTextColor.WHITE;
            green = NamedTextColor.GREEN;
            red = NamedTextColor.RED;
            gray = NamedTextColor.GRAY;
        } else {
            text = NamedTextColor.BLACK;
            green = NamedTextColor.DARK_GREEN;
            red = NamedTextColor.DARK_RED;
            gray = NamedTextColor.DARK_GRAY;
        }
    }
}
