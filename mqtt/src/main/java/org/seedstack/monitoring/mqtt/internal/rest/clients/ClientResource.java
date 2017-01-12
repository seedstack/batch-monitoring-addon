/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.internal.rest.clients;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.seedstack.mqtt.spi.MqttClientInfo;
import org.seedstack.mqtt.spi.MqttInfo;
import org.seedstack.mqtt.spi.MqttPoolInfo;
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.security.RequiresPermissions;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/seed-monitoring/mqtt{p:/?}{instance:\\w*}/clients")
public class ClientResource {
    private static final String CLIENT_ID = "clientId";
    private static final boolean TRAILING_SLASH = true;
    @PathParam("instance")
    private String instance;
    @PathParam(CLIENT_ID)
    private String clientId;
    @Inject
    private MqttInfo mqttInfo;
    @Inject
    private Injector injector;

    @GET
    @Rel(value = Rels.CLIENTS, home = true)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClients() {
        if (mqttInfo.getClientNames() != null && !mqttInfo.getClientNames().isEmpty()) {
            return clientListRepresentation();
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/{clientId}")
    @Rel(value = Rels.CLIENT)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClient() {
        boolean clientExists = clientExists(clientId);
        if (clientExists) {
            return clientRepresentation();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private Response clientListRepresentation() {
        List<Client> clientList = populate();
        ClientList clients = ClientList.create(clientList)
                .self(getValuedPath(!TRAILING_SLASH))
                .embedded(Rels.CLIENTS, clientList);

        return Response.ok(clients).build();
    }

    private List<Client> populate() {
        List<Client> clientList = Lists.newArrayList();
        for (String clientId : mqttInfo.getClientNames()) {
            final IMqttClient current = injector.getInstance(Key.get(IMqttClient.class, Names.named(clientId)));
            if (current != null) {
                Client client = Client.create(clientId, current.isConnected())
                        .self(getValuedPath(TRAILING_SLASH) + clientId);
                clientList.add(client);
            }
        }
        return clientList;
    }

    private Response clientRepresentation() {
        MqttClientInfo ci = mqttInfo.getClientInfo(clientId);
        final IMqttClient current = injector.getInstance(Key.get(IMqttClient.class, Names.named(clientId)));
        Client client = Client.create(clientId, current.isConnected())
                .serverURIs(ci.getUri());
        if (ci.getTopicFilters() != null) {
            client.topics(ci.getTopicFilters());
        }
        client.cleanSession(ci.isCleanSession())
                .connectionTimeout(ci.getConnectionTimeout())
                .keepAliveInterval(ci.getKeepAliveInterval())
                .mqttVersion(ci.getMqttVersion())
                .reconnectionInterval(ci.getReconnectionInterval())
                .reconnectionMode(ci.getMqttReconnectionMode())
                .self(getValuedPath(TRAILING_SLASH) + clientId);

        MqttPoolInfo poolInfo;
        if ((poolInfo = ci.getMqttPoolInfo()) != null) {
            client.poolCoreSize(poolInfo.getCoreSize())
                    .poolKeepAlive(poolInfo.getKeepAlive())
                    .poolMaxSize(poolInfo.getMaxSize())
                    .poolQueueSize(poolInfo.getQueueSize());
        }
        return Response.ok(client).build();
    }

    private boolean clientExists(String clientId) {
        return mqttInfo.getClientNames() != null && mqttInfo.getClientNames().contains(clientId);
    }

    private String getValuedPath(boolean trailingSlash) {
        StringBuilder path = new StringBuilder("/seed-monitoring/mqtt");
        if (instance != null && !instance.isEmpty()) {
            path.append("/").append(instance);
        }
        path.append("/clients");
        if (trailingSlash) {
            path.append("/");
        }
        return path.toString();
    }
}
