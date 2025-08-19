package com.cavetale.tutor.daily;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Unicode;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.BlockColor;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestShearSheep extends DailyQuest<DailyQuestShearSheep.Details, DailyQuest.Progress> {
    public DailyQuestShearSheep() {
        super(DailyQuestType.SHEAR_SHEEP,
              Details.class, Details::new,
              Progress.class, Progress::new);
    }

    @Override
    protected void onGenerate(final String name) {
        this.details.color = BlockColor.valueOf(name.toUpperCase());
        if (details.color == BlockColor.WHITE) {
            this.total = 20;
        } else if (details.color == BlockColor.BLACK) {
            this.total = 10;
        } else if (details.color == BlockColor.PINK) {
            this.total = 5;
        } else {
            this.total = 3;
        }
    }

    public static List<BlockColor> getAllColors() {
        return List.of(BlockColor.values());
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Shear " + total + Unicode.MULTIPLICATION.string),
                              VanillaItems.of(details.color.getMaterial(BlockColor.Suffix.WOOL)),
                              Mytems.SHEEP_FACE);
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(text("Shear " + total + " "),
                              VanillaItems.of(details.color.getMaterial(BlockColor.Suffix.WOOL)),
                              text(toCamelCase(" ", details.color) + " Sheep in survival mode."));
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return new ItemStack(details.color.getMaterial(BlockColor.Suffix.WOOL), total);
    }

    protected void shearSheep(Player player, PlayerDailyQuest playerDailyQuest, PlayerShearEntityEvent event) {
        if (!NetworkServer.current().isSurvival()) return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!(event.getEntity() instanceof Sheep sheep)) return;
        DyeColor color = sheep.getColor();
        if (color != details.color.dyeColor) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(new ItemStack(details.color.getMaterial(BlockColor.Suffix.WOOL), 64));
    }

    public static final class Details extends DailyQuest.Details {
        protected BlockColor color = BlockColor.WHITE;
    }
}
