/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.internal.rest.clients;

import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.HalRepresentation;

import java.util.List;

class MqttClientList extends HalRepresentation {

    private int totalClients;

    private List<MqttClient> clients;

    private MqttClientList() {
    }

    private MqttClientList(RelRegistry relRegistry, List<MqttClient> clients) {
        this.clients = clients;
        this.totalClients = clients.size();

        self(relRegistry.uri(Rels.CLIENTS));
        embedded(Rels.CLIENTS, clients);
    }

    private MqttClientList(RelRegistry relRegistry, List<MqttClient> clients, String instanceId) {
        this.clients = clients;
        this.totalClients = clients.size();

        self(relRegistry.uri(Rels.INSTANCE_CLIENTS).set("instanceId", instanceId));
        embedded(Rels.CLIENTS, clients);
    }

    static MqttClientList create(RelRegistry relRegistry, List<MqttClient> clients) {
        return new MqttClientList(relRegistry, clients);
    }

    static MqttClientList create(RelRegistry relRegistry, List<MqttClient> clients, String instanceId) {
        return new MqttClientList(relRegistry, clients, instanceId);
    }

    public int getTotalClients() {
        return totalClients;
    }

}
