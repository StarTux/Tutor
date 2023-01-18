package com.cavetale.tutor.session;

import com.cavetale.core.connect.ServerGroup;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.perm.Perm;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.QuestType;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.daily.DailyQuest;
import com.cavetale.tutor.daily.PlayerDailyQuest;
import com.cavetale.tutor.goal.Constraint;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Noise;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetGender;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.pet.SpawnRule;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerPet;
import com.cavetale.tutor.sql.SQLPlayerPetUnlock;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.tutor.TutorPlugin.database;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
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
    protected final Map<QuestName, PlayerQuest> currentQuests = new EnumMap<>(QuestName.class);
    protected final Map<QuestName, SQLCompletedQuest> completedQuests = new EnumMap<>(QuestName.class);
    protected final Map<PetType, SQLPlayerPetUnlock> unlockedPets = new EnumMap<>(PetType.class);
    protected final List<PlayerDailyQuest> dailyQuests = new ArrayList<>();
    protected SQLPlayerPet playerPetRow = null;
    protected boolean ready;
    protected boolean disabled;
    private final List<Runnable> deferredCallbacks = new ArrayList<>();
    protected Pet pet;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
    private Cache cache;
    private MenuSection section = MenuSection.TUTORIALS;

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

    private final class Cache {
        List<SQLPlayerQuest> playerQuestRows;
        List<SQLCompletedQuest> completedQuestRows;
        List<SQLPlayerPetUnlock> playerPetUnlockRows;
        // Dailies are not loaded here because we rely on the
        // DailyQuests class to tell us which dailies are live so we
        // can load or create them later.
    }

    protected void loadAsync(Runnable callback) {
        database().scheduleAsyncTask(() -> {
                this.cache = new Cache();
                cache.playerQuestRows = database().find(SQLPlayerQuest.class)
                    .eq("player", uuid).findList();
                cache.completedQuestRows = database().find(SQLCompletedQuest.class)
                    .eq("player", uuid).findList();
                cache.playerPetUnlockRows = database().find(SQLPlayerPetUnlock.class)
                    .eq("player", uuid).findList();
                this.playerPetRow = database().find(SQLPlayerPet.class).eq("player", uuid).findUnique();
                if (playerPetRow == null) {
                    playerPetRow = new SQLPlayerPet(uuid);
                    playerPetRow.setAutoSpawn(true);
                    database().insert(playerPetRow);
                }
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
                plugin.getLogger().warning("Quest not found: " + row);
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
                plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            completedQuests.put(questName, row);
        }
        for (SQLPlayerPetUnlock row : cache.playerPetUnlockRows) {
            PetType petType = row.parsePetType();
            if (petType == null) {
                plugin.getLogger().warning("Pet type not found: " + row);
                continue;
            }
            unlockedPets.put(petType, row);
        }
        for (DailyQuest dailyQuest : plugin.getDailyQuests().getDailyQuests()) {
            if (!dailyQuest.isActive()) continue;
            loadDailyQuest(dailyQuest);
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

    public void openCompletedQuestBook(Player player, Quest quest, SQLCompletedQuest row) {
        List<Component> pages = new ArrayList<>();
        pages.add(textOfChildren((text()
                                  .append(quest.name.displayName)
                                  .color(DARK_AQUA)
                                  .decorate(BOLD)
                                  .build()),
                                 newline(),
                                 text(quest.getName().type.upper + " ", GRAY),
                                 (DefaultFont.BACK_BUTTON.component
                                  .clickEvent(runCommand("/tutor menu"))
                                  .hoverEvent(showText(text("Open Tutor Menu", BLUE)))),
                                 text("\n\nCompleted\n", GRAY),
                                 text(dateFormat.format(row.getTime()), DARK_AQUA),
                                 text("\n\n"),
                                 (DefaultFont.START_BUTTON.component
                                  .clickEvent(runCommand("/tutor click redo " + quest.getName().key))
                                  .hoverEvent(showText(join(separator(newline()), new Component[] {
                                                  text("Repeat this " + quest.getName().type.lower, BLUE),
                                                  text("There will not be", GRAY),
                                                  text("any extra rewards.", GRAY),
                                              }))))));
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

    public void triggerQuestReminder() {
        if (!currentQuests.isEmpty()) return;
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
        final int size = 4 * 9;
        final Gui gui = new Gui().size(size);
        gui.setOverlay(GuiOverlay.BLANK.builder(size, section.backgroundColor)
                       .layer(GuiOverlay.TOP_BAR, section.backgroundColor)
                       .title(section.title));
        List<MenuSection> sectionList = new ArrayList<>(List.of(MenuSection.values()));
        if (pet == null) sectionList.remove(MenuSection.PET);
        final int menuOffset = 4 - (sectionList.size() / 2);
        for (int i = 0; i < sectionList.size(); i += 1) {
            MenuSection menuSection = sectionList.get(i);
            gui.setItem(menuOffset + i, menuSection.createIcon(this), click -> {
                    if (!click.isLeftClick()) return;
                    Noise.CLICK.play(player);
                    openMenu(player, menuSection);
                });
            if (section == menuSection) {
                gui.getOverlay().highlightSlot(menuOffset + i, section.backgroundColor);
            }
        }
        section.makeGui(gui, player, this);
        gui.setItem(Gui.OUTSIDE, null, click -> Noise.FAIL.play(player));
        gui.open(player);
    }

    private boolean canSee(QuestName questName) {
        for (QuestName dep : questName.getSeeDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        if (questName.getStartPermission() != null && !Perm.get().has(uuid, questName.getStartPermission())) {
            return false;
        }
        return true;
    }

    private boolean canStart(QuestName questName) {
        for (QuestName dep : questName.getStartDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        return true;
    }

    protected void makeTutorialMenu(Gui gui, Player player) {
        int index = 0;
        for (QuestName questName : QuestName.values()) {
            if (!completedQuests.containsKey(questName) && !canSee(questName)) continue;
            gui.setItem(9 + index++, makeQuestItem(questName), click -> {
                    if (!click.isLeftClick()) return;
                    if (currentQuests.containsKey(questName)) {
                        Noise.CLICK.play(player);
                        openQuestBook(player);
                        return;
                    }
                    if (completedQuests.containsKey(questName)) {
                        Noise.CLICK.play(player);
                        openCompletedQuestBook(player, plugin.getQuests().get(questName), completedQuests.get(questName));
                        return;
                    }
                    if (!canSee(questName) || !canStart(questName)) {
                        Noise.FAIL.play(player);
                        return;
                    }
                    if (!currentQuests.isEmpty()) {
                        Noise.FAIL.play(player);
                        QuestName active = currentQuests.keySet().iterator().next();
                        player.sendMessage(text("You already have an active " + active.type.lower + "!", RED));
                        return;
                    }
                    Noise.CLICK.play(player);
                    startQuest(questName);
                    openMenu(player);
                });
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
            text.add(text("Completed", GOLD));
        } else if (canStart(questName)) {
            item = Mytems.CHECKBOX.createIcon();
            text.add(text("Start this " + questName.type.lower + "?", GOLD));
        } else {
            item = Mytems.COPPER_KEYHOLE.createIcon();
            text.add(text("Locked", DARK_RED));
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
        Items.text(item, text);
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
        List<Gui.Slot> slots = List.of(new Gui.Slot[] {
                // Pet Type
                Gui.Slot.of(petType.mytems.createIcon(),
                            List.of(text("Choose Pet", GREEN)),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                openPetTypeSelectionMenu(player);
                            }),
                // Spawn
                Gui.Slot.of(Mytems.STAR.createIcon(),
                            List.of(text().content("Spawn ").color(GREEN)
                                    .append(petName).build()),
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
                            }),
                // Auto Respawn
                Gui.Slot.of(on ? Mytems.OK.createIcon() : Mytems.NO.createIcon(),
                            List.of(playerPetRow.isAutoSpawn()
                                    ? text("Auto Respawn Enabled", GREEN)
                                    : text("Auto Respawn Disabled", RED)),
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
                            }),
                // Name
                Gui.Slot.of(new ItemStack(Material.NAME_TAG),
                            List.of(text("Change Name", GREEN)),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                player.closeInventory();
                                player.sendMessage(text("\n  Click here to change the name of your pet\n", GREEN, BOLD)
                                                   .clickEvent(suggestCommand("/tutor rename "))
                                                   .hoverEvent(showText(text("/tutor rename", YELLOW))));
                            }),
                // Gender
                Gui.Slot.of(petGender.itemStack,
                            List.of(petGender.component),
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
                            }),
            });
        gui.setSlots(indexes, slots);
        gui.getOverlay().title(playerPetRow.getNameComponent());
    }

    protected void makeDailyQuestGui(Gui gui, Player player) {
        List<PlayerDailyQuest> visibleDailies = getVisibleDailies();
        final int offset = 4 - (visibleDailies.size() / 2);
        for (int i = 0; i < visibleDailies.size(); i += 1) {
            PlayerDailyQuest playerDailyQuest = visibleDailies.get(i);
            final ItemStack icon;
            final DailyQuest dailyQuest = playerDailyQuest.getDailyQuest();
            if (playerDailyQuest.isComplete()) {
                icon = Mytems.CHECKED_CHECKBOX.createIcon();
            } else {
                icon = dailyQuest.createIcon(playerDailyQuest);
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
            }
            Items.text(icon, text);
            gui.setItem(offset + i, 2, icon, click -> {
                    if (!click.isLeftClick()) return;
                    Noise.CLICK.play(player);
                    openDailyQuestBook(player, dailyQuest, playerDailyQuest);
                });
        }
    }

    public void openDailyQuestBook(Player player, DailyQuest dailyQuest, PlayerDailyQuest playerDailyQuest) {
        List<Component> text = new ArrayList<>();
        text.add(DefaultFont.BACK_BUTTON.component
                 .hoverEvent(showText(text("Go back", BLUE)))
                 .clickEvent(runCommand("/daily")));
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
        Gui gui = new Gui();
        PetType[] allTypes = PetType.values();
        int rows = (allTypes.length - 1) / 9 + 1;
        int size = rows * 9;
        gui.withOverlay(size, color(0x008888), text("Choose a Pet", WHITE));
        for (int index = 0; index < size; index += 1) {
            PetType petType = index < allTypes.length ? allTypes[index] : null;
            if (petType != null && petType.unlocked || unlockedPets.containsKey(petType)) {
                gui.setSlot(index,
                            Gui.Slot.of(petType.mytems.createIcon(),
                                        List.of(petType.displayName.color(GREEN)),
                                        click -> {
                                            if (!click.isLeftClick()) return;
                                            Noise.CLICK.play(player);
                                            setPetType(petType);
                                            if (pet != null && petType != pet.getType()) {
                                                pet.setType(petType);
                                                pet.despawn();
                                            }
                                            openMenu(player, MenuSection.PET);
                                        }));
            } else {
                // petType may be null!
                gui.setSlot(index,
                            Gui.Slot.of(Mytems.QUESTION_MARK.createIcon(),
                                        List.of(text("???", DARK_RED),
                                                text("Not yet unlocked", DARK_GRAY)),
                                        click -> {
                                            if (!click.isLeftClick()) return;
                                            Noise.FAIL.play(player);
                                        }));
            }
        }
        gui.setOutsideClick(click -> {
                Noise.CLICK.play(player);
                openMenu(player, MenuSection.PET);
            });
        gui.open(player);
    }

    public boolean hasPet() {
        return pet != null;
    }

    protected void expireDailyQuests(final int newDayId) {
        for (PlayerDailyQuest playerDailyQuest : List.copyOf(dailyQuests)) {
            if (playerDailyQuest.getDailyQuest().getDayId() == newDayId) continue;
            playerDailyQuest.saveAsync();
            dailyQuests.remove(playerDailyQuest);
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
                Bukkit.getScheduler().runTask(plugin, playerDailyQuest::enable);
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
        for (PlayerQuest playerQuest : getQuestList()) {
            if (playerQuest.getQuest().getName().type == QuestType.TUTORIAL) {
                lines.add(textOfChildren(text(tiny("your "), AQUA), text("/tut", YELLOW), text(tiny("orial"), AQUA)));
            } else {
                lines.add(textOfChildren(text(tiny("your "), AQUA), text("/q", YELLOW), text(tiny("uest"), AQUA)));
            }
            lines.addAll(playerQuest.getCurrentGoal().getSidebarLines(playerQuest));
            break;
        }
        List<PlayerDailyQuest> visibleDailies = getVisibleDailies();
        int unfinished = 0;
        for (var it : visibleDailies) {
            if (!it.isComplete()) unfinished += 1;
        }
        if (unfinished > 0) {
            lines.add(text(tiny("dailies (" + visibleDailies.size() + ")"), AQUA));
            for (PlayerDailyQuest playerDailyQuest : visibleDailies) {
                lines.addAll(playerDailyQuest.getDailyQuest().getSidebarLines(playerDailyQuest));
            }
        }
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
}
