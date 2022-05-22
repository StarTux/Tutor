package com.cavetale.tutor.sql;

import com.cavetale.tutor.Quest;
import com.winthier.sql.SQLRow;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Table(name = "player_quests", uniqueConstraints = {@UniqueConstraint(columnNames = {"player", "quest"})})
/**
 * The progress of one player within a certain quest (tutorial).
 */
public final class SQLPlayerQuest implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false)
    private String quest;
    @Column(nullable = false)
    private String goal; // current goal id
    @Column(nullable = true)
    private String progress; // GoalProgress json
    @Column(nullable = false)
    private Date updated;

    public SQLPlayerQuest() { }

    public SQLPlayerQuest(final UUID playerUuid, final Quest quest) {
        this.player = playerUuid;
        this.quest = quest.getName().key;
    }

    public void setNow() {
        this.updated = new Date();
    }
}
