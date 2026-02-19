package com.cavetale.tutor.daily;

import com.cavetale.core.event.structure.PlayerDiscoverStructureEvent;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.finder.FoundType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public final class DailyQuestDiscover extends DailyQuest<DailyQuestDiscover.Details, DailyQuest.Progress> {
    public DailyQuestDiscover() {
        super(
            DailyQuestType.DISCOVER,
            DailyQuestDiscover.Details.class, DailyQuestDiscover.Details::new,
            DailyQuest.Progress.class, DailyQuest.Progress::new
        );
    }

    /**
     * Amounts found in Feb 2026 in the comments.
     */
    @RequiredArgsConstructor
    protected enum Discovery {
        FOSSILS(FoundType.FOSSILS, "Nether Fossils"), // 527
        // IGLOO(FoundType.IGLOO), // 58
        MINESHAFT(FoundType.MINESHAFT), // 2305 + 14 (mesa)
        VILLAGE(FoundType.VILLAGE), // 7 + 55 + 22 + 24 + 26
        RUINED_PORTAL(FoundType.RUINED_PORTAL), // 194 + 3 + 10 + 15 + 118 + 4 + 16
        // BURIED_TREASURE(FoundType.BURIED_TREASURE), // 164
        SHIPWRECK(FoundType.SHIPWRECK), // 323 + 33
        // JUNGLE_TEMPLE(FoundType.JUNGLE_TEMPLE), // 5
        // PYRAMID(FoundType.PYRAMID), // 5
        // TRAIL_RUINS(FoundType.TRAIL_RUINS), // 67
        UNDERWATER_RUINS(FoundType.UNDERWATER_RUINS), // 355 + 124
        // PILLAGER_OUTPOST(FoundType.PILLAGER_OUTPOST), // 22
        // WITCH_HUT(FoundType.WITCH_HUT), // 1
        END_CITY(FoundType.END_CITY), // 57
        // NETHER_FORTRESS(FoundType.NETHER_FORTRESS), // 15
        // BASTION_REMNANT(FoundType.BASTION_REMNANT), // 20
        // STRONGHOLD(FoundType.STRONGHOLD), // 13
        // MONUMENT(FoundType.MONUMENT), // 75
        CAVETALE_DUNGEON(FoundType.CAVETALE_DUNGEON), // 2540
        // ANCIENT_CITY(FoundType.ANCIENT_CITY), // 18
        // WOODLAND_MANSION(FoundType.WOODLAND_MANSION), // 0
        TRIAL_CHAMBERS(FoundType.TRIAL_CHAMBERS), // 506
        MONSTER_HIVE(FoundType.MONSTER_HIVE), // 853
        ;

        Discovery(final FoundType foundType) {
            this(foundType, null);
        }

        private final FoundType foundType;
        private final String displayName;

        public String getDisplayName() {
            return displayName != null
                ? displayName
                : foundType.getDisplayName();
        }

        public ItemStack createIcon() {
            return foundType.getIcon();
        }

        public Component getChatIcon() {
            return foundType.getChatIcon();
        }
    }

    @Override
    public void onGenerate(final String name) {
        this.details.discovery = Discovery.valueOf(name.toUpperCase());
        this.total = 1;
    }

    @Override
    public Component getDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(
            text("Find "),
            details.discovery.getChatIcon(),
            text(details.discovery.getDisplayName())
        );
    }

    @Override
    public Component getDetailedDescription(PlayerDailyQuest playerDailyQuest) {
        return textOfChildren(
            text("Find previously undiscovered "),
            details.discovery.getChatIcon(),
            text(details.discovery.getDisplayName() + "."),
            text(" We recommend the Mining World with a "),
            (
                details.discovery == Discovery.CAVETALE_DUNGEON
                ? textOfChildren(VanillaItems.COMPASS, text("Compass."))
                : textOfChildren(Mytems.MASTER_FINDER, text("Finder."))
            )
        );
    }

    @Override
    public ItemStack createIcon(PlayerDailyQuest playerDailyQuest) {
        return details.discovery.createIcon();
    }

    public void onPlayerDiscoverStructure(Player player, PlayerDailyQuest playerDailyQuest, PlayerDiscoverStructureEvent event) {
        final FoundType foundType = FoundType.of(event.getStructure().getKey());
        if (foundType != details.discovery.foundType) return;
        makeProgress(playerDailyQuest, 1);
    }

    @Override
    protected List<ItemStack> generateRewards() {
        return List.of(Mytems.RUBY.createItemStack());
    }

    public static final class Details extends DailyQuest.Details {
        protected Discovery discovery = Discovery.MINESHAFT;
    }
}
