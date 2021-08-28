package com.cavetale.tutor.goal;

import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.session.Session;
import com.winthier.ticket.event.TicketEvent;
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

    public TicketGoal() {
        this.id = "ticket";
        this.displayName = Component.text("File a Ticket");
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Create a Ticket"),
                                      playerQuest -> getProgress(playerQuest).create),
                new CheckboxCondition(Component.text("View your Ticket"),
                                      playerQuest -> getProgress(playerQuest).view,
                                      playerQuest -> getProgress(playerQuest).create),
                new CheckboxCondition(Component.text("Comment on your Ticket"),
                                      playerQuest -> getProgress(playerQuest).comment,
                                      playerQuest -> getProgress(playerQuest).create),
                new CheckboxCondition(Component.text("Close your Ticket"),
                                      playerQuest -> getProgress(playerQuest).close,
                                      playerQuest -> getProgress(playerQuest).isReadyToClose()),
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Whenever you need staff attention, make a ticket."
                                       + " Try to avoid messaging us as we're often busy."
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
                                       + " So staff will be able to teleport there."),
                    }),
            });
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(40L, Component.text("Need help?"));
                pet.addSpeechBubble(20L, Component.text("Want something looked at?"));
                pet.addSpeechBubble(40L, Component.text("Have something to report?"));
                pet.addSpeechBubble(40L, Component.text("Is there an issue?"));
                pet.addSpeechBubble(40L, Component.text("Did someone grief you?"));
                pet.addSpeechBubble(40L, Component.text("Did you spot a cheater?"));
                pet.addSpeechBubble(40L, Component.text("Want to report somebody?"));
                pet.addSpeechBubble(40L, Component.text("Found a bug?"));
                pet.addSpeechBubble(40L, Component.text("Made a map and it's ready?"));
                pet.addSpeechBubble(40L, Component.text("Got something stolen?"));
                pet.addSpeechBubble(40L, Component.text("Server down?"));
                pet.addSpeechBubble(40L, Component.text("Something not working?"));
                pet.addSpeechBubble(40L, Component.text("Something broken?"));
                pet.addSpeechBubble(40L, Component.text("Someone flying?"));
                pet.addSpeechBubble(150L,
                                    Component.text("Make a ticket and our"),
                                    Component.text("very competent staff"),
                                    Component.text("will take care of it."));
            });
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
                TicketProgress progress = getProgress(playerQuest);
                switch (event.getAction()) {
                case CREATE:
                    if (!progress.create) {
                        progress.create = true;
                        event.getTicket().setSilent(true);
                        playerQuest.onProgress(progress);
                    }
                    break;
                case CREATED:
                    if (progress.ticketId == 0) {
                        final int ticketId = event.getTicket().getId();
                        progress.ticketId = ticketId;
                        playerQuest.save();
                        Bukkit.getScheduler().runTaskLater(TutorPlugin.getInstance(), () -> {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                                       "ticket comment " + ticketId + " Good job, keep it up! :)");
                            }, 60L);
                    }
                    break;
                case VIEW:
                    if (progress.create && !progress.view) {
                        progress.view = true;
                        playerQuest.onProgress(progress);
                    }
                    break;
                case COMMENT:
                    if (progress.create && !progress.comment) {
                        progress.comment = true;
                        playerQuest.onProgress(progress);
                    }
                    break;
                case CLOSE:
                    if (!progress.close && progress.isReadyToClose()) {
                        progress.close = true;
                        playerQuest.onProgress(progress);
                    }
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
        protected int ticketId;
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
