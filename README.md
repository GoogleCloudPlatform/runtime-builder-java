# Java Runtime Builder

![Build Status](https://storage.googleapis.com/java-runtimes-kokoro-build-badges/runtime-builder-java-master.png)
[![codecov](https://codecov.io/gh/GoogleCloudPlatform/runtime-builder-java/branch/master/graph/badge.svg)](https://codecov.io/gh/GoogleCloudPlatform/runtime-builder-java)
[![unstable](http://badges.github.io/stability-badges/dist/unstable.svg)](http://github.com/badges/stability-badges)

A [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/) pipeline for 
packaging Java applications into supported Google Cloud Runtime containers. It consists of a series
of docker containers, used as build steps, and a build pipeline configuration file.

## Running via Google Cloud Container Builder (recommended)
To run via Google Cloud Container Builder, first install the
[Google Cloud SDK](https://cloud.google.com/sdk/). Then, initiate a Cloud Container Build using the 
provided [java.yaml](java.yaml) file:
```bash
# Determine the name of your desired output image. Note that it must be a path to a Google Container
# Registry bucket to which your Cloud SDK installation has push access.
OUTPUT_IMAGE=gcr.io/my-gcp-project/my-application-container

# initiate the cloud container build
gcloud container builds submit /path/to/my/java/app \ 
    --config java.yaml \
    --substitutions _OUTPUT_IMAGE=$OUTPUT_IMAGE
```
After the build completes, the built application container will appear in the [gcr.io container 
registry](https://cloud.google.com/container-registry/) at the specified path.

## Running via Docker (without Cloud Container Builder)
Build steps can be run locally, one at a time, using docker. (This requires that the `runtime-builder`
image is available locally.) Note that these commands effectively mirror the steps in the
[java.yaml](java.yaml) pipeline config file.

```bash
LOCAL_APPLICATION_DIR=/path/to/my/app

# Generate docker resources
# See java.yaml for the fullly specified jdk-runtimes-map server-runtimes-map args
docker run -v $LOCAL_APPLICATION_DIR:/workspace -w /workspace runtime-builder \
    --jdk-runtimes-map='*=gcr.io/google-appengine/openjdk:8' \
    --server-runtimes-map='*|*=gcr.io/google-appengine/jetty:9' \
    --compat-docker-image='gcr.io/google-appengine/jetty9-compat' \
    --maven-docker-image='gcr.io/cloud-builders/java/mvn:3.5.0-jdk-8' \
    --gradle-docker-image='gcr.io/cloud-builders/java/gradle:4.0-jdk-8'

# package my application into a docker container
docker build -t my-app-container $LOCAL_APPLICATION_DIR
```

## Configuration
An [app.yaml](https://cloud.google.com/appengine/docs/flexible/java/configuring-your-app-with-app-yaml) 
file must be included in the sources passed to the Java Runtime Builder. The `runtime_config`
section of this file tells the builder how to build and package your source. In most cases, 
`runtime_config` can be omitted.

| Option Name | Type | Default | Description |
|----------|------|---------|-------------|
| jdk | string | openjdk8 | Select the JDK used in the generated image. The only supported value is `openjdk8`.
| server | string | jetty | Select the web server to use in the generated image. Must be either `jetty9` or `tomcat8`
| artifact | string |  Discovered based on the content of your build output | The path where the builder should expect to find the artifact to package in the resulting docker container. This setting will be required if your build produces more than one artifact. 
| build_script | string | `mvn -B -DskipTests clean package` if a maven project is detected, or `gradle build` if a gradle project is detected | The build command that is executed to build your source |
| jetty_quickstart | boolean | false | Enable the [Jetty quickstart module](http://www.eclipse.org/jetty/documentation/9.4.x/quickstart-webapp.html) to speed up the start time of the application (Only available if the jetty runtime is selected).
### Sample app.yaml
```yaml
runtime: java
env: flex

# all parameters specified below in the runtime_config block are optional
runtime_config:
  artifact: "target/my-artifact.jar"
  build_script: "mvn clean install -Pcloud-build-profile"
  jetty_quickstart: true
```

## Development guide
* See [DEVELOPING.md](DEVELOPING.md) for instructions on how to build and test this pipeline.

## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
