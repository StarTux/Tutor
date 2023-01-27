package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.event.block.PlayerBreakBlockEvent;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.perm.Perm;
import com.cavetale.core.util.Json;
import com.cavetale.inventory.mail.ItemMail;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.sql.SQLDailyQuest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import static com.cavetale.core.font.Unicode.subscript;
import static com.cavetale.core.font.Unicode.superscript;
import static com.cavetale.tutor.TutorPlugin.dailyQuests;
import static com.cavetale.tutor.TutorPlugin.database;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Getter @ToString
/**
 * Manage one daily quest.  Ready to create or load.
 * @param D The Details class
 * @param P The Progress class
 */
public abstract class DailyQuest<D extends DailyQuest.Details, P extends DailyQuest.Progress> {
    @NonNull protected final DailyQuestType type;
    @NonNull protected final Class<D> detailsClass;
    @NonNull protected final Supplier<D> detailsCtor;
    @NonNull protected final Class<P> progressClass;
    @NonNull protected final Supplier<P> progressCtor;
    protected SQLDailyQuest row;
    protected int dayId;
    protected int year;
    protected int month;
    protected int day;
    protected int index;
    protected D details;
    protected int total;
    @Setter protected boolean active;
    protected final String permission;

    protected DailyQuest(final DailyQuestType type,
                         final Class<D> detailsClass, final Supplier<D> detailsCtor,
                         final Class<P> progressClass, final Supplier<P> progressCtor) {
        this.type = type;
        this.detailsClass = detailsClass;
        this.detailsCtor = detailsCtor;
        this.progressClass = progressClass;
        this.progressCtor = progressCtor;
        this.permission = "tutor.daily." + (index + 1); // 1..3
    }

    protected final void load(SQLDailyQuest theRow) {
        this.row = theRow;
        this.index = row.getDailyIndex();
        this.dayId = row.getDayId();
        int tmp = dayId;
        this.day = tmp % 100;
        tmp /= 100;
        this.month = tmp % 100;
        tmp /= 100;
        this.year = tmp;
        this.details = parseDetails(row.getDetails());
        this.total = row.getTotal();
        onLoad();
    }

    /**
     * Run in async task!
     */
    protected final boolean makeRow() {
        this.row = new SQLDailyQuest();
        row.setDayId(dayId);
        row.setDailyIndex(index);
        row.setQuestType(type.key);
        row.setDetails(Json.serialize(details));
        row.setTotal(total);
        // row.active is true before this.active!  See enable().
        row.setActive(true);
        if (0 == database().insert(row)) {
            this.row = null;
            return false;
        }
        return true;
    }

    /**
     * Update the database with all fields that can change after the
     * initial generation and save.
     */
    protected final void saveAsync(Runnable callback) {
        assert row != null;
        onSave();
        row.setDetails(Json.serialize(details));
        row.setActive(active);
        database().updateAsync(row, Set.of("details", "active"), i -> {
                if (callback != null) callback.run();
            });
    }

    /**
     * Generate this quest with all its details.  onGenerate() is
     * called here.
     */
    public final void generate(final int theIndex) {
        this.index = theIndex;
        this.day = dailyQuests().getTimer().getDay();
        this.month = dailyQuests().getTimer().getMonth();
        this.year = dailyQuests().getTimer().getYear();
        this.dayId = day + month * 100 + year * 10000;
        this.details = newDetails();
        this.total = 1; // onGenerate will override
        onGenerate();
    }

    /**
     * Just after generation, or each time after loading.
     */
    public final void enable() {
        active = true;
        onEnable();
    }

    /**
     * Unload from memory.
     */
    public final void disable() {
        onDisable();
    }

    public final void stop() {
        active = false;
        row.setActive(false);
        database().updateAsync(row, Set.of("active"), null);
        disable();
    }

    protected void onLoad() { }

    protected void onSave() { }

    /**
     * Called when the quest is enabled on any server after it was
     * loaded.  This may happen multiple times per quest, on every
     * restart or reload.
     */
    protected void onEnable() { }

    /**
     * Called when the quest is disabled on any server before it is
     * unloaded.  This may happen multiple times per quest, on every
     * restart or reload.
     */
    protected void onDisable() { }

    /**
     * Called after the quest has been loaded, usually when a player
     * joins a server.  This can happen multiple times for any
     * daily quest.
     */
    protected void onEnablePlayer(PlayerDailyQuest playerDailyQuest) { }

    protected void onDisablePlayer(PlayerDailyQuest playerDailyQuest) { }

    /**
     * Called as a final step of generation for the implementor to do
     * its thing.  This will most likely fill in the total and the
     * Details subclass, which will already be generated.
     * The row will not be available at this point in time!
     * The details instance will have been generated already.
     * Must set total!
     */
    protected void onGenerate() { }

    protected void onComplete(PlayerDailyQuest playerDailyQuest) { }

    public abstract Component getDescription(PlayerDailyQuest playerDailyQuest);

    public abstract Component getDetailedDescription(PlayerDailyQuest playerDailyQuest);

    public abstract ItemStack createIcon(PlayerDailyQuest playerDailyQuest);

    public final D newDetails() {
        return detailsCtor.get();
    }

    public final D parseDetails(String in) {
        return Json.deserialize(in, detailsClass, detailsCtor);
    }

    public final P newProgress() {
        return progressCtor.get();
    }

    public final P parseProgress(String in) {
        return Json.deserialize(in, progressClass, progressCtor);
    }

    public final int getRowId() {
        return row != null ? row.getId() : 0;
    }

    public final int getDailyIndex() {
        return row != null ? row.getDailyIndex() : -1;
    }

    /** Event Handler. */
    protected void onBlockBreak(Player player, PlayerDailyQuest playerDailyQuest, BlockBreakEvent event) { }
    /** Event Handler. */
    protected void onBlockDropItem(Player player, PlayerDailyQuest playerDailyQuest, BlockDropItemEvent event) { }
    /** Event Handler. */
    protected void onPlayerBreakBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerBreakBlockEvent event) { }
    /** Event Handler. */
    protected void onPlayerHarvestBlock(Player player, PlayerDailyQuest playerDailyQuest, PlayerHarvestBlockEvent event) { }
    /** Event Handler. */
    protected void onPlayerFish(Player player, PlayerDailyQuest playerDailyQuest, PlayerFishEvent event) { }
    /** Event Handler. */
    protected void onPluginPlayer(Player player, PlayerDailyQuest playerDailyQuest, PluginPlayerEvent event) { }

    /**
     * To print in the sidebar.
     */
    public final List<Component> getSidebarLines(PlayerDailyQuest playerDailyQuest) {
        if (!active) return List.of();
        if (playerDailyQuest.isComplete()) {
            return List.of(textOfChildren(Mytems.CHECKED_CHECKBOX.getCurrentAnimationFrame(),
                                          space(), getDescription(playerDailyQuest)));
        } else {
            TextColor color = playerDailyQuest.progressTimer > System.currentTimeMillis()
                ? GREEN
                : GRAY;
            return List.of(textOfChildren(text(superscript(playerDailyQuest.getScore()) + "/" + subscript(total), color),
                                          space(),
                                          getDescription(playerDailyQuest)));
        }
    }

    /**
     * Add to the progress, with many checks and all the side effects.
     */
    public final void makeProgress(PlayerDailyQuest playerDailyQuest, int amount) {
        if (amount <= 0) return;
        int newScore = Math.min(total, playerDailyQuest.score + amount);
        if (newScore == playerDailyQuest.score) return;
        playerDailyQuest.score = newScore;
        if (newScore >= total) {
            playerDailyQuest.setComplete(true);
            onComplete(playerDailyQuest);
            playerDailyQuest.getSession().addDailyRollsAsync(1, null);
            playerDailyQuest.getSession().addDailiesCompletedAsync(1);
            ItemMail.send(playerDailyQuest.getSession().getUuid(),
                          List.of(Mytems.RUBY.createItemStack()),
                          textOfChildren(text("Daily Quest "), getDescription(playerDailyQuest)));
            Player player = playerDailyQuest.getSession().getPlayer();
            if (player != null) {
                player.sendMessage(textOfChildren(text("Daily Quest Complete: ", GRAY), getDescription(playerDailyQuest)));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 2.0f);
            }
        } else {
            Player player = playerDailyQuest.getSession().getPlayer();
            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 2.0f);
            }
        }
        playerDailyQuest.saveAsync();
        playerDailyQuest.progressTimer = System.currentTimeMillis() + 5_000L;
    }

    public final boolean hasPermission(Permissible player) {
        return player.hasPermission("tutor.daily")
            && player.hasPermission(this.permission)
            && player.isPermissionSet(this.permission);
    }

    public final boolean hasPermission(UUID uuid) {
        return Perm.get().has(uuid, "tutor.daily")
            && Perm.get().has(uuid, this.permission);
    }

    public static boolean checkGameModeAndSurvivalServer(Player player) {
        if (!NetworkServer.current().isSurvival()) return false;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return false;
        return true;
    }

    /**
     * The class to hold all detail information to be stored in
     * SQLDailyQuest.details.  Subclasses may choose to just use this
     * blank class if they have no need for extra information.
     */
    public static class Details {
        //protected List<InventoryStore> rewards = new ArrayList<>();
    }

    /**
     * The class to hold all progress information to be stored in
     * SQLPlayerDailyQuest.progress.  Subclasses may choose to just
     * use this blank class if they have no need for extra
     * information.
     */
    public static class Progress {
    }
}
