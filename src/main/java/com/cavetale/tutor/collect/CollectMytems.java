package com.cavetale.tutor.collect;

import com.cavetale.mytems.Mytems;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Match any item with the given Mytems type.
 */
public final class CollectMytems extends CollectItem {
    protected final Mytems mytems;

    public CollectMytems(final String key, final Mytems mytems, final int totalAmount) {
        super(CollectItemType.MYTEMS, key, totalAmount);
        this.mytems = mytems;
    }

    public CollectMytems(final Mytems mytems, final int totalAmount) {
        this("mytems:" + mytems.id, mytems, totalAmount);
    }

    public CollectMytems(final Mytems mytems) {
        this(mytems, 1);
    }

    @Override
    public Component getDisplayName() {
        return textOfChildren(mytems, mytems.getMytem().getDisplayName());
    }

    @Override
    public boolean matchItemStack(ItemStack itemStack) {
        return mytems.isItem(itemStack);
    }

    @Override
    protected ItemStack makeBaseIcon() {
        return mytems.createIcon();
    }

    @Override
    public ItemStack createItemStack(int amount) {
        return mytems.createItemStack(amount);
    }
}
