package com.cavetale.tutor.goal;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.pet.Pet;
import com.cavetale.tutor.pet.PetType;
import com.cavetale.tutor.session.PlayerQuest;
import com.cavetale.tutor.util.Gui;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
            new CheckboxCondition(Component.text("Click a pet"), pq -> getProgress(pq).click, pq -> true),
            new CheckboxCondition(Component.text("Choose a pet"), pq -> getProgress(pq).choose, pq -> true),
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
        Player player = playerQuest.getPlayer();
        Pet cat = playerQuest.getPlugin().getPets().createPet(player);
        Pet dog = playerQuest.getPlugin().getPets().createPet(player);
        cat.setTag(id);
        cat.setType(PetType.CAT);
        cat.setCustomName(Component.text("Click me!", NamedTextColor.BLUE));
        cat.setOnClick(() -> onClick(playerQuest));
        cat.setExclusive(true);
        cat.setCollidable(true);
        cat.setAutoRespawn(true);
        dog.setTag(id);
        dog.setType(PetType.DOG);
        dog.setCustomName(Component.text("Click me!", NamedTextColor.BLUE));
        dog.setOnClick(() -> onClick(playerQuest));
        dog.setExclusive(true);
        dog.setCollidable(true);
        dog.setAutoRespawn(true);
    }

    @Override
    public void onDisable(PlayerQuest playerQuest) {
        playerQuest.getPlugin().getPets().removeOwnerTag(playerQuest.getSession().getUuid(), id);
    }

    @Override
    public void onComplete(PlayerQuest playerQuest) {
        ChoosePetProgress progress = getProgress(playerQuest);
        PetType petType = Objects.requireNonNull(progress.petType);
        playerQuest.getSession().setPet(petType, true);
        Pet pet = playerQuest.getSession().spawnPet();
    }

    private void onClick(PlayerQuest playerQuest) {
        ChoosePetProgress progress = getProgress(playerQuest);
        if (!progress.click) {
            progress.click = true;
            playerQuest.onProgress(progress);
        }
        Gui gui = new Gui()
            .size(3 * 9)
            .title(TextComponent.ofChildren(DefaultFont.guiBlankOverlay(3 * 9, NamedTextColor.BLUE),
                                            Component.text("Choose a pet!", NamedTextColor.DARK_BLUE)));
        ItemStack cat = Mytems.PIC_CAT.createIcon();
        cat.editMeta(meta -> {
                meta.displayName(Component.text("I'm more of a cat person!", NamedTextColor.BLUE));
            });
        ItemStack dog = Mytems.PIC_WOLF.createIcon();
        dog.editMeta(meta -> {
                meta.displayName(Component.text("I'm more of a dog person!", NamedTextColor.BLUE));
            });
        gui.setItem(9 + 3, dog, click -> {
                if (!click.isLeftClick()) return;
                progress.choose = true;
                progress.petType = PetType.DOG;
                playerQuest.onProgress(progress);
            });
        gui.setItem(9 + 5, cat, click -> {
                if (!click.isLeftClick()) return;
                progress.choose = true;
                progress.petType = PetType.CAT;
                playerQuest.onProgress(progress);
            });
        gui.open(playerQuest.getPlayer());
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
        protected PetType petType;

        @Override
        public boolean isComplete() {
            return click && choose;
        }
    }
}
