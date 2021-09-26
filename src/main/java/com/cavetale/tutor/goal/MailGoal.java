package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public final class MailGoal extends AbstractGoal<MailProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condReadMail;
    protected final CheckboxCondition condSendMail;

    public MailGoal() {
        super(MailProgress.class, MailProgress::new);
        this.id = "mail";
        this.displayName = Component.text("You've got Mail!");
        condReadMail = new CheckboxCondition(Component.text("Read your Mail"),
                                             playerQuest -> getProgress(playerQuest).readMail,
                                             playerQuest -> getProgress(playerQuest).readMail = true);
        condSendMail = new CheckboxCondition(Component.text("Send a Mail"),
                                             playerQuest -> getProgress(playerQuest).sendMail,
                                             playerQuest -> getProgress(playerQuest).sendMail = true);
        condReadMail.setBookPageIndex(0);
        condSendMail.setBookPageIndex(2);
        this.conditions = List.of(new Condition[] {
                condReadMail,
                condSendMail,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                        Component.text("You can exchange mail with other players."
                                       + " If they are not online, they will get see it"
                                       + " when they return."
                                       + "\n\nCommands:\n"),
                        Component.text("/mail", NamedTextColor.BLUE),
                        Component.text("\nRead mail\n\n", NamedTextColor.GRAY),
                        Component.text("/mailto <player> <message>", NamedTextColor.BLUE),
                        Component.text("\nSend mail\n", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                        Component.text("When you have unread mail, it will say so in your sidebar."
                                       + "\n\nTyping "),
                        Component.text("/mail", NamedTextColor.BLUE),
                        Component.text(" will list all your unread mails in chat."
                                       + " Click one of them to display the entire message,"
                                       + " which will also mark the mail as read."),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                        Component.text("Now let's send a mail to someone:\n\n"),
                        Component.text("/mailto <player> <message>", NamedTextColor.BLUE),
                        Component.text("\n\nIf you can't think of anyone, just mail "),
                        Component.text("Cavetale", NamedTextColor.BLUE),
                        Component.text(". He's our mascot "),
                        Mytems.WINK.component,
                        Component.text(".\n\nMails are deleted from the system after 90 days."),
                    }),
            });
    }

    @Override
    public void onBegin(PlayerQuest playerQuest) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mailto " + playerQuest.getSession().getName()
                               + " Hey there! Nice progress on the tutorial.");
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                            Component.text("Remember the movie with"),
                            Component.text("Tom Hanks and Meg Ryan?"),
                        });
                    pet.addSpeechBubble(id, 0, 100L, new Component[] {
                            Component.text("No? Don't worry,"),
                            Component.text("it didn't age well."),
                        });
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case READ_MAIL:
            condReadMail.progress(playerQuest);
            break;
        case SEND_MAIL:
            condSendMail.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class MailProgress extends GoalProgress {
    boolean readMail;
    boolean sendMail;

    @Override
    public boolean isComplete() {
        return readMail && sendMail;
    }
}
