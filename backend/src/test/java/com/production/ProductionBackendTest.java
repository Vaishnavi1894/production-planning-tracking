package com.production;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class ProductionBackendTest {

    @Test
    void testBasicCalculation() {
        double planned = 500;
        double produced = 450;
        double diff = produced - planned;
        double achievement = (produced / planned) * 100.0;
        Assertions.assertEquals(-50, diff);
        Assertions.assertEquals(90.0, achievement);
    }
}
