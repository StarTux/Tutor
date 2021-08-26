package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.Background;
import com.cavetale.tutor.TutorEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A Goal represents a currently active goal for the player. A goal is
 * part of a Tutorial. It can be displayed in sidebar and a book
 * interface, and upon completion will be remembered. It may respond
 * to events or commands.
 */
public interface Goal {
    /**
     * The id must be unique within the tutorial.
     */
    String getId();

    /**
     * Return a list of conditions to complete this goal. They're
     * primarily informal; completing all of them does not necessarily
     * complete the goal.
     */
    List<Condition> getConditions();

    Component getDisplayName();

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
        return Collections.emptyList();
    }

    default List<Component> getBookPages(PlayerQuest playerQuest) {
        List<Component> pages = new ArrayList<>();
        List<Component> lines = new ArrayList<>();
        final int index = playerQuest.getQuest().goalIndex(getId());
        final int size = playerQuest.getQuest().getGoals().size();
        lines.add(Component.text()
                  .append(Component.text("Tutorial", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                  .append(Component.space())
                  .append(playerQuest.getQuest().getDisplayName())
                  .append(Component.text(" " + (index + 1) + "/" + size + " \u2014 ", NamedTextColor.DARK_GRAY))
                  .append(getDisplayName())
                  .build());
        lines.add(Component.text("[Menu]", NamedTextColor.BLUE)
                  .clickEvent(ClickEvent.runCommand("/tutor menu"))
                  .hoverEvent(HoverEvent.showText(Component.text("Open Tutorial Menu", NamedTextColor.BLUE))));
        lines.add(Component.empty());
        for (Condition condition : getConditions()) {
            if (!condition.isVisible(playerQuest)) continue;
            lines.add(condition.toComponent(playerQuest, Background.LIGHT));
        }
        pages.add(Component.join(Component.newline(), lines));
        pages.addAll(getAdditionalBookPages());
        return pages;
    }

    default List<Component> getSidebarLines(PlayerQuest playerQuest) {
        List<Component> list = new ArrayList<>();
        for (Condition condition : getConditions()) {
            if (!condition.isVisible(playerQuest) || !condition.isOnSidebar(playerQuest)) continue;
            list.add(condition.toComponent(playerQuest, Background.DARK));
        }
        return list;
    }

    default void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) { }

    default void onTutorEvent(PlayerQuest playerQuest, TutorEvent event) { }
}
