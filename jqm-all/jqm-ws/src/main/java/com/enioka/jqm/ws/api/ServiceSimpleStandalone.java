/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.ws.api;

import com.enioka.admin.JqmAdminApiException;
import com.enioka.jqm.client.api.JqmClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A extension of the ServiceSimple API designed to work in a standalone environment, where each node contains its own
 * engine, WS and DB.
 * <p>
 * In this scenario, each engine attributes IDs within a range defined by its IP address. However, a node may still
 * receive a `getStatus` request with an ID belonging to another instance. Because there is no "master" DB, the request
 * must be forwarded to the appropriate node.
 * <p>
 * Only `getStatus` was adapted. Other methods using an ID parameter will simply not work.
 */
@Component(service = ServiceSimpleStandalone.class, configurationPolicy = ConfigurationPolicy.REQUIRE, scope = ServiceScope.SINGLETON)
@JakartarsResource
@Path("/simple")
public class ServiceSimpleStandalone extends ServiceSimple
{
    private final static Logger log = LoggerFactory.getLogger(ServiceSimpleStandalone.class);

    private Integer jqmNodeId = null;

    private String localIp;

    @Activate
    @Override
    public void onServiceActivation(Map<String, Object> properties)
    {
        if (!properties.containsKey("jqmnodeid"))
        {
            throw new JqmAdminApiException("OSGi configuration for ServiceSimple does not contain a valid node ID");
        }

        jqmNodeId = (int) properties.get("jqmnodeid"); // small int - cannot be null.
        log.info("\tStarting ServiceSimple with node ID {}", jqmNodeId);

        try {
            localIp = Inet4Address.getLocalHost().getHostAddress();
            // TODO: reset ID sequence of the local engine using `idSequenceBaseFromIp(localIp)`
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    /////////////////////////////////////////////////////////////
    // ID <> IP helpers
    /////////////////////////////////////////////////////////////

    /**
     * Extracts the IP address corresponding to an ID.
     * <p>
     * Each ID, when divided by 1 million, results in the full IPv4 address (each part with padding 0's) of the node
     * that created it.
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
     * Computes the base (first value) of an ID sequence for a given IP address.
     * <p>
     * The sequence's starting value is the concatenation of each 0-padded part of the IPv4,
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

    /////////////////////////////////////////////////////////////
    // The one and only really simple API
    /////////////////////////////////////////////////////////////

    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    //@Override
    public String getStatus(@QueryParam("id") int id)
    {
        final var realDestIp = ipFromId(id);
        JqmClient client = null;
        if (!realDestIp.equals(localIp)) {
            final var p = new Properties();
            p.setProperty("com.enioka.jqm.ws.url", "http://" + realDestIp + ":1789/ws/client");
            // Redirect to distant node with Jersey client
            client = com.enioka.jqm.client.jersey.api.JqmClientFactory.getClient(null, p, true);
        } else {
            // Use local node with JDBC client
            client = com.enioka.jqm.client.jdbc.api.JqmClientFactory.getClient();
        }

        return client.getJob(id).getState().toString();
    }
}
