package com.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class HelloWorldAppReconciler implements Reconciler<HelloWorldApp> {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldAppReconciler.class);

    // something like "helloworldapps.apps.example.com/finalizer";
    private static final String HELLOWORLDAPP_FINALIZER = ReconcilerUtils.getDefaultFinalizerName(HelloWorldApp.class);

    private final KubernetesClient client;

    public HelloWorldAppReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<HelloWorldApp> reconcile(HelloWorldApp resource, Context context) {

        LOG.info("reconciling ..."+resource);

        // Check if the CR is marked to be deleted
        boolean isInstanceToBeDeleted = resource.getMetadata().getDeletionTimestamp() != null;
        if (isInstanceToBeDeleted) {
            LOG.info("Instance marked for deletion, running finalizers");
            if (resource.getFinalizers().contains(HELLOWORLDAPP_FINALIZER)) {
                finalizeHelloWorldApp(resource);
                LOG.info("finalizers completed for instance");
                resource.removeFinalizer(HELLOWORLDAPP_FINALIZER);
                return UpdateControl.updateResource(resource);
            }
            LOG.info("Instance can be deleted now");
            return UpdateControl.noUpdate();
        }

        return UpdateControl.noUpdate();
    }

    private void finalizeHelloWorldApp(HelloWorldApp resource) {
        // TODO(user): Add the cleanup steps that the operator
        // needs to do before the CR can be deleted. Examples
        // of finalizers include performing backups and deleting
        // resources that are not owned by this CR, like a PVC.
        LOG.info("Successfully finalized HelloWorldApp");
    }
}

