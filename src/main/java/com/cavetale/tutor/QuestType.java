package com.cavetale.tutor;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public enum QuestType {
    TUTORIAL("Tutorial", "/tut", List.of("tutor")),
    QUEST("Quest", "/q", List.of("quest"));

    public final String upper;
    public final String lower;
    public final String command;
    public final List<String> aliases;
    public final ClickEvent clickEvent;
    public final HoverEvent hoverEvent;

    QuestType(final String upper, final String command, final List<String> aliases) {
        this.upper = upper;
        this.lower = upper.toLowerCase();
        this.command = command;
        this.aliases = aliases;
        this.clickEvent = ClickEvent.runCommand(command);
        Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()), new Component[] {
                Component.text(command, NamedTextColor.YELLOW),
                Component.text("Open the " + upper + " menu", NamedTextColor.GRAY),
            });
        this.hoverEvent = HoverEvent.showText(tooltip);
    }

    public ClickEvent clickEvent() {
        return clickEvent;
    }

    public HoverEvent hoverEvent() {
        return hoverEvent;
    }
}

