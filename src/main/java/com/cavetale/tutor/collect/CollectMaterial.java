package com.cavetale.tutor.collect;

import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Match any item with the right material, as long as it is not marked
 * as a Mytem.
 */
public final class CollectMaterial extends CollectItem {
    protected final Material material;

    public CollectMaterial(final String key, final Material material, final int totalAmount) {
        super(CollectItemType.MATERIAL, key, totalAmount);
        this.material = material;
    }

    public CollectMaterial(final Material material, final int totalAmount) {
        this("material:" + material.getKey().getKey(), material, totalAmount);
    }

    public CollectMaterial(final Material material) {
        this(material, 1);
    }

    @Override
    public Component getDisplayName() {
        return textOfChildren(VanillaItems.componentOf(material), translatable(new ItemStack(material)));
    }

    @Override
    public boolean matchItemStack(ItemStack itemStack) {
        return itemStack.getType() == material && Mytems.forItem(itemStack) == null;
    }

    @Override
    protected ItemStack makeBaseIcon() {
        return new ItemStack(material);
    }

    @Override
    public ItemStack createItemStack(int amount) {
        return new ItemStack(material, totalAmount);
    }
}
