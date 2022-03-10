package com.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.processing.KubernetesResourceUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;

@WithKubernetesTestServer
@QuarkusTest
class HelloWorldAppReconcilerTest {

    private static final String DUMMY_MESSAGE = "dummy message";
    private static final String DUMMY_FOO = "dummy-foo";

    @KubernetesTestServer
    KubernetesServer mockServer;

    @Test
    void reconciles() {
        final HelloWorldApp cr = createDefaultCR();

        mockServer.getClient().resources(HelloWorldApp.class).create(cr);

        await().atMost(2, MINUTES)
        .pollInterval(1, SECONDS)
        .untilAsserted(() -> {
            HelloWorldApp updatedCR = fetchHelloWorldAppCR(cr);
            assertThat(updatedCR.getStatus(), is(notNullValue()));
            assertThat(updatedCR.getStatus().getConditions(), hasSize(1));
            ConfigMap cmManaged = fetchConfigMap(cr);
            assertNotNull(cmManaged);
            assertThat(cmManaged.getData(), allOf(
                    hasEntry(equalTo("message"), equalTo(DUMMY_MESSAGE)),
                    hasEntry(equalTo("foo"), equalTo(DUMMY_FOO))
            ));
        });

        mockServer.getClient().resources(HelloWorldApp.class).delete(cr);
    }

    @Test
    void cleanUp() {
        final String finalizerName =
            ReconcilerUtils.getDefaultFinalizerName(HelloWorldApp.class);
        final HelloWorldApp cr = createDefaultCR();

        mockServer.getClient().resources(HelloWorldApp.class).create(cr);

        // Assert the finalizer has been added to the Resource
        await().atMost(2, MINUTES)
        .pollInterval(1, SECONDS)
        .untilAsserted(() -> {
            HelloWorldApp updatedCR = fetchHelloWorldAppCR(cr);
            assertThat(updatedCR.getFinalizers(), hasSize(1));
            assertEquals(finalizerName, updatedCR.getFinalizers().get(0));
        });

        mockServer.getClient().resources(HelloWorldApp.class).delete(cr);

        // Assert that the CR and its childs has been deleted
        await().atMost(2, MINUTES)
        .pollInterval(1, SECONDS)
        .untilAsserted(() -> {
            // Commented because the mock server does not delete the owned referenced resources.
            // assertNull(fetchConfigMap(cr));
            assertNull(fetchHelloWorldAppCR(cr));
        });

    }

    private HelloWorldApp createDefaultCR() {
        final HelloWorldApp cr = new HelloWorldApp();
        cr.setMetadata(new ObjectMetaBuilder()
                .withName("helloworldapp-1")
                .withNamespace(mockServer.getClient().getNamespace())
                .build());
        cr.setSpec(new HelloWorldAppSpec());
        cr.getSpec().setMessage(DUMMY_MESSAGE);
        cr.getSpec().setFoo(DUMMY_FOO);
        return cr;
    }

    private HelloWorldApp fetchHelloWorldAppCR(final HelloWorldApp cr) {
        return fetchResource(cr, HelloWorldApp.class);
    }

    private ConfigMap fetchConfigMap(final HelloWorldApp cr) {
       return fetchResource(cr, ConfigMap.class);
    }

    private <T extends HasMetadata> T fetchResource(final HelloWorldApp cr, Class<T> resourceClazz) {
        return mockServer.getClient().resources(resourceClazz)
                .inNamespace(mockServer.getClient().getNamespace())
                .withName(KubernetesResourceUtils.getName(cr))
                .get();
    }
}
