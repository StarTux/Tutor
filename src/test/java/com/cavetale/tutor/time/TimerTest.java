package com.cavetale.tutor.time;

import org.junit.Test;

public class TimerTest {
    @Test
    public void main() {
        Timer timer = new Timer("UTC-11");
        timer.update();
        System.out.println(timer);
    }
}
