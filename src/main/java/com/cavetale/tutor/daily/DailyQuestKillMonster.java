package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestKillMonster extends DailyQuest<DailyQuestKillMonster.Details, DailyQuest.Progress> {
    public DailyQuestKillMonster() {
        super(DailyQuestType.KILL_MONSTER,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @RequiredArgsConstructor
    /**
     * The reward amount will be multiplied with the mob amount.
     */
    public enum TargetMob {
        CREEPER("Creepers", 5, () -> new ItemStack(Material.GUNPOWDER, 5),
                Mytems.CREEPER_FACE, Set.of(EntityType.CREEPER)),
        ENDERMAN("Endermen", 3, () -> new ItemStack(Material.ENDER_PEARL, 5),
                 Mytems.ENDERMAN_FACE, Set.of(EntityType.ENDERMAN)),
        GHAST("Ghasts", 2, () -> new ItemStack(Material.GHAST_TEAR, 2),
              Mytems.GHAST_FACE, Set.of(EntityType.GHAST)),
        SKELETON("Skeleton", 5, () -> new ItemStack(Material.BONE, 5),
                 Mytems.SKELETON_FACE, Set.of(EntityType.SKELETON, EntityType.WITHER_SKELETON, EntityType.STRAY)),
        SLIME("Slimes", 7, () -> new ItemStack(Material.SLIME_BALL, 3),
              Mytems.SLIME_FACE, Set.of(EntityType.SLIME)),
        SPIDER("Spider", 5, () -> new ItemStack(Material.STRING, 5),
               Mytems.SPIDER_FACE, Set.of(EntityType.SPIDER, EntityType.CAVE_SPIDER)),
        PILLAGER("Pillager", 1, () -> new ItemStack(Material.ARROW, 5),
                 Mytems.PILLAGER_FACE, Set.of(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER)),
        ZOMBIE("Zombie", 5, () -> new ItemStack(Material.IRON_INGOT, 3),
               Mytems.ZOMBIE_FACE, Set.of(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK)),
        WITCH("Witches", 3, () -> new ItemStack(Material.GLOWSTONE_DUST, 3),
              Mytems.WITCH_FACE, Set.of(EntityType.WITCH)),
        BLAZE("Blazes", 3, () -> new ItemStack(Material.BLAZE_POWDER, 5),
              Mytems.BLAZE_FACE, Set.of(EntityType.BLAZE)),
        PIGLIN("Piglins", 3, () -> new ItemStack(Material.GOLD_INGOT, 5),
               Mytems.PIGLIN_FACE, Set.of(EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE)),
        GUARDIAN("Guardians", 3, () -> new ItemStack(Material.SPONGE, 5),
                 Mytems.GUARDIAN_FACE, Set.of(EntityType.GUARDIAN)),
        PHANTOM("Phantom", 1, () -> new ItemStack(Material.PHANTOM_MEMBRANE, 5),
                Mytems.PHANTOM_FACE, Set.of(EntityType.PHANTOM)),
        MAGMA_CUBE("Magma Cubes", 5, () -> new ItemStack(Material.MAGMA_CREAM, 5),
                   Mytems.MAGMA_CUBE_FACE, Set.of(EntityType.MAGMA_CUBE)),
        WITHER_SKELETON("Wither Skeletons", 3, () -> new ItemStack(Material.COAL, 5),
                        Mytems.WITHER_SKELETON_FACE, Set.of(EntityType.WITHER_SKELETON)),
        ;

        public final String displayName;
        public final int total;
        public final Supplier<ItemStack> rewardSupplier;
        public final Mytems chatIcon;
        public final Set<EntityType> entityTypes;
    }

    @Override
    public void onGenerate() {
        TargetMob[] targets = TargetMob.values();
        details.target = targets[random.nextInt(targets.length)];
        this.total = details.target.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Kill " + total + Unicode.MULTIPLICATION.string),
                              details.target.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Kill " + total + " "),
                              details.target.chatIcon,
                              text(" " + details.target.displayName + " in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        ItemStack result = details.target.chatIcon.createIcon();
        result.setAmount(total);
        return result;
    }

    protected void onEntityDeath(Player player, PlayerDailyQuest playerDailyQuest, EntityDeathEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!details.target.entityTypes.contains(event.getEntity().getType())) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        ItemStack result = details.target.rewardSupplier.get();
        result.setAmount(Math.min(result.getType().getMaxStackSize(), result.getAmount() * total));
        return List.of(result);
    }

    public static final class Details extends DailyQuest.Details {
        TargetMob target = TargetMob.CREEPER;
    }
}
