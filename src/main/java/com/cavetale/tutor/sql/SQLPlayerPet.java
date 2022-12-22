package com.cavetale.tutor.sql;

import com.cavetale.tutor.pet.PetGender;
import com.cavetale.tutor.pet.PetType;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import net.kyori.adventure.text.Component;

@Data
@Name("player_pets")
@NotNull
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerPet implements SQLRow {
    @Id private Integer id;
    @Unique private UUID player;
    @VarChar(31) private String pet;
    private boolean autoSpawn;
    @VarChar(255) private String name;
    private PetGender gender = PetGender.OTHER;
    @Text @Nullable private String settings; // json
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
