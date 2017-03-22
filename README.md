# Java Runtime Builder

[![Build Status](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/runtime-builder-java)

A [Google Cloud Container Builder](https://cloud.google.com/container-builder/docs/) pipeline for 
packaging Java applications into supported Google Cloud Runtime containers. It consists of a series
of docker containers and a [cloudbuild.yaml](cloudbuild.yaml) configuration file.

## Running via Google Cloud Container Builder (recommended)
To run via Google Cloud Container Builder, first install the 
[Google Cloud SDK](https://cloud.google.com/sdk/). Then, initiate a Cloud Container Build:
```bash
gcloud container builds submit /path/to/my/java/app --config cloudbuild.yaml
```

## Building and Running locally
The build pipeline can be built using maven:
```bash
$ mvn clean install
```
Individual build steps can be run using docker:
```bash
# compile my application's source and generate a dockerfile
$ docker run -v /path/to/my/java/app:/workspace -w /workspace java-runtime-builder \
    --jar-runtime=gcr.io/google-appengine/openjdk \
    --server-runtime=gcr.io/google-appengine/jetty
    
# package my application into a docker container
$ docker build -t my-java-app /path/to/my/java/app/.docker_staging
```

## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
