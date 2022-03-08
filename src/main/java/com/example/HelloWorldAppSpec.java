package com.example;

public class HelloWorldAppSpec {

    /**
     * Number of replicas
     */
    private Integer replicas;

    /**
     * Application version desired
     */
    private String appVersion;

    /**
     * Application default message
     */
    private String message;

    /**
     * Foo property
     */
    private String foo;

    /**
     * Specifies the number of replicas
     *
     * @param replicas
     *          number of replicas
     */
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * Desired number of replicas.
     *
     * @return
     *      the number of replicas
     */
    public Integer getReplicas() {
        return replicas;
    }

    /**
     * Gets the specified app version tag in the deployment.
     *
     * @return
     *      the app version tag
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Desired application version, e.g. latest, v1.0
     *
     * @param appVersion
     *          the tag of the image
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * Desired message to show in the app.
     *
     * @return
     *      the app message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Specifies the default message to show in the app.
     *
     * @param message
     *          the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Desired FOO env var value to set in the app.
     *
     * @return
     *      the FOO env var value
     */
    public String getFoo() {
        return foo;
    }

    /**
     * Specifies the value of the env var FOO.
     *
     * @param foo
     *      value of the env var
     */
    public void setFoo(String foo) {
        this.foo = foo;
    }

}
