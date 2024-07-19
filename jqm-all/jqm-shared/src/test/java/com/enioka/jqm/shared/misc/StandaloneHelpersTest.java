package com.enioka.jqm.shared.misc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandaloneHelpersTest {

    @Test
    public void testFullIpToSequence() {
        final var ip = "192.168.124.234";
        final var expected = 192_168_124_234_000_000L;
        assertEquals(expected, StandaloneHelpers.idSequenceBaseFromIp(ip));
    }

    @Test
    public void testMixedIpToSequence() {
        final var ip = "192.168.24.1";
        final var expected = 192_168_024_001_000_000L;
        assertEquals(expected, StandaloneHelpers.idSequenceBaseFromIp(ip));
    }

    @Test
    public void testMinIpToSequence() {
        final var ip = "0.0.0.1";
        final var expected = 1_000_000L;
        assertEquals(expected, StandaloneHelpers.idSequenceBaseFromIp(ip));
    }

    @Test
    public void testIdToFullIp() {
        final var id = 192_168_124_234_000_000L;
        final var expected = "192.168.124.234";
        assertEquals(expected, StandaloneHelpers.ipFromId(id));
    }

    @Test
    public void testIdToMixedIp() {
        final var id = 192_168_024_001_000_000L;
        final var expected = "192.168.24.1";
        assertEquals(expected, StandaloneHelpers.ipFromId(id));
    }

    @Test
    public void testIdToMinIp() {
        final var id = 1_000_000L;
        final var expected = "0.0.0.1";
        assertEquals(expected, StandaloneHelpers.ipFromId(id));
    }
}
