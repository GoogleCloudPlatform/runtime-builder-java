# Java Runtime Builder

[![unstable](http://badges.github.io/stability-badges/dist/unstable.svg)](http://github.com/badges/stability-badges)
[![Build Status](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java)

A [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/) pipeline for 
packaging Java applications into supported Google Cloud Runtime containers. It consists of a series
of docker containers, used as build steps, and a build pipeline configuration file.

## Building Locally
The pipeline can be built using maven:
```bash
mvn clean install
```

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

## Configuration
An [app.yaml](https://cloud.google.com/appengine/docs/flexible/java/configuring-your-app-with-app-yaml) 
file must be included in the sources passed to the Java Runtime Builder. The `runtime_config`
section of this file tells the builder how to build and package your source. In most cases, 
`runtime_config` can be omitted.

| Option Name | Type | Default | Description |
|----------|------|---------|-------------|
| artifact | string |  Discovered based on the content of your build output | The path where the builder should expect to find the artifact to package in the resulting docker container. This setting will be required if your build produces more than one artifact. 
| build_script | string | `mvn -B -DskipTests clean package` if a maven project is detected, or `gradle build` if a gradle project is detected | The build command that is executed to build your source |

### Sample app.yaml
```yaml
runtime: java
env: flex

# all parameters specified below in the runtime_config block are optional
runtime_config:
  artifact: "target/my-artifact.jar"
  build_script: "mvn clean install -Pcloud-build-profile"
```


## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
