package com.example;

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

@ControllerConfiguration(namespaces = WATCH_CURRENT_NAMESPACE)
public class HelloWorldAppReconciler implements Reconciler<HelloWorldApp> {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldAppReconciler.class);

    private final KubernetesClient client;

    public HelloWorldAppReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<HelloWorldApp> reconcile(HelloWorldApp resource, Context context) {
        LOG.info("reconciling ..."+resource);

        var result = reconcileConfigMap(resource);

        return UpdateControl.noUpdate();
    }

    @Override
    public DeleteControl cleanup(HelloWorldApp resource, Context context) {
        // TODO(user): Add the cleanup steps that the operator
        // needs to do before the CR can be deleted. Examples
        // of finalizers include performing backups and deleting
        // resources that are not owned by this CR, like a PVC.
        LOG.info("Successfully finalized HelloWorldApp");
        return DeleteControl.defaultDelete();
    }

    /**
     * Reconciles the config map resource.
     *
     * @param resource
     *          the HelloWorldApp resource
     * @return
     *         if the result was successful
     */
    private boolean reconcileConfigMap(HelloWorldApp resource) {
        ConfigMap configmapFound = client
            .configMaps()
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(resource.getMetadata().getName())
            .get();

        if (configmapFound==null) {
            ConfigMap configmap = newConfigMapForCR(resource);
            LOG.info("Creating a new ConfigMap ",
                "ConfigMap.Namespace", configmap.getMetadata().getNamespace(),
                "ConfigMap.Name", configmap.getMetadata().getName());
            client.configMaps().create(configmap);
            return true;
        } else {
            LOG.info("ConfigMap already exists",
                "ConfigMap.Namespace", configmapFound.getMetadata().getNamespace(),
                "ConfigMap.Name", configmapFound.getMetadata().getName());
        }

        String message = configmapFound.getData().get("message");
        String requiredMessage = resource.getSpec().getMessage();

        if(!StringUtils.equalsIgnoreCase(requiredMessage, message)) {
            LOG.info("Current configmap message do no match HelloWorldApp configured Message");
            configmapFound.getData().put("message", requiredMessage);
            configmapFound.getData().put("foo", resource.getSpec().getFoo());
            // Update the message
            client.configMaps().createOrReplace(configmapFound);
            return true;
        }

        return false;
    }

    private Map<String, String> labelsForHelloWorldApp(HelloWorldApp resource) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "helloWorldApp");
        labels.put("helloworldapp_cr", resource.getMetadata().getName());
        return labels;
    }

    private OwnerReference ownerReferenceForHelloWorldApp(HelloWorldApp resource) {
        return new OwnerReferenceBuilder()
            .withApiVersion(resource.getApiVersion())
            .withKind(resource.getKind())
            .withName(resource.getMetadata().getName())
            .withUid(resource.getMetadata().getUid())
            .build();
    }

    private ConfigMap newConfigMapForCR(HelloWorldApp resource) {
        Map<String, String> data = new HashMap<>();
        data.put("message", resource.getSpec().getMessage());
        data.put("foo", resource.getSpec().getFoo());

        Map<String, String> annotations = Map.of(
            "kubernetes.io/description", "Contains message for Hello World Application");

        return new ConfigMapBuilder()
            .withData(data)
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName(resource.getMetadata().getName())
                    .withNamespace(resource.getMetadata().getNamespace())
                    .withLabels(labelsForHelloWorldApp(resource))
                    .withAnnotations(annotations)
                    .withOwnerReferences(ownerReferenceForHelloWorldApp(resource))
                    .build()
            )
            .build()
        ;
    }

}

