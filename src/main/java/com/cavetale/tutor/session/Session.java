package com.cavetale.tutor.session;

import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.Quest;
import com.cavetale.tutor.QuestName;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.Constraint;
import com.cavetale.tutor.goal.Goal;
import com.cavetale.tutor.pet.Noise;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.sql.SQLCompletedQuest;
import com.cavetale.tutor.sql.SQLPlayerPet;
import com.cavetale.tutor.sql.SQLPlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
    protected SQLPlayerPet playerPetRow = null;
    protected boolean ready;
    protected boolean disabled;
    private final List<Runnable> deferredCallbacks = new ArrayList<>();
    protected Pet pet;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");

    protected Session(final Sessions sessions, final Player player) {
        this.plugin = sessions.plugin;
        this.sessions = sessions;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    protected void load() {
        plugin.getDatabase().scheduleAsyncTask(() -> {
                List<SQLPlayerQuest> playerQuestRows = plugin.getDatabase().find(SQLPlayerQuest.class)
                    .eq("player", uuid).findList();
                List<SQLCompletedQuest> completedQuestRows = plugin.getDatabase().find(SQLCompletedQuest.class)
                    .eq("player", uuid).findList();
                playerPetRow = plugin.getDatabase().find(SQLPlayerPet.class).eq("player", uuid).findUnique();
                if (playerPetRow == null) {
                    playerPetRow = new SQLPlayerPet(uuid);
                    playerPetRow.setAutoSpawn(true);
                    plugin.getDatabase().insert(playerPetRow);
                }
                Bukkit.getScheduler().runTask(plugin, () -> enable(playerQuestRows, completedQuestRows));
            });
    }

    private void enable(List<SQLPlayerQuest> playerQuestRows, List<SQLCompletedQuest> completedQuestRows) {
        if (!sessions.enabled || disabled) return;
        spawnPet();
        for (SQLPlayerQuest row : playerQuestRows) {
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
        for (SQLCompletedQuest row : completedQuestRows) {
            QuestName questName = QuestName.of(row.getQuest());
            if (questName == null) {
                plugin.getLogger().warning("Quest not found: " + row);
                continue;
            }
            completedQuests.put(questName, row);
        }
        ready = true;
        for (Runnable callback : deferredCallbacks) {
            callback.run();
        }
        deferredCallbacks.clear();
        triggerAutomaticQuests();
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
            plugin.getDatabase().insertAsync(newRow, null);
            questName.deliverQuestReward(player);
        }
        triggerAutomaticQuests();
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
        pages.add(TextComponent.ofChildren(new Component[] {
                    (Component.text()
                     .append(quest.getDisplayName())
                     .color(NamedTextColor.DARK_AQUA)
                     .decorate(TextDecoration.BOLD)
                     .build()),
                    Component.newline(),
                    Component.text(quest.getName().type.upper + " ", NamedTextColor.GRAY),
                    (Component.text().content("[Back]")
                     .color(NamedTextColor.BLUE)
                     .clickEvent(ClickEvent.runCommand("/tutor menu"))
                     .hoverEvent(HoverEvent.showText(Component.text("Open Tutor Menu", NamedTextColor.BLUE)))
                     .build()),
                    Component.text("\n\nCompleted\n", NamedTextColor.GRAY),
                    Component.text(dateFormat.format(row.getTime()), NamedTextColor.DARK_AQUA),
                    Component.text("\n\n"),
                    (Component.text().content("[Repeat]")
                     .color(NamedTextColor.BLUE)
                     .clickEvent(ClickEvent.runCommand("/tutor redo " + quest.getName().key))
                     .hoverEvent(HoverEvent.showText(TextComponent.ofChildren(new Component[] {
                                     Component.text("Repeat this " + quest.getName().type.lower, NamedTextColor.BLUE),
                                     Component.text("\nThere will not be", NamedTextColor.GRAY),
                                     Component.text("\nany extra rewards.", NamedTextColor.GRAY),
                             })))
                     .build()),
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
            if (questName.autoStartPermission == null) continue;
            if (currentQuests.containsKey(questName)) continue;
            if (completedQuests.containsKey(questName)) continue;
            if (!player.isPermissionSet(questName.autoStartPermission)) continue;
            if (!player.hasPermission(questName.autoStartPermission)) continue;
            startQuest(questName);
            return;
        }
        for (QuestName questName : QuestName.values()) {
            if (!completedQuests.containsKey(questName) && canSee(questName)) {
                if (pet != null) {
                    pet.addSpeechBubble(60L, 150L,
                                        Component.text("There are more"),
                                        Component.text(questName.type.lower + "s waiting"),
                                        Component.text("for you!"));
                    pet.addSpeechBubble(150L,
                                        Component.text("Click me or type"),
                                        Component.text("/tutor", NamedTextColor.YELLOW));
                }
                return;
            }
        }
    }

    /**
     * Create the pet object and prepare it to be spawned.
     */
    public Pet spawnPet() {
        PetType petType = playerPetRow.parsePetType();
        if (petType == null) return null; // must not have finished beginner tut
        if (pet != null && pet.isValid() && pet.getType() != petType) {
            pet.despawn();
        }
        if (pet == null) {
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

    public void setPet(PetType petType, boolean autoSpawn) {
        playerPetRow.setPetType(petType);
        playerPetRow.setAutoSpawn(autoSpawn);
        playerPetRow.setNow();
        plugin.getDatabase().updateAsync(playerPetRow, null, "pet", "auto_spawn");
    }

    public void renamePet(String petName) {
        playerPetRow.setName(petName);
        if (pet != null) {
            pet.setCustomName(playerPetRow.getNameComponent());
        }
        plugin.getDatabase().updateAsync(playerPetRow, null, "name");
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
            openPetMenu(player);
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

    public void openPetMenu(Player player) {
        if (pet == null) return;
        int size = 3 * 9;
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.BLUE, playerPetRow.getNameComponent());
        // Pet Item
        ItemStack petItem = pet.getType().icon.createIcon();
        petItem.editMeta(meta -> {
                meta.displayName(playerPetRow.getNameComponent());
                meta.lore(Arrays.asList(new Component[] {
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
                meta.lore(Arrays.asList(new Component[] {
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
        for (QuestName dep : questName.dependencies) {
            if (!completedQuests.containsKey(dep)) return false;
        }
        return true;
    }

    public void openQuestsMenu(Player player) {
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.BLUE, playerPetRow.getNameComponent());
        // Current Quest
        if (!currentQuests.isEmpty()) {
            PlayerQuest playerQuest = currentQuests.values().iterator().next();
            ItemStack currentQuestIcon = new ItemStack(Material.WRITABLE_BOOK);
            currentQuestIcon.editMeta(meta -> {
                    meta.displayName(playerQuest.getQuest().getDisplayName());
                    meta.lore(Arrays.asList(new Component[] {
                                Component.text("Current " + playerQuest.getQuest().getName().type.upper, NamedTextColor.YELLOW),
                            }));
                    meta.addItemFlags(ItemFlag.values());
                });
            gui.setItem(0 + 4, currentQuestIcon, click -> {
                    if (!click.isLeftClick()) return;
                    Noise.CLICK.play(player);
                    openQuestBook(player);
                });
        }
        // Completed Quest List
        int index = 0;
        for (QuestName questName : QuestName.values()) {
            if (currentQuests.containsKey(questName)) {
                continue;
            } else if (completedQuests.containsKey(questName)) {
                Quest quest = plugin.getQuests().get(questName);
                ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
                item.editMeta(meta -> {
                        meta.displayName(quest.getDisplayName());
                        meta.lore(Arrays.asList(new Component[] {
                                    Component.text("Completed", NamedTextColor.GRAY),
                                }));
                        meta.addItemFlags(ItemFlag.values());
                    });
                gui.setItem(9 + index++, item, click -> {
                        if (!click.isLeftClick()) return;
                        Noise.CLICK.play(player);
                        openCompletedQuestBook(player, quest, completedQuests.get(questName));
                    });
            } else if (canSee(questName)) {
                Quest quest = plugin.getQuests().get(questName);
                ItemStack item = Mytems.STAR.createIcon();
                item.editMeta(meta -> {
                        meta.displayName(quest.getDisplayName());
                        meta.lore(Arrays.asList(new Component[] {
                                    Component.text("Start this " + questName.type.lower + "?", NamedTextColor.GRAY),
                                }));
                    });
                gui.setItem(9 + index++, item, click -> {
                        if (!click.isLeftClick()) return;
                        Noise.CLICK.play(player);
                        if (!currentQuests.isEmpty()) {
                            QuestName active = currentQuests.keySet().iterator().next();
                            player.sendMessage(Component.text("You already have an active " + active.type.lower + "!", NamedTextColor.RED));
                            return;
                        }
                        if (!currentQuests.containsKey(questName)) {
                            startQuest(quest);
                        }
                        player.closeInventory();
                    });
            }
        }
        //
        gui.setItem(Gui.OUTSIDE, null, click -> {
                Noise.CLICK.play(player);
                openPetMenu(player);
            });
        gui.open(player);
    }

    public void openPetSettingsMenu(Player player) {
        if (pet == null) return;
        int size = 3 * 9;
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.BLUE, playerPetRow.getNameComponent());
        // Auto Spawn
        boolean on = playerPetRow.isAutoSpawn();
        ItemStack autoSpawnItem = on
            ? Mytems.OK.createIcon()
            : Mytems.NO.createIcon();
        autoSpawnItem.editMeta(meta -> {
                meta.displayName(on
                                 ? Component.text("Auto Respawn Enabled", NamedTextColor.GREEN)
                                 : Component.text("Auto Respawn Disabled", NamedTextColor.RED));
            });
        gui.setItem(9 + 2, autoSpawnItem, click -> {
                if (!click.isLeftClick()) return;
                if (on == playerPetRow.isAutoSpawn()) {
                    Noise.CLICK.play(player);
                    playerPetRow.setAutoSpawn(!on);
                    pet.setAutoRespawn(!on);
                    plugin.getDatabase().updateAsync(playerPetRow, null, "auto_spawn");
                }
                openPetSettingsMenu(player);
            });
        // Name
        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        nameItem.editMeta(meta -> {
                meta.displayName(Component.text("Change Name", NamedTextColor.GREEN));
            });
        gui.setItem(9 + 6, nameItem, click -> {
                if (!click.isLeftClick()) return;
                Noise.CLICK.play(player);
                player.closeInventory();
                player.sendMessage(Component.text().content("\n  Click here to change the name of your pet\n")
                                   .color(NamedTextColor.BLUE)
                                   .decorate(TextDecoration.BOLD)
                                   .clickEvent(ClickEvent.suggestCommand("/tutor rename "))
                                   .hoverEvent(HoverEvent.showText(Component.text("/tutor rename", NamedTextColor.YELLOW))));
            });
        //
        gui.setItem(Gui.OUTSIDE, null, click -> {
                Noise.CLICK.play(player);
                openPetMenu(player);
            });
        gui.open(player);
    }

    public boolean hasPet() {
        return pet != null;
    }
}
