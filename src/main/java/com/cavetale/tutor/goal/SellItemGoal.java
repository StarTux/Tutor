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
import org.bukkit.Material;

public final class SellItemGoal implements Goal {
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Component> additionalBookPages;
    protected final NumberCondition condDiamond;
    protected static final int DIAMONDS = 3;

    public SellItemGoal() {
        this.id = "sell_item";
        this.displayName = Component.text("Selling Items");
        condDiamond = new NumberCondition(Component.text("Sell diamonds"), DIAMONDS,
                                          playerQuest -> getProgress(playerQuest).diamond,
                                          (playerQuest, diamond) -> getProgress(playerQuest).diamond = diamond);
        this.conditions = Arrays.asList(new Condition[] {
                condDiamond,
            });
        this.additionalBookPages = Arrays.asList(new Component[] {
                TextComponent.ofChildren(new Component[] {
                        Component.text("You can sell certain items in your inventory."
                                       + " The menu lists all the sellable items you currently have."
                                       + " Click an item to sell it."
                                       + "\n\nCommands:\n"),
                        Component.text("/sell", NamedTextColor.DARK_BLUE),
                        Component.text("\nOpen the sell menu", NamedTextColor.GRAY),
                        Component.newline(),
                        Component.text("/money", NamedTextColor.DARK_BLUE),
                        Component.text("\nCheck your balance", NamedTextColor.GRAY),
                    }),
            });
    }

    @Override
    public void onEnable(PlayerQuest playerQuest) {
        playerQuest.getSession().applyPet(pet -> {
                pet.addSpeechBubble(100L,
                                    Component.text("Time to learn about coins!"));
                pet.addSpeechBubble(150L,
                                    Component.text("The best way to earn some"),
                                    Component.text("coin is to sell valueable"),
                                    Component.text("items to the bank."));
                pet.addSpeechBubble(100L,
                                    Component.text("Remember this command:"),
                                    Component.text("/sell", NamedTextColor.YELLOW));
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent.Name name, PluginPlayerEvent event) {
        if (name == PluginPlayerEvent.Name.SELL_ITEM) {
            Material mat = event.getDetail(Detail.MATERIAL, null);
            int amount = event.getDetail(Detail.COUNT, 0);
            if (mat == Material.DIAMOND) {
                condDiamond.progress(playerQuest, amount);
            }
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

        @Override
        public boolean isComplete() {
            return diamond >= DIAMONDS;
        }
    }
}
