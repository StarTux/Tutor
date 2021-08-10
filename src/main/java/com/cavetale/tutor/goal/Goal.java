package com.cavetale.tutor.goal;

import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;

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

    default GoalProgress newProgress() {
        return new GoalProgress();
    }

    default GoalProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(GoalProgress.class, GoalProgress::new);
    }

    default List<Component> getBookPages(PlayerQuest playerQuest) {
        List<Component> pages = new ArrayList<>();
        List<Component> lines = new ArrayList<>();
        lines.add(playerQuest.getQuest().getDisplayName());
        lines.add(Component.empty());
        for (Condition condition : getConditions()) {
            lines.add(condition.toComponent(playerQuest, Background.LIGHT));
        }
        pages.add(Component.join(Component.newline(), lines));
        return pages;
    }

    default List<Component> getSidebarLines(PlayerQuest playerQuest) {
        List<Component> list = new ArrayList<>();
        for (Condition condition : getConditions()) {
            list.add(condition.toComponent(playerQuest, Background.DARK));
        }
        return list;
    }
}
