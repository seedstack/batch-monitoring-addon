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

public class MqttClientResourceTest extends AbstractSeedWebIT {

    private static final String LOGIN = "reader";

    private static final String PASSWORD = "password";

    private static final String INSTANCE = "f00bar";

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

    @Test
    @RunAsClient
    public void testGetClientList() {
        Response response = expect().statusCode(200)
                .given()
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/clients");
        int resultSize = from(response.asString()).get("totalClients");
        assertThat(resultSize).isEqualTo(1);
    }

    @Test
    @RunAsClient
    public void testGetClient() {
        Response response = expect().statusCode(200)
                .given().pathParam("clientId", "client_test")
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/clients/{clientId}");
        String clientId = from(response.asString()).get("clientId");
        assertThat(clientId).isEqualTo("client_test");
    }

    @Test
    @RunAsClient
    public void testGetClientListWithInstance() {
        Response response = expect().statusCode(200)
                .given().pathParam("instanceId", INSTANCE)
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/instance/{instanceId}/clients");
        int resultSize = from(response.asString()).get("totalClients");
        assertThat(resultSize).isEqualTo(1);
    }

    @Test
    @RunAsClient
    public void testGetClientWithInstance() {
        Response response = expect().statusCode(200)
                .given().pathParam("instanceId", INSTANCE)
                .pathParam("clientId", "client_test")
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/instance/{instanceId}/clients/{clientId}");
        String clientId = from(response.asString()).get("clientId");
        assertThat(clientId).isEqualTo("client_test");
    }

    @Test
    @RunAsClient
    public void testGetClientListHal() {
        Response response = expect().statusCode(200)
                .given().pathParam("instanceId", INSTANCE)
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/instance/{instanceId}/clients");
        String href = from(response.asString()).get("_links.self.href");
        assertThat(href).startsWith(baseURL.getPath());
        assertThat(href).contains(INSTANCE);
        String embeddedHref = from(response.asString()).get("_embedded.clients[0]._links.self.href");
        assertThat(embeddedHref).startsWith(baseURL.getPath());
        assertThat(embeddedHref).contains(INSTANCE);
    }

    @Test
    @RunAsClient
    public void testGetClientHal() {
        Response response = expect().statusCode(200)
                .given().pathParam("instanceId", INSTANCE)
                .pathParam("clientId", "client_test")
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/instance/{instanceId}/clients/{clientId}");
        String clientId = from(response.asString()).get("clientId");
        assertThat(clientId).isEqualTo("client_test");
        String href = from(response.asString()).get("_links.self.href");
        assertThat(href).startsWith(baseURL.getPath());
        assertThat(href).contains(INSTANCE);
    }

    @Test
    @RunAsClient
    public void testInvalidInstance() {
        expect().statusCode(404)
                .given().pathParam("instanceId", "***")
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/{instanceId}/clients");
    }

    @Test
    @RunAsClient
    public void testGetClientNotFound() {
        expect().statusCode(404)
                .given().pathParam("clientId", "client_fake")
                .auth().basic(LOGIN, PASSWORD)
                .when().get(baseURL.toString() + "seed-monitoring/mqtt/clients/{clientId}");
    }
}
