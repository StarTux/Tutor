package com.cavetale.tutor.goal;

import com.cavetale.core.font.Unicode;
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
    protected final CheckboxCondition condClick;
    protected final CheckboxCondition condChoose;
    protected final CheckboxCondition condRename;

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
        Condition[] conds = new Condition[] {
            condClick,
            condChoose,
            condRename,
        };
        Component[] pages = new Component[] {
            TextComponent.ofChildren(new Component[] {
                    Component.text("You have arrived at a strange place."
                                   + " Why not choose a pet to keep you company!"
                                   + "\n\nWe hope you enjoy your stay. "),
                    Mytems.SMILE.component,
                }),
            TextComponent.ofChildren(new Component[] {
                    Component.text("Naming your Pet:"),
                    Component.text("\n" + Unicode.BULLET_POINT.character + " Click your Pet"),
                    Component.text("\n" + Unicode.BULLET_POINT.character + " [Menu]"),
                    Component.text("\n" + Unicode.BULLET_POINT.character + " Pet Options"),
                    Component.text("\n" + Unicode.BULLET_POINT.character + " Change Name"),
                }),
        };
        this.conditions = Arrays.asList(conds);
        this.additionalBookPages = Arrays.asList(pages);
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!condChoose.isComplete(playerQuest)) {
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
        pet.addSpeechBubble(100, new Component[] {
                Component.text("Welcome to Cavetale, " + petType.speechGimmick + "!"),
            });
        pet.addSpeechBubble(100, new Component[] {
                Component.text("I will be your personal assistant."),
                Component.text("Please give me a name, " + petType.speechGimmick + "."),
            });
    }

    @Override
    public void onTutorEvent(PlayerQuest playerQuest, TutorEvent tutorEvent) {
        if (tutorEvent == TutorEvent.RENAME_PET) {
            if (condRename.progress(playerQuest)) {
                playerQuest.getSession().applyPet(pet -> {
                        pet.addSpeechBubble(400L,
                                            Component.text("When the ").append(Component.text("[Complete]", NamedTextColor.AQUA)),
                                            Component.text("appears, open the"),
                                            Component.text("tutorial menu and"),
                                            Component.text("click it."),
                                            Component.text("You can just click"),
                                            Component.text("me to open it!"));
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
