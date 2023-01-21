package com.cavetale.tutor.daily.game;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.perm.Perm;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.util.Gui;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;
import static com.cavetale.tutor.TutorPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * The game interacts with Session.
 * - Session.playerRow
 * - Session.dailyGameLocked
 * - Session::saveDiceRollAsync
 */
@RequiredArgsConstructor
public final class DailyGame {
    protected final Player player;
    protected final DailyGameTag tag;
    protected Session session;
    private Gui gui;
    private ItemStack skull;
    private boolean closed = false;
    private BukkitTask task;
    private final int diceIndex = 0;
    private int placedIndex = -1;
    private State state;
    private TextColor bg;

    public void start() {
        final int size = 6 * 9;
        this.skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(m -> {
                SkullMeta meta = (SkullMeta) m;
                meta.setPlayerProfile(player.getPlayerProfile());
                Items.text(meta, List.of(player.displayName(),
                                         text("You are here", GRAY)));
            });
        bg = tag.getBackgroundColor();
        this.gui = new Gui()
            .size(size)
            .title(GuiOverlay.BLANK.builder(size, bg)
                   .layer(tag.decoration.overlay, WHITE)
                   .layer(tag.board.overlay, WHITE)
                   .title(textOfChildren(DefaultFont.CAVETALE, text(" Daily Game")))
                   .highlightSlot(diceIndex, bg)
                   .build());
        session = plugin().getSessions().find(player);
        if (session == null) {
            plugin().getLogger().severe("[DailyGame] Session not found: " + player.getName());
            return;
        }
        gui.onClose(evt -> closed = true);
        gui.open(player);
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                session.openMenu(player);
            });
        this.task = Bukkit.getScheduler().runTaskTimer(plugin(), this::tick, 1L, 1L);
    }

    public void selectState() {
        if (tag.roll == 0) {
            idle.setup();
        } else {
            moveSkull.setup();
        }
    }

    private void tick() {
        if (state == null || closed || !player.isValid() || session.isDisabled()) {
            task.cancel();
            return;
        }
        try {
            state.tick();
        } catch (Exception e) {
            e.printStackTrace();
            task.cancel();
        }
    }

    private void placeGoodies() {
        for (DailyGameGoody goody : tag.goodies) {
            gui.setItem(tag.board.cells.get(goody.index), goody.type.createIcon());
        }
    }

    private void placeSkull(int index) {
        if (index == placedIndex) return;
        removeSkull();
        gui.setItem(tag.board.cells.get(index), skull);
        this.placedIndex = index;
    }

    private void removeSkull() {
        if (placedIndex < 0) return;
        gui.setItem(tag.board.cells.get(placedIndex), null);
        DailyGameGoody goody = tag.getGoodyAt(placedIndex);
        if (goody != null) {
            gui.setItem(tag.board.cells.get(placedIndex), goody.type.createIcon());
        }
        placedIndex = -1;
    }

    public Mytems diceIcon(int value) {
        return switch (value) {
        case 1 -> Mytems.DICE_1;
        case 2 -> Mytems.DICE_2;
        case 3 -> Mytems.DICE_3;
        case 4 -> Mytems.DICE_4;
        case 5 -> Mytems.DICE_5;
        case 6 -> Mytems.DICE_6;
        default -> Mytems.QUESTION_MARK;
        };
    }

    /**
     * Current state of the Daily Game.
     */
    public abstract class State {
        protected void enter() { }
        protected void exit() { }
        protected abstract void tick();
        public final void setup() {
            if (state != null) state.exit();
            state = this;
            enter();
        }
    }

    public final State init = new State() {
            @Override protected void tick() { }
        };

    public final State idle = new State() {
            private int ticks;
            @Override protected void enter() {
                ticks = 0;
                removeSkull();
                placeGoodies();
                placeSkull(tag.progress);
                final int playerRolls = session.getPlayerRow().getDailyGameRolls();
                if (playerRolls > 0) {
                    ItemStack diceIcon = Mytems.DICE
                        .createIcon(List.of(text("Roll the Dice", GREEN),
                                            text("You have " + playerRolls + " dice rolls", GRAY),
                                            text("Get more dice rolls by", GRAY),
                                            text("completing daily quests.", GRAY),
                                            textOfChildren(Mytems.MOUSE_LEFT, text(" Roll", GRAY))));
                    diceIcon.setAmount(Math.max(1, Math.min(64, playerRolls)));
                    gui.setItem(diceIndex, diceIcon, click -> {
                            if (!click.isLeftClick()) return;
                            if (session.isDailyGameLocked()) return;
                            diceRoll.setup();
                        });
                } else {
                    gui.setItem(diceIndex, null);
                }
            }
            @Override protected void tick() {
                int t = ticks % 20;
                if (t == 9) {
                    removeSkull();
                } else if (t == 19) {
                    placeSkull(tag.progress);
                }
                ticks += 1;
            }
            @Override protected void exit() {
                placeSkull(tag.progress);
                gui.setItem(diceIndex, null);
            }
        };

    /**
     * Show a dice rolling animation.
     */
    public final State diceRoll = new State() {
            private int ticks;
            private boolean paused;
            @Override protected void enter() {
                ticks = 0;
                gui.setItem(diceIndex, Mytems.DICE_ROLL.createIcon());
            }
            @Override protected void tick() {
                if (ticks < 60 && (ticks % 2) == 0) {
                    float pitch = 2.0f - ((float) ticks / 60f);
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 0.5f, pitch);
                } else if (ticks == 60) {
                    tag.roll = 1 + ThreadLocalRandom.current().nextInt(6);
                    plugin().getLogger().info("[DailyGame] " + player.getName() + " rolled " + tag.roll);
                    paused = true;
                    session.saveDailyGameAsync(session.getPlayerRow().getDailyGameRolls() - 1, tag, () -> {
                            paused = false;
                            gui.setItem(diceIndex, diceIcon(tag.roll).createIcon(List.of(text(tag.roll, WHITE))));
                            player.sendMessage(textOfChildren(text("Daily Game ", GRAY), diceIcon(tag.roll), text(tag.roll)));
                        });
                } else if (ticks >= 70) {
                    if (paused) return;
                    moveSkull.setup();
                }
                ticks += 1;
            }
            @Override protected void exit() { }
        };

    /**
     * Move the skull across the board.
     */
    public final State moveSkull = new State() {
            private boolean finished;
            private int moveTicks;
            private int current;
            private int to;
            @Override protected void enter() {
                finished = false;
                moveTicks = 20;
                removeSkull();
                placeGoodies();
                placeSkull(tag.progress);
                gui.setItem(diceIndex, diceIcon(tag.roll).createIcon(List.of(text(tag.roll, WHITE))));
                current = tag.progress;
                to = Math.min(tag.board.cells.size() - 1, tag.progress + tag.roll);
            }
            @Override protected void tick() {
                if (finished) return;
                if (session.isDailyGameLocked()) return;
                if (moveTicks > 0) {
                    moveTicks -= 1;
                    return;
                }
                if (current < to) {
                    moveTicks = 10;
                    current += 1;
                    placeSkull(current);
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, SoundCategory.MASTER, 1.0f, 1.5f);
                } else {
                    finished = true;
                    DailyGameGoody goody = tag.getGoodyAt(to);
                    int newRolls = session.getPlayerRow().getDailyGameRolls();
                    boolean boardComplete = to >= tag.board.cells.size() - 1;
                    if (goody != null) {
                        tag.goodies.remove(goody);
                        switch (goody.type) {
                        case REDO: to = 0; break;
                        case ROLL: newRolls += 1; break;
                        case WARP: {
                            for (DailyGameGoody it : List.copyOf(tag.goodies)) {
                                if (it.type == goody.type) {
                                    to = it.index;
                                    tag.goodies.remove(it);
                                    break;
                                }
                            }
                            break;
                        }
                        default: break;
                        }
                    }
                    if (boardComplete) {
                        tag.randomize();
                        newRolls += 1;
                    } else {
                        tag.progress = to;
                        tag.roll = 0;
                    }
                    session.saveDailyGameAsync(newRolls, tag, () -> {
                            if (goody != null) {
                                goody.type.deliver(player);
                                player.sendMessage(textOfChildren(text("Daily Game ", GRAY), goody.type.chatIcon, goody.type.description));
                                plugin().getLogger().info("[DailyGame] " + player.getName() + " landed on " + goody.type);
                            }
                            if (boardComplete) {
                                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.5f, 2.0f);
                                player.closeInventory();
                                player.sendMessage(text("Daily Game Complete", GREEN));
                                plugin().getLogger().info("[DailyGame] " + player.getName() + " completed daily game");
                                DailyGame newGame = new DailyGame(player, tag);
                                newGame.start();
                                newGame.selectState();
                                session.addDailyGamesCompletedAsync(1);
                                Perm.get().addLevelProgress(player.getUniqueId());
                            } else {
                                idle.setup();
                            }
                        });
                }
            }
            @Override protected void exit() {
            }
        };

    public final State test = new State() {
            private int moveTicks;
            @Override protected void enter() {
                moveTicks = 20;
                placeSkull(tag.progress);
                gui.setItem(diceIndex, Mytems.DICE_ROLL.createIcon());
            }
            @Override protected void tick() {
                if (moveTicks > 0) {
                    moveTicks -= 1;
                } else {
                    moveTicks = 10;
                    tag.progress += 1;
                    if (tag.progress >= tag.board.cells.size()) tag.progress = 0;
                    placeSkull(tag.progress);
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, SoundCategory.MASTER, 1.0f, 1.5f);
                }
            }
        };
}
