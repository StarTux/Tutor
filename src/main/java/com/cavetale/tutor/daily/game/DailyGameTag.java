package com.cavetale.tutor.daily.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import net.kyori.adventure.text.format.TextColor;

/**
 * Saved per player.
 */
@Data
public final class DailyGameTag {
    protected List<Integer> boardBag = new ArrayList<>();
    protected List<Integer> decorBag = new ArrayList<>();
    protected DailyGameBoard board; // enum
    protected DailyGameDecoration decoration; // enum
    protected int background; // hex
    protected int progress;
    protected int roll;
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
        DailyGameDecoration[] decorations = DailyGameDecoration.values();
        decorBag.removeIf(i -> i >= decorations.length);
        if (decorBag.isEmpty()) {
            for (DailyGameDecoration it : decorations) decorBag.add(it.ordinal());
            Collections.shuffle(decorBag, random);
        }
        this.decoration = decorations[decorBag.remove(random.nextInt(decorBag.size()))];
        List<TextColor> colors = decoration.backgroundColors;
        this.background = colors.get(random.nextInt(colors.size())).value();
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
