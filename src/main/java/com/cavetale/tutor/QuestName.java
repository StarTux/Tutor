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
                           new SetHomeGoal(),
                           new TypeTierGoal());
        }
    },
    WARP(QuestType.TUTORIAL, text("Beginner Tour")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Find out about the"),
                           text("different places"),
                           text("Cavetale has to offer,"),
                           text("and how to get there"));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new PublicHomeGoal(),
                           new WarpGoal(),
                           new ServerSwitchGoal());
        }
    },
    CHAT(QuestType.TUTORIAL, text("Chatting 101")) {
        @Override public List<Component> getDescription() {
            return List.of(text("On using our"),
                           text("chat channels"));
        }

        @Override public Set<QuestName> getSeeDependencies() {
            return Set.of(BEGINNER);
        }

        @Override protected List<Goal> createGoals() {
            return List.of(new LocalChatGoal(),
                           new PrivateChatGoal(),
                           new PartyChatGoal());
        }
    },
    MINING_WORLD(QuestType.TUTORIAL, text("The mining world")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Visit the mining world"),
                           text("and gather resources"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(BEGINNER);}
        @Override protected List<Goal> createGoals() {
            return List.of(new MineGoal(),
                           new MiningGoal(),
                           new StorageGoal(),
                           new ShopSearchGoal());
        }
    },
    MASS_STORAGE(QuestType.TUTORIAL, text("Mass Storage")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Use our Mass Storage"),
                           text("system so you never"),
                           text("lose your items"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(MINING_WORLD);}
        @Override protected List<Goal> createGoals() {
            return List.of(new StorageGoal());
        }
    },
    MARKET(QuestType.TUTORIAL, text("The Market")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Visit out Market"),
                           text("to buy and sell"),
                           text("items"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(MINING_WORLD);}
        @Override protected List<Goal> createGoals() {
            return List.of(new ShopSearchGoal());
        }
    },
    TICKETS(QuestType.TUTORIAL, text("Tickets")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Use our ticket system"),
                           text("to contact staff"),
                           text("members"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(CHAT);}
        @Override protected List<Goal> createGoals() {
            return List.of(new TicketGoal());
        }
    },
    MAILS(QuestType.TUTORIAL, text("Mails")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Write mails to contact"),
                           text("your friends while"),
                           text("they are not online"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(CHAT);}
        @Override public Set<QuestName> getStartDependencies() {return Set.of(CHAT);}
        @Override protected List<Goal> createGoals() {
            return List.of(new MailGoal());
        }
    },
    TITLES(QuestType.TUTORIAL, text("Titles")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Select a title"),
                           text("to display in chat"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(CHAT);}
        @Override protected List<Goal> createGoals() {
            return List.of(new TitleGoal());
        }
    },
    FRIENDS(QuestType.TUTORIAL, text("Friends")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Check out the friends"),
                           text("system"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(CHAT);}
        @Override protected List<Goal> createGoals() {
            return List.of(new FriendsGoal());
        }
    },
    ADVANCED_HOMES(QuestType.TUTORIAL, text("Advanced Homes")) {
         @Override public List<Component> getDescription() {
             return List.of(text("Learn more about"),
                            text("homes and how"),
                            text("to share them"));
         }
         @Override public Set<QuestName> getSeeDependencies() {return Set.of(MINING_WORLD);}
         @Override protected List<Goal> createGoals() {
            return List.of(new SetNamedHomeGoal(),
                           new InviteHomeGoal());
        }
    },
    ADVANCED_CLAIMS(QuestType.TUTORIAL, text("Advanced Claims and Homes")) {
         @Override public List<Component> getDescription() {
             return List.of(text("How to grow your"),
                            text("claim and trust your"),
                            text("friends in it"));
         }
         @Override public Set<QuestName> getSeeDependencies() {return Set.of(MINING_WORLD);}
         @Override protected List<Goal> createGoals() {
             return List.of(new ClaimTrustGoal(),
                            new ClaimGrowGoal());
         }
    },
    TELEVATOR(QuestType.TUTORIAL, text("Televator")) {
        @Override public List<Component> getDescription() {
            return List.of(text("Make quick elevators"));
        }
        @Override public Set<QuestName> getSeeDependencies() {return Set.of(ADVANCED_HOMES);}
        @Override protected List<Goal> createGoals() {
            return List.of(new TelevatorGoal());
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
        return getSeeDependencies();
    }

    public String getAutoStartPermission() {
        return null;
    }

    protected abstract List<Goal> createGoals();
}
