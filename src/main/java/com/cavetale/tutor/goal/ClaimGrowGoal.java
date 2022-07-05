package com.cavetale.tutor.goal;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.tutor.session.PlayerQuest;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class ClaimGrowGoal extends AbstractGoal<ClaimGrowProgress> {
    protected static final int CLAIM_BLOCKS = 1024;
    @Getter protected final String id;
    @Getter protected final Component displayName;
    @Getter protected final List<Condition> conditions;
    @Getter protected final List<Constraint> constraints;
    @Getter protected final List<Component> additionalBookPages;
    protected final CheckboxCondition condGrowClaim;
    protected final NumberCondition condBuyClaimBlocks;

    public ClaimGrowGoal() {
        super(ClaimGrowProgress.class, ClaimGrowProgress::new);
        this.id = "claim_grow";
        this.displayName = text("Growing Claims");
        condGrowClaim = new CheckboxCondition(text("Grow your Claim"),
                                              playerQuest -> getProgress(playerQuest).growClaim,
                                              playerQuest -> getProgress(playerQuest).growClaim = true);
        condBuyClaimBlocks = new NumberCondition(text("Buy Claim Blocks"), CLAIM_BLOCKS,
                                                 playerQuest -> getProgress(playerQuest).buyClaimBlocks,
                                                 (playerQuest, amount) -> getProgress(playerQuest).buyClaimBlocks = amount);
        condGrowClaim.setBookPageIndex(0);
        condBuyClaimBlocks.setBookPageIndex(1);
        this.conditions = List.of(new Condition[] {
                condGrowClaim,
                condBuyClaimBlocks,
            });
        this.constraints = List.of();
        this.additionalBookPages = List.of(new Component[] {
                join(noSeparators(), // 0
                     text("Your claim starts off small "),
                     text("(100x100 blocks)", GRAY),
                     text(" but you can grow it."
                          + "\n\nTo grow it to a certain spot,"
                          + " move outside near your claim and type:\n\n"),
                     text("/claim grow", BLUE),
                     text("\nGrow claim to your location", GRAY)),
                join(noSeparators(), // 2
                     text("For a claim to grow, it requires enough claim blocks."
                          + " Claim blocks are added with the following command:\n\n"),
                     text("/claim buy <amount>", BLUE),
                     text("\nEach block costs ", GRAY),
                     Mytems.COPPER_COIN,
                     text("one coin.", GRAY)),
            });
    }

    @Override
    public void onPluginPlayer(PlayerQuest playerQuest, PluginPlayerEvent event) {
        switch (event.getName()) {
        case GROW_CLAIM:
            condGrowClaim.progress(playerQuest);
            break;
        case BUY_CLAIM_BLOCKS: {
            int amount = event.getDetail(Detail.COUNT, 0);
            condBuyClaimBlocks.progress(playerQuest, amount);
            break;
        }
        default: break;
        }
    }
}

final class ClaimGrowProgress extends GoalProgress {
    protected boolean growClaim;
    protected int buyClaimBlocks;

    @Override
    public boolean isComplete() {
        return growClaim
            && buyClaimBlocks >= ClaimGrowGoal.CLAIM_BLOCKS;
    }
}
