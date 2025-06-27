package com.enioka.jqm.shared.misc;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * Various helper functions for the Standalone mode
 */
public class StandaloneHelpers
{
    /**
     * Extracts the IP address corresponding to an ID.
     * <p>
     * In Standalone mode, each ID, when divided by 1 million, results in the full IPv4 address (each part with padding 0's) of the node
     * that created it.
     *
     * @param id
     *            The ID
     * @return The corresponding IP address
     */
    public static String ipFromId(long id)
    {
        final var base = id / 1_000_000;
        return String.format("%d.%d.%d.%d", (base >> 24 & 0xff), (base >> 16 & 0xff), (base >> 8 & 0xff), (base & 0xff));
    }

    /**
     * Computes the base (first value) of an ID sequence for a given IP address for Standalone mode.
     * <p>
     * In Standalone mode, the sequence's starting value is the concatenation of each 0-padded part of the IPv4, multiplied by 1 million
     * (which gives us 1 million values before a collision with the next IP).
     *
     * @param ip
     *            The IP address
     * @return The first value of the corresponding sequence
     */
    public static long idSequenceBaseFromIp(InetAddress ip)
    {
        var ipAsInteger = 0L;
        for (var b : ip.getAddress())
        {
            ipAsInteger = (ipAsInteger << 8) + (b & 0xFF);
        }

        return ipAsInteger * 1_000_000L;
    }

    public static long idSequenceBaseFromIp(String ip)
    {
        try
        {
            return idSequenceBaseFromIp(InetAddress.getByName(ip));
        }
        catch (UnknownHostException e)
        {
            // This should never happen as the IP is supposed to be valid
            throw new RuntimeException("Unable to parse IP address: " + ip, e);
        }
    }

    public static InetAddress getLocalIpAddress()
    {
        try
        {
            for (var netInterface : Collections.list(NetworkInterface.getNetworkInterfaces()))
            {
                if (netInterface.isPointToPoint() || !netInterface.isUp() || netInterface.isVirtual()
                        || netInterface.getName().startsWith("docker") || netInterface.getName().startsWith("veth")
                        || netInterface.getName().startsWith("br-"))
                {
                    continue;
                }
                else
                {
                    for (var addr : Collections.list(netInterface.getInetAddresses()))
                    {
                        if (addr.getHostAddress().contains(":"))
                        {
                            continue;
                        }
                        return addr;
                    }
                }
            }

            return Inet4Address.getLocalHost();
        }
        catch (SocketException | UnknownHostException e)
        {
            try
            {
                return Inet4Address.getByName("127.0.0.1");
            }
            catch (UnknownHostException e1)
            {
                throw new RuntimeException("Unable to get loopback address", e1);
            }
        }
    }
}
