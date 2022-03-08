package com.example;

import java.util.List;
import io.fabric8.kubernetes.api.model.Condition;

public class HelloWorldAppStatus {

    /**
     * List of pods of the application.
     */
    private List<String> appPods;

    /**
     * Application message
     */
    private String appMessage;

    /**
     * Conditions met for the app deployment.
     */
    private List<Condition> conditions;

    public List<String> getAppPods() {
        return appPods;
    }

    public void setAppPods(List<String> appPods) {
        this.appPods = appPods;
    }

    public String getAppMessage() {
        return appMessage;
    }

    public void setAppMessage(String appMessage) {
        this.appMessage = appMessage;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

}
