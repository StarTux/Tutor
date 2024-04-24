package com.cavetale.tutor.collect;

import com.cavetale.mytems.Mytems;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.subscript;
import static com.cavetale.core.font.Unicode.superscript;
import static com.cavetale.mytems.util.Items.tooltip;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Represent one item inside an item collection.  The subclass must be
 * able to check if a given item matches the desired collectible.
 */
@Getter @RequiredArgsConstructor
public abstract class CollectItem {
    protected final CollectItemType type;
    protected final String key;
    protected final int totalAmount;

    /**
     * Match an item in someone's inventory.
     */
    public boolean matchItemStack(ItemStack itemStack) {
        return false;
    }

    public abstract Component getDisplayName();

    protected abstract ItemStack makeBaseIcon();

    public abstract ItemStack createItemStack(int amount);

    /**
     * Make an icon for the GUI menu.
     */
    public ItemStack makeIcon(PlayerItemCollection collection) {
        int score = collection.getScore(this);
        boolean complete = score >= totalAmount;
        ItemStack icon = complete
            ? Mytems.CHECKED_CHECKBOX.createIcon()
            : makeBaseIcon();
        icon.editMeta(meta -> {
                List<Component> txt = new ArrayList<>();
                txt.add(getDisplayName());
                if (!collection.isComplete()) {
                    txt.add(text("Click an item in your", GRAY));
                    txt.add(text("inventory to put it into", GRAY));
                    txt.add(text("this collection.", GRAY));
                    txt.add(empty());
                    txt.add(textOfChildren(Mytems.MOUSE_LEFT, text(" Add 1", GRAY)));
                    txt.add(textOfChildren(Mytems.SHIFT_KEY, Mytems.MOUSE_LEFT, text(" Add Stack", GRAY)));
                    txt.add(textOfChildren(text("Progress ", GRAY), text(superscript(score) + "/" + subscript(totalAmount))));
                } else {
                    txt.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text(" " + superscript(score) + "/" + subscript(totalAmount), GREEN)));
                }
                tooltip(meta, txt);
            });
        icon.setAmount(Math.max(1, Math.min(64, totalAmount - score)));
        return icon;
    }
}
