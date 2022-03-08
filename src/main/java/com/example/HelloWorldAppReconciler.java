package com.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class HelloWorldAppReconciler implements Reconciler<HelloWorldApp> {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldAppReconciler.class);

    private final KubernetesClient client;

    public HelloWorldAppReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<HelloWorldApp> reconcile(HelloWorldApp resource, Context context) {
        LOG.info("reconciling ..."+resource);

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

}

