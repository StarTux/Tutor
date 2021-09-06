package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
        condFocusLocal.setBookPageIndex(1);
        condFocusGlobal.setBookPageIndex(1);
        condSettings.setBookPageIndex(2);
        condUse.setBookPageIndex(3);
        this.conditions = Arrays.asList(new Condition[] {
                condList,
                condFocusLocal,
                condFocusGlobal,
                condSettings,
                condUse,
            });
        this.constraints = Collections.emptyList();
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("Chat has several channels."
                                       + " There is global, local, party and private."
                                       + "\n\nCommands:\n"),
                        Component.text("/ch list", NamedTextColor.DARK_BLUE),
                        Component.text("\nList chat channels", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Local chat has a range of "),
                        Component.text("500 blocks", NamedTextColor.DARK_RED, TextDecoration.UNDERLINED),
                        Component.text(". You can focus a channel to speak in there."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/l", NamedTextColor.DARK_BLUE),
                        Component.text("\nFocus local chat (500 blocks)\n", NamedTextColor.GRAY),
                        Component.text("/g", NamedTextColor.DARK_BLUE),
                        Component.text("\nFocus global chat\n", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Chat has many settings."
                                       + " You can change the look and feel of each channel to your liking."
                                       + " Returning to the default settings is easy."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/ch set", NamedTextColor.DARK_BLUE),
                        Component.text("\nOpen chat settings\n", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("Each channel command can send a message."
                                       + " It can be used without a message to focus."),
                        Component.text("\n\nCommands:\n"),
                        Component.text("/l Hello World", NamedTextColor.DARK_BLUE),
                        Component.text("\nSay \"Hello World\" in local chat\n", NamedTextColor.GRAY),
                        Component.text("/l", NamedTextColor.DARK_BLUE),
                        Component.text("\nFocus local chat\n", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(50L, 180L,
                                        Component.text("Chatting is simple"),
                                        Component.text("but with many players"),
                                        Component.text("online, we must learn"),
                                        Component.text("about channels."));
                    pet.addSpeechBubble(180L,
                                        Component.text("Long conversations"),
                                        Component.text("should be kept in"),
                                        Component.text("local, private, or"),
                                        Component.text("party chat."));
                    pet.addSpeechBubble(100L,
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
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        switch (name) {
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
