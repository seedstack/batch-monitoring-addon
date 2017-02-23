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
import org.seedstack.mqtt.spi.MqttPoolConfiguration;
import org.seedstack.seed.rest.Rel;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.security.RequiresPermissions;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/seed-monitoring/mqtt")
public class MqttClientResource {

    @Inject
    private MqttInfo mqttInfo;

    @Inject
    private Injector injector;

    @Inject
    private RelRegistry relRegistry;

    private static final String NO_INSTANCE = null;

    private static final String PARAM_INSTANCE = "instanceId";

    private static final String PARAM_CLIENT = "clientId";

    @GET
    @Path("/clients")
    @Rel(value = Rels.CLIENTS, home = true)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClientList() {
        return responseForClientList(NO_INSTANCE);
    }

    @GET
    @Path("/instance/{instanceId: \\w+}/clients")
    @Rel(value = Rels.INSTANCE_CLIENTS, home = true)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClientListWithInstance(@PathParam(PARAM_INSTANCE) String instanceId) {
        return responseForClientList(instanceId);
    }

    @GET
    @Path("/clients/{clientId}")
    @Rel(value = Rels.CLIENT)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClient(@PathParam(PARAM_CLIENT) String clientId) {
        return responseForClient(NO_INSTANCE, clientId);
    }

    @GET
    @Path("/instance/{instanceId: \\w+}/clients/{clientId}")
    @Rel(value = Rels.INSTANCE_CLIENT)
    @Produces({MediaType.APPLICATION_JSON, "application/hal+json"})
    @RequiresPermissions("seed:monitoring:mqtt:read")
    public Response getClientWithInstance(@PathParam(PARAM_INSTANCE) String instanceId, @PathParam(PARAM_CLIENT) String clientId) {
        return responseForClient(instanceId, clientId);
    }

    private Response responseForClientList(String instanceId) {
        if (mqttInfo.getClientNames() != null && !mqttInfo.getClientNames().isEmpty()) {
            return Response.ok(buildClientListRepresentation(instanceId)).build();
        }
        return Response.noContent().build();
    }

    private Response responseForClient(String instanceId, String clientId) {
        if (mqttInfo.getClientNames() != null && mqttInfo.getClientNames().contains(clientId)) {
            return Response.ok(buildClientRepresentation(clientId, instanceId)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private MqttClientList buildClientListRepresentation(String instanceId) {
        List<MqttClient> clientList = populateClientList(instanceId);
        if (instanceIsPresent(instanceId)) {
            return MqttClientList.create(relRegistry, clientList, instanceId);
        }
        return MqttClientList.create(relRegistry, clientList);
    }

    private MqttClient buildClientRepresentation(String clientId, String instanceId) {
        final MqttClientInfo clientInfo = mqttInfo.getClientInfo(clientId);
        final IMqttClient client = injector.getInstance(Key.get(IMqttClient.class, Names.named(clientId)));

        MqttClient representation = initClient(clientId, client.isConnected(), instanceId);
        representation.serverURIs(clientInfo.getUri())
                .cleanSession(clientInfo.isCleanSession())
                .connectionTimeout(clientInfo.getConnectionTimeout())
                .keepAliveInterval(clientInfo.getKeepAliveInterval())
                .mqttVersion(clientInfo.getMqttVersion())
                .reconnectionInterval(clientInfo.getReconnectionInterval())
                .reconnectionMode(clientInfo.getMqttReconnectionMode());

        if (clientInfo.getTopicFilters() != null) {
            representation.topics(clientInfo.getTopicFilters());
        }

        MqttPoolConfiguration poolConf;
        if ((poolConf = clientInfo.getMqttPoolConfiguration()) != null) {
            representation.poolCoreSize(poolConf.getCoreSize())
                    .poolKeepAlive(poolConf.getKeepAlive())
                    .poolMaxSize(poolConf.getMaxSize())
                    .poolQueueSize(poolConf.getQueueSize());
        }
        return representation;
    }

    private List<MqttClient> populateClientList(String instanceId) {
        List<MqttClient> clientList = Lists.newArrayList();
        for (String clientId : mqttInfo.getClientNames()) {
            final IMqttClient client = injector.getInstance(Key.get(IMqttClient.class, Names.named(clientId)));
            if (client != null) {
                MqttClient representation = initClient(clientId, client.isConnected(), instanceId);
                clientList.add(representation);
            }
        }
        return clientList;
    }

    private MqttClient initClient(String clientId, boolean connected, String instanceId) {
        if (instanceIsPresent(instanceId)) {
            return MqttClient.create(relRegistry, clientId, instanceId).connected(connected);
        }
        return MqttClient.create(relRegistry, clientId).connected(connected);
    }

    private boolean instanceIsPresent(String instanceId) {
        return instanceId != null && instanceId.length() > 0;
    }

}
