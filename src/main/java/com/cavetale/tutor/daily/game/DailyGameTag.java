package com.cavetale.tutor.daily.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import static net.kyori.adventure.text.format.TextColor.color;

/**
 * Saved per player.
 */
@Data
public final class DailyGameTag {
    protected List<Integer> boardBag = new ArrayList<>();
    protected DailyGameBoard board; // enum
    protected int background; // hex
    protected int progress;
    protected List<Integer> rolls = new ArrayList<>();
    protected int roll = 0;
    protected List<DailyGameGoody> goodies = new ArrayList<>();
    protected DailyGameGoody goodyWaiting;

    public TextColor getBackgroundColor() {
        return TextColor.color(background);
    }

    public DailyGameTag randomize() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        DailyGameBoard[] boards = DailyGameBoard.values();
        boardBag.removeIf(i -> i >= boards.length);
        if (boardBag.isEmpty()) {
            for (DailyGameBoard it : boards) boardBag.add(it.ordinal());
            Collections.shuffle(boardBag, random);
        }
        this.board = boards[boardBag.remove(random.nextInt(boardBag.size()))];
        List<TextColor> colors = List.of(NamedTextColor.GOLD,
                                         NamedTextColor.BLUE,
                                         NamedTextColor.GREEN,
                                         NamedTextColor.AQUA,
                                         NamedTextColor.RED,
                                         NamedTextColor.LIGHT_PURPLE,
                                         NamedTextColor.YELLOW,
                                         color(0xFF8080), // red
                                         color(0xFFFF00), // yellow
                                         color(0x8080FF), // blue
                                         color(0x80FF80), // green
                                         color(0xFF00FF), // purple
                                         color(0xFF8000), // orange
                                         color(0x00FFFF), // cyan
                                         NamedTextColor.WHITE);
        this.background = colors.get(random.nextInt(colors.size())).value();
        this.rolls.clear();
        this.roll = 0;
        this.progress = 0;
        this.goodies.clear();
        List<DailyGameGoody.Type> goodyTypes = new ArrayList<>();
        for (DailyGameGoody.Type type : DailyGameGoody.Type.values()) {
            for (int i = 0; i < type.chances; i += 1) {
                goodyTypes.add(type);
            }
        }
        Collections.shuffle(goodyTypes, random);
        final int goodyStep = 4;
        for (int offset = 1; offset < board.cells.size() - 1; offset += goodyStep) {
            if (goodyTypes.isEmpty()) break;
            int min = offset;
            int max = Math.min(board.cells.size() - 2, min + goodyStep - 1);
            int goodyIndex = min + random.nextInt(max - min + 1);
            DailyGameGoody.Type type = goodyTypes.remove(random.nextInt(goodyTypes.size()));
            goodies.add(new DailyGameGoody(goodyIndex, type));
        }
        List<Integer> warps = new ArrayList<>();
        for (int i = 1; i < board.cells.size() - 1; i += 1) {
            if (getGoodyAt(i) == null) warps.add(i);
        }
        if (warps.size() >= 2) {
            Collections.shuffle(warps, random);
            goodies.add(new DailyGameGoody(warps.get(0), DailyGameGoody.Type.WARP));
            goodies.add(new DailyGameGoody(warps.get(1), DailyGameGoody.Type.WARP));
        }
        return this;
    }

    public void debug(Random random) {
        for (int i = 1; i < board.cells.size() - 1; i += 1) {
            DailyGameGoody.Type type = random.nextBoolean()
                ? DailyGameGoody.Type.WARP
                : DailyGameGoody.Type.REDO;
            if (getGoodyAt(i) == null) goodies.add(new DailyGameGoody(i, type));
        }
    }

    public DailyGameGoody getGoodyAt(int index) {
        for (var it : goodies) {
            if (it.index == index) return it;
        }
        return null;
    }
}
