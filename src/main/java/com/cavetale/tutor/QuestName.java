package com.cavetale.tutor;

import com.cavetale.tutor.goal.*;
import com.cavetale.tutor.goal.Goal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.Component.text;

/**
 * A QuestName is the static configuration of all quests, with display
 * name, description, requirements, flags.
 */
@Getter
public enum QuestName {
    // Beginner
    BEGINNER(QuestType.TUTORIAL, text("Welcome to Cavetale!")) {
        @Override public List<Component> getDescription() {
            return List.of(text("The beginner tutorial"));
        }

        @Override public String getAutoStartPermission() {
            return "tutor.beginner";
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new ChoosePetGoal(),
                           new WildGoal(),
                           new SetHomeGoal());
        }
    },
    WARP(QuestType.TUTORIAL, text("Beginner Tour")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Find out about the"),
                           text("different places"),
                           text("Cavetale has to offer,"),
                           text("and how to get there."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(BEGINNER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new PublicHomeGoal(),
                           new WarpGoal(),
                           WarpPlaceGoal.bazaar(),
                           WarpPlaceGoal.dwarven(),
                           WarpPlaceGoal.cloud(),
                           WarpPlaceGoal.witch(),
                           new ServerSwitchGoal());
        }
    },
    CHAT(QuestType.TUTORIAL, text("Chatting 101")) {
        @Override public List<Component> getDescription() {
            return List.of(text("On using our"),
                           text("chat channels."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(BEGINNER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new LocalChatGoal(),
                           new PrivateChatGoal(),
                           new PartyChatGoal());
        }
    },
    MONEY(QuestType.TUTORIAL, text("All About Money")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Learn how to earn"),
                           text("and spend money."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(BEGINNER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new MineGoal(),
                           new MiningGoal(),
                           new StorageGoal(),
                           new SellItemGoal(),
                           new ShopSearchGoal());
        }
    },
    MEMBER(QuestType.TUTORIAL, text("The Road to Member")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Graduate from the"),
                           text("Beginner Tutorial and"),
                           text("and become a Member."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(MONEY, WARP, CHAT);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new TicketGoal(),
                           new MenuGoal(),
                           new MemberAcceptGoal());
        }
    },
    MEMBER_INTRO(QuestType.TUTORIAL, text("New Members Introduction")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Explore some of the"),
                           text("features you want to"),
                           text("know when you play"),
                           text("on Cavetale"));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(MEMBER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(MEMBER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new MailGoal(),
                           new TitleGoal(),
                           new FriendsGoal(),
                           new RaidGoal());
        }
    },
    HOME(QuestType.TUTORIAL, text("Advanced Claims and Homes")) {
         @Override public List<Component> getDescription() {
             return List.of(text("How to grow your"),
                            text("claim and trust your"),
                            text("friends in it."),
                            text("Share your homes."));
         }

         @Override public Set<QuestName> getSeeDependencies() {
             return Set.of(MEMBER);
         }

         @Override public Set<QuestName> getStartDependencies() {
             return Set.of(MEMBER_INTRO);
         }

        @Override protected List<Goal> createGoals() {
            return List.of(new ClaimTrustGoal(),
                           new ClaimGrowGoal(),
                           new SetNamedHomeGoal(),
                           new InviteHomeGoal());
        }
    },
    MOBILITY(QuestType.TUTORIAL, text("Mobility")) {
        @Override public List<Component> getDescription() {
            return List.of(text("These systems"),
                           text("give you more"),
                           text("power for making"),
                           text("your base."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(MEMBER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(MEMBER_INTRO);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new TelevatorGoal(),
                           new LinkPortalGoal(),
                           new PocketMobGoal());
        }
    },
    SPELEOLOGIST(QuestType.QUEST, text("Rank up to Speleologist")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Aquire a higher rank"),
                           text("and enjoy its perks."));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(MEMBER);
        }

        @Override public Set<QuestName> getStartDependencies() {
            return Set.of(MEMBER_INTRO, HOME, MOBILITY);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new SpeleologistGoal(),
                           new SpeleologistAcceptGoal());
        }
    },
    ;

    public static final List<String> KEY_LIST;

    public final String key;
    public final QuestType type;
    public final Component displayName;

    QuestName(final QuestType type, final Component displayName) {
        this.key = name().toLowerCase();
        this.type = type;
        this.displayName = displayName;
    }

    static {
        KEY_LIST = Stream.of(QuestName.values())
            .map(n -> n.key)
            .collect(Collectors.toUnmodifiableList());
    }

    public static QuestName of(@NonNull String key) {
        for (QuestName questName : QuestName.values()) {
            if (key.equals(questName.key)) return questName;
        }
        return null;
    }

    protected Quest create() {
        return new Quest(this, createGoals());
    }

    public boolean isQuittable() {
        return this != QuestName.BEGINNER;
    }

    public abstract List<Component> getDescription();

    public Set<QuestName> getSeeDependencies() {
        return Set.of();
    }

    public Set<QuestName> getStartDependencies() {
        return Set.of();
    }

    public String getAutoStartPermission() {
        return null;
    }

    protected abstract List<Goal> createGoals();
}
