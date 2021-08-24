package com.cavetale.tutor.pet;

import com.cavetale.mytems.Mytems;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public enum PetType {
    CAT(Component.text("Cat"), Noise.CAT, Mytems.PIC_CAT),
    DOG(Component.text("Dog"), Noise.DOG, Mytems.PIC_WOLF);

    public final Component displayName;
    public final Noise voice;
    public final Mytems icon;
}
