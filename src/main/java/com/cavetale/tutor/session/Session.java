package com.cavetale.tutor.session;

import com.cavetale.core.connect.ServerGroup;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.perm.Perm;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.TutorPlugin;
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
import com.cavetale.tutor.util.Items;
import com.winthier.playercache.PlayerCache;
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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
    protected SQLPlayerPet playerPetRow = null;
    protected boolean ready;
    protected boolean disabled;
    private final List<Runnable> deferredCallbacks = new ArrayList<>();
    protected Pet pet;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
    private Cache cache;

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
    }

    protected void loadAsync(Runnable callback) {
        plugin.getDatabase().scheduleAsyncTask(() -> {
                this.cache = new Cache();
                cache.playerQuestRows = plugin.getDatabase().find(SQLPlayerQuest.class)
                    .eq("player", uuid).findList();
                cache.completedQuestRows = plugin.getDatabase().find(SQLCompletedQuest.class)
                    .eq("player", uuid).findList();
                cache.playerPetUnlockRows = plugin.getDatabase().find(SQLPlayerPetUnlock.class)
                    .eq("player", uuid).findList();
                this.playerPetRow = plugin.getDatabase().find(SQLPlayerPet.class).eq("player", uuid).findUnique();
                if (playerPetRow == null) {
                    playerPetRow = new SQLPlayerPet(uuid);
                    playerPetRow.setAutoSpawn(true);
                    plugin.getDatabase().insert(playerPetRow);
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
        return playerQuest;
    }

    public void questComplete(QuestName questName, Player player) {
        removeQuest(questName);
        if (!completedQuests.containsKey(questName)) {
            SQLCompletedQuest newRow = new SQLCompletedQuest(uuid, questName);
            completedQuests.put(questName, newRow);
            plugin.getDatabase().insertAsync(newRow, r -> {
                    if (r != 0 && ServerGroup.current() != ServerGroup.MUSEUM) {
                        Perm.get().addLevelProgress(player.getUniqueId());
                    }
                });
        }
        triggerAutomaticQuests();
        triggerQuestReminder();
    }

    public PlayerQuest removeQuest(QuestName questName) {
        PlayerQuest playerQuest = currentQuests.remove(questName);
        if (playerQuest != null) {
            playerQuest.disable();
            plugin.getDatabase().deleteAsync(playerQuest.getRow(), null);
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
            pages.add(Component.text("No quests to show!", NamedTextColor.DARK_RED));
        } else {
            for (PlayerQuest playerQuest : quests) {
                pages.addAll(playerQuest.getCurrentGoal().getBookPages(playerQuest));
            }
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Quests");
        meta.author(Component.text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
        player.openBook(itemStack);
    }

    public void openCompletedQuestBook(Player player, Quest quest, SQLCompletedQuest row) {
        List<Component> pages = new ArrayList<>();
        pages.add(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    (Component.text()
                     .append(quest.name.displayName)
                     .color(NamedTextColor.DARK_AQUA)
                     .decorate(TextDecoration.BOLD)
                     .build()),
                    Component.newline(),
                    Component.text(quest.getName().type.upper + " ", NamedTextColor.GRAY),
                    (DefaultFont.BACK_BUTTON.component
                     .clickEvent(ClickEvent.runCommand("/tutor menu"))
                     .hoverEvent(HoverEvent.showText(Component.text("Open Tutor Menu", NamedTextColor.BLUE)))),
                    Component.text("\n\nCompleted\n", NamedTextColor.GRAY),
                    Component.text(dateFormat.format(row.getTime()), NamedTextColor.DARK_AQUA),
                    Component.text("\n\n"),
                    (DefaultFont.START_BUTTON.component
                     .clickEvent(ClickEvent.runCommand("/tutor click redo " + quest.getName().key))
                     .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                                     Component.text("Repeat this " + quest.getName().type.lower, NamedTextColor.BLUE),
                                     Component.text("There will not be", NamedTextColor.GRAY),
                                     Component.text("any extra rewards.", NamedTextColor.GRAY),
                                 })))),
                }));
        for (Goal goal : quest.getGoals()) {
            pages.addAll(goal.getAdditionalBookPages());
        }
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        meta.addPages(pages.toArray(new Component[0]));
        meta.setTitle("Tutor");
        meta.author(Component.text("Cavetale"));
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        itemStack.setItemMeta(meta);
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
                                        Component.text("There is another"),
                                        Component.text(questName.type.lower + " waiting"),
                                        Component.text("for you!"));
                    pet.addSpeechBubble("session", 0L, 150L,
                                        Component.text("Click me or type"),
                                        Component.text(questName.type.command, NamedTextColor.YELLOW));
                } else {
                    getPlayer().sendMessage(Component.text()
                                            .content("There is another " + questName.type.lower
                                                     + " waiting for you! Type ")
                                            .append(Component.text(questName.type.command, NamedTextColor.YELLOW))
                                            .color(NamedTextColor.AQUA)
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
        plugin.getDatabase().updateAsync(playerPetRow, null, "pet", "updated");
    }

    public void renamePet(String petName) {
        playerPetRow.setName(petName);
        playerPetRow.setNow();
        if (pet != null) {
            pet.setCustomName(playerPetRow.getNameComponent());
        }
        plugin.getDatabase().updateAsync(playerPetRow, null, "name", "updated");
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
            overviewMenu(player);
        }
    }

    private void onPetDespawn() {
        if (disabled) return;
        Player player = getPlayer();
        if (player == null) return;
        if (!playerPetRow.isAutoSpawn()) {
            Component msg = Component.text()
                .append(playerPetRow.getNameComponent())
                .append(Component.text(" despawned. Bring it back via "))
                .append(Component.text("/tutor", NamedTextColor.YELLOW))
                .color(NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand("/tutor"))
                .hoverEvent(HoverEvent.showText(Component.text("/tutor", NamedTextColor.YELLOW)))
                .build();
            player.sendMessage(msg);
        }
    }

    public void overviewMenu(Player player) {
        if (pet == null) return;
        int size = 3 * 9;
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.AQUA, playerPetRow.getNameComponent());
        // Pet Item
        ItemStack petItem = pet.getType().mytems.createIcon();
        petItem.editMeta(meta -> {
                meta.displayName(playerPetRow.getNameComponent());
                meta.lore(List.of(new Component[] {
                            Component.text("Access Pet Options", NamedTextColor.GRAY),
                        }));
            });
        gui.setItem(9 + 5, petItem, click -> {
                if (!click.isLeftClick()) return;
                Noise.CLICK.play(player);
                openPetSettingsMenu(player);
            });
        // Quests Item
        ItemStack questsItem = new ItemStack(Material.WRITTEN_BOOK);
        questsItem.editMeta(meta -> {
                meta.displayName(Component.text("Tutorials", NamedTextColor.YELLOW));
                meta.lore(List.of(new Component[] {
                            Component.text("Tutorial Menu", NamedTextColor.GRAY),
                        }));
                meta.addItemFlags(ItemFlag.values());
            });
        gui.setItem(9 + 3, questsItem, click -> {
                if (!click.isLeftClick()) return;
                Noise.CLICK.play(player);
                openQuestsMenu(player);
            });
        gui.setItem(Gui.OUTSIDE, null, click -> {
                Noise.CLICK.play(player);
                player.closeInventory();
            });
        gui.open(player);
    }

    private boolean canSee(QuestName questName) {
        for (QuestName dep : questName.getSeeDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        return true;
    }

    private boolean canStart(QuestName questName) {
        for (QuestName dep : questName.getStartDependencies()) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        return true;
    }

    public void openQuestsMenu(Player player) {
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, TextColor.color(0x400000), Component.text("Quests", NamedTextColor.WHITE));
        // Current Quest
        if (!currentQuests.isEmpty()) {
            QuestName questName = currentQuests.keySet().iterator().next();
            gui.setItem(0 + 4, makeQuestItem(questName), click -> {
                    if (!click.isLeftClick()) return;
                    Noise.CLICK.play(player);
                    openQuestBook(player);
                });
        }
        // Completed Quest List
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
                        player.sendMessage(Component.text("You already have an active " + active.type.lower + "!", NamedTextColor.RED));
                        return;
                    }
                    Noise.CLICK.play(player);
                    startQuest(questName);
                    player.closeInventory();
                });
        }
        //
        gui.setItem(Gui.OUTSIDE, null, click -> {
                Noise.CLICK.play(player);
                overviewMenu(player);
            });
        gui.open(player);
    }

    protected ItemStack makeQuestItem(QuestName questName) {
        Quest quest = plugin.getQuests().get(questName);
        final ItemStack item;
        final List<Component> text = new ArrayList<>();
        text.add(quest.name.displayName);
        if (currentQuests.containsKey(questName)) {
            item = new ItemStack(Material.WRITABLE_BOOK);
            text.add(Component.text("Current Quest", NamedTextColor.GOLD));
        } else if (completedQuests.containsKey(questName)) {
            item = new ItemStack(Material.WRITTEN_BOOK);
            text.add(Component.text("Completed", NamedTextColor.GOLD));
        } else if (canStart(questName)) {
            item = Mytems.STAR.createIcon();
            text.add(Component.text("Start this " + questName.type.lower + "?", NamedTextColor.GOLD));
        } else {
            item = new ItemStack(Material.CHEST);
            text.add(Component.text("Locked", NamedTextColor.DARK_RED));
        }
        if (!questName.getDescription().isEmpty()) {
            text.add(Component.empty());
            text.addAll(questName.getDescription());
        }
        if (!questName.getStartDependencies().isEmpty()) {
            text.add(Component.empty());
            text.add(Component.text(questName.type.upper + " Requirements", NamedTextColor.GRAY));
            for (QuestName dependency : questName.getStartDependencies()) {
                if (completedQuests.containsKey(dependency)) {
                    text.add(Component.text(Unicode.CHECKED_CHECKBOX.character + " ", NamedTextColor.GRAY)
                             .append(dependency.displayName));
                } else {
                    text.add(Component.text(Unicode.CHECKBOX.character + " ", NamedTextColor.DARK_GRAY)
                             .append(dependency.displayName));
                }
            }
        }
        Items.text(item, text);
        return item;
    }

    public void openPetSettingsMenu(Player player) {
        if (pet == null) return;
        List<Integer> indexes = List.of(0 + 4,
                                        9 + 1, 9 + 3, 9 + 5, 9 + 7);
        final boolean on = playerPetRow.isAutoSpawn();
        Component petName = playerPetRow.getNameComponent();
        PetType petType = playerPetRow.parsePetType();
        PetGender petGender = playerPetRow.getGender();
        List<Gui.Slot> slots = List.of(new Gui.Slot[] {
                // Pet Type
                Gui.Slot.of(petType.mytems.createIcon(),
                            List.of(Component.text("Choose Pet", NamedTextColor.GREEN)),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                openPetTypeSelectionMenu(player);
                            }),
                // Spawn
                Gui.Slot.of(Mytems.STAR.createIcon(),
                            List.of(Component.text().content("Spawn ").color(NamedTextColor.GREEN)
                                    .append(petName).build()),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                if (!pet.tryToSpawn(player, SpawnRule.LOOKAT)) {
                                    if (!pet.tryToSpawn(player, SpawnRule.NEARBY)) {
                                        player.sendMessage(Component.text("Could not spawn your pet!",
                                                                          NamedTextColor.RED));
                                    }
                                }
                                player.sendMessage(Component.text().append(petName)
                                                   .append(Component.text(" appeared!"))
                                                   .color(NamedTextColor.GREEN));
                                pet.setAutoDespawn(false);
                            }),
                // Auto Respawn
                Gui.Slot.of(on ? Mytems.OK.createIcon() : Mytems.NO.createIcon(),
                            List.of(playerPetRow.isAutoSpawn()
                                    ? Component.text("Auto Respawn Enabled", NamedTextColor.GREEN)
                                    : Component.text("Auto Respawn Disabled", NamedTextColor.RED)),
                            click -> {
                                if (!click.isLeftClick()) return;
                                if (on == playerPetRow.isAutoSpawn()) {
                                    Noise.CLICK.play(player);
                                    playerPetRow.setAutoSpawn(!on);
                                    playerPetRow.setNow();
                                    pet.setAutoRespawn(!on);
                                    plugin.getDatabase().updateAsync(playerPetRow, null, "auto_spawn", "updated");
                                }
                                openPetSettingsMenu(player);
                            }),
                // Name
                Gui.Slot.of(new ItemStack(Material.NAME_TAG),
                            List.of(Component.text("Change Name", NamedTextColor.GREEN)),
                            click -> {
                                if (!click.isLeftClick()) return;
                                Noise.CLICK.play(player);
                                player.closeInventory();
                                player.sendMessage(Component.text().content("\n  Click here to change the name of your pet\n")
                                                   .color(NamedTextColor.BLUE)
                                                   .decorate(TextDecoration.BOLD)
                                                   .clickEvent(ClickEvent.suggestCommand("/tutor rename "))
                                                   .hoverEvent(HoverEvent.showText(Component.text("/tutor rename",
                                                                                                  NamedTextColor.YELLOW))));
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
                                plugin.getDatabase().updateAsync(playerPetRow, null, "gender", "updated");
                                if (pet != null) {
                                    pet.setCustomName(playerPetRow.getNameComponent());
                                }
                                openPetSettingsMenu(player);
                            }),
            });
        int size = 3 * 9;
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.DARK_AQUA, petName);
        gui.setSlots(indexes, slots);
        gui.setOutsideClick(click -> {
                Noise.CLICK.play(player);
                overviewMenu(player);
            });
        gui.open(player);
    }

    public void openPetTypeSelectionMenu(Player player) {
        Gui gui = new Gui();
        PetType[] allTypes = PetType.values();
        int rows = (allTypes.length - 1) / 9 + 1;
        int size = rows * 9;
        gui.withOverlay(size, TextColor.color(0x008888), Component.text("Choose a Pet", NamedTextColor.WHITE));
        for (int index = 0; index < size; index += 1) {
            PetType petType = index < allTypes.length ? allTypes[index] : null;
            if (petType != null && petType.unlocked || unlockedPets.containsKey(petType)) {
                gui.setSlot(index,
                            Gui.Slot.of(petType.mytems.createIcon(),
                                        List.of(petType.displayName.color(NamedTextColor.GREEN)),
                                        click -> {
                                            if (!click.isLeftClick()) return;
                                            Noise.CLICK.play(player);
                                            setPetType(petType);
                                            if (pet != null && petType != pet.getType()) {
                                                pet.setType(petType);
                                                pet.despawn();
                                            }
                                            openPetSettingsMenu(player);
                                        }));
            } else {
                // petType may be null!
                gui.setSlot(index,
                            Gui.Slot.of(Mytems.QUESTION_MARK.createIcon(),
                                        List.of(Component.text("???", NamedTextColor.DARK_RED),
                                                Component.text("Not yet unlocked", NamedTextColor.DARK_GRAY)),
                                        click -> {
                                            if (!click.isLeftClick()) return;
                                            Noise.FAIL.play(player);
                                        }));
            }
        }
        gui.setOutsideClick(click -> {
                Noise.CLICK.play(player);
                openPetSettingsMenu(player);
            });
        gui.open(player);
    }

    public boolean hasPet() {
        return pet != null;
    }
}
