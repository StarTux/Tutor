package com.cavetale.tutor.goal;

import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.TutorEvent;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
        this.displayName = text("Choosing a Pet");
        condClick = new CheckboxCondition(text("Click a pet"),
                                          playerQuest -> getProgress(playerQuest).click,
                                          playerQuest -> getProgress(playerQuest).click = true);
        condChoose = new CheckboxCondition(text("Choose a pet"),
                                           playerQuest -> getProgress(playerQuest).choose,
                                           playerQuest -> getProgress(playerQuest).choose = true);
        condRename = new CheckboxCondition(text("Give your pet a name"),
                                           playerQuest -> getProgress(playerQuest).rename,
                                           playerQuest -> getProgress(playerQuest).rename = true);
        condClick.setBookPageIndex(0);
        condChoose.setBookPageIndex(0);
        condRename.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condClick,
                condChoose,
                condRename,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                // pg 0
                textOfChildren(text("You have arrived at a strange place."
                                    + " Why not choose a pet to keep you company!"
                                    + "\n\nWe hope you enjoy your stay. "),
                               Mytems.SMILE.component),
                // pg 1
                textOfChildren(text("Naming your Pet", BLUE),
                               newline(), newline(),
                               text("Type in Chat", GRAY),
                               newline(),
                               (textOfChildren(text("> ", GRAY), text("/tutor rename ", DARK_GRAY))
                                .clickEvent(runCommand("/tutor rename"))
                                .hoverEvent(showText(text("/tutor rename", YELLOW)))),
                               newline(),
                               text("Followed by the name of your choice.", GRAY)),
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
                pet.setCustomName(text("Click me!", BLUE));
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
        gui.withOverlay(3 * 9, BLUE, text("Choose a pet!", BLUE));
        ItemStack cat = PetType.CAT.mytems.createIcon();
        cat.editMeta(meta -> {
                meta.displayName(text("I'm more of a cat person!", BLUE));
            });
        ItemStack dog = PetType.DOG.mytems.createIcon();
        dog.editMeta(meta -> {
                meta.displayName(text("I'm more of a dog person!", BLUE));
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
        playerQuest.getSession().setPetType(petType);
        playerQuest.getSession().spawnPet();
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(id, 50L, 100L, new Component[] {
                        text("Welcome to Cavetale,"),
                        text(petType.speechGimmick + "!"),
                    });
                pet.addSpeechBubble(id, 0L, 100L, new Component[] {
                        text("I will be your"),
                        text("personal assistant."),
                        text("Please give me a name,"),
                        text(petType.speechGimmick + "."),
                    });
            });
    }

    @Override
    public void onTutorEvent(PlayerQuest playerQuest, TutorEvent tutorEvent) {
        if (tutorEvent == TutorEvent.RENAME_PET) {
            if (condRename.progress(playerQuest)) {
                playerQuest.getSession().applyPet(pet -> {
                        pet.addSpeechBubble(id, 50L, 200L,
                                            textOfChildren(text("When "), text("[Complete]", AQUA)),
                                            text("appears, open the"),
                                            text("tutor menu and"),
                                            text("click it."));
                        pet.addSpeechBubble(id, 0L, 150L,
                                            text("Or you can just"),
                                            textOfChildren(text("click me "), Mytems.HAPPY.component));
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
