/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.internal.rest.clients;

import org.seedstack.seed.rest.hal.HalRepresentation;
import org.seedstack.seed.rest.hal.Link;

import java.util.List;

public class ClientList extends HalRepresentation {

    private int resultSize;

    private List<Client> clients;

    private ClientList() {
    }

    private ClientList(List<Client> clients) {
        this.clients = clients;
        if (clients != null) {
            this.resultSize = clients.size();
        }
    }

    public static ClientList create(List<Client> clients) {
        return new ClientList(clients);
    }

    public int getResultSize() {
        return resultSize;
    }

    @Override
    public ClientList self(Link link) {
        super.self(link);
        return this;
    }

    @Override
    public ClientList self(String href) {
        super.self(href);
        return this;
    }

    @Override
    public ClientList embedded(String rel, Object embedded) {
        super.embedded(rel, embedded);
        return this;
    }
}
