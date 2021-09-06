package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public final class StorageGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condMs;
    protected final CheckboxCondition condDiamonds;
    protected final CheckboxCondition condSearch;
    protected final CheckboxCondition condStash;

    public StorageGoal() {
        this.id = "storage";
        this.displayName = Component.text("Extra Storage");
        condMs = new CheckboxCondition(Component.text("Open Mass Storage"),
                                       playerQuest -> getProgress(playerQuest).ms,
                                       playerQuest -> getProgress(playerQuest).ms = true);
        condDiamonds = new CheckboxCondition(Component.text("Store some Diamonds"),
                                             playerQuest -> getProgress(playerQuest).msDiamonds,
                                             playerQuest -> getProgress(playerQuest).msDiamonds = true);
        condSearch = new CheckboxCondition(Component.text("Find your Diamonds"),
                                           playerQuest -> getProgress(playerQuest).msSearch,
                                           playerQuest -> getProgress(playerQuest).msSearch = true);
        condStash = new CheckboxCondition(Component.text("Open Stash"),
                                          playerQuest -> getProgress(playerQuest).stash,
                                          playerQuest -> getProgress(playerQuest).stash = true);
        condMs.setBookPageIndex(0);
        condDiamonds.setBookPageIndex(0);
        condSearch.setBookPageIndex(1);
        condStash.setBookPageIndex(2);
        this.conditions = Arrays.asList(new Condition[] {
                condMs,
                condDiamonds,
                condSearch,
                condStash,
            });
        this.constraints = Arrays.asList(new MainServerConstraint());
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Mass Storage is an infinite store for simple items,"
                                       + " from cobblestone to diamonds."
                                       + "\n\nCommand:\n"),
                        Component.text("/ms", NamedTextColor.DARK_BLUE),
                        Component.text("\nOpen the Mass Storage menu\n", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Finding items in MS is easy."
                                       + " You can click your way through the menu,"
                                       + " or enter the name of what you're looking for."
                                       + "\n\nCommand:\n"),
                        Component.text("/ms diamond", NamedTextColor.DARK_BLUE),
                        Component.text("\nSearch for diamonds\n", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Your stash can hold any item (except shulker boxes),"
                                       + " just like your ender chest."
                                       + " It can transport items to the raid server."
                                       + "\n\nCommand:\n"),
                        Component.text("/st", NamedTextColor.DARK_BLUE),
                        Component.text("\nOpen your stash", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(50L, 100,
                                        Component.text("Need a place to store"),
                                        Component.text("all your items?"));
                    pet.addSpeechBubble(100,
                                        Component.text("No need for a massive"),
                                        Component.text("storage room!"));
                    pet.addSpeechBubble(100,
                                        Component.text("Mass Storage and the"),
                                        Component.text("Stash have you covered!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        switch (name) {
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
        boolean ms;
        boolean msDiamonds;
        boolean msSearch;
        boolean stash;

        @Override
        public boolean isComplete() {
            return ms
                && msDiamonds
                && msSearch
                && stash;
        }
    }
}
