package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class StorageGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condMs;
    protected final CheckboxCondition condDiamonds;
    protected final CheckboxCondition condSearch;
    protected final CheckboxCondition condBack;
    protected final CheckboxCondition condStash;

    public StorageGoal() {
        this.id = "storage";
        this.displayName = text("Extra Storage");
        condMs = new CheckboxCondition(text("Open Mass Storage"),
                                       playerQuest -> getProgress(playerQuest).ms,
                                       playerQuest -> getProgress(playerQuest).ms = true);
        condDiamonds = new CheckboxCondition(text("Store some Diamonds"),
                                             playerQuest -> getProgress(playerQuest).msDiamonds,
                                             playerQuest -> getProgress(playerQuest).msDiamonds = true);
        condSearch = new CheckboxCondition(text("Find your Diamonds"),
                                           playerQuest -> getProgress(playerQuest).msSearch,
                                           playerQuest -> getProgress(playerQuest).msSearch = true);
        condBack = new CheckboxCondition(text("Explore the MS Menu"),
                                         playerQuest -> getProgress(playerQuest).msBack,
                                         playerQuest -> getProgress(playerQuest).msBack = true);
        condStash = new CheckboxCondition(text("Open Stash"),
                                          playerQuest -> getProgress(playerQuest).stash,
                                          playerQuest -> getProgress(playerQuest).stash = true);
        condMs.setBookPageIndex(0);
        condDiamonds.setBookPageIndex(0);
        condSearch.setBookPageIndex(1);
        condBack.setBookPageIndex(2);
        condStash.setBookPageIndex(3);
        this.conditions = List.of(new Condition[] {
                condMs,
                condDiamonds,
                condSearch,
                condBack,
                condStash,
            });
        this.constraints = List.of(SurvivalServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                join(noSeparators(), new Component[] {
                        text("Mass Storage (MS) is an infinite store for simple items,"
                             + " from cobblestone to diamonds,"
                             + " and have it available wherever you go."
                             + "\n\nCommand:\n"),
                        text("/ms", BLUE),
                        text("\nOpen the Mass Storage menu\n", GRAY),
                    }),
                join(noSeparators(), new Component[] {
                        text("Finding items in MS is easy."
                             + " You can click your way through the menu,"
                             + " or enter the name of what you're looking for."
                             + "\n\nCommand:\n"),
                        text("/ms diamond", BLUE),
                        text("\nSearch for diamonds\n", GRAY),
                    }),
                join(noSeparators(), new Component[] {
                        text("Menu navigation is key."
                             + " On Cavetale, you can return to the previous"
                             + " menu by clicking outside the menu window."
                             + "\n\n"
                             + "Open MS, explore the categories,"
                             + " and use this back function by clicking outside."),
                    }),
                join(noSeparators(), new Component[] {
                        text("Your stash can hold any item (except shulker boxes),"
                             + " just like your ender chest."
                             + "\n\nCommand:\n"),
                        text("/st", BLUE),
                        text("\nOpen your stash", GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100,
                                        text("Need a place to store"),
                                        text("all your items?"));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        text("No need for a massive"),
                                        text("storage room!"));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        text("Mass Storage and the"),
                                        text("Stash have you covered!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case OPEN_MASS_STORAGE:
            condMs.progress(playerQuest);
            break;
        case STORE_MASS_STORAGE: {
            Material mat = event.getDetail(Detail.MATERIAL, null);
            if (mat == Material.DIAMOND) {
                int amount = Detail.COUNT.get(event, 0);
                condDiamonds.progress(playerQuest);
            }
            break;
        }
        case SEARCH_MASS_STORAGE: {
            // This could be removed?
            String term = event.getDetail(Detail.NAME, "");
            if (term.toLowerCase().startsWith("diamond")) {
                condSearch.progress(playerQuest);
            }
            break;
        }
        case FIND_MASS_STORAGE: {
            if (Detail.MATERIAL.is(event, Material.DIAMOND)) {
                condSearch.progress(playerQuest);
            }
            break;
        }
        case MASS_STORAGE_GO_BACK: {
            condBack.progress(playerQuest);
            break;
        }
        case OPEN_STASH:
            condStash.progress(playerQuest);
            break;
        default: break;
        }
    }

    @Override
    public StorageProgress newProgress() {
        return new StorageProgress();
    }

    @Override
    public StorageProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(StorageProgress.class, StorageProgress::new);
    }

    protected static final class StorageProgress extends GoalProgress {
        protected boolean ms;
        protected boolean msDiamonds;
        protected boolean msSearch;
        protected boolean msBack;
        protected boolean stash;

        @Override
        public boolean isComplete() {
            return ms
                && msDiamonds
                && msSearch
                && stash;
        }
    }
}
