package com.cavetale.tutor.collect;

import java.util.HashMap;
import java.util.Map;

/**
 * Per player collection progress details.
 * Saved as JSON in SQLPlayerItemCollection and managed by
 * PlayerItemCollection.
 */
public final class ItemCollectionProgress {
    protected Map<String, Integer> items = new HashMap<>();
}
