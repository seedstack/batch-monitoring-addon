/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.monitoring.mqtt.internal.rest.clients;

import io.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.monitoring.mqtt.fixtures.BrokerFixture;
import org.seedstack.seed.it.AbstractSeedWebIT;

import java.net.URL;

import static io.restassured.RestAssured.expect;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientResourceTest extends AbstractSeedWebIT {

    @ArquillianResource
    private URL baseURL;

    @BeforeClass
    public static void beforeClass() {
        BrokerFixture.startBroker();
    }

    @AfterClass
    public static void afterClass() {
        BrokerFixture.stopBroker();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @RunAsClient
    @Test
    public void testGetClients() {
        Response response = expect().statusCode(200).when().get(baseURL.toString() + "seed-monitoring/mqtt/clients");
        int resultSize = from(response.asString()).get("resultSize");
        assertThat(resultSize).isEqualTo(1);
    }

    @RunAsClient
    @Test
    public void testGetClient() {
        Response response = expect().statusCode(200).when().get(baseURL.toString() + "seed-monitoring/mqtt/clients/client_test");
        String clientId = from(response.asString()).get("clientId");
        assertThat(clientId).isEqualTo("client_test");
    }

    @RunAsClient
    @Test
    public void testGetClientNotFound() {
        expect().statusCode(404).when().get(baseURL.toString() + "seed-monitoring/mqtt/clients/client_fake");
    }
}
