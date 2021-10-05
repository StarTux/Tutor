package com.cavetale.tutor;

import com.cavetale.tutor.goal.Goals;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * A QuestName is the static configuration of all quests, with display
 * name, description, requirements, flags.  Actual goals are set in
 * `goal.Goals`.
 */
@Getter
public enum QuestName {
    // Beginner
    BEGINNER(QuestType.TUTORIAL, Component.text("Welcome to Cavetale!"),
             List.of(Component.text("The beginner tutorial")),
             Set.of(),
             Set.of(),
             QuestFlag.autoStart("tutor.beginner")),
    WARP(QuestType.TUTORIAL, Component.text("Beginner Tour"),
         List.of(Component.text("Find out about the"),
                 Component.text("different places"),
                 Component.text("Cavetale has to offer,"),
                 Component.text("and how to get there.")),
         Set.of(BEGINNER),
         Set.of(BEGINNER)),
    CHAT(QuestType.TUTORIAL, Component.text("Chatting 101"),
         List.of(Component.text("On using our"),
                 Component.text("chat channels.")),
         Set.of(BEGINNER),
         Set.of(BEGINNER)),
    MONEY(QuestType.TUTORIAL, Component.text("All About Money"),
          List.of(Component.text("Learn how to earn"),
                  Component.text("and spend money.")),
          Set.of(BEGINNER),
          Set.of(BEGINNER)),
    MEMBER(QuestType.TUTORIAL, Component.text("The Road to Member"),
           List.of(Component.text("Graduate from the"),
                   Component.text("Beginner Tutorial and"),
                   Component.text("and become a Member.")),
           Set.of(BEGINNER),
           Set.of(MONEY, WARP, CHAT)),
    // Member
    MEMBER_INTRO(QuestType.TUTORIAL, Component.text("New Members Introduction"),
                 List.of(Component.text("Explore some of the"),
                         Component.text("features you want to"),
                         Component.text("know when you play"),
                         Component.text("on Cavetale")),
                 Set.of(MEMBER), Set.of(MEMBER)),
    HOME(QuestType.TUTORIAL, Component.text("Advanced Claims and Homes"),
         List.of(Component.text("How to grow your"),
                 Component.text("claim and trust your"),
                 Component.text("friends in it."),
                 Component.text("Share your homes.")),
         Set.of(MEMBER), Set.of(MEMBER_INTRO)),
    MOBILITY(QuestType.TUTORIAL, Component.text("Mobility"),
             List.of(Component.text("These systems"),
                     Component.text("give you more"),
                     Component.text("power for making"),
                     Component.text("your base.")),
             Set.of(MEMBER), Set.of(MEMBER_INTRO)),
    SPELEOLOGIST(QuestType.TUTORIAL, Component.text("Rank up to Speleologist"),
                 List.of(Component.text("Aquire a higher rank"),
                         Component.text("and enjoy its perks.")),
                 Set.of(MEMBER),
                 Set.of(MEMBER_INTRO, HOME, MOBILITY));
    public static final List<String> KEY_LIST;

    public final QuestType type;
    public final String key;
    public final Component displayName;
    public final List<Component> description;
    public final Set<QuestName> seeDependencies;
    public final Set<QuestName> startDependencies;
    public final List<QuestFlag> flags;
    @Getter protected QuestFlag.AutoStartPermission autoStartPermission;

    QuestName(final QuestType type,
              final Component displayName,
              final List<Component> description,
              final Set<QuestName> seeDependencies,
              final Set<QuestName> startDependencies,
              final QuestFlag... flags) {
        this.type = type;
        this.key = name().toLowerCase();
        this.displayName = displayName;
        this.description = description;
        this.seeDependencies = seeDependencies;
        this.startDependencies = startDependencies;
        this.flags = List.of(flags);
        for (QuestFlag flag : flags) {
            if (flag instanceof QuestFlag.AutoStartPermission) {
                this.autoStartPermission = (QuestFlag.AutoStartPermission) flag;
            }
        }
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
        return new Quest(this, Goals.create(this));
    }

    public boolean isQuittable() {
        return this != QuestName.BEGINNER;
    }
}
