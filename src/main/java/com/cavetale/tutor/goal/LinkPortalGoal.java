package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class LinkPortalGoal extends AbstractGoal<LinkPortalProgress> {
    protected static final UUID SERVER = new UUID(0L, 0L);
    protected static final int BUILD = 2;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condUsePublic;
    protected final NumberCondition condBuild;
    protected final CheckboxCondition condUse;

    public LinkPortalGoal() {
        super(LinkPortalProgress.class, LinkPortalProgress::new);
        this.id = "link_portal";
        this.displayName = Component.text("Link Portals");
        condUsePublic = new CheckboxCondition(Component.text("Use Link Portals at Spawn"),
                                              playerQuest -> getProgress(playerQuest).usePublic,
                                              playerQuest -> getProgress(playerQuest).usePublic = true);
        condBuild = new NumberCondition(Component.text("Build Link Portals"), BUILD,
                                        playerQuest -> getProgress(playerQuest).build,
                                        (playerQuest, amount) -> getProgress(playerQuest).build = amount,
                                        playerQuest -> getProgress(playerQuest).usePublic);
        condUse = new CheckboxCondition(Component.text("Use your Link Portal"),
                                        playerQuest -> getProgress(playerQuest).use,
                                        playerQuest -> getProgress(playerQuest).use = true,
                                        playerQuest -> getProgress(playerQuest).usePublic);
        condUsePublic.setBookPageIndex(0);
        condBuild.setBookPageIndex(1);
        condUse.setBookPageIndex(3);
        this.conditions = List.of(new Condition[] {
                condUsePublic,
                condBuild,
                condUse,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("The server spawn has display Link Portals."
                                       + " They're inside the barn by the farming area."
                                       + " Find them and travel through them."
                                       + "\n\nCommand:\n"),
                        Component.text("/spawn", NamedTextColor.BLUE),
                        Component.text("\nTeleport and turn around to find the farms", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Now it's time to build our own!"
                                       + " Just place a "),
                        VanillaItems.OAK_SIGN.component,
                        Component.text("sign and write "),
                        Component.text("[link]", NamedTextColor.BLUE),
                        Component.text(" in the first line."
                                       + "\n\nWiki Page:\n"),
                        (Component.text().content("cavetale.com/wiki/link-portals")
                         .color(NamedTextColor.BLUE)
                         .decorate(TextDecoration.UNDERLINED)
                         .hoverEvent(HoverEvent.showText(Component.text("cavetale.com/wiki/link-portals",
                                                                        NamedTextColor.BLUE)))
                         .clickEvent(ClickEvent.openUrl("https://cavetale.com/wiki/link-portals"))
                         .build()),
                    }),
                TextComponent.ofChildren(new Component[] {// 2
                        Component.text("Link portals come in 2 kinds."
                                       + "\n\n" + Unicode.BULLET_POINT.character
                                       + " Sign attached to a "),
                        Component.text("nether portal", NamedTextColor.BLUE),
                        Component.text(". Just walk through it."
                                       + "\n\n" + Unicode.BULLET_POINT.character
                                       + " Sign over a "),
                        Component.text("1x2 frame", NamedTextColor.BLUE),
                        Component.text(". This one is used by pressing a"
                                       + " button or pressure plate inside"
                                       + " the frame."),
                    }),
                TextComponent.ofChildren(new Component[] {// 3
                        Component.text("In order to confirm that your portals are working,"
                                       + " step through them."
                                       + "\n\nDepending on their make,"
                                       + " you use the nether portal,"
                                       + " push the button,"
                                       + " or step on the pressure plate."),
                    }),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case MAKE_LINK_PORTAL:
            condBuild.progress(playerQuest);
            break;
        case LINK_PORTAL_TRAVEL:
            if (Detail.OWNER.is(event, playerQuest.getPlayer().getUniqueId())) {
                condUse.progress(playerQuest);
            } else if (Detail.OWNER.is(event, SERVER)) {
                String name = Detail.NAME.get(event, "");
                if (name != null && name.startsWith("Tutorial")) {
                    condUsePublic.progress(playerQuest);
                }
            }
            break;
        default: break;
        }
    }
}

final class LinkPortalProgress extends GoalProgress {
    protected boolean usePublic;
    protected int build;
    protected boolean use;

    @Override
    public boolean isComplete() {
        return usePublic
            && build >= LinkPortalGoal.BUILD
            && use;
    }
}
