package com.cavetale.tutor.pet;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;

@RequiredArgsConstructor
public enum PetType {
    CAT(Component.text("Cat"), Noise.of(Sound.ENTITY_CAT_AMBIENT, 1.2f)),
    DOG(Component.text("Dog"), Noise.of(Sound.ENTITY_WOLF_AMBIENT, 1.2f));

    public final Component displayName;
    public final Noise voice;
}
