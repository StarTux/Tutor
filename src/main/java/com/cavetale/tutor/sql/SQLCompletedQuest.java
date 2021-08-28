package com.cavetale.tutor.sql;

import com.cavetale.tutor.QuestName;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Table(name = "completed_quests", uniqueConstraints = {@UniqueConstraint(columnNames = {"player", "quest"})})
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLCompletedQuest {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false)
    private String quest;
    @Column(nullable = false)
    private Date time;

    public SQLCompletedQuest() { }

    public SQLCompletedQuest(final UUID playerUuid, final QuestName questName) {
        this.player = playerUuid;
        this.quest = questName.key;
        this.time = new Date();
    }
}
