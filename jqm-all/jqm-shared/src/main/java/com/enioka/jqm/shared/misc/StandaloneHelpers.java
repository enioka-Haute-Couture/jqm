package com.enioka.jqm.shared.misc;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Various helper functions for the Standalone mode
 */
public class StandaloneHelpers {

    /**
     * Extracts the IP address corresponding to an ID.
     * <p>
     * In Standalone mode, each ID, when divided by 1 million, results in the full IPv4 address (each part with padding
     * 0's) of the node that created it.
     *
     * @param id The ID
     * @return The corresponding IP address
     */
    public static String ipFromId(long id) {
        final var base = id / 1_000_000;
        return "" +
            base / 1000L / 1000L / 1000 % 1000L +
            '.' +
            base / 1000L / 1000L % 1000L +
            '.' +
            base / 1000L % 1000L +
            '.' +
            base % 1000L;
    }

    /**
     * Computes the base (first value) of an ID sequence for a given IP address for Standalone mode.
     * <p>
     * In Standalone mode, the sequence's starting value is the concatenation of each 0-padded part of the IPv4,
     * multiplied by 1 million (which gives us 1 million values before a collision with the next IP).
     *
     * @param ip The IP address
     * @return The first value of the corresponding sequence
     */
    public static long idSequenceBaseFromIp(String ip) {
        return Long.parseLong(Arrays.stream(ip.split("\\."))
            .map(stringValue -> String.format("%03d", Integer.parseInt(stringValue)))
            .collect(Collectors.joining())
        ) * 1_000_000L;
    }
}
