package com.cavetale.tutor.daily;

import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
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
        COW("Cows", 5, Mytems.COW_FACE, Set.of(EntityType.COW), List.of(Material.WHEAT)),
        GOAT("Goats", 5, Mytems.GOAT_FACE, Set.of(EntityType.GOAT), List.of(Material.WHEAT)),
        SHEEP("Sheeps", 5, Mytems.SHEEP_FACE, Set.of(EntityType.SHEEP), List.of(Material.WHEAT)),
        PIG("Pigs", 5, Mytems.PIG_FACE, Set.of(EntityType.PIG), List.of(Material.CARROT)),
        CHICKEN("Chickens", 5, Mytems.CHICKEN_FACE, Set.of(EntityType.CHICKEN),
                List.of(Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS,
                        Material.MELON_SEEDS, Material.PUMPKIN_SEEDS)),
        RABBIT("Rabbits", 5, Mytems.BROWN_RABBIT_FACE, Set.of(EntityType.RABBIT),
               List.of(Material.DANDELION, Material.CARROT, Material.GOLDEN_CARROT)),
        HORSE("Horse", 1, Mytems.CREAMY_HORSE_FACE, Set.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE),
              List.of(Material.GOLDEN_APPLE, Material.GOLDEN_CARROT)),
        WOLF("Wolf", 1, Mytems.WOLF_FACE, Set.of(EntityType.WOLF),
             List.of(Material.CHICKEN, Material.COOKED_CHICKEN,
                     Material.PORKCHOP, Material.BEEF,
                     Material.RABBIT, Material.COOKED_PORKCHOP,
                     Material.COOKED_BEEF, Material.ROTTEN_FLESH,
                     Material.COOKED_MUTTON, Material.COOKED_RABBIT)),
        CAT("Cat", 1, Mytems.OCELOT_FACE, Set.of(EntityType.CAT, EntityType.OCELOT), List.of(Material.COD, Material.SALMON)),
        AXOLOTL("Axolotl", 1, Mytems.CYAN_AXOLOTL_FACE, Set.of(EntityType.AXOLOTL), List.of(Material.TROPICAL_FISH_BUCKET)),
        LLAMA("Llama", 1, Mytems.CREAMY_LLAMA_FACE, Set.of(EntityType.LLAMA, EntityType.TRADER_LLAMA), List.of(Material.HAY_BLOCK)),
        PANDA("Pandas", 3, Mytems.PANDA_FACE, Set.of(EntityType.PANDA), List.of(Material.BAMBOO)),
        FOX("Foxes", 3, Mytems.FOX_FACE, Set.of(EntityType.FOX), List.of(Material.SWEET_BERRIES, Material.GLOW_BERRIES)),
        STRIDER("Strider", 1, Mytems.STRIDER_FACE, Set.of(EntityType.STRIDER), List.of(Material.WARPED_FUNGUS)),
        HOGLIN("Hoglin", 1, Mytems.HOGLIN_FACE, Set.of(EntityType.HOGLIN), List.of(Material.CRIMSON_FUNGUS)),
        FROG("Frogs", 3, Mytems.TEMPERATE_FROG_FACE, Set.of(EntityType.FROG), List.of(Material.SLIME_BALL)),
        // CAMEL("Camel Baby", 1, Mytems.CAMEL_FACE, Set.of(EntityType.CAMEL), List.of(Material.CACTUS)), // 1.20!
        ;

        public final String displayName;
        public final int total;
        public final Mytems chatIcon;
        public final Set<EntityType> entityTypes;
        public final List<Material> breedMaterials;
    }

    @Override
    public void onGenerate(final int index) {
        BreedMob[] mobs = BreedMob.values();
        details.mob = mobs[index];
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

    @Override
    protected List<ItemStack> generateRewards() {
        List<ItemStack> result = new ArrayList<>();
        Material material = details.mob.breedMaterials.get(random.nextInt(details.mob.breedMaterials.size()));
        int amount = Math.min(material.getMaxStackSize(), total * 2);
        for (int i = 0; i < 3; i += 1) {
            result.add(new ItemStack(material, amount));
        }
        return result;
    }

    protected void onEntityBreed(Player player, PlayerDailyQuest playerDailyQuest, EntityBreedEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        if (!details.mob.entityTypes.contains(event.getEntity().getType())) return;
        makeProgress(playerDailyQuest, 1);
    }

    protected void onEntityFertilizeEgg(Player player, PlayerDailyQuest playerDailyQuest, EntityFertilizeEggEvent event) {
        if (!checkGameModeAndSurvivalServer(player)) return;
        Set<EntityType> types = details.mob.entityTypes;
        if (types.contains(event.getMother().getType()) || types.contains(event.getFather().getType())) {
            makeProgress(playerDailyQuest, 1);
        }
    }

    public static final class Details extends DailyQuest.Details {
        BreedMob mob = BreedMob.COW;
    }
}
