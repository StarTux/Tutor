package com.cavetale.tutor.collect;

import com.cavetale.core.font.VanillaItems;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Match an exact ItemStack.
 */
public final class CollectItemStack extends CollectItem {
    protected final ItemStack itemStack;

    public CollectItemStack(final String key, final ItemStack itemStack, final int totalAmount) {
        super(CollectItemType.ITEM_STACK, key, totalAmount);
        this.itemStack = itemStack;
    }

    public CollectItemStack(final ItemStack itemStack, final int totalAmount) {
        this("item:" + itemStack.getType().getKey().getKey(), itemStack, totalAmount);
    }

    public CollectItemStack editMeta(Consumer<ItemMeta> consumer) {
        itemStack.editMeta(consumer);
        return this;
    }

    @Override
    public Component getDisplayName() {
        return textOfChildren(VanillaItems.componentOf(itemStack.getType()), translatable(itemStack));
    }

    @Override
    public boolean matchItemStack(ItemStack theItemStack) {
        return itemStack.isSimilar(theItemStack);
    }

    @Override
    protected ItemStack makeBaseIcon() {
        return itemStack.clone();
    }

    @Override
    public ItemStack createItemStack(int amount) {
        return itemStack.asQuantity(amount);
    }
}
