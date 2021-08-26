package com.cavetale.tutor.goal;

import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.TutorEvent;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public final class ChoosePetGoal implements Goal {
    private final String id;
    private final Component displayName;
    private final List<Condition> conditions;
    private final List<Component> additionalBookPages;

    public ChoosePetGoal() {
        this.id = "choose_pet";
        this.displayName = Component.text("Choosing a Pet");
        Condition[] conds = new Condition[] {
            new CheckboxCondition(Component.text("Click a pet"),
                                  playerQuest -> getProgress(playerQuest).click),
            new CheckboxCondition(Component.text("Choose a pet"),
                                  playerQuest -> getProgress(playerQuest).choose),
            new CheckboxCondition(Component.text("Give your pet a name"),
                                  playerQuest -> getProgress(playerQuest).rename),
        };
        Component[] pages = new Component[] {
            TextComponent.ofChildren(new Component[] {
                    Component.text("You have arrived at a strange place."),
                    Component.space(),
                    Component.text("Why not choose a pet to keep you company!"),
                    Component.newline(),
                    Component.newline(),
                    Component.text("We hope you enjoy your stay."),
                    Component.space(),
                    Mytems.SMILE.component,
                }),
        };
        this.conditions = Arrays.asList(conds);
        this.additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        ChoosePetProgress progress = getProgress(playerQuest);
        if (!progress.choose) {
            Player player = playerQuest.getPlayer();
            Pet cat = playerQuest.getPlugin().getPets().createPet(player, PetType.CAT);
            Pet dog = playerQuest.getPlugin().getPets().createPet(player, PetType.DOG);
            cat.setTag(id);
            cat.setCustomName(Component.text("Click me!", NamedTextColor.BLUE));
            cat.setOnClick(() -> onClick(playerQuest));
            cat.setExclusive(true);
            cat.setCollidable(true);
            cat.setAutoRespawn(true);
            dog.setTag(id);
            dog.setCustomName(Component.text("Click me!", NamedTextColor.BLUE));
            dog.setOnClick(() -> onClick(playerQuest));
            dog.setExclusive(true);
            dog.setCollidable(true);
            dog.setAutoRespawn(true);
        }
    }

    @Override
    public void onDisable(PlayerQuest playerQuest) {
        playerQuest.getPlugin().getPets().removeOwnerTag(playerQuest.getSession().getUuid(), id);
    }

    private void onClick(PlayerQuest playerQuest) {
        ChoosePetProgress progress = getProgress(playerQuest);
        if (!progress.click) {
            progress.click = true;
            playerQuest.onProgress(progress);
        }
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
        ChoosePetProgress progress = getProgress(playerQuest);
        if (progress.choose) return;
        playerQuest.getPlugin().getPets().removeOwnerTag(playerQuest.getSession().getUuid(), id);
        playerQuest.getSession().setPet(petType, true);
        Pet pet = playerQuest.getSession().spawnPet();
        pet.addSpeechBubble(100, new Component[] {
                Component.text("Welcome to Cavetale, " + petType.speechGimmick + "!"),
            });
        pet.addSpeechBubble(100, new Component[] {
                Component.text("I will be your personal assistant."),
                Component.text("Please give me a name, " + petType.speechGimmick + "."),
            });
        progress.choose = true;
        playerQuest.onProgress(progress);
    }

    @Override
    public void onTutorEvent(PlayerQuest playerQuest, TutorEvent tutorEvent) {
        if (tutorEvent == TutorEvent.RENAME_PET) {
            ChoosePetProgress progress = getProgress(playerQuest);
            if (!progress.rename) {
                progress.rename = true;
                playerQuest.onProgress(progress);
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
