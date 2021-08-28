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
    private static final int DIAMOND = 10;

    public SellItemGoal() {
        this.id = "sell_item";
        this.displayName = Component.text("Selling Items");
        this.conditions = Arrays.asList(new Condition[] {
                new NumberCondition(Component.text("Sell diamonds"),
                                    playerQuest -> NumberProgress.of(getProgress(playerQuest).diamond, DIAMOND)),
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
                SellItemProgress progress = getProgress(playerQuest);
                if (progress.diamond < DIAMOND && amount > 0) {
                    progress.diamond = Math.min(DIAMOND, progress.diamond + amount);
                    playerQuest.onProgress(progress);
                }
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

    protected final class SellItemProgress extends GoalProgress {
        int diamond;

        @Override
        public boolean isComplete() {
            return diamond >= DIAMOND;
        }
    }
}
