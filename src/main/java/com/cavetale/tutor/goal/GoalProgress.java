package com.cavetale.tutor.goal;

import com.cavetale.tutor.util.Json;
import java.util.function.Supplier;
import lombok.Data;

/**
 * Stores the progress of any given goal. Subclass in order to add
 * information!
 */
@Data
public class GoalProgress {
    public final String serialize() {
        return Json.serialize(this);
    }

    public static <T extends GoalProgress> T deserialize(String json, Class<T> clazz, Supplier<T> dfl) {
        return Json.deserialize(json, clazz, dfl);
    }

    public static GoalProgress deserialize(String json) {
        return deserialize(json, GoalProgress.class, GoalProgress::new);
    }
}
