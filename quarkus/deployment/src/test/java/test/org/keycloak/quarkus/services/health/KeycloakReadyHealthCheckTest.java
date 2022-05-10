/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package test.org.keycloak.quarkus.services.health;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.QuarkusUnitTest;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KeycloakReadyHealthCheckTest {

    @Inject
    AgroalDataSource agroalDataSource;

    @RegisterExtension
    static final QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("keycloak.conf", "META-INF/keycloak.conf"));

    @Test
    @Order(1)
    public void testLivenessUp() {
        given()
            .when().get("/health/live")
            .then()
                .statusCode(200)
                .body(Matchers.containsString("UP"));
    }

    @Test
    @Order(2)
    public void testReadinessUp() throws SQLException {
        given()
            .when().get("/health/ready")
            .then()
                .statusCode(200)
                .body(Matchers.containsString("UP"));
    }

    @Test
    // Make sure this test is executed as last
    @Order(3)
    public void testReadinessDown() {
        agroalDataSource.close();
        Awaitility.await()
                .untilAsserted(() -> {
                    try {
                        assertTrue(agroalDataSource.getConnection().isClosed());
                    } catch (SQLException ex) {
                        // skip
                    }});
        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() ->
                    given()
                            .when().get("/health/ready")
                            .then()
                            .statusCode(503)
                            .body(Matchers.containsString("DOWN")));
    }
}
