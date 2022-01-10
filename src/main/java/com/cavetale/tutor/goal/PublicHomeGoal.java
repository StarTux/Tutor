package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class PublicHomeGoal extends AbstractGoal<PublicHomeProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condListVisits;
    protected final CheckboxCondition condUseVisit;

    public PublicHomeGoal() {
        super(PublicHomeProgress.class, PublicHomeProgress::new);
        this.id = "public_home";
        this.displayName = Component.text("Public Homes");
        condListVisits = new CheckboxCondition(Component.text("List public homes"),
                                               playerQuest -> getProgress(playerQuest).listVisits,
                                               playerQuest -> getProgress(playerQuest).listVisits = true);
        condUseVisit = new CheckboxCondition(Component.text("Visit a public home"),
                                             playerQuest -> getProgress(playerQuest).useVisit,
                                             playerQuest -> getProgress(playerQuest).useVisit = true);
        condListVisits.setBookPageIndex(0);
        condUseVisit.setBookPageIndex(0);
        this.conditions = List.of(new Condition[] {
                condListVisits,
                condUseVisit,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Public homes are made by players."
                                       + " Anyone can turn their named home into a public home."
                                       + "\n\nCommand:\n"),
                        Component.text("/visit", NamedTextColor.BLUE),
                        Component.text("\nList public homes. Click to visit", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 150L,
                                        Component.text("Sharing is caring,"),
                                        Component.text("and we can share our"),
                                        Component.text("homes with others."));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("There are public homes"),
                                        Component.text("and public warps."));
                    pet.addSpeechBubble(id, 0L, 60L,
                                        Component.text("Let's check them out!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case VIEW_PUBLIC_HOMES:
            condListVisits.progress(playerQuest);
            break;
        case VISIT_PUBLIC_HOME:
            condUseVisit.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class PublicHomeProgress extends GoalProgress {
    protected boolean listVisits;
    protected boolean useVisit;

    @Override
    public boolean isComplete() {
        return listVisits
            && useVisit;
    }
}
