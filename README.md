# helloworld-operator using Java

Instructions extracted from [https://github.com/operator-framework/java-operator-plugins/blob/main/docs/tutorial.md]


## Requirements

- operator-sdk v.1.18.0 or higher
- java 11 or higher
- maven 3.6.3 or higher
- GNU make
- podman


```sh
➜ operator-sdk version
operator-sdk version: "v1.18.0", commit: "c9c61b6921b29d731e64cd3cc33d268215fb3b25", kubernetes version: "1.21", go version: "go1.17.7", GOOS: "linux", GOARCH: "amd64"
➜ mvn -version
Apache Maven 3.8.2 (ea98e05a04480131370aa0c110b8c54cf726c06f)
Maven home: /home/jzuriaga/sw/maven/maven3
Java version: 11.0.14.1, vendor: Red Hat, Inc., runtime: /usr/lib/jvm/java-11-openjdk-11.0.14.1.1-5.fc35.x86_64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.16.12-200.fc35.x86_64", arch: "amd64", family: "unix"
```

## Create project

```
➜ operator-sdk init --plugins quarkus --domain example.com --project-name helloworld-operator
```

add to the Makefile goals that build the native image :

```
-Dquarkus.native.container-runtime=podman
```

## Create api

```sh
➜ operator-sdk create api --plugins quarkus --group apps --version v1alpha1 --kind HelloWorldApp
```

## Generating Custom Resource and CRD

```sh
➜ mvn clean install
..
[INFO] [io.quarkiverse.operatorsdk.deployment.OperatorSDKProcessor] Processed 'com.example.HelloWorldAppReconciler' reconciler named 'helloworldappreconciler' for 'helloworldapps.apps.example.com' resource (version 'apps.example.com/v1alpha1')
[INFO] Generating 'helloworldapps.apps.example.com' version 'v1alpha1' with com.example.HelloWorldApp (spec: com.example.HelloWorldAppSpec / status com.example.HelloWorldAppStatus)...
[INFO] [io.quarkiverse.operatorsdk.deployment.OperatorSDKProcessor] Generated helloworldapps.apps.example.com CRD:
[INFO] [io.quarkiverse.operatorsdk.deployment.OperatorSDKProcessor]   - v1 -> /home/jzuriaga/sandbox/operator.sdk/jello-operator/target/kubernetes/helloworldapps.apps.example.com-v1.yml
...
➜ find target/kubernetes
target/kubernetes
target/kubernetes/helloworldapps.apps.example.com-v1.yml
target/kubernetes/kubernetes.yml
target/kubernetes/kubernetes.json
```

Apply the resource

```sh
➜ make install

```