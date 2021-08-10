package com.cavetale.tutor.sql;

import com.cavetale.tutor.Quest;
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
public final class SQLCompletedQuests {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false)
    private String quest;
    @Column(nullable = false)
    private Date time;

    public SQLCompletedQuests() { }

    public SQLCompletedQuests(final UUID playerUuid, final Quest quest) {
        this.player = playerUuid;
        this.quest = quest.getName().key;
        this.time = new Date();
    }
}
