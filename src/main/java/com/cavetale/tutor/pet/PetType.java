package com.cavetale.tutor.pet;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public enum PetType {
    CAT(Component.text("Cat")),
    DOG(Component.text("Dog"));

    public final Component displayName;
}
