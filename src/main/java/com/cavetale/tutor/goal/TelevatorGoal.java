package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.tutor.TutorPlugin;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class TelevatorGoal extends AbstractGoal<TelevatorProgress> implements Listener {
    protected static final int PLACE = 2;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final NumberCondition condPlace;
    protected final CheckboxCondition condUp;
    protected final CheckboxCondition condDown;

    public TelevatorGoal() {
        super(TelevatorProgress.class, TelevatorProgress::new);
        this.id = "televator";
        this.displayName = Component.text("");
        condPlace = new NumberCondition(Component.text("Place " + PLACE + " Gold Blocks"), PLACE,
                                        playerQuest -> getProgress(playerQuest).place,
                                        (playerQuest, amount) -> getProgress(playerQuest).place = amount);
        condUp = new CheckboxCondition(Component.text("Travel Televator Up"),
                                       playerQuest -> getProgress(playerQuest).up,
                                       playerQuest -> getProgress(playerQuest).up = true,
                                       playerQuest -> getProgress(playerQuest).place >= PLACE);
        condDown = new CheckboxCondition(Component.text("Travel Televator Down"),
                                         playerQuest -> getProgress(playerQuest).down,
                                         playerQuest -> getProgress(playerQuest).down = true,
                                         playerQuest -> getProgress(playerQuest).place >= PLACE);
        condPlace.setBookPageIndex(0);
        condUp.setBookPageIndex(1);
        condDown.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condPlace,
                condUp,
                condDown,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("Align two or more "),
                        VanillaItems.GOLD_BLOCK.component,
                        Component.text("gold blocks vertically,"
                                       + " and you have a Televator!"
                                       + "\n\nIt's simple. All you need is 18"),
                        VanillaItems.GOLD_INGOT.component,
                        Component.text("gold ingots."
                                       + " Craft them into blocks and make a Televator"
                                       + " which speeds up traveling up and down."),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Using the Televator is even simpler:\n\n"),
                        Component.text("Jump", NamedTextColor.BLUE),
                        Component.text("\nGo Up\n\n", NamedTextColor.GRAY),
                        Component.text("Sneak", NamedTextColor.BLUE),
                        Component.text("\nGo Down", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, TutorPlugin.getInstance());
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("Stairs?!"),
                                        Component.text("Where we're going,"),
                                        Component.text("we don't need..."),
                                        Component.text("stairs!"));
                    pet.addSpeechBubble(id, 20L, 40L,
                                        Component.text("Get it?"));
                });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.GOLD_BLOCK) {
            TutorPlugin.getInstance().getSessions().applyGoals(event.getPlayer(), (playerQuest, goal) -> {
                    if (goal != this) return;
                    condPlace.progress(playerQuest);
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case RIDE_TELEVATOR:
            if (Detail.DIRECTION.is(event, BlockFace.UP)) {
                condUp.progress(playerQuest);
            } else if (Detail.DIRECTION.is(event, BlockFace.DOWN)) {
                condDown.progress(playerQuest);
            }
            break;
        default: break;
        }
    }
}

final class TelevatorProgress extends GoalProgress {
    protected int place;
    protected boolean up;
    protected boolean down;

    @Override
    public boolean isComplete() {
        return place >= TelevatorGoal.PLACE && up && down;
    }
}
