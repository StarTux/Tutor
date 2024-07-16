package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.treechopper.ChoppedType;
import com.cavetale.mytems.item.treechopper.TreeChopEvent;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestTreeChopper extends DailyQuest<DailyQuestTreeChopper.Details, DailyQuest.Progress> {
    public DailyQuestTreeChopper() {
        super(DailyQuestType.TREE_CHOPPER,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    public void onGenerate(final int index) {
        List<ChoppedType> types = getAllTypes();
        this.details.chopped = types.get(index);
        this.total = 10;
    }

    public static List<ChoppedType> getAllTypes() {
        List<ChoppedType> types = new ArrayList<>();
        for (ChoppedType type : ChoppedType.values()) {
            if (!type.isJustAGuess()) types.add(type);
        }
        return types;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Chop " + total + Unicode.MULTIPLICATION.string),
                              details.chopped.chatIcon,
                              text(" with "), Mytems.TREE_CHOPPER);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Chop " + total + " "),
                              details.chopped.chatIcon,
                              text(" " + toCamelCase(" ", details.chopped) + " Trees with a "),
                              Mytems.TREE_CHOPPER,
                              text(" Tree Chopper in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.chopped.saplings.getValues().iterator().next(), total);
    }

    protected void onTreeChop(Player player, PlayerDailyQuest playerDailyQuest, TreeChopEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!event.isSuccessful()) return;
        if (event.getTreeChop().getChoppedType() != details.chopped) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        List<ItemStack> result = new ArrayList<>();
        switch (random.nextInt(3)) {
        case 0:
            for (Material material : details.chopped.logs.getValues()) {
                result.add(new ItemStack(material, material.getMaxStackSize()));
            }
            break;
        case 1:
            for (Material material : details.chopped.saplings.getValues()) {
                result.add(new ItemStack(material, material.getMaxStackSize()));
            }
            break;
        case 2: default:
            for (Material material : details.chopped.leaves.getValues()) {
                result.add(new ItemStack(material, material.getMaxStackSize()));
            }
            break;
        }
        return result;
    }

    public static final class Details extends DailyQuest.Details {
        protected ChoppedType chopped = ChoppedType.OAK;
    }
}
