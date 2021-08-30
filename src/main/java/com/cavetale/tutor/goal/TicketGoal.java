package com.cavetale.tutor.goal;

import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import com.winthier.ticket.TicketPlugin;
import com.winthier.ticket.event.TicketEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TicketGoal implements Goal, Listener {
    @Getter protected final String id;
    @Getter private final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter private final List<Component> additionalBookPages;
    private final CheckboxCondition condCreate;
    private final CheckboxCondition condView;
    private final CheckboxCondition condComment;
    private final CheckboxCondition condClose;

    public TicketGoal() {
        this.id = "ticket";
        this.displayName = Component.text("File a Ticket");
        condCreate = new CheckboxCondition(Component.text("Create a Ticket"),
                                           playerQuest -> getProgress(playerQuest).create,
                                           playerQuest -> getProgress(playerQuest).create = true);
        condView = new CheckboxCondition(Component.text("View your Ticket"),
                                         playerQuest -> getProgress(playerQuest).view,
                                         playerQuest -> getProgress(playerQuest).view = true,
                                         playerQuest -> getProgress(playerQuest).create);
        condComment = new CheckboxCondition(Component.text("Comment on your Ticket"),
                                            playerQuest -> getProgress(playerQuest).comment,
                                            playerQuest -> getProgress(playerQuest).comment = true,
                                            playerQuest -> getProgress(playerQuest).create);
        condClose = new CheckboxCondition(Component.text("Close your Ticket"),
                                          playerQuest -> getProgress(playerQuest).close,
                                          playerQuest -> getProgress(playerQuest).close = true,
                                          playerQuest -> getProgress(playerQuest).isReadyToClose());
        condCreate.setBookPageIndex(0);
        condView.setBookPageIndex(0);
        condComment.setBookPageIndex(0);
        condClose.setBookPageIndex(0);
        this.conditions = Arrays.asList(new Condition[] {
                condCreate,
                condView,
                condComment,
                condClose,
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Whenever you need staff assistance, make a ticket."
                                       + " This is the best way to reach out to staff."
                                       + "\n\nCommand:\n"),
                        Component.text("/ticket", NamedTextColor.DARK_BLUE),
                        Component.text("\nView the ticket menu,"
                                       + " click the buttons in chat for more.",
                                       NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("For the purpose of this tutorial, write any message in the ticket."
                                       + " When you make a ticket for real, try to provide as much information"
                                       + " as possible."
                                       + " Tickets remember where they were made."
                                       + " Our staff will be able to teleport there."),
                    }),
            });
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(50L, 60L, Component.text("Need help?"));
                    pet.addSpeechBubble(60L, Component.text("Someone bothering you?"));
                    pet.addSpeechBubble(60L, Component.text("Found a bug?"));
                    pet.addSpeechBubble(150L,
                                        Component.text("Make a ticket and we"),
                                        Component.text("will look into it."));
                });
        }
    }

    @Override
    public void onComplete(PlayerQuest playerQuest) {
        for (int ticketId : getProgress(playerQuest).tickets) {
            TicketPlugin.deleteTicket(ticketId);
        }
    }

    @EventHandler
    void onTicket(TicketEvent event) {
        if (!event.hasPlayer()) return;
        Player player = event.getPlayer();
        Session session = TutorPlugin.getInstance().getSessions().find(player);
        // We want to modify the ticket, so can't rely on delayed
        // apply tasks.
        if (session == null) return;
        session.applyGoals((playerQuest, goal) -> {
                if (goal != this) return;
                switch (event.getAction()) {
                case CREATED:
                    event.getTicket().setSilent(true);
                    final int ticketId = event.getTicket().getId();
                    Bukkit.getScheduler().runTaskLater(TutorPlugin.getInstance(), () -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                                   "ticket comment " + ticketId + " Good job, keep it up! :)");
                        }, 80L);
                    getProgress(playerQuest).tickets.add(ticketId);
                    if (!condCreate.progress(playerQuest)) {
                        playerQuest.save();
                    }
                    break;
                case VIEW:
                    condView.progress(playerQuest);
                    break;
                case COMMENT:
                    condComment.progress(playerQuest);
                    break;
                case CLOSE:
                    condClose.progress(playerQuest);
                    break;
                default: break;
                }
            });
    }

    @Override
    public TicketProgress newProgress() {
        return new TicketProgress();
    }

    @Override
    public TicketProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(TicketProgress.class, TicketProgress::new);
    }

    protected static final class TicketProgress extends GoalProgress {
        protected List<Integer> tickets = new ArrayList<>();
        protected boolean create;
        protected boolean view;
        protected boolean comment;
        protected boolean close;

        @Override
        public boolean isComplete() {
            return create && view && comment && close;
        }

        protected boolean isReadyToClose() {
            return create && view && comment;
        }
    }
}
