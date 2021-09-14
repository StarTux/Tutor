package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public final class DummyGoal extends AbstractGoal<DummyProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition cond;

    public DummyGoal() {
        super(DummyProgress.class, DummyProgress::new);
        this.id = "";
        this.displayName = Component.text("");
        cond = new CheckboxCondition(Component.text(""),
                                     playerQuest -> getProgress(playerQuest).done,
                                     playerQuest -> getProgress(playerQuest).done = true);
        cond.setBookPageIndex(0);
        this.conditions = Arrays.asList(new Condition[] {
                cond,
            });
        this.constraints = Collections.emptyList();
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text(""),
                    }),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
    }
}

final class DummyProgress extends GoalProgress {
    boolean done;

    @Override
    public boolean isComplete() {
        return done;
    }
}
