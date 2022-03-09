package com.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
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
        final HelloWorldApp cr = new HelloWorldApp();
        cr.setMetadata(new ObjectMetaBuilder()
                .withName("helloworldapp-1")
                .withNamespace(mockServer.getClient().getNamespace())
                .build());
        cr.setSpec(new HelloWorldAppSpec());
        cr.getSpec().setMessage(DUMMY_MESSAGE);
        cr.getSpec().setFoo(DUMMY_FOO);

        mockServer.getClient().resources(HelloWorldApp.class).create(cr);

        await().atMost(2, MINUTES)
        .pollInterval(1, SECONDS)
        .untilAsserted(() -> {
            HelloWorldApp updatedCR = mockServer.getClient().resources(HelloWorldApp.class)
                    .inNamespace(mockServer.getClient().getNamespace())
                    .withName(KubernetesResourceUtils.getName(cr))
                    .get();
            assertThat(updatedCR.getStatus(), is(notNullValue()));
            assertThat(updatedCR.getStatus().getConditions(), hasSize(1));
            ConfigMap cmManaged = mockServer.getClient().resources(ConfigMap.class)
                    .inNamespace(mockServer.getClient().getNamespace())
                    .withName(KubernetesResourceUtils.getName(cr))
                    .get();
            assertNotNull(cmManaged);
            assertThat(cmManaged.getData(), allOf(
                    hasEntry(equalTo("message"), equalTo(DUMMY_MESSAGE)),
                    hasEntry(equalTo("foo"), equalTo(DUMMY_FOO))
            ));
        });
    }

}
