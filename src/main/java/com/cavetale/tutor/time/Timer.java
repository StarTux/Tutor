package com.cavetale.tutor.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@Getter @ToString @RequiredArgsConstructor
public final class Timer {
    private final ZoneId zoneId;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int dayOfWeek;
    private int dayId;
    @Setter private Runnable onDayBreak;
    @Setter private Runnable onHourChange;

    public Timer(final String zoneId) {
        this(ZoneId.of(zoneId));
    }

    protected void update() {
        final Instant instant = Instant.now();
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        final LocalDate localDate = localDateTime.toLocalDate();
        this.year = localDate.getYear();
        this.month = localDate.getMonth().getValue();
        this.day = localDate.getDayOfMonth();
        this.hour = localDateTime.getHour();
        this.dayOfWeek = localDate.getDayOfWeek().getValue() - 1; // 0-6
        this.dayId = year * 10000 + month * 100 + day;
    }

    public void enable(Plugin plugin) {
        update();
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        final int oldDay = day;
        final int oldHour = hour;
        update();
        if (oldHour != hour && onHourChange != null) {
            onHourChange.run();
        }
        if (oldDay != day && onDayBreak != null) {
            onDayBreak.run();
        }
    }

    public String getTodaysName() {
        Month theMonth = Month.of(month);
        return theMonth.getDisplayName(TextStyle.FULL, Locale.US) + " " + day;
    }
}
