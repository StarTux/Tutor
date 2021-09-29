package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ShopSearchGoal extends AbstractGoal<ShopSearchProgress> {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condMarket;
    protected final CheckboxCondition condShopSearch;
    protected final CheckboxCondition condShopPort;

    public ShopSearchGoal() {
        super(ShopSearchProgress.class, ShopSearchProgress::new);
        this.id = "shop_search";
        this.displayName = Component.text("Spending Coin");
        condMarket = new CheckboxCondition(Component.text("Visit the Market"),
                                           playerQuest -> getProgress(playerQuest).market,
                                           playerQuest -> getProgress(playerQuest).market = true);
        condShopSearch = new CheckboxCondition(Component.text("Search the Market"),
                                               playerQuest -> getProgress(playerQuest).shopSearch,
                                               playerQuest -> getProgress(playerQuest).shopSearch = true);
        condShopPort = new CheckboxCondition(Component.text("Port to a Shop Chest"),
                                             playerQuest -> getProgress(playerQuest).shopPort,
                                             playerQuest -> getProgress(playerQuest).shopPort = true);
        condMarket.setBookPageIndex(0);
        condShopSearch.setBookPageIndex(1);
        condShopPort.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condMarket,
                condShopSearch,
                condShopPort,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 0
                        Component.text("The market contains player created shops."
                                       + " Starting with the "),
                        Component.text("Speleologist", NamedTextColor.BLUE),
                        Component.text(" rank, you can claim a market plot"
                                       + " and set up shop chests there."
                                       + "\n\nCommand:\n"),
                        Component.text("/market", NamedTextColor.BLUE),
                        Component.text("\nWarp to the market world\n", NamedTextColor.GRAY),
                    }),
                Component.join(JoinConfiguration.noSeparators(), new Component[] {// 1
                        Component.text("You can search the market for specific items:\n\n"),
                        Component.text("/shop search <item>", NamedTextColor.BLUE),
                        Component.text("\nExample:\n", NamedTextColor.DARK_GRAY),
                        Component.text("/shop search oak", NamedTextColor.BLUE),
                        Component.text("\nSearch the shops for an item. Click the ", NamedTextColor.GRAY),
                        Component.text("[Port]", NamedTextColor.BLUE),
                        Component.text(" button in chat to port to the chest", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 200L,
                                        Component.text("Now that we have some"),
                                        Component.text("coins, let's find out"),
                                        Component.text("how they can be spent!"));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case USE_WARP:
            if (Detail.NAME.is(event, "market")) {
                condMarket.progress(playerQuest);
            }
            break;
        case SHOP_SEARCH:
            condShopSearch.progress(playerQuest);
            break;
        case SHOP_SEARCH_PORT:
            condShopPort.progress(playerQuest);
            break;
        default: break;
        }
    }
}

final class ShopSearchProgress extends GoalProgress {
    protected boolean market;
    protected boolean shopSearch;
    protected boolean shopPort;

    @Override
    public boolean isComplete() {
        return market
            && shopSearch
            && shopPort;
    }
}
