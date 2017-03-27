# Java Runtime Builder

[![Build Status](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java)
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
# compile my application's source and generate a dockerfile
docker run -v /path/to/my/java/app:/workspace -w /workspace runtime-builder \
    --jar-runtime=gcr.io/google-appengine/openjdk \
    --server-runtime=gcr.io/google-appengine/jetty
    
# package my application into a docker container
docker build -t my-java-app /path/to/my/java/app/.docker_staging
```

## Configuration
Configuration must be provided to the builder via a json-encoded environment variable named 
`$_GCP_RUNTIME_BUILDER_CONFIG`. This is optional, and only required for customizing default behavior.

| Option Name | Type | Default | Description |
|----------|------|---------|-------------|
| artifact | string |  Discovered based on the content of your build output | The path where the builder should expect to find the artifact to package in the resulting docker container. This setting will be required if your build produces more than one artifact. 
| build_script | string | `mvn -B -DskipTests clean package` if a maven project is detected, or `gradle build` if a gradle project is detected | The build command that is executed to build your source |
| entrypoint | string | Auto-generated depending on the base runtime and artifact being deployed | The docker entrypoint used when starting your application container.
| packages | list of strings | N/A | A list of the extra debian packages to be installed while building the application container

### Example Configuration
```bash
# create config json object
BUILDER_CONFIG='{
  "artifact": "path/to/my/custom/artifact",
  "build_script": "gradle build test",
  "entrypoint": "java -jar /path/to/myapp.jar",
  "packages": [
    "ffmpeg", 
    "imagemagick=1.1.2"
  ]
}'

# pass the configuration when invoking the container build
gcloud container builds submit /path/to/my/java/app \ 
    --config cloudbuild.yaml \
    --substitutions "_OUTPUT_IMAGE=gcr.io/$GCP_PROJECT_ID/my-application-container,_GCP_RUNTIME_BUILDER_CONFIG=$BUILDER_CONFIG"
```

## Development guide
* See [DEVELOPING.md](DEVELOPING.md) for instructions on how to build and test this pipeline.

## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
