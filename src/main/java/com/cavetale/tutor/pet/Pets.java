package com.cavetale.tutor.pet;

import com.cavetale.core.event.entity.PluginEntityEvent;
import com.cavetale.tutor.TutorPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Pets runtime manager.
 */
@RequiredArgsConstructor
public final class Pets implements Listener {
    protected final TutorPlugin plugin;
    protected final Map<Integer, Pet> petMap = new HashMap<>();
    // updated by Pet
    protected final Map<Integer, Pet> entityPetMap = Collections.synchronizedMap(new HashMap<>());
    protected final Map<Integer, Pet> armorStandMap = new HashMap<>();
    private final Map<UUID, List<Pet>> ownerPetMap = new HashMap<>();
    private int nextPetId = 0;
    private static final PacketType[] ENTITY_PACKETS = {
        PacketType.Play.Server.ANIMATION,
        PacketType.Play.Server.ATTACH_ENTITY,
        PacketType.Play.Server.BLOCK_BREAK_ANIMATION,
        PacketType.Play.Server.COLLECT,
        PacketType.Play.Server.ENTITY_EFFECT,
        PacketType.Play.Server.ENTITY_EQUIPMENT,
        PacketType.Play.Server.ENTITY_HEAD_ROTATION,
        PacketType.Play.Server.ENTITY_LOOK,
        PacketType.Play.Server.ENTITY_METADATA,
        PacketType.Play.Server.ENTITY_STATUS,
        PacketType.Play.Server.ENTITY_TELEPORT,
        PacketType.Play.Server.ENTITY_VELOCITY,
        PacketType.Play.Server.NAMED_ENTITY_SPAWN,
        PacketType.Play.Server.REL_ENTITY_MOVE,
        PacketType.Play.Server.REMOVE_ENTITY_EFFECT,
        PacketType.Play.Server.SPAWN_ENTITY,
        PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
        PacketType.Play.Server.SPAWN_ENTITY_LIVING,
        PacketType.Play.Server.SPAWN_ENTITY_PAINTING,
        PacketType.Play.Server.ENTITY_DESTROY,
    };

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        PacketAdapter packetAdapter = new PacketAdapter(plugin, ENTITY_PACKETS) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY) {
                        for (int entityId : event.getPacket().getIntLists().read(0)) {
                            if (shouldCancelPacket(entityId, event)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    } else {
                        int entityId = event.getPacket().getIntegers().read(0);
                        if (shouldCancelPacket(entityId, event)) {
                            event.setCancelled(true);
                        }
                    }
                }
            };
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
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
            entityPetMap.put(pet.entity.getEntityId(), pet);
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
        pets = new ArrayList<>(pets);
        for (Pet pet : pets) {
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

    private boolean shouldCancelPacket(int entityId, PacketEvent event) {
        Pet pet = entityPetMap.get(entityId);
        if (pet == null) {
            pet = armorStandMap.get(entityId);
        }
        if (pet == null) return false;
        if (!pet.exclusive) return false;
        if (pet.ownerId.equals(event.getPlayer().getUniqueId())) return false;
        return true;
    }

    @EventHandler
    void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        int entityId = event.getEntity().getEntityId();
        Pet pet = entityPetMap.remove(entityId);
        if (pet != null) {
            pet.entity = null;
            return;
        }
        armorStandMap.remove(entityId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityDamage(EntityDamageEvent event) {
        Pet pet = entityPetMap.get(event.getEntity().getEntityId());
        if (pet == null) return;
        event.setCancelled(true);
        pet.despawn();
        pet.autoRespawnCooldown = System.currentTimeMillis() + 10000L;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityCombust(EntityCombustEvent event) {
        Pet pet = entityPetMap.get(event.getEntity().getEntityId());
        if (pet != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onEntityPortal(EntityPortalEvent event) {
        Pet pet = entityPetMap.get(event.getEntity().getEntityId());
        if (pet != null) event.setCancelled(true);
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
        Player player = event.getPlayer();
        for (Pet pet : findPets(player)) {
            pet.onOwnerMove(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Pet pet = entityPetMap.get(event.getRightClicked().getEntityId());
        if (pet == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!Objects.equals(player.getUniqueId(), pet.ownerId)) return;
        if (pet.onClick != null) pet.onClick.run();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityMove(EntityMoveEvent event) {
        int entityId = event.getEntity().getEntityId();
        Pet pet = entityPetMap.get(entityId);
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
        if (event.getPlugin() == plugin) return;
        int entityId = event.getEntity().getEntityId();
        Pet pet = entityPetMap.get(entityId);
        if (pet == null) return;
        event.setCancelled(true);
    }
}
