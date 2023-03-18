package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
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
        CREEPER("Creepers", 10, rnd -> new ItemStack(Material.GUNPOWDER, 64),
                Mytems.CREEPER_FACE, Set.of(EntityType.CREEPER)),
        ENDERMAN("Endermen", 5, rnd -> new ItemStack(Material.ENDER_PEARL, 16),
                 Mytems.ENDERMAN_FACE, Set.of(EntityType.ENDERMAN)),
        GHAST("Ghasts", 3, rnd -> new ItemStack(Material.GHAST_TEAR, 16),
              Mytems.GHAST_FACE, Set.of(EntityType.GHAST)),
        SKELETON("Skeleton", 10, rnd -> new ItemStack(Material.BONE, 64),
                 Mytems.SKELETON_FACE, Set.of(EntityType.SKELETON, EntityType.WITHER_SKELETON, EntityType.STRAY)),
        SLIME("Slimes", 10, rnd -> new ItemStack(Material.SLIME_BALL, 64),
              Mytems.SLIME_FACE, Set.of(EntityType.SLIME)),
        SPIDER("Spider", 10, rnd -> new ItemStack((rnd.nextBoolean() ? Material.STRING : Material.SPIDER_EYE), 64),
               Mytems.SPIDER_FACE, Set.of(EntityType.SPIDER, EntityType.CAVE_SPIDER)),
        PILLAGER("Pillager", 1, rnd -> new ItemStack(Material.ARROW, 64),
                 Mytems.PILLAGER_FACE, Set.of(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER)),
        ZOMBIE("Zombie", 10, rnd -> new ItemStack(Material.IRON_INGOT, 64),
               Mytems.ZOMBIE_FACE, Set.of(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK)),
        WITCH("Witches", 3, rnd -> switch (rnd.nextInt(6)) {
            case 0 -> new ItemStack(Material.GLOWSTONE_DUST, 64);
            case 1 -> new ItemStack(Material.GLASS_BOTTLE, 64);
            case 2 -> new ItemStack(Material.REDSTONE, 64);
            case 3 -> new ItemStack(Material.GUNPOWDER, 64);
            case 4 -> new ItemStack(Material.SPIDER_EYE, 64);
            case 5 -> new ItemStack(Material.SUGAR, 64);
            default -> new ItemStack(Material.STICK, 64);
            }, Mytems.WITCH_FACE, Set.of(EntityType.WITCH)),
        BLAZE("Blazes", 10, rnd -> new ItemStack(Material.BLAZE_ROD, 64),
              Mytems.BLAZE_FACE, Set.of(EntityType.BLAZE)),
        PIGLIN("Piglins", 5, rnd -> new ItemStack(Material.GOLD_INGOT, 64),
               Mytems.PIGLIN_FACE, Set.of(EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE)),
        GUARDIAN("Guardians", 5, rnd -> new ItemStack(Material.WET_SPONGE, 64),
                 Mytems.GUARDIAN_FACE, Set.of(EntityType.GUARDIAN)),
        PHANTOM("Phantoms", 3, rnd -> new ItemStack(Material.PHANTOM_MEMBRANE, 16),
                Mytems.PHANTOM_FACE, Set.of(EntityType.PHANTOM)),
        MAGMA_CUBE("Magma Cubes", 10, rnd -> new ItemStack(Material.MAGMA_CREAM, 64),
                   Mytems.MAGMA_CUBE_FACE, Set.of(EntityType.MAGMA_CUBE)),
        WITHER_SKELETON("Wither Skeletons", 5, rnd -> rnd.nextBoolean()
                        ? new ItemStack(Material.COAL, 64)
                        : new ItemStack(Material.WITHER_SKELETON_SKULL, 3),
                        Mytems.WITHER_SKELETON_FACE, Set.of(EntityType.WITHER_SKELETON)),
        ;

        public final String displayName;
        public final int total;
        public final Function<Random, ItemStack> rewardsFunction;
        public final Mytems chatIcon;
        public final Set<EntityType> entityTypes;
    }

    @Override
    public void onGenerate() {
        TargetMob[] targets = TargetMob.values();
        details.target = targets[random.nextInt(targets.length)];
        this.total = details.target.total;
        if (total > 1) total += random.nextInt(3) * details.target.total;
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
        ItemStack result = details.target.rewardsFunction.apply(random);
        result.setAmount(Math.min(result.getType().getMaxStackSize(), result.getAmount() * total));
        return List.of(result);
    }

    public static final class Details extends DailyQuest.Details {
        TargetMob target = TargetMob.CREEPER;
    }
}
