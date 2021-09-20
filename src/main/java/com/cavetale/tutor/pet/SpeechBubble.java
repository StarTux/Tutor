package com.cavetale.tutor.pet;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

@Getter @RequiredArgsConstructor
public final class SpeechBubble {
    protected long created = System.currentTimeMillis();
    private final Pet pet;
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private boolean disabled;
    private List<Component> lines;
    private long lifetime;
    /** The tag exists so we remember who created the speech bubble,
        in case it must be removed. */
    @Setter private String tag;
    @Setter private long warmup;
    @Setter private Runnable onDisable;

    private Location getArmorStandLocation(int index, int total) {
        double offset = (double) (total - index - 1) * 0.24;
        return pet.entity.getLocation().add(0.0, pet.entity.getHeight() + 0.25 + offset, 0.0);
    }

    public void setLines(long theLifetime, Component... theLines) {
        this.lines = List.of(theLines);
        this.lifetime = theLifetime;
    }

    public void enable() {
        if (warmup > 0) {
            Bukkit.getScheduler().runTaskLater(pet.pets.plugin, () -> {
                    if (disabled) return;
                    enableNow();
                }, warmup);
        } else {
            enableNow();
        }
    }

    private void enableNow() {
        if (lines != null) {
            showLines();
            Player target = pet.getOwner();
            if (target != null) {
                Component wholeMessage = Component.text()
                    .append(Component.text().color(NamedTextColor.GRAY)
                            .append(pet.getType().mytems.component)
                            .append(pet.getCustomName())
                            .append(Component.text(": ")))
                    .append(Component.join(Component.space(), lines))
                    .build();
                target.sendMessage(wholeMessage);
            }
            Bukkit.getScheduler().runTaskLater(pet.pets.plugin, () -> {
                    if (!disabled) disable();
                }, lifetime);
        }
        final long noiseInterval = 8L;
        final long noiseAmount = Math.min(5, lifetime / noiseInterval);
        for (long i = 0; i < noiseAmount; i += 1) {
            Bukkit.getScheduler().runTaskLater(pet.pets.plugin, () -> {
                    if (disabled || !pet.isSpawned()) return;
                    Player owner = pet.getOwner();
                    pet.getType().voice.play(owner, pet.entity.getLocation());
                }, i * noiseInterval);
        }
    }

    public void disable() {
        disabled = true;
        clear();
        if (onDisable != null) onDisable.run();
    }

    protected void clear() {
        for (ArmorStand armorStand : armorStands) {
            armorStand.remove();
        }
        armorStands.clear();
    }

    private void showLines() {
        if (!pet.isSpawned()) return;
        for (int i = 0; i < lines.size(); i += 1) {
            final Component line = lines.get(i);
            Location location = getArmorStandLocation(i, lines.size());
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, as -> {
                    pet.pets.armorStandMap.put(as.getEntityId(), pet);
                    as.setMetadata("nomap", new FixedMetadataValue(pet.pets.plugin, true));
                    as.setVisible(false);
                    as.setPersistent(false);
                    as.setMarker(true);
                    as.setSmall(true);
                    as.setBasePlate(false);
                    as.setGravity(false);
                    as.setCanMove(false);
                    as.setCanTick(false);
                    as.customName(line);
                    as.setCustomNameVisible(true);
                });
            armorStands.add(armorStand);
        }
    }

    protected void updateLocations() {
        for (int i = 0; i < armorStands.size(); i += 1) {
            Location location = getArmorStandLocation(i, lines.size());
            armorStands.get(i).teleport(location);
        }
    }
}
