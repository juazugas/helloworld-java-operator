package com.example;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class HelloWorldAppReconciler implements Reconciler<HelloWorldApp> { 
  private final KubernetesClient client;

  public HelloWorldAppReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<HelloWorldApp> reconcile(HelloWorldApp resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

