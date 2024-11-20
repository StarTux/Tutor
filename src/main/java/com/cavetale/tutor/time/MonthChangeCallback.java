package com.cavetale.tutor.time;

@FunctionalInterface
public interface MonthChangeCallback {
    void call(int year, int month);
}
