package com.cavetale.tutor.collect;

import com.cavetale.core.util.Json;
import com.cavetale.tutor.session.Session;
import com.cavetale.tutor.sql.SQLPlayerItemCollection;
import java.util.function.Consumer;
import lombok.Getter;
import static com.cavetale.tutor.TutorPlugin.database;
import static com.cavetale.tutor.TutorPlugin.plugin;

/**
 * An instance of this is created by Session for each player and
 * collection.
 * Most will not have a row or progress instance at first, only
 * creating them when the collection is unlocked.
 */
@Getter
public final class PlayerItemCollection {
    protected final Session session;
    protected final ItemCollectionType collection;
    private SQLPlayerItemCollection row;
    private ItemCollectionProgress progress;

    public PlayerItemCollection(final Session session,
                                final ItemCollectionType collection,
                                final SQLPlayerItemCollection row) {
        this.session = session;
        this.collection = collection;
        this.row = row;
        this.progress = row != null
            ? Json.deserialize(row.getProgress(), ItemCollectionProgress.class, ItemCollectionProgress::new)
            : new ItemCollectionProgress();
    }

    private void severe(String msg) {
        plugin().getLogger().severe("[PlayerItemCollection] [" + collection + "] [" + session.getName() + "] " + msg);
    }

    public boolean isUnlocked() {
        return row != null && row.isUnlocked();
    }

    public boolean isComplete() {
        return row != null && row.isComplete();
    }

    public boolean isClaimed() {
        return row != null && row.isClaimed();
    }

    /**
     * Create the row.
     */
    public void unlock() {
        if (row != null || session.isCollectionsLocked()) return;
        row = new SQLPlayerItemCollection(session.getUuid(), collection, new ItemCollectionProgress());
        database().insertAsync(row, i -> {
                if (i == null) {
                    severe("Could not insert " + row);
                    return;
                }
            });
    }

    public void save(Consumer<Boolean> callback) {
        if (row == null || session.isCollectionsLocked()) return;
        row.setProgress(Json.serialize(progress));
        session.setCollectionsLocked(true);
        database().updateAsync(row, i -> {
                if (i != 1) {
                    plugin().getLogger().severe("Could not update " + row);
                    return;
                }
                session.setCollectionsLocked(false);
                if (callback != null) callback.accept(i == 1);
            });
    }

    public int getScore() {
        return row != null
            ? row.getScore()
            : 0;
    }

    public int getScore(CollectItem item) {
        return progress != null
            ? progress.items.getOrDefault(item.getKey(), 0)
            : 0;
    }

    public void addScore(CollectItem item, int increment, Runnable callback) {
        if (increment <= 0) return;
        final String key = item.getKey();
        final int oldScore = progress.items.getOrDefault(key, 0);
        progress.items.put(key, oldScore + increment);
        row.setScore(row.getScore() + increment);
        updateCompletion();
        save(b -> {
                if (!b) {
                    severe("Could not add score " + increment);
                    return;
                }
                if (callback != null) callback.run();
            });
    }

    /**
     * Update completion.  Called above.
     * @return if completion has changed from false to true.
     */
    private boolean updateCompletion() {
        if (isComplete()) return false;
        for (CollectItem collectItem : collection.getItems()) {
            if (getScore(collectItem) < collectItem.getTotalAmount()) return false;
        }
        row.setCompletedNow();
        session.addCollectionsCompletedAsync(1);
        return true;
    }

    public void claim(Runnable callback) {
        row.setClaimedNow();
        save(b ->  {
             if (b && callback != null) {
                 callback.run();
             }
            });
    }
}
