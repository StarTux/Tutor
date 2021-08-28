package com.cavetale.tutor.pet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

@Getter @RequiredArgsConstructor
public final class SpeechBubble {
    private final Pet pet;
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private boolean disabled;
    private List<Component> lines;
    private long lifetime;

    private Location getArmorStandLocation(int index, int total) {
        double offset = (double) (total - index - 1) * 0.24;
        return pet.entity.getLocation().add(0.0, pet.entity.getHeight() + 0.25 + offset, 0.0);
    }

    public void setLines(long theLifetime, Component... theLines) {
        this.lines = Arrays.asList(theLines);
        this.lifetime = theLifetime;
    }

    public void enable() {
        if (lines != null) {
            showLines();
            pet.getOwner().sendMessage(Component.text()
                                       .append(Component.text().color(NamedTextColor.GRAY)
                                               .append(pet.getCustomName())
                                               .append(Component.text(": ")))
                                       .append(Component.join(Component.space(), lines)));
            Bukkit.getScheduler().runTaskLater(pet.pets.plugin, () -> {
                    if (!disabled) disable();
                }, lifetime);
        }
    }

    public void disable() {
        disabled = true;
        clear();
        pet.currentSpeechBubble = null;
        pet.triggerSpeechBubble();
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
