package com.cavetale.tutor.util;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.util.Items;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.tutor.TutorPlugin.plugin;

public final class Gui implements InventoryHolder {
    public static final int OUTSIDE = -999;
    private Inventory inventory;
    private Map<Integer, Slot> slots = new HashMap<>();
    private Consumer<InventoryCloseEvent> onClose = null;
    private Consumer<InventoryOpenEvent> onOpen = null;
    @Getter @Setter private boolean editable = false;
    @Getter private int size = 3 * 9;
    @Getter private Component title = Component.empty();
    boolean locked = false;
    @Getter @Setter private GuiOverlay.Builder overlay;
    @Setter private Consumer<InventoryClickEvent> onClickBottom = null;

    @RequiredArgsConstructor
    public static final class Slot {
        protected final ItemStack item;
        protected final Consumer<InventoryClickEvent> onClick;

        public static Slot of(ItemStack theItem, Consumer<InventoryClickEvent> clickHandler) {
            return new Slot(theItem, clickHandler);
        }

        public static Slot of(ItemStack theItem, List<Component> text, Consumer<InventoryClickEvent> clickHandler) {
            Items.text(theItem, text);
            return new Slot(theItem, clickHandler);
        }
    }

    public Gui title(Component newTitle) {
        title = newTitle;
        return this;
    }

    public Gui size(int newSize) {
        if (newSize <= 0 || newSize % 9 != 0) {
            throw new IllegalArgumentException("newSize=" + newSize);
        }
        size = newSize;
        return this;
    }

    public Gui rows(int rowCount) {
        if (rowCount <= 0) throw new IllegalArgumentException("rowCount=" + rowCount);
        size = rowCount * 9;
        return this;
    }

    public Gui withOverlay(final int theSize, TextColor color, Component theTitle) {
        return size(theSize).title(DefaultFont.guiBlankOverlay(theSize, color, theTitle));
    }

    public Inventory getInventory() {
        if (inventory == null) {
            if (overlay != null) title = overlay.build();
            inventory = Bukkit.getServer().createInventory(this, size, title);
            for (int i = 0; i < size; i += 1) {
                Slot slot = slots.get(i);
                if (slot != null) {
                    inventory.setItem(i, slot.item);
                }
            }
        }
        return inventory;
    }

    public ItemStack getItem(int index) {
        if (index < 0) index = OUTSIDE;
        Slot slot = slots.get(index);
        return slot != null
            ? slot.item
            : null;
    }

    public void setItem(int index, ItemStack item) {
        setItem(index, item, null);
    }

    public void setItem(int index, ItemStack item, Consumer<InventoryClickEvent> responder) {
        if (inventory != null && index >= 0 && inventory.getSize() > index) {
            inventory.setItem(index, item);
        }
        if (index < 0) index = OUTSIDE;
        Slot slot = new Slot(item, responder);
        slots.put(index, slot);
    }

    public void setSlot(int index, Slot slot) {
        this.slots.put(index, slot);
    }

    public void setSlots(List<Integer> indexList, List<Slot> slotList) {
        if (indexList.size() != slotList.size()) {
            throw new IllegalArgumentException(indexList.size() + "!=" + slotList.size());
        }
        for (int i = 0; i < indexList.size(); i += 1) {
            this.slots.put(indexList.get(i), slotList.get(i));
        }
    }

    public void setItem(int column, int row, ItemStack item, Consumer<InventoryClickEvent> responder) {
        if (column < 0 || column > 8) {
            throw new IllegalArgumentException("column=" + column);
        }
        if (row < 0) throw new IllegalArgumentException("row=" + row);
        setItem(column + row * 9, item, responder);
    }

    public void setOutsideClick(Consumer<InventoryClickEvent> clickHandler) {
        this.slots.put(OUTSIDE, new Slot(null, clickHandler));
    }

    public Gui open(Player player) {
        player.openInventory(getInventory());
        return this;
    }

    public Gui reopen(Player player) {
        player.closeInventory();
        inventory = null;
        player.openInventory(getInventory());
        return this;
    }

    public Gui onClose(Consumer<InventoryCloseEvent> responder) {
        onClose = responder;
        return this;
    }

    public Gui onOpen(Consumer<InventoryOpenEvent> responder) {
        onOpen = responder;
        return this;
    }

    public Gui clear() {
        if (inventory != null) inventory.clear();
        slots.clear();
        onOpen = null;
        onClose = null;
        return this;
    }

    void onInventoryOpen(InventoryOpenEvent event) {
        if (onOpen != null) {
            Bukkit.getScheduler().runTask(plugin(), () -> onOpen.accept(event));
        }
    }

    void onInventoryClose(InventoryCloseEvent event) {
        if (onClose != null) {
            Bukkit.getScheduler().runTask(plugin(), () -> onClose.accept(event));
        }
    }

    void onInventoryClick(InventoryClickEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
        if (locked) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != null
            && !inventory.equals(event.getClickedInventory())) {
            if (onClickBottom != null) {
                locked = true;
                Bukkit.getScheduler().runTask(plugin(), () -> {
                        locked = false;
                        onClickBottom.accept(event);
                    });
            }
            return;
        }
        Slot slot = slots.get(event.getSlot());
        if (slot != null && slot.onClick != null) {
            locked = true;
            Bukkit.getScheduler().runTask(plugin(), () -> {
                    locked = false;
                    slot.onClick.accept(event);
                });
        }
    }

    void onInventoryDrag(InventoryDragEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
    }

    @RequiredArgsConstructor
    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryOpen(final InventoryOpenEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryOpen(event);
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryClose(final InventoryCloseEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryClose(event);
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryClick(final InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryClick(event);
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryDrag(final InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryDrag(event);
            }
        }

        @EventHandler
        void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin() == plugin()) {
                Gui.disable();
            }
        }
    }

    public static Gui of(Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return null;
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) return null;
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof Gui)) return null;
        Gui gui = (Gui) holder;
        return gui;
    }

    public static void enable() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), plugin());
    }

    public static void disable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Gui gui = Gui.of(player);
            if (gui != null) player.closeInventory();
        }
    }
}
