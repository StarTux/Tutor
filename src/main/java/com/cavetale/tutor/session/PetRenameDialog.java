package com.cavetale.tutor.session;

import com.cavetale.tutor.TutorEvent;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public final class PetRenameDialog {
    private final Player player;
    private final Session session;
    private String oldName;

    public void open() {
        oldName = session.getPlayerPetRow().getName();
        if (oldName == null) oldName = "";
        player.showDialog(Dialog.create(this::dialog));
    }

    private void dialog(RegistryBuilderFactory<Dialog, ? extends DialogRegistryEntry.Builder> factory) {
        factory.empty()
            .base(
                DialogBase.builder(text("Rename your Pet"))
                .inputs(
                    List.of(
                        DialogInput.text(
                            "name", // key
                            text("") // label
                        )
                        .initial(oldName)
                        .labelVisible(false)
                        .maxLength(32)
                        .multiline(MultilineOptions.create(1, null))
                        .build()
                    )
                )
                .build()
            )
            .type(
                DialogType.confirmation(
                    ActionButton.builder(text("Rename"))
                    .action(
                        DialogAction.customClick(
                            this::accept,
                            ClickCallback.Options.builder()
                            .lifetime(Duration.ofMinutes(10))
                            .uses(1)
                            .build()
                        )
                    )
                    .build(),
                    ActionButton.builder(text("Cancel"))
                    .action(DialogAction.staticAction(ClickEvent.callback(this::cancel)))
                    .build()
                )
            );
    }

    private void accept(DialogResponseView response, Audience audience) {
        final String name = response.getText("name");
        if (session.getPet() != null && name != null && name.length() >= 3 && name.length() <= 32) {
            session.renamePet(name);
            session.applyGoals((playerQuest, goal) -> {
                    goal.onTutorEvent(playerQuest, TutorEvent.RENAME_PET);
                });
        }
        session.openMenu(player, MenuSection.PET);
    }

    private void cancel(Audience audience) {
        session.openMenu(player, MenuSection.PET);
    }
}
