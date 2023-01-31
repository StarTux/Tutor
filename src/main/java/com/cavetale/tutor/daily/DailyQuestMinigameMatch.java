package com.cavetale.tutor.daily;

import com.cavetale.core.event.minigame.MinigameMatchCompleteEvent;
import com.cavetale.core.event.minigame.MinigameMatchType;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestMinigameMatch extends DailyQuest<DailyQuestMinigameMatch.Details, DailyQuest.Progress> {
    public DailyQuestMinigameMatch() {
        super(DailyQuestType.MINIGAME_MATCH,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @RequiredArgsConstructor
    public enum Game {
        COLORFALL(2, "Colorfall", "Colorfall", MinigameMatchType.COLORFALL, VanillaItems.PINK_DYE, () -> new ItemStack(Material.PINK_DYE)),
        EXTREME_GRASS_GROWING(2, "EGG", "Extreme Grass Growing", MinigameMatchType.EXTREME_GRASS_GROWING,
                              VanillaItems.GRASS, () -> new ItemStack(Material.GRASS)),
        HIDE_AND_SEEK(2, "Hide&Seek", "Hide and Seek", MinigameMatchType.HIDE_AND_SEEK, Mytems.MAGNIFYING_GLASS, Mytems.MAGNIFYING_GLASS::createIcon),
        PVP_ARENA(2, "PvP Arena", "PvP Arena", MinigameMatchType.PVP_ARENA, Mytems.SCARLET_SWORD, Mytems.SCARLET_SWORD::createIcon),
        TETRIS(2, "Tetris", "Tetris", MinigameMatchType.TETRIS, Mytems.TETRIS_T, Mytems.TETRIS_T::createIcon),
        VERTIGO(2, "Vertigo", "Vertigo", MinigameMatchType.VERTIGO, VanillaItems.WATER_BUCKET, () -> new ItemStack(Material.WATER_BUCKET)),
        ;

        public final int minPlayers;
        public final String shortName;
        public final String displayName;
        public final MinigameMatchType type;
        public final ComponentLike chatIcon;
        public final Supplier<ItemStack> iconSupplier;
    }

    @Override
    public void onGenerate() {
        Game[] games = Game.values();
        details.game = games[random.nextInt(games.length)];
        this.total = 1;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return text("Play " + details.game.shortName);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Play a game of "),
                              details.game.chatIcon, text(details.game.displayName),
                              text(" with at least " + details.game.minPlayers + " fellow players."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = details.game.iconSupplier.get();
        result.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return result;
    }

    protected void onMinigameMatchComplete(Player player, PlayerDailyQuest playerDailyQuest, MinigameMatchCompleteEvent event) {
        if (event.getType() != details.game.type) return;
        if (event.getPlayerUuids().size() < details.game.minPlayers) return;
        makeProgress(playerDailyQuest, 1);
    }


    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(Mytems.KITTY_COIN.createItemStack(),
                       Mytems.RUBY.createItemStack(4),
                       Mytems.GOLDEN_COIN.createItemStack(3));
    }

    public static final class Details extends DailyQuest.Details {
        Game game = Game.COLORFALL;
    }
}
