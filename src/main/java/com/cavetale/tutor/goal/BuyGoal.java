package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class BuyGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;
    private static final int CLAIM_BLOCKS = 2000;
    protected final CheckboxCondition condShopSearch;
    protected final CheckboxCondition condShopPort;
    protected final NumberCondition condBuyClaimBlocks;

    public BuyGoal() {
        this.id = "buy";
        this.displayName = Component.text("Spending Coin");
        condShopSearch = new CheckboxCondition(Component.text("Search the Market"),
                                               playerQuest -> getProgress(playerQuest).shopSearch,
                                               playerQuest -> getProgress(playerQuest).shopSearch = true);
        condShopPort = new CheckboxCondition(Component.text("Port to a Shop Chest"),
                                             playerQuest -> getProgress(playerQuest).shopPort,
                                             playerQuest -> getProgress(playerQuest).shopPort = true,
                                             playerQuest -> getProgress(playerQuest).shopSearch);
        condBuyClaimBlocks = new NumberCondition(Component.text("Buy Claim Blocks"), CLAIM_BLOCKS,
                                                 playerQuest -> getProgress(playerQuest).buyClaimBlocks,
                                                 (playerQuest, amount) -> getProgress(playerQuest).buyClaimBlocks = amount);
        this.conditions = Arrays.asList(new Condition[] {
                condShopSearch,
                condShopPort,
                condBuyClaimBlocks,
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("The market contains player created shops."
                                       + " Starting with a certain rank,"
                                       + " you can claim market plots and setup shop chests there."
                                       + "\n\nCommand:\n"),
                        Component.text("/market", NamedTextColor.DARK_BLUE),
                        Component.text("\nWarp to the public market\n", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("You can search the market for specific items"
                                       + " and port to the search results."
                                       + "\n\nCommand:\n"),
                        Component.text("/shop search <item>", NamedTextColor.DARK_BLUE),
                        Component.text("\nSearch the shops for an item. Click the ", NamedTextColor.GRAY),
                        Component.text("[Port]", NamedTextColor.DARK_BLUE),
                        Component.text(" button to port to the chest", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {
                        Component.text("To grow your claim, buy more claim blocks first."
                                       + " It will grow automatically in all directions"
                                       + " unless configured otherwise."
                                       + "\n\nCommand:\n"),
                        Component.text("/claim buy <amount>", NamedTextColor.DARK_BLUE),
                        Component.text("\nEach block costs 10 Cents", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(200L,
                                    Component.text("Now that we have some"),
                                    Component.text("coins, let's find out"),
                                    Component.text("how they can be spent!"));
            });
    }

    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.SHOP_SEARCH) {
            condShopSearch.progress(playerQuest);
        } else if (name == PluginPlayerEvent.Name.SHOP_SEARCH_PORT) {
            condShopPort.progress(playerQuest);
        } else if (name == PluginPlayerEvent.Name.BUY_CLAIM_BLOCKS) {
            int amount = event.getDetail(Detail.COUNT, 0);
            condBuyClaimBlocks.progress(playerQuest, amount);
        }
    }

    @Override
    public BuyProgress newProgress() {
        return new BuyProgress();
    }

    @Override
    public BuyProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(BuyProgress.class, BuyProgress::new);
    }

    protected static final class BuyProgress extends GoalProgress {
        boolean shopSearch;
        boolean shopPort;
        int buyClaimBlocks;

        @Override
        public boolean isComplete() {
            return shopSearch
                && shopPort
                && buyClaimBlocks >= CLAIM_BLOCKS;
        }
    }
}
