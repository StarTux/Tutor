package com.cavetale.tutor.sql;

import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.pet.PetGender;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.kyori.adventure.text.Component;

@Data @Table(name = "player_pets")
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerPet {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID player;
    @Column(nullable = true, length = 31)
    private String pet;
    @Column(nullable = false)
    private boolean autoSpawn;
    @Column(nullable = true, length = 255)
    private String name;
    @Column(nullable = false)
    private PetGender gender = PetGender.OTHER;
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
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public void setPetType(PetType petType) {
        this.pet = petType.name().toLowerCase();
    }

    public Component getNameComponent() {
        String result = (name != null ? name : "Your Pet")
            + (gender.ordinal() > 0 ? gender.character : "");
        return Component.text(result);
    }
}
