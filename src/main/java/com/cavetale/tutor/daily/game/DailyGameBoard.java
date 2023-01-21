package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.GuiOverlay;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DailyGameBoard {
    RED(GuiOverlay.DAILY_GAME_RED,
        List.of(1, 2, 3, 4, 5, 6, 7,
                16, 15, 14, 13, 12, 11, 10,
                19, 20, 21, 22, 23, 24, 25,
                34, 33, 32, 31, 30, 29, 28,
                37, 38, 39, 40, 41, 42, 43,
                52, 51, 50, 49, 48, 47, 46)),
    ;

    public final GuiOverlay overlay;
    public final List<Integer> cells;
}
