package com.cavetale.tutor.sql;

import com.cavetale.core.util.Json;
import com.cavetale.tutor.collect.ItemCollectionProgress;
import com.cavetale.tutor.collect.ItemCollectionType;
import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
@NotNull
@Name("player_item_collections")
@UniqueKey({"player", "collection"})
public final class SQLPlayerItemCollection implements SQLRow {
    @Id private Integer id;
    private UUID player;
    @VarChar(40) private String collection;
    @Text private String progress;
    @Default("0") private int score;
    private Date unlocked; // ie created
    @Nullable private Date completed;
    @Nullable private Date claimed;

    public SQLPlayerItemCollection() { }

    public SQLPlayerItemCollection(final UUID player,
                                   final ItemCollectionType itemCollection,
                                   final ItemCollectionProgress progress) {
        this.player = player;
        this.collection = itemCollection.name().toLowerCase();
        this.progress = Json.serialize(progress);
        this.unlocked = new Date();
    }

    public ItemCollectionType getItemCollectionType() {
        try {
            return ItemCollectionType.valueOf(collection.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public boolean isUnlocked() {
        return unlocked != null;
    }

    public boolean isComplete() {
        return completed != null;
    }

    public void setCompletedNow() {
        this.completed = new Date();
    }

    public boolean isClaimed() {
        return claimed != null;
    }

    public void setClaimedNow() {
        this.claimed = new Date();
    }
}
