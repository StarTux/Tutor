package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class WarpGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condSpawn;
    protected final CheckboxCondition condListWarps;
    protected final CheckboxCondition condUseWarp;
    protected final CheckboxCondition condListVisits;
    protected final CheckboxCondition condUseVisit;

    public WarpGoal() {
        this.id = "warp";
        this.displayName = Component.text("Getting around");
        condSpawn = new CheckboxCondition(Component.text("Visit spawn"),
                                          playerQuest -> getProgress(playerQuest).spawn,
                                          playerQuest -> getProgress(playerQuest).spawn = true);
        condListWarps = new CheckboxCondition(Component.text("List warps"),
                                              playerQuest -> getProgress(playerQuest).listWarps,
                                              playerQuest -> getProgress(playerQuest).listWarps = true);
        condUseWarp = new CheckboxCondition(Component.text("Use a warp"),
                                            playerQuest -> getProgress(playerQuest).useWarp,
                                            playerQuest -> getProgress(playerQuest).useWarp = true,
                                            playerQuest -> getProgress(playerQuest).listWarps);
        condListVisits = new CheckboxCondition(Component.text("List public homes"),
                                               playerQuest -> getProgress(playerQuest).listVisits,
                                               playerQuest -> getProgress(playerQuest).listVisits = true);
        condUseVisit = new CheckboxCondition(Component.text("Use a public home"),
                                             playerQuest -> getProgress(playerQuest).useVisit,
                                             playerQuest -> getProgress(playerQuest).useVisit = true,
                                             playerQuest -> getProgress(playerQuest).listVisits);
        this.conditions = Arrays.asList(new Condition[] {
                condSpawn,
                condListWarps,
                condUseWarp,
                condListVisits,
                condUseVisit
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("Get back to the place you started any time."
                                       + " There are portals, merchants, and secrets to be discovered."
                                       + "\n\nCommand:\n"),
                        Component.text("/spawn", NamedTextColor.DARK_BLUE),
                        Component.text("\nTeleport to spawn", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Warps can take you to key locations on the server."
                                       + " They are public places setup by the admins."
                                       + "\n\nCommand:\n"),
                        Component.text("/warp", NamedTextColor.DARK_BLUE),
                        Component.text("\nView the warp list. Click to warp", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("Public homes are made by players."
                                       + " Anyone can turn their named home into a public homes."
                                       + "\n\nCommand:\n"),
                        Component.text("/visit", NamedTextColor.DARK_BLUE),
                        Component.text("\nView the public home list. Click to teleport", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(150L,
                                    Component.text("Sharing is caring,"),
                                    Component.text("and we can share our"),
                                    Component.text("homes with others."));
                pet.addSpeechBubble(100L,
                                    Component.text("There are public homes"),
                                    Component.text("and public warps."));
                pet.addSpeechBubble(60L,
                                    Component.text("Let's check them out!"));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        switch (name) {
        case USE_SPAWN:
            condSpawn.progress(playerQuest);
            break;
        case LIST_WARPS:
            condListWarps.progress(playerQuest);
            break;
        case USE_WARP:
            condUseWarp.progress(playerQuest);
            break;
        case VIEW_PUBLIC_HOMES:
            condListVisits.progress(playerQuest);
            break;
        case VISIT_PUBLIC_HOME:
            condUseVisit.progress(playerQuest);
            break;
        default: break;
        }
    }

    @Override
    public WarpProgress newProgress() {
        return new WarpProgress();
    }

    @Override
    public WarpProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(WarpProgress.class, WarpProgress::new);
    }

    protected static final class WarpProgress extends GoalProgress {
        boolean spawn;
        boolean listWarps;
        boolean useWarp;
        boolean listVisits;
        boolean useVisit;

        @Override
        public boolean isComplete() {
            return spawn
                && listWarps
                && useWarp
                && listVisits
                && useVisit;
        }
    }
}
