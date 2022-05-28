package com.cavetale.tutor.event;

import com.cavetale.tutor.pet.Pet;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 */
@Getter @RequiredArgsConstructor
public final class PetSpawnEvent extends Event implements Cancellable {
    @NonNull private final Pet pet;
    @NonNull private final Location location;
    @Setter private boolean cancelled;

    /**
     * Required by Event.
     */
    @Getter private static HandlerList handlerList = new HandlerList();

    /**
     * Required by Event.
     */
    @Override public HandlerList getHandlers() {
        return handlerList;
    }
}

