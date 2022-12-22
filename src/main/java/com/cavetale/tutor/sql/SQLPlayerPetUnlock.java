package com.cavetale.tutor.sql;

import com.cavetale.tutor.pet.PetType;
import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
@Name("player_pet_unlocks")
@NotNull
@UniqueKey({"player", "pet_type"})
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerPetUnlock implements SQLRow {
    @Id private Integer id;
    private UUID player;
    @VarChar(31) private String petType;
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
