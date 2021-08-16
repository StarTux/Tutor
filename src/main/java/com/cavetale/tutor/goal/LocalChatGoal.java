package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class LocalChatGoal implements Goal {
    @Getter private final String id;
    @Getter private Component displayName;
    @Getter private final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;

    public LocalChatGoal() {
        this.id = "local_chat";
        this.displayName = Component.text("Local Chat");
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Focus local chat"),
                                      playerQuest -> getProgress(playerQuest).stage == 0),
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                Component.text().content("View the channel list via ")
                .append(Component.text("/ch list", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("."))
                .build(),
            });
    }

    @Override
    public LocalChatProgress newProgress() {
        return new LocalChatProgress();
    }

    @Override
    public LocalChatProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(LocalChatProgress.class, LocalChatProgress::new);
    }

    public static final class LocalChatProgress extends GoalProgress {
        protected int stage = 0;
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        LocalChatProgress progress = getProgress(playerQuest);
        switch (progress.stage) {
        case 0:
            if (name == PluginPlayerEvent.Name.FOCUS_CHAT_CHANNEL
                && "local".equals(event.getDetail("channel_key", String.class, null))) {
                progress.stage += 1;
                playerQuest.save();
            }
            break;
        default: break;
        }
    }
}
