package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.TutorEvent;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ChoosePetGoal implements Goal {
    @Getter private final String id;
    @Getter private final Component displayName;
    @Getter private final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter private final List<Component> additionalBookPages;
    protected final CheckboxCondition condClick;
    protected final CheckboxCondition condChoose;
    protected final CheckboxCondition condRename;
    protected static final PetType[] PET_TYPES = {PetType.CAT, PetType.DOG};

    public ChoosePetGoal() {
        this.id = "choose_pet";
        this.displayName = Component.text("Choosing a Pet");
        condClick = new CheckboxCondition(Component.text("Click a pet"),
                                          playerQuest -> getProgress(playerQuest).click,
                                          playerQuest -> getProgress(playerQuest).click = true);
        condChoose = new CheckboxCondition(Component.text("Choose a pet"),
                                           playerQuest -> getProgress(playerQuest).choose,
                                           playerQuest -> getProgress(playerQuest).choose = true);
        condRename = new CheckboxCondition(Component.text("Give your pet a name"),
                                           playerQuest -> getProgress(playerQuest).rename,
                                           playerQuest -> getProgress(playerQuest).rename = true);
        condClick.setBookPageIndex(0);
        condChoose.setBookPageIndex(0);
        condRename.setBookPageIndex(1);
        this.conditions = Arrays.asList(new Condition[] {
                condClick,
                condChoose,
                condRename,
            });
        this.constraints = Collections.emptyList();
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("You have arrived at a strange place."
                                       + " Why not choose a pet to keep you company!"
                                       + "\n\nWe hope you enjoy your stay. "),
                        Mytems.SMILE.component,
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Naming your Pet:"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Click your Pet"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Click [Back] in the book"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Click Your Pet to access Pet Options"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Click \"Change Name\""),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Click the prompt in chat"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Fill in the new name"),
                        Component.text("\n" + Unicode.BULLET_POINT.character + " Hit ENTER"),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (playerQuest.getSession().hasPet()) {
            getProgress(playerQuest).click = true;
            getProgress(playerQuest).choose = true;
        } else {
            Player player = playerQuest.getPlayer();
            for (PetType petType : PET_TYPES) {
                Pet pet = playerQuest.getPlugin().getPets().createPet(player, petType);
                pet.setTag(id);
                pet.setCustomName(Component.text("Click me!", NamedTextColor.BLUE));
                pet.setOnClick(() -> onClick(playerQuest));
                pet.setExclusive(true);
                pet.setCollidable(true);
                pet.setAutoRespawn(true);
                pet.setOwnerDistance(2.0);
            }
        }
    }

    @Override
    public void onDisable(PlayerQuest playerQuest) {
        playerQuest.getPlugin().getPets().removeOwnerTag(playerQuest.getSession().getUuid(), id);
    }

    private void onClick(PlayerQuest playerQuest) {
        condClick.progress(playerQuest);
        Gui gui = new Gui();
        gui.withOverlay(3 * 9, NamedTextColor.BLUE, Component.text("Choose a pet!", NamedTextColor.DARK_BLUE));
        ItemStack cat = PetType.CAT.icon.createIcon();
        cat.editMeta(meta -> {
                meta.displayName(Component.text("I'm more of a cat person!", NamedTextColor.BLUE));
            });
        ItemStack dog = PetType.DOG.icon.createIcon();
        dog.editMeta(meta -> {
                meta.displayName(Component.text("I'm more of a dog person!", NamedTextColor.BLUE));
            });
        gui.setItem(9 + 3, dog, click -> {
                if (!click.isLeftClick()) return;
                onChoosePet(playerQuest, PetType.DOG);
                click.getWhoClicked().closeInventory();
            });
        gui.setItem(9 + 5, cat, click -> {
                if (!click.isLeftClick()) return;
                onChoosePet(playerQuest, PetType.CAT);
                click.getWhoClicked().closeInventory();
            });
        gui.open(playerQuest.getPlayer());
    }

    private void onChoosePet(PlayerQuest playerQuest, PetType petType) {
        if (!condChoose.progress(playerQuest)) {
            return;
        }
        playerQuest.getPlugin().getPets().removeOwnerTag(playerQuest.getSession().getUuid(), id);
        playerQuest.getSession().setPet(petType, true);
        Pet pet = playerQuest.getSession().spawnPet();
        pet.addSpeechBubble(50L, 100L, new Component[] {
                Component.text("Welcome to Cavetale, " + petType.speechGimmick + "!"),
            });
        pet.addSpeechBubble(100L, new Component[] {
                Component.text("I will be your personal assistant."),
                Component.text("Please give me a name, " + petType.speechGimmick + "."),
            });
    }

    @Override
    public void onTutorEvent(PlayerQuest playerQuest, TutorEvent tutorEvent) {
        if (tutorEvent == TutorEvent.RENAME_PET) {
            if (condRename.progress(playerQuest)) {
                playerQuest.getSession().applyPet(pet -> {
                        pet.addSpeechBubble(50L, 200L,
                                            Component.text("When ").append(Component.text("[Complete]", NamedTextColor.AQUA)),
                                            Component.text("appears, open the"),
                                            Component.text("tutor menu and"),
                                            Component.text("click it."));
                        pet.addSpeechBubble(150L,
                                            Component.text("Or you can just"),
                                            Component.text().content("click me ")
                                            .append(Mytems.HAPPY.component)
                                            .build());
                    });
            }
        }
    }

    @Override
    public ChoosePetProgress newProgress() {
        return new ChoosePetProgress();
    }

    @Override
    public ChoosePetProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(ChoosePetProgress.class, ChoosePetProgress::new);
    }

    protected static final class ChoosePetProgress extends GoalProgress {
        protected boolean click;
        protected boolean choose;
        protected boolean rename;

        @Override
        public boolean isComplete() {
            return click && choose && rename;
        }
    }
}
