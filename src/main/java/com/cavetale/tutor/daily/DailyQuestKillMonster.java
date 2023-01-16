package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
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
    public enum TargetMob {
        CREEPER("Creepers", 5, Mytems.CREEPER_FACE, Set.of(EntityType.CREEPER)),
        ENDERMAN("Endermen", 3, Mytems.ENDERMAN_FACE, Set.of(EntityType.ENDERMAN)),
        GHAST("Ghasts", 2, Mytems.GHAST_FACE, Set.of(EntityType.GHAST)),
        SKELETON("Skeleton", 5, Mytems.SKELETON_FACE, Set.of(EntityType.SKELETON, EntityType.WITHER_SKELETON, EntityType.STRAY)),
        SLIME("Slimes", 7, Mytems.SLIME_FACE, Set.of(EntityType.SLIME)),
        SPIDER("Spider", 5, Mytems.SPIDER_FACE, Set.of(EntityType.SPIDER, EntityType.CAVE_SPIDER)),
        PILLAGER("Pillager", 1, Mytems.PILLAGER_FACE, Set.of(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER)),
        ZOMBIE("Zombie", 5, Mytems.ZOMBIE_FACE, Set.of(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK)),
        WITCH("Witches", 3, Mytems.WITCH_FACE, Set.of(EntityType.WITCH)),
        BLAZE("Blazes", 3, Mytems.BLAZE_FACE, Set.of(EntityType.BLAZE)),
        PIGLIN("Piglins", 3, Mytems.PIGLIN_FACE, Set.of(EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE)),
        GUARDIAN("Guardians", 3, Mytems.GUARDIAN_FACE, Set.of(EntityType.GUARDIAN)),
        PHANTOM("Phantom", 1, Mytems.PHANTOM_FACE, Set.of(EntityType.PHANTOM)),
        MAGMA_CUBE("Magma Cubes", 5, Mytems.MAGMA_CUBE_FACE, Set.of(EntityType.MAGMA_CUBE)),
        WITHER_SKELETON("Wither Skeletons", 3, Mytems.WITHER_SKELETON_FACE, Set.of(EntityType.WITHER_SKELETON)),
        ;

        public final String displayName;
        public final int total;
        public final Mytems chatIcon;
        public final Set<EntityType> entityTypes;
    }

    @Override
    public void onGenerate() {
        TargetMob[] targets = TargetMob.values();
        details.target = targets[ThreadLocalRandom.current().nextInt(targets.length)];
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

    public static final class Details extends DailyQuest.Details {
        TargetMob target = TargetMob.CREEPER;
    }
}
