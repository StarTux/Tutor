package com.cavetale.tutor.pet;

import net.kyori.adventure.text.Component;

public enum PetGender {
    OTHER((char) 0),
    MALE('\u2642'),
    FEMALE('\u2640');

    public final char character;
    public final Component component;

    PetGender(final char character) {
        this.character = character;
        this.component = character > 0 ? Component.text(character) : Component.empty();
    }
}
