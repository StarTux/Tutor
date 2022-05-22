package com.cavetale.tutor.sql;

import com.cavetale.tutor.pet.PetType;
import com.winthier.sql.SQLRow;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Table(name = "player_pet_unlocks",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"player", "pet_type"}),
       })
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerPetUnlock implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 31)
    private String petType;
    @Column(nullable = false)
    private Date created;

    public SQLPlayerPetUnlock() { }

    public SQLPlayerPetUnlock(final UUID player, final PetType petType) {
        this.player = player;
        this.petType = petType.key;
        this.created = new Date();
    }

    public PetType parsePetType() {
        try {
            return PetType.valueOf(petType.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
