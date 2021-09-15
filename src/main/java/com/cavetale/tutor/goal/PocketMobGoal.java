package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public final class PocketMobGoal extends AbstractGoal<PocketMobProgress> {
    protected static final String NPC = "PocketMob";
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condTalk;
    protected final CheckboxCondition condCatch;
    protected final CheckboxCondition condRelease;

    public PocketMobGoal() {
        super(PocketMobProgress.class, PocketMobProgress::new);
        this.id = "pocket_mob";
        this.displayName = Component.text("Pocket Mob");
        condTalk = new CheckboxCondition(Component.text("Visit the PocketMob Store"),
                                         playerQuest -> getProgress(playerQuest).talk,
                                         playerQuest -> getProgress(playerQuest).talk = true);
        condCatch = new CheckboxCondition(Component.text("Catch a Mob"),
                                          playerQuest -> getProgress(playerQuest).caught,
                                          playerQuest -> getProgress(playerQuest).caught = true,
                                          playerQuest -> getProgress(playerQuest).talk);
        condRelease = new CheckboxCondition(Component.text("Release a Mob"),
                                            playerQuest -> getProgress(playerQuest).release,
                                            playerQuest -> getProgress(playerQuest).release = true,
                                            playerQuest -> getProgress(playerQuest).talk);
        condTalk.setBookPageIndex(0);
        condCatch.setBookPageIndex(1);
        condRelease.setBookPageIndex(2);
        this.conditions = List.of(new Condition[] {
                condTalk,
                condCatch,
                condRelease,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("Mob Catchers"),
                        Mytems.MOB_CATCHER.component,
                        Component.text(" can be bought at spawn."
                                       + " Find the store near the micro block marketplace"
                                       + " and bring the required resources."
                                       + "\n\nAlso in the store,"
                                       + " catchers can be upgraded to have a higher"
                                       + " success rate with certain mob types: "),
                        Mytems.MONSTER_CATCHER.component,
                        Mytems.VILLAGER_CATCHER.component,
                        Mytems.FISH_CATCHER.component,
                        Mytems.ANIMAL_CATCHER.component,
                        Mytems.PET_CATCHER.component,
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Catch a mob by throwing the "),
                        Mytems.MOB_CATCHER.component,
                        Component.text("catcher at it."
                                       + " Hostile mobs are harder to catch than animals."
                                       + " Specialized mob catchers can improve the catch"
                                       + " chance under the right circumstances."),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("To release a mob, throw the Pocket Mob at a block."
                                       + " You can generally release mobs in areas where you"
                                       + " can also build,"
                                       + " or else the Pocket Mob will just drop."
                                       + " When that happens,"
                                       + " pick it up and try somewhere else."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("Store your mobs in"),
                                        Component.text("an egg and carry"),
                                        Component.text("them around?"));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("Pocket Mob has"),
                                        Component.text("you covered!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case INTERACT_NPC:
            if (Detail.NAME.is(event, NPC)) {
                condTalk.progress(playerQuest);
            }
            break;
        case POCKET_MOB_CATCH:
            if (condCatch.progress(playerQuest)) {
                playerQuest.getSession().applyPet(pet -> {
                        pet.addSpeechBubble(id, 20L, 100L,
                                            Component.text("Bingo!"),
                                            Component.text("Nice catch!"));
                    });
            }
            break;
        case POCKET_MOB_RELEASE:
            condRelease.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class PocketMobProgress extends GoalProgress {
    protected boolean talk;
    protected boolean caught; // catch is a keyword!
    protected boolean release;

    @Override
    public boolean isComplete() {
        return talk && caught && release;
    }
}
