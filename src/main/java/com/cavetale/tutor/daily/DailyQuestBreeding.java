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
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

/**
 * Breed two entities.
 */
public final class DailyQuestBreeding extends DailyQuest<DailyQuestBreeding.Details, DailyQuest.Progress> {
    public DailyQuestBreeding() {
        super(DailyQuestType.BREEDING,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @RequiredArgsConstructor
    public enum BreedMob {
        COW("Cows", 5, Mytems.COW_FACE, Set.of(EntityType.COW)),
        GOAT("Goats", 5, Mytems.GOAT_FACE, Set.of(EntityType.GOAT)),
        SHEEP("Sheeps", 5, Mytems.SHEEP_FACE, Set.of(EntityType.SHEEP)),
        PIG("Pigs", 5, Mytems.PIG_FACE, Set.of(EntityType.PIG)),
        CHICKEN("Chickens", 5, Mytems.CHICKEN_FACE, Set.of(EntityType.CHICKEN)),
        RABBIT("Rabbits", 5, Mytems.BROWN_RABBIT_FACE, Set.of(EntityType.RABBIT)),
        HORSE("Horse", 1, Mytems.CREAMY_HORSE_FACE, Set.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE)),
        WOLF("Wolf", 1, Mytems.WOLF_FACE, Set.of(EntityType.WOLF)),
        CAT("Cat", 1, Mytems.OCELOT_FACE, Set.of(EntityType.CAT, EntityType.OCELOT)),
        AXOLOTL("Axolotl", 1, Mytems.CYAN_AXOLOTL_FACE, Set.of(EntityType.AXOLOTL)),
        LLAMA("Llama", 1, Mytems.CREAMY_LLAMA_FACE, Set.of(EntityType.LLAMA, EntityType.TRADER_LLAMA)),
        PANDA("Pandas", 3, Mytems.PANDA_FACE, Set.of(EntityType.PANDA)),
        FOX("Foxes", 3, Mytems.FOX_FACE, Set.of(EntityType.FOX)),
        STRIDER("Strider", 1, Mytems.STRIDER_FACE, Set.of(EntityType.STRIDER)),
        HOGLIN("Hoglin", 1, Mytems.HOGLIN_FACE, Set.of(EntityType.HOGLIN)),
        FROG("Frogs", 3, Mytems.TEMPERATE_FROG_FACE, Set.of(EntityType.FROG)),
        // CAMEL("Camel Baby", 1, Mytems.CAMEL_FACE, Set.of(EntityType.CAMEL)), // 1.20!
        ;

        public final String displayName;
        public final int total;
        public final Mytems chatIcon;
        public final Set<EntityType> entityTypes;
    }

    @Override
    public void onGenerate() {
        BreedMob[] mobs = BreedMob.values();
        details.mob = mobs[ThreadLocalRandom.current().nextInt(mobs.length)];
        this.total = details.mob.total;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Breed " + total + Unicode.MULTIPLICATION.string),
                              details.mob.chatIcon);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Breed " + total + " "),
                              details.mob.chatIcon,
                              text(" " + details.mob.displayName + " in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return details.mob.chatIcon.createIcon(total);
    }

    protected void onEntityBreed(Player player, PlayerDailyQuest playerDailyQuest, EntityBreedEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!details.mob.entityTypes.contains(event.getEntity().getType())) return;
        makeProgress(playerDailyQuest, 1);
    }

    public static final class Details extends DailyQuest.Details {
        BreedMob mob = BreedMob.COW;
    }
}
