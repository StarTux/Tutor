package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class LocalChatGoal implements Goal {
    @Getter private final String id;
    @Getter private Component displayName;
    @Getter private final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condList;
    protected final CheckboxCondition condFocusLocal;
    protected final CheckboxCondition condFocusGlobal;
    protected final CheckboxCondition condSettings;
    protected final CheckboxCondition condUse;

    public LocalChatGoal() {
        this.id = "local_chat";
        this.displayName = Component.text("Local Chat");
        condList = new CheckboxCondition(Component.text("View the channel list"),
                                         playerQuest -> getProgress(playerQuest).list,
                                         playerQuest -> getProgress(playerQuest).list = true);
        condFocusLocal = new CheckboxCondition(Component.text("Focus local chat"),
                                               playerQuest -> getProgress(playerQuest).focusLocal,
                                               playerQuest -> getProgress(playerQuest).focusLocal = true,
                                               playerQuest -> getProgress(playerQuest).list);
        condFocusGlobal = new CheckboxCondition(Component.text("Focus global chat"),
                                                playerQuest -> getProgress(playerQuest).focusGlobal,
                                                playerQuest -> getProgress(playerQuest).focusGlobal = true,
                                                playerQuest -> getProgress(playerQuest).list);
        condSettings = new CheckboxCondition(Component.text("Open local chat settings"),
                                             playerQuest -> getProgress(playerQuest).settings,
                                             playerQuest -> getProgress(playerQuest).settings = true,
                                             playerQuest -> getProgress(playerQuest).list);
        condUse = new CheckboxCondition(Component.text("Send message in local chat"),
                                        playerQuest -> getProgress(playerQuest).use,
                                        playerQuest -> getProgress(playerQuest).use = true,
                                        playerQuest -> getProgress(playerQuest).focusLocal);
        condList.setBookPageIndex(0);
        condFocusLocal.setBookPageIndex(2);
        condFocusGlobal.setBookPageIndex(2);
        condSettings.setBookPageIndex(3);
        condUse.setBookPageIndex(4);
        this.conditions = List.of(new Condition[] {
                condList,
                condFocusLocal,
                condFocusGlobal,
                condSettings,
                condUse,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Chat has several channels."
                                       + " There is global, local, party and private."
                                       + "\n\nCommands:\n"),
                        Component.text("/ch list", NamedTextColor.BLUE),
                        Component.text("\nList chat channels", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("Chat has components:\n\n"),
                        (Component.text()
                         .append(Component.text(Unicode.ARROW_RIGHT.character + " ", NamedTextColor.BLUE))
                         .append(Component.text("["))
                         .append(Component.text("G", NamedTextColor.GRAY))
                         .append(Component.text("]"))
                         .build()),
                        (Component.text()
                         .color(NamedTextColor.GRAY)
                         .content("\nThe channel. There is ")
                         .append(Component.text("G", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                         .append(Component.text("lobal, "))
                         .append(Component.text("L", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                         .append(Component.text("ocal, "))
                         .append(Component.text("PM", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                         .append(Component.text(" (private), and "))
                         .append(Component.text("P", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                         .append(Component.text("arty\n\n"))
                         .build()),
                        (Component.text()
                         .append(Component.text(Unicode.ARROW_RIGHT.character + " ", NamedTextColor.BLUE))
                         .append(Component.text("["))
                         .append(Component.text("Friendly", NamedTextColor.DARK_GRAY))
                         .append(Component.text("]"))
                         .build()),
                        Component.text("\nThe chosen title\n\n", NamedTextColor.GRAY),
                        (Component.text()
                         .append(Component.text(Unicode.ARROW_RIGHT.character + " ", NamedTextColor.BLUE))
                         .append(Component.text("Notch: ", NamedTextColor.GRAY))
                         .append(Component.text("Hello!"))
                         .build()),
                        Component.text("\nThe name and message", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("Local chat has a range of "),
                        Component.text("500 blocks", NamedTextColor.DARK_RED, TextDecoration.UNDERLINED),
                        Component.text(". You can focus a channel to speak in there."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/l", NamedTextColor.BLUE),
                        Component.text("\nFocus local chat (500 blocks)\n", NamedTextColor.GRAY),
                        Component.text("/g", NamedTextColor.BLUE),
                        Component.text("\nFocus global chat\n", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("Chat has many settings."
                                       + " You can change the look and feel of each channel to your liking."
                                       + " Returning to the default settings is easy."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/ch set", NamedTextColor.BLUE),
                        Component.text("\nOpen chat settings\n", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 4
                        Component.text("Each channel command can send a message."
                                       + " It can be used without a message to focus."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/l Hello World", NamedTextColor.BLUE),
                        Component.text("\nSay \"Hello World\" in local chat\n", NamedTextColor.GRAY),
                        Component.text("/l", NamedTextColor.BLUE),
                        Component.text("\nFocus local chat\n", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 180L,
                                        Component.text("Chatting is simple"),
                                        Component.text("but with many players"),
                                        Component.text("online, we must learn"),
                                        Component.text("about channels."));
                    pet.addSpeechBubble(id, 0L, 180L,
                                        Component.text("Personal conversations"),
                                        Component.text("should be kept in"),
                                        Component.text("local, private, or"),
                                        Component.text("party chat."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("Remember this command:"),
                                        Component.text("/ch", NamedTextColor.YELLOW));
                });
        }
    }

    @Override
    public LocalChatProgress newProgress() {
        return new LocalChatProgress();
    }

    @Override
    public LocalChatProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(LocalChatProgress.class, LocalChatProgress::new);
    }

    protected static final class LocalChatProgress extends GoalProgress {
        protected boolean list;
        protected boolean focusLocal;
        protected boolean focusGlobal;
        protected boolean use;
        protected boolean settings;

        @Override
        public boolean isComplete() {
            return list
                && focusLocal
                && focusGlobal
                && use
                && settings;
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case LIST_CHAT_CHANNELS:
            condList.progress(playerQuest);
            break;
        case FOCUS_CHAT_CHANNEL:
            if (PluginPlayerEvent.Detail.NAME.is(event, "local")) {
                condFocusLocal.progress(playerQuest);
            } else if (PluginPlayerEvent.Detail.NAME.is(event, "global")) {
                condFocusGlobal.progress(playerQuest);
            }
            break;
        case OPEN_CHAT_SETTINGS:
            if (PluginPlayerEvent.Detail.NAME.is(event, "local")) {
                condSettings.progress(playerQuest);
            }
            break;
        case USE_CHAT_CHANNEL:
            if (PluginPlayerEvent.Detail.NAME.is(event, "local")) {
                condUse.progress(playerQuest);
            }
            break;
        default: break;
        }
    }
}
