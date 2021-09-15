package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class ShopChestGoal extends AbstractGoal<ShopChestProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condShopChest;

    public ShopChestGoal() {
        super(ShopChestProgress.class, ShopChestProgress::new);
        this.id = "shop_chest";
        this.displayName = Component.text("Chest Shops");
        condShopChest = new CheckboxCondition(Component.text("Make a Chest Shop"),
                                              playerQuest -> getProgress(playerQuest).shopChest,
                                              playerQuest -> getProgress(playerQuest).shopChest = true);
        condShopChest.setBookPageIndex(0);
        this.conditions = List.of(new Condition[] {
                condShopChest,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("Making a Chest Shop is simple."
                                       + " Just place a "),
                        VanillaItems.OAK_SIGN.component,
                        Component.text("sign above a chest and write "),
                        Component.text("[shop]", NamedTextColor.BLUE),
                        Component.text(" in the first line."
                                       + "\n\nWik Page:\n"),
                        (Component.text().content("cavetale.com/wiki/chest-shops")
                         .color(NamedTextColor.BLUE)
                         .decorate(TextDecoration.UNDERLINED)
                         .hoverEvent(HoverEvent.showText(Component.text("cavetale.com/wiki/chest-shops",
                                                                        NamedTextColor.BLUE)))
                         .clickEvent(ClickEvent.openUrl("https://cavetale.com/wiki/chest-shops"))
                         .build()),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Completing enough tutorials to rank up to "),
                        Component.text("Speleologist", NamedTextColor.BLUE),
                        Component.text(" will allow you to purchase a plot in the market world."
                                       + " Shops made in there will show in the Shop Search."),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("Wanna sell stuff to"),
                                        Component.text("your fellow players?"));
                    pet.addSpeechBubble(id, 0L, 100L,
                                        Component.text("A Shop Chest is"),
                                        Component.text("the way to go!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case MAKE_SHOP_CHEST:
            condShopChest.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class ShopChestProgress extends GoalProgress {
    protected boolean shopChest;

    @Override
    public boolean isComplete() {
        return shopChest;
    }
}
