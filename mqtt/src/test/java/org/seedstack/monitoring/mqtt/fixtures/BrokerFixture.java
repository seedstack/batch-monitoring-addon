/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.fixtures;

import io.moquette.server.Server;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BrokerFixture {

    private static final Server server = new Server();

    public static void startBroker() {
        startServer(server, "/configMqttServer.conf");
    }

    private static void startServer(Server server, String configFile) {
        final File c;
        try {
            c = new File(BrokerFixture.class.getResource(configFile).toURI());
            server.startServer(c);
        } catch (URISyntaxException e) {
            Assertions.fail(e.getMessage(), e);
        } catch (IOException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }

    public static void stopBroker() {
        server.stopServer();
    }
}
