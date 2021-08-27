package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class StorageGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;

    public StorageGoal() {
        this.id = "storage";
        this.displayName = Component.text("Extra Storage");
        this.conditions = Arrays.asList(new Condition[] {
                new CheckboxCondition(Component.text("Open Mass Storage"),
                                      playerQuest -> getProgress(playerQuest).ms),
                new CheckboxCondition(Component.text("Open Stash"),
                                      playerQuest -> getProgress(playerQuest).stash),
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Mass Storage is an infinite store for simple items,"
                                       + " from cobblestone to diamonds."
                                       + "\n\nCommands:\n"),
                        Component.text("/ms", NamedTextColor.DARK_BLUE, TextDecoration.BOLD),
                        Component.text("\nOpen the Mass Storage menu", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Your stash can hold any item (except shulker boxes),"
                                       + " just like your ender chest."
                                       + " It can transport items to the raid server."
                                       + "\n\nCommands:\n"),
                        Component.text("/stash", NamedTextColor.DARK_BLUE, TextDecoration.BOLD),
                        Component.text("\nOpen your stash", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100,
                                    Component.text("Need a place to store all your items?"));
                pet.addSpeechBubble(100,
                                    Component.text("No need for a massive storage room!"));
                pet.addSpeechBubble(100,
                                    Component.text("Mass Storage and the Stash have you covered!"));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.OPEN_MASS_STORAGE) {
            StorageProgress progress = getProgress(playerQuest);
            if (!progress.ms) {
                progress.ms = true;
                playerQuest.onProgress(progress);
            }
        } else if (name == PluginPlayerEvent.Name.OPEN_STASH) {
            StorageProgress progress = getProgress(playerQuest);
            if (!progress.stash) {
                progress.stash = true;
                playerQuest.onProgress(progress);
            }
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

    protected final class StorageProgress extends GoalProgress {
        boolean ms;
        boolean stash;

        @Override
        public boolean isComplete() {
            return ms && stash;
        }
    }
}
