package com.cavetale.tutor.sql;

import com.cavetale.tutor.pet.PetType;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data @Table(name = "player_pets")
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerPet {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID player;
    @Column(nullable = true, length = 255)
    private String pet;
    @Column(nullable = false)
    private boolean autoSpawn;
    @Column(nullable = true, length = 4096) // text
    private String settings; // json
    @Column(nullable = false)
    private Date updated;

    public SQLPlayerPet() { }

    public SQLPlayerPet(final UUID player) {
        this.player = player;
        this.updated = new Date();
    }

    public void setNow() {
        this.updated = new Date();
    }

    public PetType parsePetType() {
        if (pet == null) return null;
        try {
            return PetType.valueOf(pet.toUpperCase());
        } catch (IllegalStateException iae) {
            return null;
        }
    }

    public void setPetType(PetType petType) {
        this.pet = petType.name().toLowerCase();
    }
}
