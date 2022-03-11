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

## Generating the OLM bundle

Build with the CSV generation option:

```sh
➜ mvn package -Pnative -Dquarkus.operator-sdk.generate-csv=true \
          -Dquarkus.docker.executable-name=podman -Dquarkus.container-image.build=true \
          -Dquarkus.native.container-runtime=podman \
          -Dquarkus.container-image.image=quay.io/$USERNAME/helloworld-java-operator:v0.0.1beta1-java
...
[INFO] [io.quarkiverse.operatorsdk.csv.deployment.ManifestsProcessor] Generating CSV for helloworldappreconciler controller -> /home/jzuriaga/sandbox/operator.sdk/jello-operator/target/manifests/helloworldappreconciler.csv.yml
...
```

Move to the destination directory :

```sh
➜ mv target/manifests src/main/k8s/bundle
➜ mv target/kubernetes/*-v1.yml src/main/k8s/bundle/manifests
```

Generate the OLM bundle

```sh
➜ operator-sdk generate bundle --overwrite --version ${VERSION:-0.0.1} --metadata --output-dir ./src/main/k8s/bundle
Generating bundle metadata
INFO[0000] Creating bundle.Dockerfile
INFO[0000] Creating src/main/k8s/bundle/metadata/annotations.yaml
INFO[0000] Bundle metadata generated suceessfully
```

Build and publish the bundle image :

```sh
➜ export BUNDLE_IMG=quay.io/$USERNAME/helloworld-java-operator-bundle:v${VERSION}
➜ podman build -f bundle.Dockerfile -t ${BUNDLE_IMG} .
➜ podman push ${BUNDLE_IMG}
```

Test run the bundle :

```sh
➜ operator-sdk run bundle --timeout=5m ${BUNDLE_IMG} --install-mode OwnNamespace
INFO[0008] Successfully created registry pod: quay-io-jzuriaga-helloworld-java-operator-bundle-v0-0-1
INFO[0008] Created CatalogSource: helloworld-operator-catalog
INFO[0008] OperatorGroup "operator-sdk-og" created
INFO[0008] Created Subscription: helloworldappreconciler-v0-0-1-sub
INFO[0014] Approved InstallPlan install-jdhtt for the Subscription: helloworldappreconciler-v0-0-1-sub
INFO[0014] Waiting for ClusterServiceVersion "joperators-test/helloworldappreconciler.v0.0.1" to reach 'Succeeded' phase
INFO[0014]   Waiting for ClusterServiceVersion "joperators-test/helloworldappreconciler.v0.0.1" to appear
INFO[0027]   Found ClusterServiceVersion "joperators-test/helloworldappreconciler.v0.0.1" phase: Pending
INFO[0029]   Found ClusterServiceVersion "joperators-test/helloworldappreconciler.v0.0.1" phase: Installing
INFO[0038]   Found ClusterServiceVersion "joperators-test/helloworldappreconciler.v0.0.1" phase: Succeeded
INFO[0038] OLM has successfully installed "helloworldappreconciler.v0.0.1"
```

Check that the operator is running :

```sh
➜ kubectl get all
NAME                                                                  READY   STATUS      RESTARTS   AGE
pod/helloworld-operator-operator-6749c9d65c-8bktl                     1/1     Running     0          5m41s
pod/quay-io-jzuriaga-helloworld-java-operator-bundle-v0-0-1           1/1     Running     0          6m4s

NAME                                           READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/helloworld-operator-operator   1/1     1            1           5m41s

NAME                                                      DESIRED   CURRENT   READY   AGE
replicaset.apps/helloworld-operator-operator-6749c9d65c   1         1         1       5m41s
```

Cleanup after testing :

```sh
➜ operator-sdk cleanup helloworld-operator
INFO[0000] subscription "helloworldappreconciler-v0-0-1-sub" deleted
INFO[0000] customresourcedefinition "helloworldapps.apps.example.com" deleted
INFO[0005] clusterserviceversion "helloworldappreconciler.v0.0.1" deleted
INFO[0005] catalogsource "helloworld-operator-catalog" deleted
INFO[0005] operatorgroup "operator-sdk-og" deleted
INFO[0005] Operator "helloworld-operator" uninstalled
```

## Generating the catalog

Build the catalog image :

```og image :

```sh
➜ export CATALOG_IMAGE=quay.io/$USERNAME/helloworld-java-operator-catalog:v${VERSION}
➜ opm index add --container-tool podman --mode semver --tag $CATALOG_IMAGE --bundles quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1
INFO[0000] building the index                            bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0000] running /usr/bin/podman pull quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1  bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] running podman create                         bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] running podman cp                             bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] running podman rm                             bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] Could not find optional dependencies file     file=bundle_tmp3183370443/metadata load=annotations with=./bundle_tmp3183370443
INFO[0002] Could not find optional properties file       file=bundle_tmp3183370443/metadata load=annotations with=./bundle_tmp3183370443
INFO[0002] Could not find optional dependencies file     file=bundle_tmp3183370443/metadata load=annotations with=./bundle_tmp3183370443
INFO[0002] Could not find optional properties file       file=bundle_tmp3183370443/metadata load=annotations with=./bundle_tmp3183370443
INFO[0002] Generating dockerfile                         bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] writing dockerfile: ./index.Dockerfile634357330  bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] running podman build                          bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
INFO[0002] [podman build --format docker -f ./index.Dockerfile634357330 -t quay.io/jzuriaga/helloworld-java-operator-catalog:v0.0.1 .]  bundles="[quay.io/jzuriaga/helloworld-java-operator-bundle:v0.0.1]"
```

Publish the catalog image :

```sh
➜ podman push ${CATALOG_IMAGE}
```
