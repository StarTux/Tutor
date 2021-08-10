package com.cavetale.tutor.goal;

import com.cavetale.tutor.Background;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter @RequiredArgsConstructor
public final class ClickableCondition implements Condition {
    protected final Component description;
    protected final String token;
    protected final Consumer<PlayerQuest> clickHandler;

    @Override
    public Component toComponent(PlayerQuest playerQuest, Background background) {
        return Component.text()
            .append(Component.text('\u203A', background.green))
            .append(Component.space())
            .append(description)
            .hoverEvent(description)
            .clickEvent(ClickEvent.runCommand("/tutor click " + token))
            .build();
    }
}
