package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
                                          playerQuest -> getProgress(playerQuest).caught = true);
        condRelease = new CheckboxCondition(Component.text("Release a Mob"),
                                            playerQuest -> getProgress(playerQuest).release,
                                            playerQuest -> getProgress(playerQuest).release = true);
        condTalk.setBookPageIndex(0);
        condCatch.setBookPageIndex(2);
        condRelease.setBookPageIndex(4);
        this.conditions = List.of(new Condition[] {
                condTalk,
                condCatch,
                condRelease,
            });
        this.constraints = List.of(SurvivalServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("Mob Catchers"),
                        Mytems.MOB_CATCHER.component,
                        Component.text(" can be bought at Spawn."
                                       + "\n\nFind the store near the "),
                        Component.text("Player Heads", NamedTextColor.BLUE),
                        Component.text(" marketplace and bring the required resources."
                                       + "\n\nWiki Page:"),
                        Component.text("cavetale.com/wiki/pocket-mob",
                                       NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(Component.text("cavetale.com/wiki/pocket-mob",
                                                                       NamedTextColor.BLUE)))
                        .clickEvent(ClickEvent.openUrl("https://cavetale.com/wiki/pocket-mob")),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("In the store,"
                                       + " catchers can be upgraded to have a higher"
                                       + " success rate with certain mob types:\n\n"),
                        Mytems.MONSTER_CATCHER.component,
                        Component.text(" Monsters\n"),
                        Mytems.VILLAGER_CATCHER.component,
                        Component.text(" Villagers\n"),
                        Mytems.FISH_CATCHER.component,
                        Component.text(" Water Mobs\n"),
                        Mytems.ANIMAL_CATCHER.component,
                        Component.text(" Animals\n"),
                        Mytems.PET_CATCHER.component,
                        Component.text(" Your Pets\n"),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 2
                        Component.text("To catch a mob, throw the the "),
                        Mytems.MOB_CATCHER.component,
                        Component.text("catcher at it."
                                       + "\nIf "),
                        Component.text("successful", NamedTextColor.BLUE),
                        Component.text(", the mob will turn into an item."
                                       + " Make sure to pick it up!"
                                       + "\n\nIf it "),
                        Component.text("fails", NamedTextColor.RED),
                        Component.text(", your catcher will be gone."
                                       + "\n\nIn some areas such as Spawn,"
                                       + " catchers will just drop."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 3
                        Component.text("Hostile mobs are harder to catch than passive ones."
                                       + "\n\nSpecialized mob catchers can improve the catch"
                                       + " chance. Make sure to read the item description"
                                       + " carefully."),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 4
                        Component.text("To "),
                        Component.text("release", NamedTextColor.BLUE),
                        Component.text(" a mob, throw the Pocket Mob at a block."
                                       + "\n\nYou can release mobs in areas where you"
                                       + " can build,"
                                       + " or else the Pocket Mob will drop."
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
