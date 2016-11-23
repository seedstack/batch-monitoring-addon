/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.internal.rest.clients;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import org.seedstack.seed.rest.hal.HalRepresentation;
import org.seedstack.seed.rest.hal.Link;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
final class Client extends HalRepresentation {

    private String clientId;

    private boolean connected;

    private List<String> serverURIs;

    private List<String> topics;

    private String reconnectionMode;

    private Integer reconnectionInterval;

    private Integer keepAliveInterval;

    private Boolean cleanSession;

    private Integer mqttVersion;

    private Integer connectionTimeout;

    private Integer poolCoreSize;

    private Integer poolMaxSize;

    private Integer poolQueueSize;

    private Integer poolKeepAlive;

    private Client() {
    }

    private Client(String clientId, boolean connected) {
        this.clientId = clientId;
        this.connected = connected;
    }

    public static Client create(String clientId, boolean connected) {
        return new Client(clientId, connected);
    }

    Client serverURIs(String... serverURIs) {
        if (this.serverURIs == null) {
            this.serverURIs = Lists.newArrayList();
        }


        Collections.addAll(this.serverURIs, serverURIs);
        return this;
    }

    Client topics(String... topics) {
        if (this.topics == null) {
            this.topics = Lists.newArrayList();
        }
        Collections.addAll(this.topics, topics);
        return this;
    }

    Client reconnectionMode(String reconnectionMode) {
        this.reconnectionMode = reconnectionMode;
        return this;
    }

    Client reconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
        return this;
    }

    Client keepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }

    Client cleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    Client mqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
        return this;
    }

    Client connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    Client poolCoreSize(int poolCoreSize) {
        this.poolCoreSize = poolCoreSize;
        return this;
    }

    Client poolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
        return this;
    }

    Client poolQueueSize(int poolQueueSize) {
        this.poolQueueSize = poolQueueSize;
        return this;
    }

    Client poolKeepAlive(int poolKeepAlive) {
        this.poolKeepAlive = poolKeepAlive;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isConnected() {
        return connected;
    }

    public List<String> getServerURIs() {
        return serverURIs;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getReconnectionMode() {
        return reconnectionMode;
    }

    public Integer getReconnectionInterval() {
        return reconnectionInterval;
    }

    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public Boolean getCleanSession() {
        return cleanSession;
    }

    public Integer getMqttVersion() {
        return mqttVersion;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getPoolCoreSize() {
        return poolCoreSize;
    }

    public Integer getPoolMaxSize() {
        return poolMaxSize;
    }

    public Integer getPoolQueueSize() {
        return poolQueueSize;
    }

    public Integer getPoolKeepAlive() {
        return poolKeepAlive;
    }

    @Override
    public Client self(Link link) {
        super.self(link);
        return this;
    }

    @Override
    public Client self(String href) {
        super.self(href);
        return this;
    }
}
