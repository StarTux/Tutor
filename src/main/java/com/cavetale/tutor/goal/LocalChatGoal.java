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
                new CheckboxCondition(Component.text("View the channel list"),
                                      playerQuest -> getProgress(playerQuest).stage > 0),
                new CheckboxCondition(Component.text("Focus local chat"),
                                      playerQuest -> getProgress(playerQuest).stage > 1),
                new CheckboxCondition(Component.text("Use local chat"),
                                      playerQuest -> getProgress(playerQuest).stage > 3),
                new CheckboxCondition(Component.text("Open local chat settings"),
                                      playerQuest -> getProgress(playerQuest).stage > 4),
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                Component.text().content("Chat is organized in channels.")
                .append(Component.space())
                .append(Component.text("There is global, local, party and private."))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("View the channel list via "))
                .append(Component.text("/ch list", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text("."))
                .build(),
                Component.text().content("Local chat has a range of ")
                .append(Component.text("500 blocks", NamedTextColor.DARK_RED, TextDecoration.UNDERLINED))
                .append(Component.text("."))
                .append(Component.space())
                .append(Component.text("You can use the channel commands to send single messages."))
                .append(Component.space())
                .append(Component.text("Or you can focus on a channel to speak in there."))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("/g", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(" - Use global chat", NamedTextColor.DARK_GRAY))
                .append(Component.newline())
                .append(Component.text("/l", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(" - Use local chat (500 blocks)", NamedTextColor.DARK_GRAY))
                .build(),
                Component.text().content("Chat has many settings.")
                .append(Component.space())
                .append(Component.text("You can change the look and feel of each channel to your liking."))
                .append(Component.space())
                .append(Component.text("Returning to the default settings is easy."))
                .append(Component.newline())
                .append(Component.text("/ch set", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(" - Open chat settings", NamedTextColor.DARK_GRAY))
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
                && PluginPlayerEvent.Detail.NAME.is(event, "local")) {
                progress.stage += 1;
                playerQuest.save();
            }
            break;
        default: break;
        }
    }
}
