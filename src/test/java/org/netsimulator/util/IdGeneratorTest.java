package org.netsimulator.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdGeneratorTest {

    @Test
    public void getNextId() {
        IdGenerator g = new IdGenerator();
        assertEquals(0, g.getCurrentId());
        assertEquals(1, g.getNextId());
        assertEquals(1, g.getCurrentId());
    }

}