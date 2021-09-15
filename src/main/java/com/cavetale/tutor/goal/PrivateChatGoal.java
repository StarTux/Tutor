package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Console;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class PrivateChatGoal extends AbstractGoal<PrivateChatProgress> {
    private static final UUID CONSOLE = new UUID(0L, 0L);
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condWhisper;
    protected final CheckboxCondition condReply;
    protected final CheckboxCondition condFocus;
    protected final CheckboxCondition condReturn;

    public PrivateChatGoal() {
        super(PrivateChatProgress.class, PrivateChatProgress::new);
        this.id = "private_chat";
        this.displayName = Component.text("Whisper");
        condWhisper = new CheckboxCondition(Component.text("Reply to Console"),
                                            playerQuest -> getProgress(playerQuest).whisper,
                                            playerQuest -> getProgress(playerQuest).whisper = true);
        condReply = new CheckboxCondition(Component.text("Reply to Console"),
                                          playerQuest -> getProgress(playerQuest).reply,
                                          playerQuest -> getProgress(playerQuest).reply = true,
                                          playerQuest -> getProgress(playerQuest).whisper);
        condFocus = new CheckboxCondition(Component.text("Focus on Console"),
                                          playerQuest -> getProgress(playerQuest).focus,
                                          playerQuest -> getProgress(playerQuest).focus = true,
                                          playerQuest -> getProgress(playerQuest).whisper);
        condReturn = new CheckboxCondition(Component.text("Leave Private Chat"),
                                           playerQuest -> getProgress(playerQuest).ret,
                                           playerQuest -> getProgress(playerQuest).ret = true,
                                           playerQuest -> getProgress(playerQuest).focus);
        condWhisper.setBookPageIndex(0);
        condReply.setBookPageIndex(1);
        condFocus.setBookPageIndex(2);
        condReturn.setBookPageIndex(4);
        this.conditions = Arrays.asList(new Condition[] {
                condReply,
                condFocus,
                condReturn,
            });
        this.constraints = Collections.emptyList();
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("There are many commands to send private messages:"),
                        Component.text("\n/tell <player> <msg>", NamedTextColor.BLUE),
                        Component.text("\n/msg <player> <msg>", NamedTextColor.BLUE),
                        Component.text("\n/w <player> <msg>", NamedTextColor.BLUE),
                        Component.text("\n\nLet's send a PM to console:\n", NamedTextColor.GRAY),
                        Component.text("/msg #console hi", NamedTextColor.BLUE),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("When somebody sends you a private message "
                                       + ", you can quickly reply via the command:\n\n"),
                        Component.text("/reply <message>", NamedTextColor.BLUE),
                        Component.newline(),
                        Component.text("/r <message>", NamedTextColor.BLUE),
                        Component.text("\nReply to the last person who sent you a PM", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("You can also focus on a private conversation,"
                                       + " just like a chat channel, except you need a target:\n\n"),
                        Component.text("/r", NamedTextColor.BLUE),
                        Component.text("\nFocus on the last person who messaged you\n\n", NamedTextColor.GRAY),
                        Component.text("/msg <player>", NamedTextColor.BLUE),
                        Component.text("\nFocus on a specific person", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("If console just message you, do this:\n"),
                        Component.text("/r", NamedTextColor.BLUE),
                        Component.text("\n\nOr just use their full name:\n"),
                        Component.text("/msg #console", NamedTextColor.BLUE),
                        Component.text("\n\nKeep in mind that player names don't start with a hash."
                                       + " Console is special."),
                    }),
                TextComponent.ofChildren(new Component[] {// 4
                        Component.text("Once you're done,"
                                       + " return to the regular chat channels as always:\n\n"),
                        Component.text("/g", NamedTextColor.BLUE),
                        Component.newline(),
                        Component.text("/l", NamedTextColor.BLUE),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        PrivateChatProgress progress = getProgress(playerQuest);
        if (progress.whisper && !progress.reply || !progress.focus) {
            runConsoleWhisper(playerQuest.getPlayer());
        }
    }

    private void runConsoleWhisper(Player player) {
        String name = player.getName();
        Console.delayedCommand(40L, "msg " + name + " Hi there! Good job on your progress.");
        Console.delayedCommand(80L, "msg " + name + " You can reply to me with the /r command.");
        Console.delayedCommand(120L, "msg " + name + " Or message me via /msg #console");
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_PRIVATE_CHAT:
            if (Detail.TARGET.is(event, CONSOLE) && condWhisper.progress(playerQuest)) {
                runConsoleWhisper(playerQuest.getPlayer());
            }
            break;
        case USE_PRIVATE_CHAT_REPLY:
            condReply.progress(playerQuest);
            break;
        case FOCUS_PRIVATE_CHAT:
            if (Detail.TARGET.is(event, CONSOLE)) {
                condFocus.progress(playerQuest);
            }
            break;
        case FOCUS_CHAT_CHANNEL:
            condReturn.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class PrivateChatProgress extends GoalProgress {
    protected boolean whisper;
    protected boolean reply;
    protected boolean focus;
    protected boolean ret;

    @Override
    public boolean isComplete() {
        return whisper && reply && focus && ret;
    }
}
