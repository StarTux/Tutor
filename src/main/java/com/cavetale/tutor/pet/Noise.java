package com.cavetale.tutor.pet;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

@Value
public final class Noise {
    public final Sound sound;
    public final float volume;
    public final float pitch;

    public static Noise of(Sound sound, final float pitch) {
        return new Noise(sound, 1.0f, pitch);
    }

    public static Noise of(Sound sound) {
        return new Noise(sound, 1.0f, 1.0f);
    }

    public void play(Player target, Location at) {
        target.playSound(at, sound, SoundCategory.MASTER, volume, pitch);
    }
}
