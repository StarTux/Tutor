package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public final class SellItemGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final NumberCondition condDiamond;
    protected final CheckboxCondition condMoney;
    protected static final int DIAMONDS = 3;

    public SellItemGoal() {
        this.id = "sell_item";
        this.displayName = Component.text("Selling Items");
        condDiamond = new NumberCondition(Component.text("Sell diamonds"), DIAMONDS,
                                          playerQuest -> getProgress(playerQuest).diamond,
                                          (playerQuest, diamond) -> getProgress(playerQuest).diamond = diamond);
        condMoney = new CheckboxCondition(Component.text("Check your balance"),
                                          playerQuest -> getProgress(playerQuest).money,
                                          playerQuest -> getProgress(playerQuest).money = true);
        condDiamond.setBookPageIndex(0);
        condMoney.setBookPageIndex(1);
        condDiamond.setBookPageIndex(0);
        this.conditions = List.of(new Condition[] {
                condDiamond,
                condMoney,
            });
        this.constraints = List.of(MainServerConstraint.instance());
        this.additionalBookPages = List.of(new Component[] {
                TextComponent.ofChildren(new Component[] {// 0
                        Component.text("You can sell certain items in your inventory."
                                       + " The sell menu lists all the sellable items you currently have."
                                       + " Click an item to sell it."
                                       + "\n\nCommand:\n"),
                        Component.text("/sell", NamedTextColor.BLUE),
                        Component.text("\nOpen the sell menu", NamedTextColor.GRAY),
                    }),
                TextComponent.ofChildren(new Component[] {// 1
                        Component.text("Keep an eye on your bank balance."
                                       + "\n\nCommand:\n"),
                        Component.text("/money", NamedTextColor.BLUE),
                        Component.text("\nCheck your balance", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        if (!getProgress(playerQuest).isComplete()) {
            playerQuest.getSession().applyPet(pet -> {
                    pet.addSpeechBubble(id, 50L, 100L,
                                        Component.text("Time to learn"),
                                        Component.text("about coins!"));
                    pet.addSpeechBubble(id, 0L, 150L,
                                        Component.text("The best way to earn"),
                                        Component.text("some coin is to sell"),
                                        Component.text("valuable items to"),
                                        Component.text("the bank."));
                    pet.addSpeechBubble(id, 0L, 200L,
                                        Component.text("Remember these commands:"),
                                        Component.text("/sell", NamedTextColor.YELLOW),
                                        Component.text("and"),
                                        Component.text("/money", NamedTextColor.YELLOW));
                });
        }
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        if (event.getName() == PluginPlayerEvent.Name.SELL_ITEM) {
            Material mat = event.getDetail(Detail.MATERIAL, null);
            int amount = event.getDetail(Detail.COUNT, 0);
            if (mat == Material.DIAMOND) {
                condDiamond.progress(playerQuest, amount);
            }
        } else if (event.getName() == PluginPlayerEvent.Name.USE_MONEY) {
            condMoney.progress(playerQuest);
        }
    }

    @Override
    public SellItemProgress newProgress() {
        return new SellItemProgress();
    }

    @Override
    public SellItemProgress getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(SellItemProgress.class, SellItemProgress::new);
    }

    protected static final class SellItemProgress extends GoalProgress {
        int diamond;
        boolean money;

        @Override
        public boolean isComplete() {
            return diamond >= DIAMONDS;
        }
    }
}
