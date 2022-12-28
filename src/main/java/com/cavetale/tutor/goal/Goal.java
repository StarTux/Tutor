package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.DefaultFont;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.TutorEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

/**
 * A Goal represents a currently active goal for the player. A goal is
 * part of a Tutorial. It can be displayed in sidebar and a book
 * interface, and upon completion will be remembered. It may respond
 * to events or commands.
 */
public interface Goal {
    /**
     * The id must be unique within the quest.
     */
    String getId();

    Component getDisplayName();

    /**
     * Return a list of conditions to complete this goal. They're
     * primarily informal; completing all of them does not necessarily
     * complete the goal.
     */
    List<Condition> getConditions();

    /**
     * Constraints can limit the applicability of any goal in a
     * normalized way, without requiring any additional logic inside
     * the goal.
     */
    List<Constraint> getConstraints();

    /**
     * Called once per instance, as opposed to onEnable, which is
     * called for every player.
     */
    default void enable() { }

    /**
     * Called by PlayerQuest when a goal is started or loaded from the database.
     */
    default void onEnable(PlayerQuest playerQuest) { }

    /**
     * Called by PlayerQuest when a goal is removed from the player, so when it was
     * completed, abandoned, the plugin is disabled, or the player
     * disconnects.
     */
    default void onDisable(PlayerQuest playerQuest) { }

    /**
     * Called by PlayerQuest when a new goal is started.
     */
    default void onBegin(PlayerQuest playerQuest) { }

    /**
     * Called by PlayerQuest when this goal is successfully completed.
     */
    default void onComplete(PlayerQuest playerQuest) { }

    default GoalProgress newProgress() {
        return new GoalProgress();
    }

    default GoalProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(GoalProgress.class, GoalProgress::new);
    }

    default List<Component> getAdditionalBookPages() {
        return List.of();
    }

    default List<Component> getBookPages(PlayerQuest playerQuest) {
        List<Component> pages = new ArrayList<>();
        List<Component> lines = new ArrayList<>();
        final int index = playerQuest.getQuest().goalIndex(getId());
        final int size = playerQuest.getQuest().getGoals().size();
        lines.add(text().append(playerQuest.getQuest().name.displayName)
                  .color(DARK_AQUA)
                  .decorate(BOLD)
                  .build());
        lines.add(text()
                  .append(text(playerQuest.getQuest().getName().type.upper + " ", GRAY))
                  .append(DefaultFont.BACK_BUTTON.forPlayer(playerQuest.getPlayer()))
                  .clickEvent(ClickEvent.runCommand("/tutor menu"))
                  .hoverEvent(HoverEvent.showText(text("Open Tutor Menu", BLUE)))
                  .build());
        lines.add(text()
                  .append(getDisplayName())
                  .append(text(" (" + (index + 1) + "/" + size + ") ", DARK_GRAY))
                  .build());
        lines.add(Component.empty());
        for (Condition condition : getConditions()) {
            if (!condition.isVisible(playerQuest)) continue;
            Component conditionComponent = condition.toComponent(playerQuest, Background.LIGHT);
            if (!(condition instanceof ClickableCondition)) {
                // Generate tooltip
                if (condition.hasBookPage()) {
                    // With book page changer
                    int toPage = condition.getBookPageIndex() + 2;
                    conditionComponent = conditionComponent
                        .hoverEvent(HoverEvent.showText(join(separator(newline()),
                                                             condition.toComponent(playerQuest, Background.DARK),
                                                             text("Page " + toPage, GRAY))))
                        .clickEvent(ClickEvent.changePage(toPage));
                } else {
                    // Without book page changer
                    conditionComponent = conditionComponent
                        .hoverEvent(HoverEvent.showText(condition.toComponent(playerQuest, Background.DARK)));
                }
            }
            lines.add(conditionComponent);
        }
        boolean hasMissedConstraint = false;
        for (Constraint constraint : getConstraints()) {
            if (constraint.doesMeet(playerQuest)) continue;
            lines.add(constraint.getMissedMessage(playerQuest, Background.LIGHT));
            hasMissedConstraint = true;
        }
        boolean complete = !hasMissedConstraint && playerQuest.getCurrentProgress().isComplete();
        boolean abandonable = !complete
            && (playerQuest.getQuest().getName().isQuittable()
                || playerQuest.getSession().getCompletedQuests().containsKey(playerQuest.getQuest().name));
        if (complete) {
            lines.add(DefaultFont.OK_BUTTON.forPlayer(playerQuest.getPlayer())
                      .hoverEvent(HoverEvent.showText(text("Complete this part", GREEN)))
                      .clickEvent(ClickEvent.runCommand("/tutor click complete " + playerQuest.getQuest().getName().key)));
        } else if (abandonable) {
            lines.add(Component.empty());
            lines.add(DefaultFont.CANCEL_BUTTON.forPlayer(playerQuest.getPlayer())
                      .hoverEvent(HoverEvent.showText(text("Abandon this " + playerQuest.getQuest().getName().type.lower,
                                                           RED)))
                      .clickEvent(ClickEvent.runCommand("/tutor click quit " + playerQuest.getQuest().getName().key)));
        }
        pages.add(join(separator(newline()), lines));
        pages.addAll(getAdditionalBookPages());
        return pages;
    }

    default List<Component> getSidebarLines(PlayerQuest playerQuest) {
        List<Component> list = new ArrayList<>();
        for (Condition condition : getConditions()) {
            if (!condition.isVisible(playerQuest) || !condition.isOnSidebar(playerQuest)) continue;
            list.add(condition.toComponent(playerQuest, Background.DARK));
        }
        boolean hasMissedConstraint = false;
        for (Constraint constraint : getConstraints()) {
            if (constraint.doesMeet(playerQuest)) continue;
            list.add(constraint.getMissedMessage(playerQuest, Background.DARK));
            hasMissedConstraint = true;
        }
        if (!hasMissedConstraint && playerQuest.getCurrentProgress().isComplete()) {
            list.add(text().color(WHITE)
                     .append(text("Type "))
                     .append(text(playerQuest.getQuest().getName().type.command,
                                  GREEN))
                     .append(text(" to progress!"))
                     .build());
        }
        return list;
    }

    default void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) { }

    default void onTutorEvent(PlayerQuest playerQuest, TutorEvent event) { }

    default boolean hasMissedConstraints(PlayerQuest playerQuest) {
        for (Constraint constraint : getConstraints()) {
            if (!constraint.doesMeet(playerQuest)) return true;
        }
        return false;
    }
}
