package com.example;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("apps.example.com")
public class HelloWorldApp extends CustomResource<HelloWorldAppSpec, HelloWorldAppStatus> implements Namespaced {

    public HelloWorldAppStatus status() {
        if (null==getStatus()) {
            setStatus(new HelloWorldAppStatus());
        }
        return getStatus();
    }
}

