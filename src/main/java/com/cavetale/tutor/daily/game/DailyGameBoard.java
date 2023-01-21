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
    LUDO(GuiOverlay.DAILY_GAME_LUDO,
         List.of(19, 20, 11, 10, 1, 2, 3,
                 12, 21, 22, 13, 4, 5, 14,
                 23, 24, 15, 6, 7, 16, 25,
                 34, 33, 32, 41, 42, 43, 52,
                 51, 50, 49, 40, 31, 30, 29,
                 38, 39, 48, 47, 46, 37, 28)),
    SPIRAL(GuiOverlay.DAILY_GAME_SPIRAL,
           List.of(46, 47, 48, 49, 50, 51, 52,
                   43, 34, 25, 16, 7,
                   6, 5, 4, 3, 2, 1,
                   10, 19, 28, 37,
                   38, 39, 40, 41, 42,
                   33, 24, 15,
                   14, 13, 12, 11,
                   20, 29,
                   30, 31, 32,
                   23, 22, 21)),
    ;

    public final GuiOverlay overlay;
    public final List<Integer> cells;
}
