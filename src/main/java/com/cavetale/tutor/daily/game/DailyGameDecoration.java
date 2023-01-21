package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.GuiOverlay;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.TextColor;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public enum DailyGameDecoration {
    WINTER(GuiOverlay.DECOR_WINTER, List.of(AQUA, DARK_AQUA, WHITE)),
    SUMMER(GuiOverlay.DECOR_SUMMER, List.of(YELLOW, GOLD)),
    TETRIS(GuiOverlay.DECOR_TETRIS, List.of(RED, BLUE, LIGHT_PURPLE, GREEN, YELLOW)),
    ;

    public final GuiOverlay overlay;
    public final List<TextColor> backgroundColors;
}
