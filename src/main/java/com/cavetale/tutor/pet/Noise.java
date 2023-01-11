package com.cavetale.tutor.pet;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

@Value
public final class Noise {
    public static final Noise CAT = new Noise(Sound.ENTITY_CAT_AMBIENT, SoundCategory.NEUTRAL, 0.5f, 1.2f);
    public static final Noise DOG = new Noise(Sound.ENTITY_WOLF_AMBIENT, SoundCategory.NEUTRAL, 0.5f, 1.2f);
    public static final Noise CLICK = Noise.of(Sound.UI_BUTTON_CLICK);
    public static final Noise FAIL = Noise.of(Sound.UI_BUTTON_CLICK, 0.5f);
    public final Sound sound;
    public final SoundCategory category;
    public final float volume;
    public final float pitch;

    public static Noise of(Sound sound, final float pitch) {
        return new Noise(sound, SoundCategory.MASTER, 1.0f, pitch);
    }

    public static Noise of(Sound sound) {
        return new Noise(sound, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public void play(Player target, Location at) {
        target.playSound(at, sound, category, volume, pitch);
    }

    public void play(Player target) {
        target.playSound(target.getLocation(), sound, category, volume, pitch);
    }
}
