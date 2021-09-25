package com.cavetale.tutor;

import lombok.Value;

public interface QuestFlag {
    @Value
    final class AutoStartPermission implements QuestFlag {
        public final String permission;
    }

    static AutoStartPermission autoStart(String permission) {
        return new AutoStartPermission(permission);
    }
}
