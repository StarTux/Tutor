package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class TitleGoal extends AbstractGoal<TitleProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condListTitles;
    protected final CheckboxCondition condSelectTitle;

    public TitleGoal() {
        super(TitleProgress.class, TitleProgress::new);
        this.id = "title";
        this.displayName = Component.text("Titles");
        condListTitles = new CheckboxCondition(Component.text("List your Titles"),
                                               playerQuest -> getProgress(playerQuest).listTitles,
                                               playerQuest -> getProgress(playerQuest).listTitles = true);
        condSelectTitle = new CheckboxCondition(Component.text("Select a Title"),
                                                playerQuest -> getProgress(playerQuest).selectTitle,
                                                playerQuest -> getProgress(playerQuest).selectTitle = true);
        this.conditions = List.of(new Condition[] {
                condListTitles,
                condSelectTitle,
            });
        condListTitles.setBookPageIndex(0);
        condSelectTitle.setBookPageIndex(1);
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 0
                        Component.text("The tag before your name in chat is not your rank,"
                                       + " it's a title."
                                       + "\n\nTitles can be collected and are yours to keep and display."
                                       + "\n\nCommand:\n"),
                        Component.text("/title", NamedTextColor.BLUE),
                        Component.text("\nList your titles", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {// 1
                        Component.text("Some titles express your rank, but most are decorative."
                                       + " You earn them by ranking up,"
                                       + " participating in weekly events,"
                                       + " or achieving certain accomplishments."
                                       + "\n\nTo select a title,"
                                       + " use the title command and click your preference in chat."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 120L, new Component[] {
                            Component.text("Titles and ranks are"),
                            Component.text("not the same."),
                        });
                    pet.addSpeechBubble(id, 0L, 80L, new Component[] {
                            Component.text("Titles can be"),
                            Component.text("collected."),
                        });
                    pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                            Component.text("You can choose a title"),
                            Component.text("from your collection."),
                        });
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case LIST_PLAYER_TITLES:
            condListTitles.progress(playerQuest);
            break;
        case SELECT_PLAYER_TITLE:
            condSelectTitle.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class TitleProgress extends GoalProgress {
    protected boolean listTitles;
    protected boolean selectTitle;

    @Override
    public boolean isComplete() {
        return listTitles && selectTitle;
    }
}
