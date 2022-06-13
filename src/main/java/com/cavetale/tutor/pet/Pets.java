package com.cavetale.tutor.pet;

import com.cavetale.core.event.entity.PluginEntityEvent;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.goal.MainServerConstraint;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.entity.TurtleLayEggEvent;
import com.destroystokyo.paper.event.entity.TurtleStartDiggingEvent;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PigZombieAngerEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Pets runtime manager.
 */
@RequiredArgsConstructor
public final class Pets implements Listener {
    protected final TutorPlugin plugin;
    private final Map<Integer, Pet> petMap = new TreeMap<>();
    private final Map<UUID, Pet> entityPetMap = new TreeMap<>();
    private final Map<UUID, List<Pet>> ownerPetMap = new HashMap<>();
    private int nextPetId = 0;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        for (Pet pet : petMap.values()) {
            pet.despawn();
        }
        petMap.clear();
        entityPetMap.clear();
        ownerPetMap.clear();
    }

    public Pet createPet(final Player owner, final PetType petType) {
        return createPet(owner.getUniqueId(), petType);
    }

    public Pet createPet(final UUID ownerId, final PetType petType) {
        Pet pet = new Pet(this, ownerId, nextPetId++);
        pet.setType(petType);
        addPet(pet);
        return pet;
    }

    public List<Pet> findPets(Player owner) {
        return findPets(owner.getUniqueId());
    }

    public List<Pet> findPets(UUID owner) {
        return ownerPetMap.computeIfAbsent(owner, u -> new ArrayList<>());
    }

    private void addPet(Pet pet) {
        petMap.put(pet.petId, pet);
        if (pet.ownerId != null) {
            findPets(pet.ownerId).add(pet);
        }
        if (pet.entity != null) {
            registerLivingEntity(pet.entity, pet);
        }
    }

    private void removePet(Pet pet) {
        pet.despawn();
        petMap.remove(pet.petId);
        if (pet.ownerId != null) {
            findPets(pet.ownerId).remove(pet);
        }
    }

    public void removeOwner(Player owner) {
        List<Pet> pets = ownerPetMap.remove(owner.getUniqueId());
        if (pets == null) return;
        for (Pet pet : pets) {
            pet.despawn();
            petMap.remove(pet.petId);
        }
    }

    /**
     * Remove all pets belonging to owner who have the given tag
     * assigned.
     */
    public void removeOwnerTag(UUID owner, String tag) {
        List<Pet> pets = findPets(owner);
        if (pets == null) return;
        for (Pet pet : List.copyOf(pets)) {
            if (Objects.equals(pet.getTag(), tag)) {
                removePet(pet);
            }
        }
    }

    private void despawnOwner(Player owner) {
        for (Pet pet : findPets(owner)) {
            pet.despawn();
        }
    }

    /**
     * Quick generic event checker. Cancels the event if entity is a
     * pet and event is not null.
     * @param entity the event entity
     * @param event the event (nullable)
     * @return The pet if the entity is a pet, null otherwise.
     */
    protected Pet handleEventEntity(Entity entity, Cancellable event) {
        Pet pet = entityPetMap.get(entity.getUniqueId());
        if (pet == null) return null;
        if (event != null) event.setCancelled(true);
        return pet;
    }

    @EventHandler
    void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        Pet pet = entityPetMap.remove(event.getEntity().getUniqueId());
        if (pet != null && Objects.equals(pet.entity, event.getEntity())) {
            pet.entity = null;
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityDamage(EntityDamageEvent event) {
        Pet pet = handleEventEntity(event.getEntity(), event);
        if (pet == null) return;
        if (pet.autoRespawn || pet.spawnOnce) {
            pet.autoRespawnCooldown = System.currentTimeMillis() + 10000L;
            if (pet.tag == null && event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent eedbee = (EntityDamageByEntityEvent) event;
                if (Objects.equals(pet.ownerId, eedbee.getDamager().getUniqueId())) {
                    pet.autoRespawnCooldown = System.currentTimeMillis() + 30000L;
                    pet.resetSpeechBubbles();
                }
            }
        } else {
            pet.autoRespawnCooldown = 0L;
        }
        pet.despawn();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityCombust(EntityCombustEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityPortal(EntityPortalEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        entityPetMap.forEach((uuid, pet) -> {
                if (pet.exclusive && !pet.isOwner(player)) {
                    player.hideEntity(plugin, Bukkit.getEntity(uuid));
                }
            });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        removeOwner(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.isDead()) return;
        despawnOwner(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        despawnOwner(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerTeleport(PlayerTeleportEvent event) {
        Location a = event.getFrom();
        Location b = event.getTo();
        if (!Objects.equals(a.getWorld(), b.getWorld()) || a.distanceSquared(b) > 16.0 * 16.0) {
            despawnOwner(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerMove(PlayerMoveEvent event) {
        if (!MainServerConstraint.isTrue()) return;
        Player player = event.getPlayer();
        for (Pet pet : findPets(player)) {
            pet.onOwnerMove(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Pet pet = handleEventEntity(event.getRightClicked(), event);
        if (pet == null) return;
        Player player = event.getPlayer();
        if (!Objects.equals(player.getUniqueId(), pet.ownerId)) return;
        if (pet.onClick != null) pet.onClick.run();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityMove(EntityMoveEvent event) {
        Pet pet = handleEventEntity(event.getEntity(), null); // do not cancel
        if (pet == null) return;
        pet.onEntityMove();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        switch (event.getNewGameMode()) {
        case SURVIVAL:
        case ADVENTURE:
            return;
        default:
            despawnOwner(event.getPlayer());
        }
    }

    @EventHandler
    void onPluginEntity(PluginEntityEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityInteract(EntityInteractEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityTarget(EntityTargetEvent event) {
        handleEventEntity(event.getEntity(), event);
        if (event.getTarget() != null) {
            handleEventEntity(event.getTarget(), event);
        }
    }

    @EventHandler
    protected void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        for (Iterator<LivingEntity> iter = event.getAffectedEntities().iterator(); iter.hasNext();) {
            if (handleEventEntity(iter.next(), null) != null) {
                iter.remove();
            }
        }
    }

    @EventHandler
    protected void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onProjectileCollide(ProjectileCollideEvent event) {
        handleEventEntity(event.getCollidedWith(), event);
    }

    @EventHandler
    protected void onTurtleLayEgg(TurtleLayEggEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onTurtleStartDigging(TurtleStartDiggingEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityInsideBlock(EntityInsideBlockEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityAirChange(EntityAirChangeEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityChangeBlock(EntityChangeBlockEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityDropItem(EntityDropItemEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityEnterBlock(EntityEnterBlockEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityEnterLoveMode(EntityEnterLoveModeEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityPickupItem(EntityPickupItemEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityPotionEffect(EntityPotionEffectEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onEntityTeleport(EntityTeleportEvent event) {
        Pet pet = handleEventEntity(event.getEntity(), null);
        if (pet != null && !pet.teleporting) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    protected void onFoodLevelChange(FoodLevelChangeEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onPigZombieAnger(PigZombieAngerEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onProjectileHit(ProjectileHitEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onSlimeSplit(SlimeSplitEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    @EventHandler
    protected void onStriderTemperatureChange(StriderTemperatureChangeEvent event) {
        handleEventEntity(event.getEntity(), event);
    }

    protected void registerLivingEntity(LivingEntity living, Pet pet) {
        entityPetMap.put(living.getUniqueId(), pet);
        if (pet.exclusive) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!pet.isOwner(player)) {
                    player.hideEntity(plugin, living);
                }
            }
        }
    }

    protected void registerArmorStand(ArmorStand armorStand, Pet pet) {
        registerLivingEntity(armorStand, pet);
    }
}
