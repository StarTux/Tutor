package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class TypeTierGoal extends AbstractGoal<TypeTierProgress> {
    protected static final int CLAIM_BLOCKS = 1024;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condUseTier;

    public TypeTierGoal() {
        super(TypeTierProgress.class, TypeTierProgress::new);
        this.id = "claim_grow";
        this.displayName = Component.text("Growing Claims");
        condUseTier = new CheckboxCondition(Component.text("Type /tier"),
                                            playerQuest -> getProgress(playerQuest).done,
                                            playerQuest -> getProgress(playerQuest).done = true);
        condUseTier.setBookPageIndex(0);
        this.conditions = List.of(new Condition[] {
                condUseTier,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Learn more about tiers by typing "),
                        Component.text("/tier", NamedTextColor.BLUE),
                        Component.text(". You will find a list of tiers and their perks."
                                       + "\n\nYou gain tiers by completing quests."),
                    }),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_TIER:
            condUseTier.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class TypeTierProgress extends GoalProgress {
    protected boolean done;

    @Override
    public boolean isComplete() {
        return done;
    }
}
