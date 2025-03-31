package com.cavetale.tutor.session;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.connect.ServerGroup;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.core.item.ItemKinds;
import com.cavetale.core.menu.MenuItemEvent;
import com.cavetale.core.perm.Perm;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.core.util.Json;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.QuestType;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.collect.CollectItem;
import com.cavetale.tutor.collect.CollectItemSlots;
import com.cavetale.tutor.collect.ItemCollectionType;
import com.cavetale.tutor.collect.PlayerItemCollection;
import com.cavetale.tutor.daily.DailyQuest;
import com.cavetale.tutor.daily.PlayerDailyQuest;
import com.cavetale.tutor.daily.game.DailyGame;
import com.cavetale.tutor.daily.game.DailyGameChest;
import com.cavetale.tutor.daily.game.DailyGameTag;
import com.cavetale.tutor.goal.Constraint;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Noise;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetGender;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.pet.SpawnRule;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayer;
import com.cavetale.tutor.sql.SQLPlayerItemCollection;
import com.cavetale.tutor.sql.SQLPlayerPet;
import com.cavetale.tutor.sql.SQLPlayerPetUnlock;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import com.cavetale.tutor.util.Reward;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.mytems.util.Items.tooltip;
import static com.cavetale.tutor.TutorPlugin.database;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

/**
 * Cache data on a player, logged in or not.
 */
@Getter
public final class Session {
    protected final TutorPlugin plugin;
    protected final Sessions sessions;
    protected final UUID uuid;
    protected final String name;
    protected SQLPlayer playerRow = null;
    protected final Map<QuestName, PlayerQuest> currentQuests = new EnumMap<>(QuestName.class);
    protected final Map<QuestName, SQLCompletedQuest> completedQuests = new EnumMap<>(QuestName.class);
    protected final Map<PetType, SQLPlayerPetUnlock> unlockedPets = new EnumMap<>(PetType.class);
    protected final Map<ItemCollectionType, PlayerItemCollection> collections = new EnumMap<>(ItemCollectionType.class);
    protected final List<PlayerDailyQuest> dailyQuests = new ArrayList<>();
    protected SQLPlayerPet playerPetRow = null;
    protected boolean ready;
    protected boolean disabled;
    private final List<Runnable> deferredCallbacks = new ArrayList<>();
    protected Pet pet;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
    private Cache cache;
    private MenuSection section = MenuSection.TUTORIALS;
    @Setter private boolean dailyGameLocked = false;
    @Setter private boolean collectionsLocked = false;
    private int collectionPage = 0;
    private boolean filterCollections;
    private static final int[] BOTTOM_4_LEFT_SLOTS = {
        18, 19, 20, 21, 22, 23, 24, 25,
        27, 28, 29, 30, 31, 32, 33, 34,
        36, 37, 38, 39, 40, 41, 42, 43,
        45, 46, 47, 48, 49, 50, 51, 52,
    };

    /**
     * This is the constructor for a regular session which will be
     * used to hold the state of a player's quest progress.
     */
    protected Session(final Sessions sessions, final Player player) {
        this.plugin = sessions.plugin;
        this.sessions = sessions;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    /**
     * This is the constructor for a temporary session which was
     * requirested by an admin command.
     * It will call loadAsync(), but not enable().
     */
    protected Session(final Sessions sessions, final PlayerCache playerCache) {
        this.plugin = sessions.plugin;
        this.sessions = sessions;
        this.uuid = playerCache.uuid;
        this.name = playerCache.name;
    }

    private void severe(String msg) {
        plugin.getLogger().severe("[Session] [" + name + "] " + msg);
    }

    private void warning(String msg) {
        plugin.getLogger().warning("[Session] [" + name + "] " + msg);
    }

    private final class Cache {
        List<SQLPlayerQuest> playerQuestRows;
        List<SQLCompletedQuest> completedQuestRows;
        List<SQLPlayerPetUnlock> playerPetUnlockRows;
        List<SQLPlayerItemCollection> playerCollectionRows;
        // Dailies are not loaded here because we rely on the
        // DailyQuests class to tell us which dailies are live so we
        // can load or create them later.
    }

    public void loadSync() {
        this.cache = new Cache();
        this.playerRow = database().find(SQLPlayer.class)
            .eq("player", uuid).findUnique();
        if (playerRow == null) {
            playerRow = new SQLPlayer(uuid);
            database().insert(playerRow);
        }
        cache.playerQuestRows = database().find(SQLPlayerQuest.class)
            .eq("player", uuid).findList();
        cache.completedQuestRows = database().find(SQLCompletedQuest.class)
            .eq("player", uuid).findList();
        cache.playerPetUnlockRows = database().find(SQLPlayerPetUnlock.class)
            .eq("player", uuid).findList();
        this.playerPetRow = database().find(SQLPlayerPet.class)
            .eq("player", uuid).findUnique();
        if (playerPetRow == null) {
            playerPetRow = new SQLPlayerPet(uuid);
            playerPetRow.setAutoSpawn(true);
            database().insert(playerPetRow);
        }
        cache.playerCollectionRows = database().find(SQLPlayerItemCollection.class)
            .eq("player", uuid).findList();
    }

    public void loadAsync(Runnable callback) {
        database().scheduleAsyncTask(() -> {
                loadSync();
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, callback);
                }
            });
    }

    protected void enable() {
        Objects.requireNonNull(cache, "cache=null");
        if (!sessions.enabled || disabled) return;
        spawnPet();
        for (SQLPlayerQuest row : cache.playerQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                warning("Quest not found: " + row);
                continue;
            }
            Quest quest = plugin.getQuests().get(questName);
            PlayerQuest playerQuest = new PlayerQuest(this, row, quest);
            currentQuests.put(playerQuest.quest.getName(), playerQuest);
            playerQuest.loadRow(); // enable
        }
        for (SQLCompletedQuest row : cache.completedQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                warning("Quest not found: " + row);
                continue;
            }
            completedQuests.put(questName, row);
        }
        for (SQLPlayerPetUnlock row : cache.playerPetUnlockRows) {
            PetType petType = row.parsePetType();
            if (petType == null) {
                warning("Pet type not found: " + row);
                continue;
            }
            unlockedPets.put(petType, row);
        }
        for (DailyQuest dailyQuest : plugin.getDailyQuests().getDailyQuests()) {
            if (!dailyQuest.isActive()) continue;
            loadDailyQuest(dailyQuest);
        }
        for (SQLPlayerItemCollection row : cache.playerCollectionRows) {
            ItemCollectionType type = row.getItemCollectionType();
            if (type == null) {
                warning("Unknown item collection type: " + row);
                continue;
            }
            collections.put(type, new PlayerItemCollection(this, type, row));
        }
        for (ItemCollectionType itemCollectionType : ItemCollectionType.values()) {
            collections.computeIfAbsent(itemCollectionType, t -> new PlayerItemCollection(this, t, null));
        }
        cache = null;
        ready = true;
        for (Runnable callback : deferredCallbacks) {
            callback.run();
        }
        deferredCallbacks.clear();
        if (getPlayer().hasPermission("tutor.tutor")) {
            triggerAutomaticQuests();
            triggerQuestReminder();
        }
        if (Perm.get().has(uuid, "tutor.collect")) {
            checkUnlockedCollections();
        }
    }

    protected void disable() {
        if (ready) {
            for (PlayerQuest playerQuest : currentQuests.values()) {
                playerQuest.disable();
            }
            currentQuests.clear();
            for (PlayerDailyQuest playerDailyQuest : dailyQuests) {
                playerDailyQuest.disable();
            }
            dailyQuests.clear();
        }
        disabled = true;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public PlayerQuest getQuest(QuestName questName) {
        return currentQuests.get(questName);
    }

    public PlayerQuest startQuest(QuestName questName) {
        if (currentQuests.containsKey(questName)) {
            throw new IllegalStateException("Duplicate player quest: " + questName);
        }
        Quest quest = plugin.getQuests().get(questName);
        return startQuest(quest);
    }

    public PlayerQuest startQuest(Quest quest) {
        if (currentQuests.containsKey(quest.getName())) {
            throw new IllegalStateException("Duplicate player quest: " + quest.getName());
        }
        PlayerQuest playerQuest = new PlayerQuest(this, new SQLPlayerQuest(uuid, quest), quest);
        currentQuests.put(quest.getName(), playerQuest);
        playerQuest.onQuestStart();
        playerQuest.save();
        plugin.getLogger().info(name + " started " + quest.name);
        return playerQuest;
    }

    public void questComplete(QuestName questName, Player player) {
        plugin.getLogger().info(name + " completed " + questName);
        removeQuest(questName);
        if (!completedQuests.containsKey(questName)) {
            SQLCompletedQuest newRow = new SQLCompletedQuest(uuid, questName);
            completedQuests.put(questName, newRow);
            database().insertAsync(newRow, r -> {
                    plugin.getLogger().info(name + " insert complete " + questName + " => " + r);
                    if (r != 0 && ServerGroup.current() != ServerGroup.MUSEUM) {
                        Perm.get().addLevelProgress(player.getUniqueId());
                    }
                });
        }
        if (!playerRow.isQuestReminder()) {
            playerRow.setQuestReminder(true);
            database().updateAsync(playerRow, null, "questReminder");
        }
        triggerAutomaticQuests();
        triggerQuestReminder();
    }

    public PlayerQuest removeQuest(QuestName questName) {
        plugin.getLogger().info(name + " remove " + questName);
        PlayerQuest playerQuest = currentQuests.remove(questName);
        if (playerQuest != null) {
            playerQuest.disable();
            database().deleteAsync(playerQuest.getRow(), null);
        }
        return playerQuest;
    }

    public boolean hasQuest(QuestName questName) {
        return currentQuests.containsKey(questName);
    }

    public List<PlayerQuest> getQuestList() {
        return new ArrayList<>(currentQuests.values());
    }

    public void openQuestBook(Player player) {
        List<PlayerQuest> quests = getQuestList();
        List<Component> pages = new ArrayList<>();
        if (quests.isEmpty()) {
            pages.add(text("No quests to show!", DARK_RED));
        } else {
            for (PlayerQuest playerQuest : quests) {
                pages.addAll(playerQuest.getCurrentGoal().getBookPages(playerQuest));
            }
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Quests");
        meta.author(text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.closeInventory();
        player.openBook(itemStack);
    }

    public void openQuestBook(Player player, Quest quest) {
        List<Component> pages = new ArrayList<>();
        pages.add(textOfChildren(quest.name.displayName.color(DARK_AQUA).decorate(BOLD),
                                 newline(),
                                 (DefaultFont.BACK_BUTTON.forPlayer(player)
                                  .clickEvent(runCommand("/tutor menu"))
                                  .hoverEvent(showText(text("Open Tutor Menu", BLUE)))),
                                 space(), text(quest.getName().type.upper, GRAY),
                                 newline(), newline(),
                                 Mytems.CHECKED_CHECKBOX, text(" Not yet completed", GRAY),
                                 (currentQuests.isEmpty()
                                  ? textOfChildren(newline(), newline(), DefaultFont.START_BUTTON.forPlayer(player)
                                                   .clickEvent(runCommand("/tutor click start " + quest.getName().key))
                                                   .hoverEvent(showText(text("Start this " + quest.getName().type.lower, BLUE))))
                                  : empty()),
                                 newline(), newline(),
                                 join(separator(space()), quest.getName().getDescription())));
        for (Goal goal : quest.getGoals()) {
            pages.addAll(goal.getAdditionalBookPages());
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Tutor");
        meta.author(text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.closeInventory();
        player.openBook(itemStack);
    }

    public void openCompletedQuestBook(Player player, Quest quest, SQLCompletedQuest row) {
        List<Component> pages = new ArrayList<>();
        pages.add(textOfChildren(quest.name.displayName.color(DARK_AQUA).decorate(BOLD),
                                 newline(),
                                 (DefaultFont.BACK_BUTTON.forPlayer(player)
                                  .clickEvent(runCommand("/tutor menu"))
                                  .hoverEvent(showText(text("Open Tutor Menu", BLUE)))),
                                 space(), text(quest.getName().type.upper, GRAY),
                                 newline(), newline(),
                                 Mytems.CHECKED_CHECKBOX,
                                 text(" Completed ", GRAY), text(dateFormat.format(row.getTime()), DARK_AQUA),
                                 (currentQuests.isEmpty()
                                  ? textOfChildren(newline(), newline(),
                                                   DefaultFont.START_BUTTON.forPlayer(player)
                                                   .clickEvent(runCommand("/tutor click redo " + quest.getName().key))
                                                   .hoverEvent(showText(join(separator(newline()),
                                                                             text("Repeat this " + quest.getName().type.lower, BLUE),
                                                                             text("There will not be", GRAY),
                                                                             text("any extra rewards.", GRAY)))))
                                  : empty()),
                                 newline(), newline(),
                                 join(separator(space()), quest.getName().getDescription())));
        for (Goal goal : quest.getGoals()) {
            pages.addAll(goal.getAdditionalBookPages());
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Tutor");
        meta.author(text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.closeInventory();
        player.openBook(itemStack);
    }

    public void apply(Consumer<Session> callback) {
        if (ready) {
            callback.accept(this);
        } else {
            deferredCallbacks.add(() -> callback.accept(this));
        }
    }

    public void applyGoals(BiConsumer<PlayerQuest, Goal> callback) {
        if (ready) {
            applyGoalsNow(callback);
        } else {
            deferredCallbacks.add(() -> applyGoalsNow(callback));
        }
    }

    private void applyGoalsNow(BiConsumer<PlayerQuest, Goal> callback) {
        QUESTS: for (PlayerQuest playerQuest : currentQuests.values()) {
            for (Constraint constraint : playerQuest.getCurrentGoal().getConstraints()) {
                if (!constraint.doesMeet(playerQuest)) continue QUESTS;
            }
            callback.accept(playerQuest, playerQuest.getCurrentGoal());
        }
    }

    public void triggerAutomaticQuests() {
        if (!currentQuests.isEmpty()) return;
        Player player = Objects.requireNonNull(getPlayer());
        for (QuestName questName : QuestName.values()) {
            if (currentQuests.containsKey(questName)) continue;
            if (completedQuests.containsKey(questName)) continue;
            if (questName.getAutoStartPermission() != null) {
                if (Perm.get().has(uuid, questName.getAutoStartPermission())) {
                    startQuest(questName);
                    return;
                }
            }
        }
    }

    public void checkUnlockedCollections() {
        if (!NetworkServer.current().isSurvival()) return;
        if (!Perm.get().has(uuid, "tutor.collect")) return;
        Set<ItemCollectionType> completed = EnumSet.noneOf(ItemCollectionType.class);
        for (ItemCollectionType it : ItemCollectionType.values()) {
            if (collections.get(it).isComplete()) completed.add(it);
        }
        boolean didUnlock = false;
        for (ItemCollectionType it : ItemCollectionType.values()) {
            if (!collections.get(it).isUnlocked() && completed.containsAll(it.getDependencies())) {
                collections.get(it).unlock();
                didUnlock = true;
            }
        }
        if (didUnlock && !playerRow.isCollectionReminder()) {
            playerRow.setCollectionReminder(true);
            database().updateAsync(playerRow, null, "collectionReminder");
        }
    }

    public void triggerQuestReminder() {
        if (!currentQuests.isEmpty()) return;
        if (!playerRow.isIgnoreQuests() && playerRow.isQuestReminder()) {
            for (QuestName questName : QuestName.values()) {
                if (!completedQuests.containsKey(questName) && canSee(questName)) {
                    if (pet != null && (pet.isSpawned() || playerPetRow.isAutoSpawn())) {
                        pet.addSpeechBubble("session", 60L, 150L,
                                            text("There is another"),
                                            text(questName.type.lower + " waiting"),
                                            text("for you!"));
                        pet.addSpeechBubble("session", 0L, 150L,
                                            text("Click me or type"),
                                            text(questName.type.command, YELLOW));
                    } else {
                        getPlayer().sendMessage(text()
                                                .content("There is another " + questName.type.lower
                                                         + " waiting for you! Type ")
                                                .append(text(questName.type.command, YELLOW))
                                                .color(AQUA)
                                                .clickEvent(questName.type.clickEvent())
                                                .hoverEvent(questName.type.hoverEvent()));
                    }
                    return;
                }
            }
        }
    }

    /**
     * Create the pet object and prepare it to be spawned.
     */
    public Pet spawnPet() {
        if (pet != null) return pet;
        PetType petType = playerPetRow.parsePetType();
        if (petType == null) return null; // must not have finished beginner tut
        if (pet != null) {
            pet.setOnDespawn(null);
            pet.despawn();
            pet.setType(petType);
        } else {
            pet = plugin.getPets().createPet(uuid, petType);
        }
        pet.setExclusive(true);
        pet.setAutoRespawn(playerPetRow.isAutoSpawn());
        pet.setOwnerDistance(4.0);
        pet.setCustomName(playerPetRow.getNameComponent());
        pet.setOnClick(() -> {
                clickPet(getPlayer());
            });
        pet.setOnDespawn(this::onPetDespawn);
        return pet;
    }

    public boolean applyPet(Consumer<Pet> callback) {
        if (pet == null) return false;
        callback.accept(pet);
        return true;
    }

    public void setPetType(PetType petType) {
        if (petType == playerPetRow.parsePetType()) return;
        playerPetRow.setPetType(petType);
        playerPetRow.setNow();
        database().updateAsync(playerPetRow, null, "pet", "updated");
    }

    public void renamePet(String petName) {
        playerPetRow.setName(petName);
        playerPetRow.setNow();
        if (pet != null) {
            pet.setCustomName(playerPetRow.getNameComponent());
        }
        database().updateAsync(playerPetRow, null, "name", "updated");
    }

    /**
     * Clicking your pet should intelligently open the most reasonable menu.
     * - If you have a quest: Quest book
     * - Otherwise: Tutor menu
     */
    public void clickPet(Player player) {
        if (!currentQuests.isEmpty()) {
            openQuestBook(player);
        } else {
            openMenu(player);
        }
    }

    private void onPetDespawn() {
        if (disabled) return;
        Player player = getPlayer();
        if (player == null) return;
        if (!playerPetRow.isAutoSpawn()) {
            Component msg = text()
                .append(playerPetRow.getNameComponent())
                .append(text(" despawned. Bring it back via "))
                .append(text("/tutor", YELLOW))
                .color(GRAY)
                .clickEvent(runCommand("/tutor"))
                .hoverEvent(showText(text("/tutor", YELLOW)))
                .build();
            player.sendMessage(msg);
        }
    }

    public void openMenu(Player player, MenuSection theSection) {
        this.section = theSection;
        openMenu(player);
    }

    public void openMenu(Player player) {
        final Gui gui = new Gui(plugin)
            .size(6 * 9)
            .layer(GuiOverlay.BLANK, section.backgroundColor)
            .title(section.title);
        List<MenuSection> sectionList = new ArrayList<>(List.of(MenuSection.values()));
        if (pet == null) sectionList.remove(MenuSection.PET);
        final int menuOffset = 4 - ((sectionList.size() * 2 - 1) / 2);
        for (int i = 0; i < sectionList.size(); i += 1) {
            MenuSection menuSection = sectionList.get(i);
            final int slot = menuOffset + i + i;
            gui.setItem(slot, menuSection.createIcon(this), click -> {
                    if (!click.isLeftClick()) return;
                    Noise.CLICK.play(player);
                    openMenu(player, menuSection);
                });
            if (section == menuSection) {
                gui.tab(slot, section.backgroundColor, color(0x303040));
            }
        }
        section.makeGui(gui, player, this);
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                MenuItemEvent.openMenu(player);
            });
        gui.open(player);
    }

    public boolean canSee(QuestName questName) {
        for (QuestName dep : questName.getSeeDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        if (questName.getStartPermission() != null && !Perm.get().has(uuid, questName.getStartPermission())) {
            return false;
        }
        return true;
    }

    public boolean canStart(QuestName questName) {
        for (QuestName dep : questName.getStartDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        return true;
    }

    protected void makeTutorialMenu(Gui gui, Player player) {
        int nextIndex = 0;
        for (QuestName questName : QuestName.values()) {
            if (!completedQuests.containsKey(questName) && !canSee(questName)) continue;
            final int slot = BOTTOM_4_LEFT_SLOTS[nextIndex++];
            gui.setItem(slot, makeQuestItem(questName), click -> {
                    if (!click.isLeftClick()) return;
                    if (currentQuests.containsKey(questName)) {
                        Noise.CLICK.play(player);
                        openQuestBook(player);
                    } else if (completedQuests.containsKey(questName)) {
                        Noise.CLICK.play(player);
                        openCompletedQuestBook(player, plugin.getQuests().get(questName), completedQuests.get(questName));
                    } else {
                        Noise.CLICK.play(player);
                        openQuestBook(player, plugin.getQuests().get(questName));
                    }
                });
        }
        if (playerRow.isIgnoreQuests()) {
            gui.setItem(8, 2, Mytems.BLIND_EYE.createIcon(List.of(text("Ignoring Quests", RED),
                                                                  text("Quests and tutorials", GRAY),
                                                                  text("will not show in your", GRAY),
                                                                  text("sidebar.", GRAY),
                                                                  empty(),
                                                                  textOfChildren(Mytems.MOUSE_LEFT, text(" Unignore", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                            playerRow.setIgnoreQuests(false);
                            database().updateAsync(playerRow, null, "ignoreQuests");
                            openMenu(player);
                        });
        } else {
            gui.setItem(8, 3, Mytems.MAGNIFYING_GLASS.createIcon(List.of(text("Observing Quests", GREEN),
                                                                         text("Quests and tutorials", GRAY),
                                                                         text("will show in your", GRAY),
                                                                         text("sidebar.", GRAY),
                                                                         empty(),
                                                                         textOfChildren(Mytems.MOUSE_LEFT, text(" Ignore", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                            playerRow.setIgnoreQuests(true);
                            database().updateAsync(playerRow, null, "ignoreQuests");
                            openMenu(player);
                        });
        }
        if (playerRow.isQuestReminder()) {
            playerRow.setQuestReminder(false);
            database().updateAsync(playerRow, null, "questReminder");
        }
    }

    protected ItemStack makeQuestItem(QuestName questName) {
        Quest quest = plugin.getQuests().get(questName);
        final ItemStack item;
        final List<Component> text = new ArrayList<>();
        text.add(quest.name.displayName);
        if (currentQuests.containsKey(questName)) {
            item = Mytems.STAR.createIcon();
            text.add(text("Current Quest", GOLD));
        } else if (completedQuests.containsKey(questName)) {
            item = Mytems.CHECKED_CHECKBOX.createIcon();
            text.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text("Completed", GREEN)));
        } else if (canStart(questName)) {
            item = Mytems.CHECKBOX.createIcon();
            text.add(textOfChildren(Mytems.CHECKBOX, text("Start this " + questName.type.lower + "?", BLUE)));
        } else {
            item = Mytems.COPPER_KEYHOLE.createIcon();
            text.add(textOfChildren(Mytems.COPPER_KEYHOLE, text("Locked", DARK_RED)));
        }
        if (!questName.getDescription().isEmpty()) {
            text.add(empty());
            text.addAll(questName.getDescription());
        }
        if (!questName.getStartDependencies().isEmpty()) {
            text.add(empty());
            text.add(text(questName.type.upper + " Requirements", GRAY));
            for (QuestName dependency : questName.getStartDependencies()) {
                if (completedQuests.containsKey(dependency)) {
                    text.add(text(Unicode.CHECKED_CHECKBOX.character + " ", GRAY)
                             .append(dependency.displayName));
                } else {
                    text.add(text(Unicode.CHECKBOX.character + " ", DARK_GRAY)
                             .append(dependency.displayName));
                }
            }
        }
        tooltip(item, text);
        return item;
    }

    protected void makePetMenu(Gui gui, Player player) {
        if (pet == null) return;
        List<Integer> indexes = List.of(9 + 4,
                                        18 + 1, 18 + 3, 18 + 5, 18 + 7);
        final boolean on = playerPetRow.isAutoSpawn();
        Component petName = playerPetRow.getNameComponent();
        PetType petType = playerPetRow.parsePetType();
        PetGender petGender = playerPetRow.getGender();
        // Pet Type
        gui.setItem(4, 2, petType.mytems.createIcon(List.of(text("Choose Pet", GREEN))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        Noise.CLICK.play(player);
                        openPetTypeSelectionMenu(player);
                    });
        // Spawn
        gui.setItem(1, 4, Mytems.STAR.createIcon(List.of(textOfChildren(text("Spawn ", GREEN), petName))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        Noise.CLICK.play(player);
                        if (!pet.tryToSpawn(player, SpawnRule.LOOKAT)) {
                            if (!pet.tryToSpawn(player, SpawnRule.NEARBY)) {
                                player.sendMessage(text("Could not spawn your pet!",
                                                        RED));
                            }
                        }
                        player.sendMessage(text().append(petName)
                                           .append(text(" appeared!"))
                                           .color(GREEN));
                        pet.setAutoDespawn(false);
                    });
        // Auto Respawn
        gui.setItem(3, 4, (on
                           ? Mytems.OK.createIcon(List.of(text("Auto Respawn Enabled", GREEN)))
                           : Mytems.NO.createIcon(List.of(text("Auto Respawn Disabled", RED)))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        if (on == playerPetRow.isAutoSpawn()) {
                            Noise.CLICK.play(player);
                            playerPetRow.setAutoSpawn(!on);
                            playerPetRow.setNow();
                            pet.setAutoRespawn(!on);
                            database().updateAsync(playerPetRow, null, "auto_spawn", "updated");
                        }
                        openMenu(player, MenuSection.PET);
                    });
        // Name
        gui.setItem(5, 4, tooltip(new ItemStack(Material.NAME_TAG), List.of(text("Change Name", GREEN))),
                    click -> {
                        if (!click.isLeftClick()) return;
                        Noise.CLICK.play(player);
                        player.closeInventory();
                        player.sendMessage(text("\n  Click here to change the name of your pet\n", GREEN, BOLD)
                                           .clickEvent(suggestCommand("/tutor rename "))
                                           .hoverEvent(showText(text("/tutor rename", YELLOW))));
                    });
        // Gender
        gui.setItem(7, 4, tooltip(petGender.itemStack.clone(), List.of(petGender.component)),
                    click -> {
                        if (!click.isLeftClick()) return;
                        if (playerPetRow.getGender() != petGender) {
                            return;
                        }
                        Noise.CLICK.play(player);
                        PetGender[] allGenders = PetGender.values();
                        PetGender newGender = allGenders[(petGender.ordinal() + 1) % allGenders.length];
                        playerPetRow.setGender(newGender);
                        playerPetRow.setNow();
                        database().updateAsync(playerPetRow, null, "gender", "updated");
                        if (pet != null) {
                            pet.setCustomName(playerPetRow.getNameComponent());
                        }
                        openMenu(player, MenuSection.PET);
                    });
        gui.title(playerPetRow.getNameComponent().color(WHITE));
    }

    protected void makeDailyQuestGui(Gui gui, Player player) {
        List<PlayerDailyQuest> visibleDailies = getVisibleDailies();
        final int offset = 4 - (visibleDailies.size() / 2);
        for (int i = 0; i < visibleDailies.size(); i += 1) {
            PlayerDailyQuest playerDailyQuest = visibleDailies.get(i);
            final ItemStack icon;
            final DailyQuest<?, ?> dailyQuest = playerDailyQuest.getDailyQuest();
            if (playerDailyQuest.isComplete()) {
                icon = Mytems.CHECKED_CHECKBOX.createIcon();
            } else {
                icon = dailyQuest.createIcon(playerDailyQuest);
                gui.highlight(offset + i, 3, MenuSection.DAILY.backgroundColor);
            }
            List<Component> text = new ArrayList<>();
            text.add(dailyQuest.getDescription(playerDailyQuest));
            final int hours = 24 - plugin.getDailyQuests().getTimer().getHour();
            final String box = "\u2588";
            final int fullBars;
            final int emptyBars;
            final int maxBars = 12;
            if (dailyQuest.getTotal() <= maxBars) {
                fullBars = playerDailyQuest.getScore();
                emptyBars = dailyQuest.getTotal() - fullBars;
            } else {
                fullBars = (playerDailyQuest.getScore() * maxBars) / dailyQuest.getTotal();
                emptyBars = maxBars - fullBars;
            }
            String fullBarString = "";
            String emptyBarString = "";
            for (int j = 0; j < fullBars; j += 1) fullBarString += box;
            for (int j = 0; j < emptyBars; j += 1) emptyBarString += box;
            text.add(textOfChildren(text(fullBarString, GREEN), text(emptyBarString, GRAY)));
            text.add(textOfChildren(text("Progress ", GRAY), text(playerDailyQuest.getScore() + "/" + dailyQuest.getTotal(), WHITE)));
            text.add(textOfChildren(text("Time left ", GRAY), text(hours + "h", WHITE)));
            if (playerDailyQuest.isComplete()) {
                text.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text(" Complete", GREEN)));
            } else {
                for (ItemStack item : dailyQuest.getRewards()) {
                    text.add(textOfChildren(text("Reward ", GRAY), ItemKinds.chatDescription(item).color(WHITE)));
                }
                text.add(textOfChildren(text("Reward ", GRAY), Mytems.DICE, text("Roll", GRAY)));
            }
            text.add(empty());
            text.add(textOfChildren(Mytems.MOUSE_LEFT, text(" Details", GRAY)));
            tooltip(icon, text);
            gui.setItem(offset + i, 3, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                    openDailyQuestBook(player, dailyQuest, playerDailyQuest);
                });
        }
        final int rolls = playerRow.getDailyGameRolls();
        final int totalRolls = playerRow.getTotalRolls();
        final int totalGames = playerRow.getDailyGames();
        ItemStack diceIcon = Mytems.DICE.createIcon(List.of(text("Play the Daily Game", GREEN),
                                                            text("Play a board game to level", GRAY),
                                                            text("up your tiers and find", GRAY),
                                                            text("rewards along the way.", GRAY),
                                                            text("You can get more dice rolls", GRAY),
                                                            text("by completing daily quests,", GRAY),
                                                            text("tutorials, and collections.", GRAY),
                                                            empty(),
                                                            textOfChildren(text(tiny("rolls available "), GRAY),
                                                                           text(rolls + Unicode.MULTIPLICATION.string, WHITE), Mytems.DICE),
                                                            textOfChildren(text(tiny("total rolls "), GRAY), text(totalRolls, WHITE)),
                                                            textOfChildren(text(tiny("total games "), GRAY), text(totalGames, WHITE)),
                                                            textOfChildren(Mytems.MOUSE_LEFT, text(" Play", GRAY))));
        diceIcon.setAmount(Math.max(1, Math.min(64, rolls)));
        gui.setItem(0, 3, diceIcon,
                    click -> {
                        if (!click.isLeftClick()) return;
                        if (dailyGameLocked) return;
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                        openDailyGame(player);
                    });
        if (playerRow.isIgnoreDailies()) {
            gui.setItem(8, 3, Mytems.BLIND_EYE.createIcon(List.of(text("Ignoring Daily Quests", RED),
                                                                  text("Daily Quests will not", GRAY),
                                                                  text("show in your sidebar.", GRAY),
                                                                  empty(),
                                                                  textOfChildren(Mytems.MOUSE_LEFT, text(" Unignore", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                            playerRow.setIgnoreDailies(false);
                            database().updateAsync(playerRow, null, "ignoreDailies");
                            openMenu(player);
                        });
        } else {
            gui.setItem(8, 3, Mytems.MAGNIFYING_GLASS.createIcon(List.of(text("Observing Daily Quests", GREEN),
                                                                         text("Daily Quests will show", GRAY),
                                                                         text("in your sidebar.", GRAY),
                                                                         empty(),
                                                                         textOfChildren(Mytems.MOUSE_LEFT, text(" Ignore", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                            playerRow.setIgnoreDailies(true);
                            database().updateAsync(playerRow, null, "ignoreDailies");
                            openMenu(player);
                        });
        }
    }

    public void openDailyQuestBook(Player player, DailyQuest<?, ?> dailyQuest, PlayerDailyQuest playerDailyQuest) {
        List<Component> text = new ArrayList<>();
        text.add(DefaultFont.BACK_BUTTON.forPlayer(player)
                 .hoverEvent(showText(text("Go back", BLUE)))
                 .clickEvent(runCommand("/daily back")));
        text.add(empty());
        text.add(dailyQuest.getDetailedDescription(playerDailyQuest));
        final int hours = 24 - plugin.getDailyQuests().getTimer().getHour();
        final String box = "\u2588";
        final int fullBars;
        final int emptyBars;
        final int maxBars = 12;
        if (dailyQuest.getTotal() <= maxBars) {
            fullBars = playerDailyQuest.getScore();
            emptyBars = dailyQuest.getTotal() - fullBars;
        } else {
            fullBars = (playerDailyQuest.getScore() * maxBars) / dailyQuest.getTotal();
            emptyBars = maxBars - fullBars;
        }
        String fullBarString = "";
        String emptyBarString = "";
        for (int j = 0; j < fullBars; j += 1) fullBarString += box;
        for (int j = 0; j < emptyBars; j += 1) emptyBarString += box;
        text.add(textOfChildren(text(fullBarString, BLUE), text(emptyBarString, GRAY)));
        text.add(textOfChildren(text("Progress ", DARK_GRAY), text(playerDailyQuest.getScore() + "/" + dailyQuest.getTotal())));
        text.add(textOfChildren(text("Time left ", DARK_GRAY), text(hours + "h")));
        if (playerDailyQuest.isComplete()) {
            text.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text(" Complete", BLUE)));
        }
        text.add(empty());
        List<Component> rewardComponents = new ArrayList<>();
        rewardComponents.add(text("Rewards", GRAY));
        for (ItemStack item : dailyQuest.getRewards()) {
            rewardComponents.add(ItemKinds.chatDescription(item));
        }
        rewardComponents.add(textOfChildren(Mytems.DICE, text("Roll", GRAY))
                             .hoverEvent(showText(text("1 Daily Game Roll", GRAY))));
        text.add(join(separator(space()), rewardComponents));
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.editMeta(m -> {
                BookMeta meta = (BookMeta) m;
                meta.author(text("Cavetale"));
                meta.title(text("Tutor"));
                meta.pages(List.of(join(separator(newline()), text)));
            });
        player.closeInventory();
        player.openBook(book);
    }

    public void openPetTypeSelectionMenu(Player player) {
        final PetType[] allTypes = PetType.values();
        final int rows = (allTypes.length - 1) / 9 + 1;
        final int size = 6 * 9;
        Gui gui = new Gui(plugin)
            .size(size)
            .layer(GuiOverlay.BLANK, color(0x008888))
            .title(text("Choose a Pet", WHITE));
        for (int index = 0; index < size; index += 1) {
            PetType petType = index < allTypes.length ? allTypes[index] : null;
            if (petType != null && petType.unlocked || unlockedPets.containsKey(petType)) {
                gui.setItem(index, petType.mytems.createIcon(List.of(petType.displayName.color(GREEN))),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                setPetType(petType);
                                if (pet != null && petType != pet.getType()) {
                                    pet.setType(petType);
                                    pet.despawn();
                                }
                                openMenu(player, MenuSection.PET);
                            });
            } else {
                // petType may be null!
                gui.setItem(index, Mytems.QUESTION_MARK.createIcon(List.of(text("???", DARK_RED),
                                                                           text("Not yet unlocked", DARK_GRAY))),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.FAIL.play(player);
                            });
            }
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                Noise.CLICK.play(player);
                openMenu(player, MenuSection.PET);
            });
        gui.open(player);
    }

    /**
     * Collections overview menu.
     */
    protected void makeCollectMenu(Gui gui, Player player) {
        final int pageSize = BOTTOM_4_LEFT_SLOTS.length;
        final List<PlayerItemCollection> displayCollections = new ArrayList<>();
        for (ItemCollectionType itemCollectionType : ItemCollectionType.values()) {
            final PlayerItemCollection playerItemCollection = collections.get(itemCollectionType);
            if (filterCollections && (!playerItemCollection.isUnlocked() || (playerItemCollection.isComplete() && playerItemCollection.isClaimed()))) {
                continue;
            }
            displayCollections.add(playerItemCollection);
        }
        final int pageCount = ((displayCollections.size() - 1) / pageSize) + 1;
        if (collectionPage >= pageCount) {
            collectionPage = pageCount - 1;
        }
        gui.title(textOfChildren(text("Collections", WHITE),
                                 text(" (" + (collectionPage + 1) + "/" + pageCount + ")")));
        if (collectionPage > 0) {
            gui.setItem(9, Mytems.ARROW_LEFT.createIcon(List.of(text("Previous Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    collectionPage -= 1;
                    openMenu(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                });
        }
        if (collectionPage < pageCount - 1) {
            gui.setItem(17, Mytems.ARROW_RIGHT.createIcon(List.of(text("Next Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    collectionPage += 1;
                    openMenu(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                });
        }
        if (!filterCollections) {
            gui.setItem(8, 3, Mytems.EARTH.createIcon(List.of(text("Showing all Collections", GRAY),
                                                              empty(),
                                                              textOfChildren(Mytems.MOUSE_LEFT, text(" Filter", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            filterCollections = true;
                            openMenu(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                        });
        } else {
            gui.setItem(8, 3, Mytems.BINOCULARS.createIcon(List.of(text("Showing only open Collections", GRAY),
                                                                   empty(),
                                                                   textOfChildren(Mytems.MOUSE_LEFT, text(" Show all", GRAY)))),
                        click -> {
                            if (!click.isLeftClick()) return;
                            filterCollections = false;
                            openMenu(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                        });
        }
        for (int i = 0; i < pageSize; i += 1) {
            final int valueIndex = pageSize * collectionPage + i;
            if (valueIndex >= displayCollections.size()) {
                break;
            }
            final PlayerItemCollection playerItemCollection = displayCollections.get(valueIndex);
            final ItemCollectionType itemCollectionType = playerItemCollection.getCollection();
            final int slot = BOTTOM_4_LEFT_SLOTS[i];
            if (!playerItemCollection.isUnlocked()) {
                // Locked
                gui.setItem(slot, Mytems.COPPER_KEYHOLE.createIcon(List.of(text("???", DARK_RED),
                                                                           text("Not yet unlocked", DARK_GRAY))),
                            click -> {
                                if (!click.isLeftClick()) return;
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
                            });
            } else if (playerItemCollection.isComplete() && playerItemCollection.isClaimed()) {
                // Finished
                gui.setItem(slot, Mytems.CHECKED_CHECKBOX.createIcon(itemCollectionType.makeIconText()),
                            click -> {
                                if (!click.isLeftClick()) return;
                                openItemCollectionMenu(player, itemCollectionType);
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                            });
            } else {
                // Unfinished
                gui.setItem(slot, itemCollectionType.makeIcon(), click -> {
                        if (!click.isLeftClick()) return;
                        openItemCollectionMenu(player, itemCollectionType);
                        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 0.5f, 1f);
                    });
                if (playerItemCollection.isComplete()) {
                    gui.highlight(slot, GOLD);
                }
            }
        }
        if (playerRow.isCollectionReminder()) {
            playerRow.setCollectionReminder(false);
            database().updateAsync(playerRow, null, "collectionReminder");
        }
    }

    /**
     * Menu of one collection.
     */
    protected void openItemCollectionMenu(Player player, ItemCollectionType itemCollectionType) {
        PlayerItemCollection playerItemCollection = collections.get(itemCollectionType);
        Gui gui = new Gui(plugin)
            .size(6 * 9)
            .layer(GuiOverlay.BLANK, LIGHT_PURPLE)
            .layer(GuiOverlay.WHITE, itemCollectionType.getBackground())
            .layer(GuiOverlay.ITEM_COLLECTION, itemCollectionType.getColor())
            .title(text(itemCollectionType.getDisplayName() + " Collection", itemCollectionType.getColor()));
        int nextIndex = 0;
        List<CollectItem> collectItems = itemCollectionType.getItems();
        List<Integer> slots = CollectItemSlots.slotsForSize(collectItems.size());
        for (CollectItem collectItem : collectItems) {
            final int index = nextIndex++;
            gui.setItem(slots.get(index), collectItem.makeIcon(playerItemCollection));
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                openMenu(player, MenuSection.COLLECT);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 0.5f, 1.0f);
            });
        gui.onClickBottom(click -> {
                if (!NetworkServer.current().isSurvival()) return;
                if (!click.isLeftClick()) return;
                if (!playerItemCollection.isUnlocked()) return;
                if (collectionsLocked) return;
                ItemStack item = click.getCurrentItem();
                if (item == null || item.getType().isAir()) return;
                for (CollectItem collectItem : itemCollectionType.getItems()) {
                    if (!collectItem.matchItemStack(item)) continue;
                    final int score = playerItemCollection.getScore(collectItem);
                    final int total = collectItem.getTotalAmount();
                    if (score >= total) continue;
                    final int increment = click.isShiftClick()
                        ? Math.min(item.getAmount(), total - score)
                        : 1;
                    item.subtract(increment);
                    playerItemCollection.addScore(collectItem, increment, () -> {
                            if (playerItemCollection.isComplete()) {
                                checkUnlockedCollections();
                                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.5f, 2.0f);
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1.0f, 1.25f);
                            }
                            openItemCollectionMenu(player, itemCollectionType);
                        });
                    // success!
                    return;
                }
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 0.5f);
            });
        List<Integer> rewardSlots = CollectItemSlots.rewards();
        List<ItemStack> rewards = itemCollectionType.getRewards();
        for (int i = 0; i < rewardSlots.size() && i < rewards.size(); i += 1) {
            gui.setItem(rewardSlots.get(i), rewards.get(i), click -> {
                    if (!NetworkServer.current().isSurvival()) return;
                    if (!click.isLeftClick()) return;
                    if (!playerItemCollection.isUnlocked()) return;
                    if (!playerItemCollection.isComplete()) return;
                    if (playerItemCollection.isClaimed()) return;
                    if (collectionsLocked) return;
                    playerItemCollection.claim(() -> {
                            if (disabled || !player.isOnline()) {
                                severe("Player left claiming reward: " + itemCollectionType);
                                return;
                            }
                            Gui rewardGui = Reward.give(player, rewards, itemCollectionType.getDisplayName(), itemCollectionType.getColor());
                            rewardGui.setItem(Gui.OUTSIDE, null, c -> openMenu(player, MenuSection.COLLECT));
                            addDailyRollsAsync(1, null);
                            Perm.get().addLevelProgress(uuid);
                        });
                });
        }
        gui.open(player);
    }

    public boolean hasPet() {
        return pet != null;
    }

    protected void expireDailyQuests(final int newDayId) {
        for (PlayerDailyQuest playerDailyQuest : List.copyOf(dailyQuests)) {
            if (playerDailyQuest.getDailyQuest().getDayId() == newDayId) continue;
            dailyQuests.remove(playerDailyQuest);
            playerDailyQuest.disable();
        }
    }

    public void cleanDailyQuests() {
        for (PlayerDailyQuest playerDailyQuest : List.copyOf(dailyQuests)) {
            if (plugin.getDailyQuests().getDailyQuests().contains(playerDailyQuest.getDailyQuest())) {
                continue;
            }
            dailyQuests.remove(playerDailyQuest);
            playerDailyQuest.disable();
        }
    }

    /**
     * Call this after a daily quest row was loaded from the database,
     * OR created and activated.
     */
    protected void loadDailyQuest(final DailyQuest<?, ?> dailyQuest) {
        for (PlayerDailyQuest playerDailyQuest : dailyQuests) {
            if (playerDailyQuest.getDailyQuest().getRowId() == dailyQuest.getRowId()) {
                return;
            }
        }
        PlayerDailyQuest playerDailyQuest = new PlayerDailyQuest(this, dailyQuest);
        dailyQuests.add(playerDailyQuest);
        database().scheduleAsyncTask(() -> {
                do {
                    if (playerDailyQuest.loadRow()) break;
                    if (playerDailyQuest.makeRow()) break;
                    if (playerDailyQuest.loadRow()) break;
                    throw new IllegalStateException("loadDailyQuest() fail: " + dailyQuest);
                } while (false);
                Bukkit.getScheduler().runTask(plugin, () -> {
                        dailyQuests.sort(Comparator.comparing(pdq -> pdq.getDailyQuest().getGroup()));
                        playerDailyQuest.enable();
                    });
            });
    }

    public List<PlayerDailyQuest> getVisibleDailies() {
        List<PlayerDailyQuest> result = new ArrayList<>();
        for (PlayerDailyQuest playerDailyQuest : dailyQuests) {
            if (!playerDailyQuest.isReady()) continue;
            if (!playerDailyQuest.getDailyQuest().isActive()) continue;
            if (!playerDailyQuest.getDailyQuest().hasPermission(uuid)) continue;
            result.add(playerDailyQuest);
        }
        return result;
    }

    protected void sidebar(List<Component> lines) {
        if (!playerRow.isIgnoreQuests()) {
            for (PlayerQuest playerQuest : getQuestList()) {
                if (playerQuest.getQuest().getName().type == QuestType.TUTORIAL) {
                    lines.add(textOfChildren(text(tiny("your "), AQUA), text("/tut", YELLOW), text(tiny("orial"), AQUA)));
                } else {
                    lines.add(textOfChildren(text(tiny("your "), AQUA), text("/q", YELLOW), text(tiny("uest"), AQUA)));
                }
                lines.addAll(playerQuest.getCurrentGoal().getSidebarLines(playerQuest));
                break;
            }
        }
        if (!playerRow.isIgnoreDailies()) {
            List<PlayerDailyQuest> visibleDailies = getVisibleDailies();
            if (countUnfinishedDailies(visibleDailies) > 0) {
                Component prefix = Mytems.COLORFALL_HOURGLASS.getCurrentAnimationFrame();
                lines.add(textOfChildren(prefix, text("/daily ", YELLOW), text(tiny("quests (" + visibleDailies.size() + ")"), AQUA)));
                for (PlayerDailyQuest playerDailyQuest : visibleDailies) {
                    for (Component line : playerDailyQuest.getDailyQuest().getSidebarLines(playerDailyQuest)) {
                        lines.add(textOfChildren(prefix, line));
                    }
                }
            }
        }
        if (playerRow.isRollReminder() && playerRow.getDailyGameRolls() > 0) {
            lines.add(textOfChildren(Mytems.DICE, text("You have ", AQUA), text("/daily", YELLOW), text(" rolls", AQUA)));
        }
        if (playerRow.isCollectionReminder()) {
            lines.add(textOfChildren(VanillaItems.BUNDLE, text("You have new ", AQUA)));
            lines.add(textOfChildren(VanillaItems.BUNDLE, text("/collect", YELLOW), text("ions", AQUA)));
        }
    }

    public static int countUnfinishedDailies(List<PlayerDailyQuest> visibleDailies) {
        int result = 0;
        for (var it : visibleDailies) {
            if (!it.isComplete()) result += 1;
        }
        return result;
    }

    protected void applyDailyQuests(Consumer<PlayerDailyQuest> callback) {
        for (PlayerDailyQuest playerDailyQuest : dailyQuests) {
            if (!playerDailyQuest.isReady()) continue;
            if (!playerDailyQuest.getDailyQuest().isActive()) continue;
            if (playerDailyQuest.isComplete()) continue;
            if (!playerDailyQuest.getDailyQuest().hasPermission(uuid)) continue;
            callback.accept(playerDailyQuest);
        }
    }

    public void saveDailyGameAsync(int newRolls, DailyGameTag tag, Runnable callback) {
        dailyGameLocked = true;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .set("dailyGame", Json.serialize(tag))
            .atomic("dailyGameRolls", newRolls)
            .async(i -> {
                    dailyGameLocked = false;
                    if (i == 0) {
                        severe("saveDiceRollAsync could not save "
                               + " rolls=" + playerRow.getDailyGameRolls() + "/" + newRolls
                               + " tag=" + Json.serialize(tag));
                    } else {
                        callback.run();
                    }
                });
    }

    public void addDailyRollsAsync(int chrolls, Runnable callback) {
        if (chrolls == 0) return;
        dailyGameLocked = true;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("dailyGameRolls", chrolls)
            .set("rollReminder", true)
            .async(i -> {
                    if (i == 0) {
                        severe("addDailyRollsAsync: " + i);
                    } else {
                        dailyGameLocked = false;
                        playerRow.setDailyGameRolls(playerRow.getDailyGameRolls() + chrolls);
                        if (callback != null) callback.run();
                    }
                });
    }

    public void addQuestsCompletedAsync(int value) {
        if (value == 0) return;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("quests", value)
            .async(i -> {
                    if (i == 0) severe("addQuestsCompletedAsync: " + i);
                    playerRow.setQuests(playerRow.getQuests() + value);
                });
    }

    public void addDailiesCompletedAsync(int value) {
        if (value == 0) return;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("dailies", value)
            .async(i -> {
                    if (i == 0) severe("addDailiesCompletedAsync: " + i);
                    playerRow.setDailies(playerRow.getDailies() + value);
                });
    }

    public void addDailyGamesCompletedAsync(int value) {
        if (value == 0) return;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("dailyGames", value)
            .async(i -> {
                    if (i == 0) severe("addDailyRollsAsync: " + i);
                    playerRow.setDailyGames(playerRow.getDailyGames() + value);
                });
    }

    public void addCollectionsCompletedAsync(int value) {
        if (value == 0) return;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("collections", value)
            .async(i -> {
                    if (i == 0) severe("addCollectionsCompletedAsync: " + i);
                    playerRow.setCollections(playerRow.getCollections() + value);
                });
    }

    public void addTotalRollsAsync(int value) {
        if (value == 0) return;
        database().update(SQLPlayer.class)
            .row(playerRow)
            .add("totalRolls", value)
            .async(i -> {
                    if (i == 0) severe("addTotalRollsAsync: " + i);
                    playerRow.setTotalRolls(playerRow.getTotalRolls() + value);
                });
    }

    public void openDailyGame(Player player) {
        DailyGameTag tag = playerRow.parseDailyGameTag();
        if (tag.getChestSeed() != 0L) {
            DailyGameChest dailyGameChest = new DailyGameChest(player, tag, this, tag.getChestSeed());
            dailyGameChest.start();
        } else {
            DailyGame game = new DailyGame(player, tag);
            game.start();
            game.selectState();
            if (playerRow.isRollReminder()) {
                playerRow.setRollReminder(false);
                database().updateAsync(playerRow, null, "rollReminder");
            }
        }
    }
}
