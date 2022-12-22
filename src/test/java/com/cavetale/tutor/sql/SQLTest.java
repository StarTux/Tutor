package com.cavetale.tutor.sql;

import org.junit.Test;
import static com.winthier.sql.SQLDatabase.testTableCreation;

public class SQLTest {
    @Test
    public void main() {
        for (var it : SQLStatic.getAllTableClasses()) {
            System.out.println(testTableCreation(it));
        }
    }
}
