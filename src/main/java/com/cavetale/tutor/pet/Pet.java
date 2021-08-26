package com.cavetale.tutor.pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Cat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.util.RayTraceResult;

@Getter @RequiredArgsConstructor
public final class Pet {
    @NonNull protected final Pets pets;
    @NonNull protected final UUID ownerId;
    protected final int petId;
    @Setter protected String tag;
    @Setter protected PetType type;
    @Setter protected boolean exclusive;
    @Setter protected boolean autoRespawn;
    @Setter protected AutoRespawnRule autoRespawnRule = AutoRespawnRule.AREA;
    @Setter protected boolean collidable;
    @Setter protected double ownerDistance;
    protected Component customName;
    @Setter protected boolean customNameVisible;
    @Setter protected Runnable onClick;
    @Setter protected Runnable onDespawn;
    protected LivingEntity entity;
    private long tickCooldown;
    protected long autoRespawnCooldown;
    protected long moveToCooldown;
    private int moveToFails;
    protected SpeechBubble currentSpeechBubble;
    private List<SpeechBubble> speechBubbleQueue = new ArrayList<>();

    public void spawn(Location location) {
        despawn();
        switch (Objects.requireNonNull(type)) {
        case CAT:
            entity = location.getWorld().spawn(location, Cat.class, cat -> {
                    prepLivingEntity(cat);
                    cat.setCollarColor(DyeColor.LIGHT_BLUE);
                    cat.setCatType(Cat.Type.BLACK);
                });
            break;
        case DOG:
            entity = location.getWorld().spawn(location, Wolf.class, dog -> {
                    prepLivingEntity(dog);
                    dog.setCollarColor(DyeColor.LIGHT_BLUE);
                });
            break;
        default:
            throw new IllegalStateException("type=" + type);
        }
        triggerSpeechBubble();
    }

    public void teleport(Location location) {
        if (entity == null) {
            spawn(location);
            return;
        }
        entity.teleport(location);
    }

    public void despawn() {
        if (entity != null) {
            pets.entityPetMap.remove(entity.getEntityId()); // necessary?
            entity.remove();
            entity = null;
        }
        if (currentSpeechBubble != null) {
            currentSpeechBubble.disable(); // calls triggerSpeechBubble()
        }
        if (onDespawn != null) onDespawn.run();
    }

    public Player getOwner() {
        return Bukkit.getPlayer(ownerId);
    }

    /**
     * Attempt to trigger the next speech bubble. Do nothing if
     * anything makes this impossible (for now).
     */
    protected void triggerSpeechBubble() {
        if (currentSpeechBubble != null) return;
        if (entity == null) return;
        if (speechBubbleQueue.isEmpty()) return;
        currentSpeechBubble = speechBubbleQueue.remove(0);
        currentSpeechBubble.enable();
        for (int i = 0; i < 5; i += 1) {
            Bukkit.getScheduler().runTaskLater(pets.plugin, () -> {
                    if (!isSpawned() || !isValid()) return;
                    Player owner = Bukkit.getPlayer(ownerId);
                    type.voice.play(owner, entity.getLocation());
                }, (long) i * 8L);
        }
    }

    public void addSpeechBubble(long lifetime, Component... lines) {
        SpeechBubble speechBubble = new SpeechBubble(this);
        speechBubble.setLines(lifetime, lines);
        speechBubbleQueue.add(speechBubble);
        if (currentSpeechBubble == null) {
            triggerSpeechBubble();
        }
    }

    public void resetSpeechBubbles() {
        speechBubbleQueue.clear();
        if (currentSpeechBubble != null) {
            currentSpeechBubble.disable();
        }
    }

    public boolean isSpawned() {
        return entity != null;
    }

    public void setCustomName(Component component) {
        this.customName = component;
        if (entity != null) {
            entity.customName(component);
        }
    }

    private void prepLivingEntity(LivingEntity living) {
        pets.entityPetMap.put(living.getEntityId(), this);
        living.setPersistent(false);
        living.setSilent(true);
        living.setCollidable(collidable);
        living.customName(customName);
        living.setCustomNameVisible(customNameVisible);
        if (living instanceof Mob) {
            Mob mob = (Mob) living;
            Bukkit.getMobGoals().removeAllGoals(mob);
        }
        if (living instanceof Tameable) {
            Tameable tameable = (Tameable) living;
            tameable.setTamed(true);
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null) tameable.setOwner(owner);
        }
    }

    protected void onEntityMove() {
        if (currentSpeechBubble != null) {
            currentSpeechBubble.updateLocations();
        }
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            Player owner = Bukkit.getPlayer(ownerId);
            Location mobLocation = mob.getLocation();
            Location ownerLocation = owner.getLocation();
            if (!Objects.equals(mobLocation.getWorld(), ownerLocation.getWorld())) {
                despawn();
            } else if (mobLocation.distance(ownerLocation) < ownerDistance) {
                mob.getPathfinder().stopPathfinding();
                if (entity instanceof Sittable) {
                    Sittable sittable = (Sittable) entity;
                    sittable.setSitting(true);
                }
                mob.lookAt(owner.getEyeLocation());
            }
        }
    }

    protected void onOwnerMove(@NonNull Player owner) {
        switch (owner.getGameMode()) {
        case SURVIVAL: case ADVENTURE: break;
        default: return;
        }
        long now = System.currentTimeMillis();
        if (now < tickCooldown) return;
        tickCooldown = now + 100L;
        if (entity != null) {
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                Location mobLocation = mob.getLocation();
                Location ownerLocation = owner.getLocation();
                if (!Objects.equals(mobLocation.getWorld(), ownerLocation.getWorld())) {
                    despawn();
                    return;
                }
                double distance = mobLocation.distance(ownerLocation);
                if (mobLocation.distance(ownerLocation) > 16.0) {
                    despawn();
                    return;
                }
                if (now >= moveToCooldown) {
                    if (distance > ownerDistance) {
                        if (entity instanceof Sittable) {
                            Sittable sittable = (Sittable) entity;
                            sittable.setSitting(false);
                        }
                        boolean moveToResult = mob.getPathfinder().moveTo(owner);
                        if (moveToResult) {
                            moveToCooldown = now + 1000L;
                            moveToFails = 0;
                        } else {
                            moveToFails += 1;
                            if (moveToFails > 10) {
                                despawn();
                            }
                        }
                    } else {
                        mob.getPathfinder().stopPathfinding();
                        if (entity instanceof Sittable) {
                            Sittable sittable = (Sittable) entity;
                            sittable.setSitting(true);
                        }
                        mob.lookAt(owner.getEyeLocation());
                        moveToCooldown = now + 1000L;
                    }
                }
            }
        } else { // if (entity == null) {
            if (autoRespawn) {
                if (now < autoRespawnCooldown) return;
                tryToAutoRespawn(owner);
                if (entity != null) {
                    autoRespawnCooldown = now + 10000L;
                } else {
                    autoRespawnCooldown = now + 100L;
                }
            }
        }
    }

    private void tryToAutoRespawn(Player owner) {
        switch (autoRespawnRule) {
        case LOOKAT: {
            RayTraceResult rayTraceResult = owner.rayTraceBlocks(6.0);
            if (rayTraceResult == null) return;
            Block block = rayTraceResult.getHitBlock();
            if (block == null) return;
            BlockFace blockFace = rayTraceResult.getHitBlockFace();
            if (blockFace == null) return;
            if (blockFace.getModY() < 0) return;
            Block spawnBlock = block.getRelative(blockFace);
            if (!spawnBlock.isEmpty()) return;
            if (!spawnBlock.getRelative(BlockFace.DOWN).isSolid()) return;
            Location spawnLocation = spawnBlock.getLocation().add(0.5, 0.0, 0.5);
            for (Pet otherPet : pets.findPets(owner)) {
                if (otherPet.entity != null && otherPet.entity.getBoundingBox().contains(spawnLocation.toVector())) {
                    return;
                }
            }
            spawn(spawnLocation);
            return;
        }
        case AREA: {
            Random random = ThreadLocalRandom.current();
            Block base = owner.getLocation().getBlock();
            final int r = 8;
            for (int i = 0; i < 10; i += 1) {
                int dx = random.nextBoolean() ? random.nextInt(r) : -random.nextInt(r);
                int dz = random.nextBoolean() ? random.nextInt(r) : -random.nextInt(r);
                Block spawnBlock = base.getRelative(dx, 0, dz);
                for (int j = 0; j < 4; j += 1) {
                    if (!spawnBlock.isEmpty()) break;
                    spawnBlock = spawnBlock.getRelative(BlockFace.DOWN);
                }
                for (int j = 0; j < 4; j += 1) {
                    if (spawnBlock.isEmpty()) break;
                    spawnBlock = spawnBlock.getRelative(BlockFace.UP);
                }
                if (!spawnBlock.isEmpty()) continue;
                if (!spawnBlock.getRelative(BlockFace.DOWN).isSolid()) continue;
                Location spawnLocation = spawnBlock.getLocation().add(0.5, 0.0, 0.5);
                spawn(spawnLocation);
                return;
            }
            return;
        }
        default:
            throw new IllegalStateException("autoRespawnRule=" + autoRespawnRule);
        }
    }

    public boolean isValid() {
        return pets.petMap.containsKey(this.petId);
    }
}
